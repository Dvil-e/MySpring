package com.gupaoedu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Dvil
 *
 * @Target({ElementType.TYPE,ElementType.METHOD}) 被该注解注释的注解用于 METHOD方法上 TYPE接口 类 枚举 注解上
 * @Retention(RetentionPolicy.RUNTIME) 被该注解注释的注解 不光保存到class文件中 还保存到class文件被jvm加载之后 加载之后也不会删除
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPRequestMapping {
    String value() default "";
}
