package com.zongxi.voiceserver.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class VideoAdminController {

    @Value("${storage.root}")
    private String storageRoot;

    @GetMapping("/videoAdmin")
    public String listVideos(Model model, @RequestParam("code") String code) {
    	if(!code.equals("Temp12345!")) {
    		return "videoAdmin";
    	}
        String videoDirPath = storageRoot + File.separator + "video" + File.separator;
        File videoDir = new File(videoDirPath);
        
        String frameDirPath = storageRoot + File.separator + "frame" + File.separator;
        File frameDir = new File(frameDirPath);


        List<FileInfo> items = new ArrayList<FileInfo>();

        if (videoDir.exists() && videoDir.isDirectory()) {
            // Optional: filter only common video extensions
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name == null) return false;
                    String n = name.toLowerCase();
                    return true;
                }
            };

            File[] files = videoDir.listFiles(filter);
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.isFile()) {
                        items.add(new FileInfo(
                                f.getName(),
                                f.length(),
                                f.lastModified()
                        ));
                    }
                }
            }
        }
        
        if (frameDir.exists() && frameDir.isDirectory()) {

            File[] files = frameDir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.isFile()) {
                        items.add(new FileInfo(
                                f.getName(),
                                f.length(),
                                f.lastModified()
                        ));
                    }
                }
            }

            // Sort newest first
            Collections.sort(items, new Comparator<FileInfo>() {
                @Override
                public int compare(FileInfo a, FileInfo b) {
                	return a.name.compareToIgnoreCase(b.name);
                }
            });
        }

        model.addAttribute("videoDir", videoDirPath);
        model.addAttribute("files", items);
        return "videoAdmin";
    }

    @GetMapping("/videoAdmin/download")
    public ResponseEntity<Resource> downloadVideo(@RequestParam("name") String name) throws Exception {

        // Basic validation to prevent ../ traversal
        if (name == null || name.trim().isEmpty()
                || name.contains("..") || name.contains("/") || name.contains("\\") ) {
            return ResponseEntity.badRequest().build();
        }

        String videoDirPath = storageRoot + File.separator + "video" + File.separator;
        File baseDir = new File(videoDirPath).getCanonicalFile();
        File target = new File(baseDir, name).getCanonicalFile();

        // Enforce that target is inside baseDir
        if (!target.getPath().startsWith(baseDir.getPath() + File.separator)) {
            return ResponseEntity.status(403).build();
        }

        if (!target.exists() || !target.isFile()) {
            return ResponseEntity.notFound().build();
        }

        // Let browser download
        String encoded = URLEncoder.encode(target.getName(), "UTF-8").replaceAll("\\+", "%20");

        Resource resource = new FileSystemResource(target);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encoded)
                .contentLength(target.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    public static class FileInfo {
        public final String name;
        public final long sizeBytes;
        public final long lastModified;

        public FileInfo(String name, long sizeBytes, long lastModified) {
            this.name = name;
            this.sizeBytes = sizeBytes;
            this.lastModified = lastModified;
        }

        public String getName() { return name; }
        public long getSizeBytes() { return sizeBytes; }
        public long getLastModified() { return lastModified; }

        public String getSizeHuman() {
            double s = (double) sizeBytes;
            String[] units = new String[] {"B","KB","MB","GB","TB"};
            int u = 0;
            while (s >= 1024 && u < units.length - 1) {
                s /= 1024;
                u++;
            }
            return String.format(java.util.Locale.US, "%.2f %s", s, units[u]);
        }

        public String getLastModifiedHuman() {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new java.util.Date(lastModified));
        }
    }
}
