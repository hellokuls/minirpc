package com.mini.rpc.provider.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指明了修饰的这个注解的使用范围，即被描述的注解可以用在哪里。
 *
 * ElementType的取值包含以下几种：
 *
 * TYPE:类，接口或者枚举
 * FIELD:域，包含枚举常量
 * METHOD:方法
 * PARAMETER:参数
 * CONSTRUCTOR:构造方法
 * LOCAL_VARIABLE:局部变量
 * ANNOTATION_TYPE:注解类型
 * PACKAGE:包
 */
@Retention(RetentionPolicy.RUNTIME) //运行级别保留，编译后的class文件中存在，在jvm运行时保留，可以被反射调用。
@Target(ElementType.TYPE)
@Component //@RpcService 注解本质上就是 @Component，可以将服务实现类注册成 Spring 容器所管理的 Bean
public @interface RpcService {

    Class<?> serviceInterface() default Object.class; //服务类型

    String serviceVersion() default "1.0"; //服务版本
}
