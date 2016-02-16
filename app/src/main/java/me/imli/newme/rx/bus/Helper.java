package me.imli.newme.rx.bus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Em on 2015/11/27.
 */
public class Helper {

    public static List<Method> findAnnotatedMethods(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<>();
        Method[] ms = type.getDeclaredMethods();
        for (Method method : ms) {
            // Must not static
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            // Must be public
            if (Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            // Must has only one parameter
            if (method.getParameterTypes().length != 1) {
                continue;
            }
            // Must has annotation
            if (!method.isAnnotationPresent(annotation)) {
                continue;
            }
            methods.add(method);
        }
        return methods;
    }

}
