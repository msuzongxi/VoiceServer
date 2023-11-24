package com.zongxi.voiceserver.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

@Controller
public class GtmTestController {

	@GetMapping("/test")
	public String greeting(Model model) {
		model.addAttribute("randomid", UUID.randomUUID());
		model.addAttribute("name", "zongxi");
		return "test";
	}

}
