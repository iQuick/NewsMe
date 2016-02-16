package me.imli.newme.rx.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Em on 2015/11/27.
 */
@Target(ElementType.METHOD)//表示这个注解适用于方法
@Retention(RetentionPolicy.RUNTIME)//表示这个注解需要保留到运行时
public @interface BusReceiver {
}
