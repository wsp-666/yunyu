package com.yunyu.dto;

import lombok.Data;

@Data
public class PostDTO {
    private Integer type;
    private String title;
    private String content;
    private String images;
    private String videoUrl;
    private Integer spotId;
    private Integer venueId;
    private String topicTag;
}
