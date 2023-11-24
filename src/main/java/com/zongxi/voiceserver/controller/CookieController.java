package com.zongxi.voiceserver.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CookieController {
	
	@GetMapping("/addCookie")
	public String getLogin(Model model, HttpServletResponse response, @RequestParam String uid) {
		Cookie cookie = new Cookie("username", uid);
		response.addCookie(cookie);
		model.addAttribute("cookie", "username"+":"+uid);
		return "addCookieForm";
	}
	
	@GetMapping("/displayCookie")
	public String list(HttpServletRequest request, Model model) {
		Cookie[] cookies = request.getCookies();
		List<String> rets = new ArrayList<String>();
		if(cookies != null) {
		for(Cookie c : cookies) {
			rets.add(c.getName()+":"+c.getValue());
		}
		}
		model.addAttribute("cookies", rets);
		return "cookieResultForm";
	}

}
