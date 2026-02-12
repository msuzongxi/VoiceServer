package com.zongxi.voiceserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpSession;

@Controller
public class VideoController {

    @Value("${storage.root}")
    private String storageRoot;
    
    @Value("${qualtrics.link}")
    private String qualtricsLink;

    // Render page
    @GetMapping("/videoRecord")
    public String videoPage(Model model,
                            @RequestParam(value="test", required=false, defaultValue="n") String test,
                            HttpSession session) {
        session.setAttribute("test", test);
        return "videoRecord";
    }

    // Upload endpoint
    @PostMapping(value = "/video/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String uploadVideo(@RequestParam("file") MultipartFile file) {

        String uploadDir = storageRoot + File.separator + "video" + File.separator;
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filename = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "recording.webm"
                : file.getOriginalFilename();

        try {
            File dest = new File(uploadDir + filename);
            file.transferTo(dest);
            return "Upload successful: " + dest.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "Upload failed: Server Error.";
        }
    }

    // Next step check
    @GetMapping("/videoRecordNext")
    public String videoRecordNext(HttpSession session) {

        Object uuidObj = session.getAttribute("uuid");
        if (uuidObj == null) {
            return "redirect:/videoRecord?error=session";
        }

        String uuid = uuidObj.toString();

        String uploadDir = storageRoot + File.separator + "video" + File.separator;
        File directory = new File(uploadDir);

        if (!directory.exists() || !directory.isDirectory()) {
            return "redirect:/videoRecord?error=novideo";
        }

        boolean fileFound = false;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    String name = f.getName();
                    if (name != null && name.startsWith(uuid + ".")) {
                        fileFound = true;
                        break;
                    }
                }
            }
        }

        if (fileFound) {
            String encodedUuid = URLEncoder.encode(uuid, StandardCharsets.UTF_8);
            String externalUrl = qualtricsLink + "?uuid=" + encodedUuid;
            return "redirect:" + externalUrl;
        }

        return "redirect:/videoRecord?error=novideo";
    }
}
