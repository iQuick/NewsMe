package me.imli.newme.rx.bus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.imli.newme.rx.bus.annotation.BusReceiver;

/**
 * Created by Em on 2015/11/27.
 */
public class RxBus implements RxIBus {

    private static final String TAG = "RxBus";

    private Map<Object, List<Method>> mMethodMap = new HashMap<>();

    public static RxBus getDefault() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public boolean register(Object target) {
        List<Method> methods = Helper.findAnnotatedMethods(target.getClass(), BusReceiver.class);
        if (methods == null || methods.isEmpty()) {
            return false;
        }
        mMethodMap.put(target, methods);
        return true;
    }

    @Override
    public boolean unregister(Object target) {
        mMethodMap.remove(target);
        return true;
    }

    @Override
    public void post(Object event) {
        final Class<?> eventClass = event.getClass();
        for (Map.Entry<Object, List<Method>> en : mMethodMap.entrySet()) {
            final Object target = en.getKey();
            final List<Method> methods = en.getValue();
            if (methods == null || methods.isEmpty()) {
                continue;
            }
            for (Method method : methods) {
                // 如果事件类型相符，就调用对应的方法 改善事件
                // 这里的类型是要求精确匹配的，没有考虑继承
                if (eventClass.equals(method.getParameterTypes()[0])) {
                    try {
                        method.invoke(target, en);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * SingletonHolder
     */
    private static class SingletonHolder {
        static final RxBus INSTANCE = new RxBus();
    }



}
