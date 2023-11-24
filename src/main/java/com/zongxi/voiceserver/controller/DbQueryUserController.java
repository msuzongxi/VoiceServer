package com.zongxi.voiceserver.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DbQueryUserController {
	
//	@Autowired
//	private JdbcTemplate jdbcTemplate;
//	
//	@GetMapping("/queryUser")
//	public String getLogin(Model model) {
//		return "queryUserForm";
//	}
//
//	@RequestMapping(value="/doQuery", method=RequestMethod.POST)
//	public String postLogin(Model model, @RequestParam String username, @RequestParam String password) {
//		System.out.println(username+""
//				+ ","+password);
//		String query = "select * from tb_user where fname='"+username+"' and pwd='"+password+"'"; 
//		System.out.println(query);
//		List<Map<String, Object>> rets = jdbcTemplate.queryForList(query);
//
//		model.addAttribute("username", username);
//		model.addAttribute("password", password);
//		model.addAttribute("query", query);
//		model.addAttribute("users", rets);
//		
//		return "queryResultForm";
//	}

}
