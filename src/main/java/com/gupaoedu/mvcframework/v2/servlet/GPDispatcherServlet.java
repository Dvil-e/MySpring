package com.gupaoedu.mvcframework.v2.servlet;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class GPDispatcherServlet extends HttpServlet {
    //保存application.properties配置文件中的内容
    private Properties contextConfig = new Properties();

    //保存扫描到的所有类名
    private List<String> classNames = new ArrayList<>();

    //传说中的IOC容器，（学习阶段 不考虑用ConcurrentHashMap， 主要为设计原理和思想）
    private Map<String,Object> ioc = new HashMap<>();

    //保存url和Method的对应关系
    private Map<String,Method> handlerMapping = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException{

        //1 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2 扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3 初始化扫描到的类，并将它们放入IoC容器中
        doInstance();

        //4 完成依赖注入
        duAutowired();
        //5 初始化 HandlerMapping
        initHandlerMapping();

        System.out.println("GP Spring framework is init");
    }

    // 加载配置文件
    private void doLoadConfig(String contextConfigLocation){
        //直接通过类路径找到Spring主配置文件所在路径将其读取出来放到Properties对象中 相当于将 scanPackage = com.gupaoedu.demo 保存到了内存中
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream("contextConFigLocation");
        try {
            //读取配置文件的属性列表
            contextConfig.load(fis);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(null != fis){
                try {
                    fis.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //扫描相关类
    private void doScanner(String scanPackage){
        // scanPackage = com.gupaodu.demo 储存包路径
        // 转换为文本路径，实际上就是吧 . 替换为 /
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file:classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                //获取文件的名字 将后缀.class去掉
                String className = (scanPackage + '.' + file.getName().replace(".class",""));
                //添加到类名称里
                classNames.add(className);
            }
        }
    }
    //工厂模式的具体实现
    private void doInstance() {
        //初始化为DI做准备（控制反转（依赖注入））
        if(classNames.isEmpty()){
            return;
        }

        try {
            for (String className: classNames) {
                Class<?> clazz = Class.forName(className);

                //判断此class类中是否存在 当前 注解
                if(clazz.isAnnotationPresent(GPController.class)){
                    //创建当前 获取对象的实例（也就是new）
                    Object instance = clazz.newInstance();
                    //Spring 默认类名手写字母小写 (之所以要自己写一个方法 而不是用 clazz.getSimpleName().toLowerCase(); 是因为 他可能是驼峰命名 后面字段中存在大写字母)
                    String beanName =   doLowerFirsCase(clazz.getSimpleName());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String doLowerFirsCase(String simpleName){
        char[] chars = simpleName.toCharArray();
        //用ASCII码做转换 大写字母的ASCII码比小写字母少32
        chars[0] += 32;
        return String.valueOf(chars);
    }
    
    private void duAutowired() {
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //获取所有字段，包括private、protected、default类型的
            //正常来说，普通的OOP编程只能获得public字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if(!field.isAnnotationPresent(GPAutowired.class)){
                    continue;
                }
                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                //如果用户没有自定义beanName，默认就根据类型注入
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    //获得接口的类型，作为key 用key去IOC容器中取值
                    beanName = field.getType().getName();
                }
                //如果是public意外的类型，只要加了@Autowired注解都要强制复制
                //反射中叫作暴力访问
                field.setAccessible(true);

                try {
                    //利用反射机制给字段赋值
                    //set 将指定对象参数上由此Field对象表示的字段设置为指定的新值。 如果基础字段具有原始类型，则新值会自动解包
                    field.set(entry.getValue(),ioc.get(beanName));
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //策略模式的应用案例 算法模式 模式， 策略模式是一种定义一系列算法的方法，从概念上来看，所有这些算法完成的都是相同的工作， 只是实现不同， 它可以以相同的方式调用所有的算法，减少了各种算法类与使用算法类之间的耦合
    //初始化url和Method的一对一关系
    private void initHandlerMapping(){
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if(clazz.isAnnotationPresent(GPController.class)){continue;}
            //保存写在类上面的@GPRequestMapping（"/demo"）
            String baseUrl = "";
            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
                //获取到当前类上所注释的注解
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                //获取到当前注解的属性值url例（/demo）获取属性 之后可以处理生成访问
                baseUrl = requestMapping.value();
            }
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(GPRequestMapping.class)){
                    continue;
                }
                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                //优化
                String url = ("/" + baseUrl + "/" +requestMapping.value()).replaceAll("/+","/");
                System.out.println("Mapped :" + url + "," + method);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        //运行阶段
        try{
            //委派模式
            doDispatch(req,resp);
        }catch (Exception e){
            e.printStackTrace();
            resp.getWriter().write("500 Exection,Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }

    //虽然完成了动态委派并进行了反射带哦用，但对url的处理还是静态的
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        //获取所有路径，然后替换掉相对路径获取根目录
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        //当保存的路径中没有找到此路径则为404 这为@GPRequestMapping的访问页面不存在的实现
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found ！");
            return;
        }
        Method method = this.handlerMapping.get(url);
        // 第一个参数：方法所在的实例
        // 第二个参数：调用时所需要的实参

        Map<String, String[]> params = req.getParameterMap();
        //投机取巧的方式
        String beanName = doLowerFirsCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),new Object[]{req,resp,params.get("name")[0]});
    }
}
