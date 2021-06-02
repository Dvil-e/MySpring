package com.gupaoedu.demo.mvc.action;

import com.gupaoedu.demo.service.IDemoService;
import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@GPController
@GPRequestMapping("/demo")
public class DemoAction {
    @GPAutowired
    private IDemoService demoService;
    @GPRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp, @GPRequestParam("name") String name){
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @GPRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp, @GPRequestParam("a") Integer a, @GPRequestParam("b") Integer b){
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @GPRequestMapping("/remove")
    public void remove(HttpServletRequest req, HttpServletResponse resp, @GPRequestParam("id") Integer id){

    }
}
