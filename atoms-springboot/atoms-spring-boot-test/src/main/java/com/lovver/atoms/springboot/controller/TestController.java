package com.lovver.atoms.springboot.controller;

import com.lovver.atoms.core.CacheChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @Autowired
    private  CacheChannel cacheChannel;
    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        cacheChannel.set("hello","jobell","test");
        System.out.println(cacheChannel.get("hello","jobell"));
        return "hello";
    }
}
