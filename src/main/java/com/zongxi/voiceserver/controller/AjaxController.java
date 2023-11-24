package com.zongxi.voiceserver.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AjaxController {
	
	@GetMapping("/ajax")
	public String queryAjax(Model model) {
		return "queryAjaxForm";
	}
	
	@GetMapping("/doAjax")
	public String getLogin(Model model) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		model.addAttribute("time", timeStamp);
		return "doAjaxForm";
	}
}
