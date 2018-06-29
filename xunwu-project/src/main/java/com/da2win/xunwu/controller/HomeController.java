package com.da2win.xunwu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author Darwin
 * @date 2018/6/29
 */
@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
