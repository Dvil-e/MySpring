package com.gupaoedu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Dvil
 * @Target({ElementType.FIELD}) 被该注解注释的注解 说明该注解用于字段，枚举常量上
 * @Retention(RetentionPolicy.RUNTIME) 被该注解注释的注解 说明该注解不光保存class文件中 直到class类被javm加载之后他也一直存在
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPAutowired {
    String value() default "";
}
