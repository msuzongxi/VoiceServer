package com.zongxi.voiceserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class VoiceSurveyController {
	
    @Value("${icebreaker.q1}")
    private String q1;    
    @Value("${icebreaker.q2}")
    private String q2;    
    @Value("${icebreaker.q3}")
    private String q3;    
    @Value("${icebreaker.q4}")
    private String q4;    
    @Value("${icebreaker.q5}")
    private String q5;    

    @Value("${storage.root}")
    private String storageRoot;    


    @GetMapping("/voiceRecord")
	public String greeting(Model model, HttpSession session) {
    	String uuid = (String) session.getAttribute("uuid");
    	File file = new File(storageRoot+"consent"+File.separator+uuid+".txt");
    	if(!file.exists()) {
    		throw new RuntimeException("uuid doesn't exist:"+uuid);
    	}
		List questions = getQuestions();
		int qidx = Integer.parseInt((String) session.getAttribute("qidx"));
		model.addAttribute("question", questions.get(qidx));
		return "voiceSurvey";
	}


	private List getQuestions() {
		List q = new ArrayList<String>();
		q.add(q1);
		q.add(q2);
		q.add(q3);
		q.add(q4);
		q.add(q5);
		return q;
	}

}