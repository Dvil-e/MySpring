package com.gupaoedu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Dvil
 * @Target({ElementType.TYPE}) 被该注解注释的注解 用于 接口 类 枚举 注解上
 * @Rentention(RetentionPolicy.RUNTIME) 被该注解注释的注解 表明该注解 在class被JVM加载后也不会被销毁
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPController {
    String value() default "";
}
