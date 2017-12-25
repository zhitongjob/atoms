package com.lovver.atoms.springboot.controller;

import com.lovver.atoms.core.CacheChannel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        CacheChannel.getInstance().get("hello","jobell");
        return "hello";
    }
}
