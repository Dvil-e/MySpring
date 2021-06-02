package com.gupaoedu.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Dvil
 * https://blog.csdn.net/github_35180164/article/details/52107204 注解基本知识
 * @Target 注解用于说明当前 注解的使用范围Annotation可被用于 packages、types（类、接口、枚举、Annotation类型）、类型成员（方法、构造方法、成员变量、枚举值）、方法参数和本地变量（如循环变量、catch参数）。在Annotation类型的声明中使用了target可更加明晰其修饰的目标。
 *        @Target(ElementType.TYPE)——接口、类、枚举、注解
 *        @Target(ElementType.FIELD)——字段、枚举的常量
 *        @Target(ElementType.METHOD)——方法
 *        @Target(ElementType.PARAMETER)——方法参数
 *        @Target(ElementType.CONSTRUCTOR) ——构造函数
 *        @Target(ElementType.LOCAL_VARIABLE)——局部变量
 *        @Target(ElementType.ANNOTATION_TYPE)——注解
 *        @Target(ElementType.PACKAGE)——包
 *
 * @Retention() 作用是被他所注解的注解要保留多久
 *        source：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；被编译器忽略
 *        class：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期
 *        runtime：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
 *        这3个生命周期分别对应于：Java源文件(.java文件) ---> .class文件 ---> 内存中的字节码。
 *
 * @Documented 用于生成javadoc文档
 *
 * default 修饰方法只能在接口中使用，在接口中被default标记的方法为普通方法，可以直接写方法体。
 *
 * https://blog.csdn.net/mrwxh/article/details/80583589 String value() default 各种类型介绍
 * String value() default "" 为字段名称
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPService {
    String value() default "";
}
