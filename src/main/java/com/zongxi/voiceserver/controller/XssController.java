package com.zongxi.voiceserver.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class XssController {
	@GetMapping("/xss")
	public String getxss(Model model) {
		return "xssForm";
	}

	@RequestMapping(value="/doXss", method=RequestMethod.POST)
	public String postxss(Model model, @RequestParam String profile) {
		model.addAttribute("profile", profile);
		return "xssProfileForm";
	}


}
