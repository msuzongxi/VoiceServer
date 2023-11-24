package com.zongxi.voiceserver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FileController {
	
    @Value("${storage.root}")
    private String storageRoot;    

	
	@GetMapping("/audioFiles")
	public String listUploadedFiles(Model model, @RequestParam String r) throws IOException {
		if(!"UWMTemp12345!".equals(r)) {
			throw new RuntimeException("invalid key");
		}
		File file = new File(storageRoot+"audio/");
		File[] files = file.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
		List<FileObj> fileObjs = new ArrayList<FileObj>();
		for(File f: files) {
			FileObj fo = new FileObj();
			if(f.getName().contains(".DS_Store"))
				continue;
			fo.setFileName(f.getName());
			fo.setLength(String.valueOf(f.length()));
			fo.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(f.lastModified())));
			fileObjs.add(fo);
		}
		
	    model.addAttribute("fileObjs", fileObjs);

	    return "fileListForm";
	}
	
	@GetMapping("/consentFiles")
	public String listConsentFiles(Model model, @RequestParam String r) throws IOException {
		if(!"UWMTemp12345!".equals(r)) {
			throw new RuntimeException("invalid key");
		}

		File file = new File(storageRoot+"consent/");
		File[] files = file.listFiles();
		Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
		
		List<FileObj> fileObjs = new ArrayList<FileObj>();
		for(File f: files) {
			FileObj fo = new FileObj();
			fo.setFileName(f.getName());
			if(f.getName().contains(".DS_Store"))
				continue;
			fo.setLength(String.valueOf(f.length()));
			fo.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(f.lastModified())));
			fileObjs.add(fo);
		}
		
	    model.addAttribute("fileObjs", fileObjs);

	    return "consentFileListForm";
	}
	
	
	@RequestMapping(value = "/openFile/{file}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity playAudio(HttpServletRequest request,HttpServletResponse response, @PathVariable("file") String file) throws FileNotFoundException{

		String filePath = storageRoot + "consent/" + file;
		HttpHeaders httpHeaders = new HttpHeaders();
		if(file.contains(".wav") || file.contains(".mp4")) {
			file = file.replace(".mp4", ".wav");
			filePath = storageRoot + "audio/" + file;
			httpHeaders.add("Content-Type", "audio/mpeg");
		}

        InputStreamResource inputStreamResource = new InputStreamResource( new FileInputStream(filePath));
        
//        httpHeaders.setContentLength(length);
        httpHeaders.setCacheControl(CacheControl.noCache().getHeaderValue());
        return new ResponseEntity(inputStreamResource, httpHeaders, HttpStatus.OK);
    }
	
	public static class FileObj{
		private String fileName;
		private String date;
		private String length;
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public String getLength() {
			return length;
		}
		public void setLength(String length) {
			this.length = length;
		}
	}

}
