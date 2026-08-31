package com.yunyu.controller;

import com.yunyu.common.Result;
import com.yunyu.dto.VideoDTO;
import com.yunyu.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/videos/";

    @Autowired
    private VideoService videoService;

    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadVideo(
            @RequestParam MultipartFile video,
            @RequestParam int venueId,
            @RequestParam int type,
            @RequestParam(required = false) Integer fishCount,
            @RequestParam(required = false) String fishSpecies,
            @RequestParam(required = false) String fishingTime,
            @RequestParam(required = false) String ticketPrice,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String ext = ".mp4";
        String originalName = video.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + ext;
        String videoUrl = "/videos/" + filename;

        try {
            video.transferTo(new File(dir, filename));
        } catch (Exception e) {
            e.printStackTrace();
        }

        VideoDTO dto = new VideoDTO();
        dto.setVenueId(venueId);
        dto.setType(type);
        dto.setFishCount(fishCount);
        dto.setFishSpecies(fishSpecies);

        int videoId = videoService.uploadVideo(videoUrl, null, dto, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("videoId", videoId);
        result.put("videoUrl", videoUrl);
        return Result.success(result);
    }

    @DeleteMapping("/delete/{videoId}")
    public Result<Void> deleteVideo(@PathVariable int videoId) {
        videoService.deleteVideo(videoId);
        return Result.success();
    }

    @PostMapping("/like/{videoId}")
    public Result<Void> likeVideo(@PathVariable int videoId) {
        videoService.likeVideo(videoId);
        return Result.success();
    }
}
