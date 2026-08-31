package com.yunyu.controller;

import com.yunyu.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/images/";

    @PostMapping("/upload-images")
    public Result<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                String ext = ".jpg";
                String originalName = file.getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + ext;
                File dest = new File(dir, filename);
                file.transferTo(dest);
                urls.add("/images/" + filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Result.success(urls);
    }

    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件为空");
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        try {
            String ext = ".jpg";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + ext;
            File dest = new File(dir, filename);
            file.transferTo(dest);
            return Result.success("/images/" + filename);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}
