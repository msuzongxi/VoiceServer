package com.zongxi.voiceserver.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.util.StringUtils;

import io.netty.util.internal.StringUtil;

@Controller
public class ConsentController {
	
    @Value("${storage.root}")
    private String storageRoot;    
	
	@GetMapping("/consentForm")
	public String consentForm(Model model, @RequestParam(value="test", required=false, defaultValue="n")String test, HttpSession session) {
		model.addAttribute("uuid", UUID.randomUUID());
		session.setAttribute("test", test);
		return "consentForm";
	}
	
	@GetMapping("/instruction")
	public String instruction(Model model,HttpSession session) {
    	String uuid = (String) session.getAttribute("uuid");
    	System.out.println("before"+storageRoot+uuid);
    	File file = new File(storageRoot+"consent/"+uuid+".txt");
    	if(!file.exists()) {
    		throw new RuntimeException("uuid doesn't exist:"+uuid);
    	}
    	System.out.println("after"+storageRoot+uuid);
		return "instructionForm";
	}
	
	@RequestMapping("/instructionNext")
	public RedirectView instructionNext(Model model, HttpSession session) {
    	String uuid = (String) session.getAttribute("uuid");
    	File file = new File(storageRoot+"consent/"+uuid+".txt");
    	if(!file.exists()) {
    		throw new RuntimeException("uuid doesn't exist:"+uuid);
    	}
    	return new RedirectView("voiceRecord");
	}


	@RequestMapping(value="/saveConsent", method=RequestMethod.POST)
	public RedirectView saveConsent(Model model, HttpSession session, @RequestParam String terms,  @RequestParam String uuid) {
		if(!"on".equalsIgnoreCase(terms) || StringUtils.isEmpty(uuid))
			throw new RuntimeException("invalid post");
		try
		{
		    File file = new File(storageRoot+"consent/"+uuid+".txt");
		    FileOutputStream os = new FileOutputStream(file);
			Random random = new Random();
			int qidx = random.nextInt(5);
		    os.write(String.valueOf(qidx).getBytes());
		    os.close();
			session.setAttribute("qidx", String.valueOf(qidx));
			session.setAttribute("uuid", uuid);
			
			return new RedirectView("instruction");
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
		return null;

	}
}
