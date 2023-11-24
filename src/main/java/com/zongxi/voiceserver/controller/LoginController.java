package com.zongxi.voiceserver.controller;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
	
	@GetMapping("/login")
	public String getLogin(Model model) {
		return "loginForm";
	}

	@RequestMapping(value="/doLogin", method=RequestMethod.POST)
	public String postLogin(Model model, @RequestParam String username, @RequestParam String password) {
		System.out.println(username+password);
		model.addAttribute("username", username);
		return "loginSuccessForm";
	}

}
