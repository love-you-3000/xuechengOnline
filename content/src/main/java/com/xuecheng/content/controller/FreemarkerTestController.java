package com.xuecheng.content.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @className: ThymeleafTest
 * @author: 朱江
 * @description:
 * @date: 2023/6/12
 **/

@Controller
public class FreemarkerTestController {

    @GetMapping("/testfree")
    public String test(Model model) {
        model.addAttribute("name","test success!!!!lu");
        return "test";
    }
}
