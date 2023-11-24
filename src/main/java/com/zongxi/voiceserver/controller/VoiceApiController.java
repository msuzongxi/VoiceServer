package com.zongxi.voiceserver.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import com.zongxi.voiceserver.service.AmazonClient;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Base64;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Random;


@RestController
@RequestMapping("/api")
public class VoiceApiController {
	
	@Resource
	private AmazonClient amazonClient;

	@Value("${storage.root}")
    private String storageRoot;    

	@Value("${api.popularity.py}")
    private String popularityPy;    
	@Value("${api.urbcf.py}")
    private String urbcfPy;    
	@Value("${api.vbcf.py}")
    private String vbcfPy;    
	
	@Value("${emb.py}")
    private String embeddingPy;    
	
    
	@CrossOrigin
	@RequestMapping(value="/voices", method=RequestMethod.POST)
	public StreamingResponseBody saveAudio(@RequestBody VoiceBody voice) {
		System.out.println(voice);
		byte[] decoded = Base64.getDecoder().decode(voice.getMesage());
		String s = null;
		try
		{
//		    File file = new File("/Users/lewis/Documents/voiceserver/upload/"+voice.getRid()+ ".wav");
		    File file = new File(storageRoot+"audio/"+voice.getRid()+ ".wav");
		    FileOutputStream os = new FileOutputStream(file);
		    os.write(decoded);
		    os.close();
		    
		    Process p = Runtime.getRuntime().exec(embeddingPy+" --file "+voice.getRid()+".wav");
			BufferedReader stdInput = new BufferedReader(new 
	                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
		return new StreamingResponseBody() {

			@Override
			public void writeTo(OutputStream out) throws IOException {
				out.write("hello audio saver".getBytes());
				out.flush();
			}
			
		};
	}
	
	@CrossOrigin
	@RequestMapping(value="/recommend", method=RequestMethod.POST)
	public StreamingResponseBody recommend(@RequestBody RatingsBody rating) {
		System.out.println(rating);
		return new StreamingResponseBody() {

			@Override
			public void writeTo(OutputStream out) throws IOException {
				String s = null;
				try {
					Random rand = new Random();
					int randNum = rand.nextInt(3) + 1;
					String algPy = vbcfPy;
					if(randNum == 1) {
						algPy = popularityPy;
					}else if(randNum == 2) {
						algPy = urbcfPy;
					}else {
						algPy = vbcfPy;
					}
					File file = new File(storageRoot+"random/"+rating.getUid()+".txt");
				    FileOutputStream os = new FileOutputStream(file);
				    os.write(String.valueOf(randNum).getBytes());
				    os.close();

				    System.out.println("Here is the randomization result:" + String.valueOf(randNum)+", "+algPy);
					
					Process p = Runtime.getRuntime().exec(algPy+" "+rating.getMesage()+" "+rating.getUid());
					BufferedReader stdInput = new BufferedReader(new 
			                 InputStreamReader(p.getInputStream()));

		            BufferedReader stdError = new BufferedReader(new 
		                 InputStreamReader(p.getErrorStream()));

		            // read the output from the command
		            StringBuilder builder = new StringBuilder();
		            while ((s = stdInput.readLine()) != null) {
		                builder.append(s);
		            }
		            System.out.println("Here is the standard output of the command (if any):\n");
		            System.out.println(builder.toString());
		            
		            out.write(builder.toString().getBytes());
		            out.flush();
		            
		            // read any errors from the attempted command
		            System.out.println("Here is the standard error of the command (if any):\n");
		            while ((s = stdError.readLine()) != null) {
		                System.out.println(s);
		            }
				}
				catch (IOException e) {
		            System.out.println("exception happened - here's what I know: ");
		            e.printStackTrace();
		        }
//				out.write("{\"songs\":\"1;2;3;4;5;6;7;8;9;10\"}".getBytes());
//				out.flush();
			}
			
		};
	}
	

}
