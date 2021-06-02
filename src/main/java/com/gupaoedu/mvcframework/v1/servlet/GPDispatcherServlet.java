package com.gupaoedu.mvcframework.v1.servlet;

import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GPDispatcherServlet extends HttpServlet {
    private Map<String,Object> mapping = new HashMap<>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            doDispath(req, resp);
        }catch (Exception e){
            //返回给前端
            resp.getWriter().write("500 Exception" + Arrays.toString(e.getStackTrace()));
        }
    }
    private void doDispath(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        String url = req.getRequestURI();
        String contextPath  = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if(!this.mapping.containsKey(url)){
            resp.getWriter().write("404 Not Found !!");
            return;
        }
        Method method = (Method) this.mapping.get(url);
        Map<String, String[]> params = req.getParameterMap();
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()),new Object[]{req, resp, params.get("name")[0]});
    }

    /**
     *  ServletConfig servlet 容器用于在初始化期间将信息传递给 servlet 的 servlet 配置对象
     */
    @Override
    public void init(ServletConfig config) throws  ServletException{
        //字节输入流
        InputStream is = null;
        try {
            //Properties 类位于 java.util.Properties ，是Java 语言的配置文件所使用的类， Xxx.properties 为Java 语言常见的配置文件，如数据库的配置 jdbc.properties, 系统参数配置 system.properties。 这里，讲解一下Properties 类的具体使用。以key=value 的 键值对的形式进行存储值。 key值不能重复。
            Properties configContext = new Properties();
            //getClass 返回当前运行的Object类 getClassLoader 返回这个类的类加载器  getResourceAsStream 从路径中读取指定资源的输入流
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            //从输入比特流中读取配置文件的属性值 进行简单的行读取
            configContext.load(is);
            //找指定key值的value
            String scanPackage = configContext.getProperty("scanPackage");
            doScanner(scanPackage);
            //mapping.keySet() 返回此映射中包含的键的Set视图
            for (String className : mapping.keySet()) {
                if(!className.contains(".")){
                    continue;
                }
                //获取class类
                Class<?> clazz = Class.forName(className);
                // 获取被 GPController  GPRequestMapping GPService 加入Spring bean控制class的类
                // 如果此元素上存在指定类型的注释，则返回 true，否则返回 false。
                if(clazz.isAnnotationPresent(GPController.class)){
                    //clazz.newInstance() 创建由此Class对象表示的类的新实例
                    mapping.put(className, clazz.newInstance());
                    String beasUrl = "";
                    // 如果此元素上存在指定类型的注释，则返回 true，否则返回 false。
                    if(clazz.isAnnotationPresent(GPRequestMapping.class)){
                        //clazz.getAnnotation 返回指定键映射到的值，如果此映射不包含键的映射，则返回null 。
                        GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                        beasUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method: methods) {
                        if(!method.isAnnotationPresent(GPRequestMapping.class)){
                            continue;
                        }
                        GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                        String url = (beasUrl + "/" + requestMapping.value().replaceAll("/+", "/"));
                        mapping.put(url, method);
                        System.out.println("Mapped" + url + "," + method);
                    }
                } else if(clazz.isAnnotationPresent(GPService.class)) {
                    GPService service = clazz.getAnnotation(GPService.class);
                    String beanName = service.value();
                    if ("".equals(beanName)){
                        beanName = clazz.getName();
                    }
                    Object instance = clazz.newInstance();
                    mapping.put(beanName,instance);
                    for (Class<?> i : clazz.getInterfaces()) {
                        mapping.put(i.getName(), instance);
                    }
                }else{
                    continue;
                }
            }
            // 分析加入Spring bean管理的class对象 控制对其进行控制
            // 取出上面for循环存入的class对象
            for (Object object : mapping.values()) {
                if(object == null){
                    continue;
                }
                Class clazz = object.getClass();
                if(clazz.isAnnotationPresent(GPController.class)){
                    // 获取到 注释的类 和 接口上 对其进行分析
                    // Field Field提供有关类或接口的单个​​字段的信息和动态访问。
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        //如果此类或者接口上存在此注解则返回true
                        if(!field.isAnnotationPresent(GPAutowired.class)){
                            continue;
                        }
                        GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                        String beanName = autowired.value();
                        //如果没有beanName则 获取这个类的名字并赋值
                        if("".equals(beanName)){
                            beanName = field.getType().getName();
                        }
                        /*
                        将此反射对象的accessible标志设置为指示的布尔值。
                        值为true表示反射对象在使用时应取消对 Java 语言访问控制的检查。
                        值为false表示反射对象在使用时应强制检查 Java 语言访问控制，并在类描述中注明变化。
                        如果以下任何一项成立，则类C的调用者可以使用此方法来启用对declaring class D的member的访问：
                         */
                        field.setAccessible(true);
                        try {
                            //将指定对象参数上由此Field对象表示的字段设置为指定的新值。 如果基础字段具有原始类型，则新值会自动解包
                            field.set(mapping.get(clazz.getName()), mapping.get(beanName));
                        }
                        //当应用程序尝试反射性地创建实例（数组除外）、设置或获取字段或调用方法，但当前执行的方法无权访问指定类、字段的定义时，将抛出 IllegalAccessException方法或构造函数。
                        catch (IllegalAccessException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }catch (Exception e){
        } finally {
            if(is != null){
                try {
                    is.close();
                }
                //表示发生了某种 I/O 异常。 此类是由失败或中断的 I/O 操作产生的异常的一般类。
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        System.out.println("GP MVC Framework is init");
    }
    private void doScanner(String scanPackage){
        // getClass获取当前加载类 getClassLoader获取当前类加载器 getResource 根据给定资源的路径查找具有给定名称的资源
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        //getFile 获取此URL的文件名。 返回的文件部分将与getPath()相同，加上getQuery()值的串联getQuery()如果有）。 如果没有查询部分，此方法和getPath()将返回相同的结果
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if (!file.getName().endsWith(".class")){
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class",""));
                mapping.put(clazzName,null);
            }
        }
    }
}
