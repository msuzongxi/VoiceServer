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
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

@Controller
public class VideoController {

    @Value("${storage.root}")
    private String storageRoot;
    
    @Value("${qualtrics.link}")
    private String qualtricsLink;
    
    @Value("${scripts.python}")        private String pythonBin;
    @Value("${scripts.extractFrames}") private String extractFramesScript;
    @Value("${scripts.extractAudio}")  private String extractAudioScript;
    @Value("${scripts.detectFace}")    private String detectFaceScript;
    @Value("${scripts.detectSpeech}")  private String detectSpeechScript;

    @Value("${storage.frames}")        private String framesDir;
    @Value("${storage.audio}")         private String audioDir;
    @Value("${models.yunet}")          private String yunetModelPath;

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
    public Map<String, Object> uploadVideo(@RequestParam("file") MultipartFile file, HttpSession session) {

        String uploadDir = storageRoot + File.separator + "video" + File.separator;
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filename = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "recording.webm"
                : file.getOriginalFilename();
        
        Map<String, Object> result = new HashMap<>();
        try {
            File dest = new File(uploadDir + filename);
            file.transferTo(dest);
            String uuid = filename.substring(0, filename.indexOf('.'));
            
            runAndCapture(new ProcessBuilder(
                    pythonBin,
                    extractFramesScript,
                    dest.getAbsolutePath(),
                    framesDir,
                    uuid
            ));

            // 4) run extract_audio.py
            // Usage: extract_audio.py <video_path> <audio_output_dir> <uuid>
            runAndCapture(new ProcessBuilder(
                    pythonBin,
                    extractAudioScript,
                    dest.getAbsolutePath(),
                    audioDir,
                    uuid
            ));

            // 5) run detect_face_yunet.py
            // Usage: detect_face_yunet.py <frames_dir> <uuid> <yunet_onnx_path> [score_th]
            String faceOut = runAndCapture(new ProcessBuilder(
                    pythonBin,
                    detectFaceScript,
                    framesDir,
                    uuid,
                    yunetModelPath,
                    "0.5"
            ));
            boolean faceOk = "1".equals(faceOut.trim());

            // 6) run detect_speech.py
            // Usage: detect_speech.py <wav_path> [mode] [speech_ratio_threshold]
            String wavPath = audioDir + File.separator + uuid + ".wav";
            String speechOut = runAndCapture(new ProcessBuilder(
                    pythonBin,
                    detectSpeechScript,
                    wavPath,
                    "1",      // less strict
                    "0.03"    // less strict
            ));
            boolean speechOk = "1".equals(speechOut.trim());

            // optionally store results in session for next step
            session.setAttribute("faceOk", faceOk ? "y" : "n");
            session.setAttribute("speechOk", speechOk ? "y" : "n");
            result.put("faceOk", faceOk);
            result.put("speechOk", speechOk);
            result.put("success", faceOk && speechOk);

            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            return result;
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
    
    private String runAndCapture(ProcessBuilder pb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process p = pb.start();

        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader r = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
        }

        int code = p.waitFor(); // sync
        if (code != 0) {
            throw new IOException("Command failed (exit=" + code + "):\n" + sb.toString());
        }
        return sb.toString().trim();
    }

}
