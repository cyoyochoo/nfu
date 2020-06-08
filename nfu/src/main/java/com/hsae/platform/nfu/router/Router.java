package com.hsae.platform.nfu.router;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private static final String ROUTER_SIGN = "com.hsae.platform.nfu.router.interfaces";
    private static Map<Class, Object> sRouters = new HashMap<>();

    public static void register(Class impl) {
        try {
            Class[] interfaces = impl.getInterfaces();
            for (Class anInterface : interfaces) {
                if (anInterface.getName().startsWith(ROUTER_SIGN)) {
                    sRouters.put(anInterface, impl.newInstance());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerInstance(Object instance) {
        Class[] interfaces = instance.getClass().getInterfaces();
        for (Class anInterface : interfaces) {
            if (anInterface.getName().startsWith(ROUTER_SIGN)) {
                sRouters.put(anInterface, instance);
                break;
            }
        }
    }

    public static void unregisterInstance(Object instance) {
        Class[] interfaces = instance.getClass().getInterfaces();
        for (Class anInterface : interfaces) {
            if (anInterface.getName().startsWith(ROUTER_SIGN)) {
                sRouters.remove(anInterface);
                break;
            }
        }
    }

    public static <T> T get(Class<T> type) {
        Object o = sRouters.get(type);
        return o == null ? null : (T) o;
    }

    private static void clear() {
        sRouters.clear();
    }
}
