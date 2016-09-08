package com.github.jeckep.spark;


import org.eclipse.jetty.server.session.AbstractSession;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.io.Serializable;
import java.util.*;

/*
* Persist session filter.
* As there is no way to modify configuration of embedded jetty
* related to session manager to use another session manager.
* The only option is to create custom filter, use own cookie
* instead of JSESSIONID cookie
* */

public class PSF {
    public static final String SESSION_COOKIE_NAME = "PERSISTENT_SESSION";

    private Persister persister;
    private List<EventListener> listeners = new ArrayList<>();
    private int expire = 20 * 60; //sec = 20min
    private boolean secure = false; //cookie passed only over https if true

    public PSF(Persister persister) {
        this.persister = persister;
        addEventListener(new SessionChangedListener());
    }

    private Filter beforeFilter = (Request request, Response response) -> {
        addListenersIfItIsFirstRequest(request.session());

        Cookie sessionCookie = getCookieFromRequest(request, SESSION_COOKIE_NAME);
        if(sessionCookie == null){
            sessionCookie = genCookie();
            //save cookie value in session to use it in after-filter
            request.session().attribute(SESSION_COOKIE_NAME, sessionCookie.getValue());
        }else{
            //save cookie value in session to use it in after-filter
            request.session().attribute(SESSION_COOKIE_NAME, sessionCookie.getValue());

            // we have to restore session from persistent storage every time,
            // if we have more than one app node and load balancer without sticky session
            Map<String, Serializable> attrs = persister.restore(sessionCookie.getValue(), expire);
            for(String key: attrs.keySet()){
                request.session().attribute(key, attrs.get(key));
            }
        }

        // add cookie to every response, to update expire time in user browser
        // we have to generate it again in order to set correct cookie attributes, cookie from request has wrong attributes
        response.raw().addCookie(genCookie(sessionCookie.getValue()));
    };

    private Filter afterFilter = (Request request, Response response) -> {
        final Session session = request.session();

        if(SessionChangedListener.checkChanged(session)) {
            SessionChangedListener.setUnchanged(session);

            Map<String, Serializable> sessionAttrs = new HashMap<>();
            for(String key: session.attributes()){
                // do not store session cookie value in a map, because it is a key,
                // we can do it, but there is no need
                if(PSF.SESSION_COOKIE_NAME.equals(key)) continue;
                sessionAttrs.put(key, session.attribute(key));
            }

            String sessionCookieValue = request.session().attribute(SESSION_COOKIE_NAME);
            persister.save(sessionCookieValue, sessionAttrs, expire);
        }
    };

    private  Cookie genCookie(){
        return genCookie(UUID.randomUUID().toString());
    }

    private  Cookie genCookie(String cookieValue){
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setMaxAge(expire);
        cookie.setPath("/");
        return cookie;
    }

    private static Cookie getCookieFromRequest(Request req, String name){
        for(Cookie cookie: req.raw().getCookies()){
            if(name.equals(cookie.getName())){
                return cookie;
            }
        }
        return null;
    }

    public PSF setExpire(int expire) {
        this.expire = expire;
        return this;
    }

    public PSF setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public PSF addEventListener(EventListener listener){
        listeners.add(listener);
        return this;
    }

    public Filter getBeforeFilter() {
        return beforeFilter;
    }

    public Filter getAfterFilter() {
        return afterFilter;
    }


    private static class SessionChangedListener implements HttpSessionAttributeListener {
        private static final String ATTR_NAME = "SESSION_CHANGED";

        @Override
        public void attributeAdded(HttpSessionBindingEvent event) {
            setChanged(event);
        }

        @Override
        public void attributeRemoved(HttpSessionBindingEvent event) {
            setChanged(event);
        }

        @Override
        public void attributeReplaced(HttpSessionBindingEvent event) {
            setChanged(event);
        }

        private void setChanged(HttpSessionBindingEvent event){
            if(!ATTR_NAME.equals(event.getName())){
                event.getSession().setAttribute(ATTR_NAME,Boolean.TRUE);
            }
        }

        public static Boolean checkChanged(Session session){
            return session.attribute(SessionChangedListener.ATTR_NAME) != null;
        }

        public static void setUnchanged(Session session){
            session.removeAttribute(ATTR_NAME);
        }
    }

    //it is hack because I don't know how to get get access to SessionManager during configuration of spark
    private boolean firstRequest = true;
    private void  addListenersIfItIsFirstRequest(Session session){
        if(firstRequest){
            for(EventListener l: listeners){
                ((AbstractSession)session.raw()).getSessionManager().addEventListener(l);
            }
        }
        firstRequest = false;
    }
}
