package com.gupaoedu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Dvil
 * @Target(ElementType.PARAMETER) 被该注解注释的注解用于 方法参数上
 * @Retention(RetentionPolicy.RUNTIME) 被该注解注释的注解 表明该注解不光保存在class文件中 还在class被jvm加载之后还保存`
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPRequestParam {
    String value() default "";
}
