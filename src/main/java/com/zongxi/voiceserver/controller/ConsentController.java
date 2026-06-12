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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

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
	
	@GetMapping("/consentFormEsign")
	public String consentFormEsign(Model model, @RequestParam(value="test", required=false, defaultValue="n")String test, HttpSession session) {
		model.addAttribute("uuid", UUID.randomUUID());
		session.setAttribute("test", test);
		return "consentFormEsign";
	}
	
	@GetMapping("/instruction")
	public String instruction(Model model,HttpSession session) {
    	String uuid = (String) session.getAttribute("uuid");
    	System.out.println("before"+storageRoot+uuid);
    	File file = new File(storageRoot+"consent1"+File.separator+uuid+".txt");
    	if(!file.exists()) {
    		throw new RuntimeException("uuid doesn't exist:"+uuid);
    	}
    	System.out.println("after"+storageRoot+uuid);
		return "instructionForm";
	}
	
	@RequestMapping("/instructionNext")
	public RedirectView instructionNext(Model model, HttpSession session) {
    	String uuid = (String) session.getAttribute("uuid");
    	File file = new File(storageRoot+"consent1"+File.separator+uuid+".txt");
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
		    File file = new File(storageRoot+"consent1"+File.separator+uuid+".txt");
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
	
	@RequestMapping(value="/saveConsentEsign", method=RequestMethod.POST)
	public RedirectView saveConsentEsign(
	        Model model,
	        HttpSession session,
	        HttpServletRequest request,

	        @RequestParam String terms,
	        @RequestParam String uuid,
	        @RequestParam String printedName,
	        @RequestParam String signatureData,
	        @RequestParam(required = false) String consentTimestamp,
	        @RequestParam(required = false) String consentVersion
	) {
	    boolean validTerms =
	            "on".equalsIgnoreCase(terms)
	            || "agree".equalsIgnoreCase(terms);

	    if (!validTerms || StringUtils.isEmpty(uuid)) {
	        throw new RuntimeException("invalid post: missing consent checkbox or uuid");
	    }

	    if (StringUtils.isEmpty(printedName)) {
	        throw new RuntimeException("invalid post: missing printed name");
	    }

	    if (StringUtils.isEmpty(signatureData)) {
	        throw new RuntimeException("invalid post: missing signature");
	    }

	    try {
	        // Prevent path traversal or unsafe filenames
	        String safeUuid = uuid.replaceAll("[^a-zA-Z0-9_-]", "");
	        if (StringUtils.isEmpty(safeUuid)) {
	            throw new RuntimeException("invalid uuid");
	        }

	        String cleanPrintedName = printedName.trim();

	        // Main consent directory
	        Path consentDir = Paths.get(storageRoot, "consent1");
	        Files.createDirectories(consentDir);

	        // Keep your original qidx logic
	        Random random = new Random();
	        int qidx = random.nextInt(5);

	        // Existing file: used by your current workflow
	        Path qidxFile = consentDir.resolve(safeUuid + ".txt");
	        Files.write(
	                qidxFile,
	                String.valueOf(qidx).getBytes(StandardCharsets.UTF_8)
	        );

	        // Save signature image
	        String base64Signature = signatureData;

	        // Remove data URL prefix:
	        // data:image/png;base64,xxxxxxxx
	        String prefix = "data:image/png;base64,";
	        if (base64Signature.startsWith(prefix)) {
	            base64Signature = base64Signature.substring(prefix.length());
	        }

	        byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);

	        if (signatureBytes.length < 100) {
	            throw new RuntimeException("invalid post: signature image too small");
	        }

	        Path signatureFile = consentDir.resolve(safeUuid + "_signature.png");
	        Files.write(signatureFile, signatureBytes);

	        // Save consent metadata
	        String serverTimestamp = java.time.Instant.now().toString();
	        String ipAddress = request.getRemoteAddr();
	        String userAgent = request.getHeader("User-Agent");

	        Path consentRecordFile = consentDir.resolve(safeUuid + "_consent_record.txt");

	        StringBuilder record = new StringBuilder();
	        record.append("uuid=").append(safeUuid).append("\n");
	        record.append("printedName=").append(cleanPrintedName).append("\n");
	        record.append("terms=").append(terms).append("\n");
	        record.append("participantConsentTimestamp=").append(nullToEmpty(consentTimestamp)).append("\n");
	        record.append("serverTimestamp=").append(serverTimestamp).append("\n");
	        record.append("consentVersion=").append(nullToEmpty(consentVersion)).append("\n");
	        record.append("signatureFile=").append(signatureFile.getFileName().toString()).append("\n");
	        record.append("qidx=").append(qidx).append("\n");
	        record.append("ipAddress=").append(nullToEmpty(ipAddress)).append("\n");
	        record.append("userAgent=").append(nullToEmpty(userAgent)).append("\n");

	        Files.write(
	                consentRecordFile,
	                record.toString().getBytes(StandardCharsets.UTF_8)
	        );

	        session.setAttribute("qidx", String.valueOf(qidx));
	        session.setAttribute("uuid", safeUuid);

	        return new RedirectView("instruction");
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException("failed to save consent", e);
	    }
	}

	private String nullToEmpty(String value) {
	    return value == null ? "" : value;
	}
}
