package com.yunyu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    @Value("${yunyu.deepseek.api-key}")
    private String apiKey;

    @Value("${yunyu.deepseek.base-url}")
    private String baseUrl;

    @Value("${yunyu.deepseek.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public DeepSeekService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
    }

    public String chat(String systemPrompt, String userPrompt) {
        return callApi(systemPrompt, userPrompt);
    }

    public String chatWithImage(String systemPrompt, String userPrompt, String base64Image) {
        List<Map<String, Object>> userContent = new ArrayList<>();

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", userPrompt);
        userContent.add(textPart);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        imagePart.put("image_url", imageUrl);
        userContent.add(imagePart);

        return callApiWithVision(systemPrompt, userContent);
    }

    private String callApi(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.7);
            body.put("max_tokens", 2048);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String errMsg = error.path("message").asText("API错误");
                System.err.println("DeepSeek API error: " + errMsg);
                throw new RuntimeException("DeepSeek: " + errMsg);
            }
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("DeepSeek API调用失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("DeepSeek API调用失败: " + e.getMessage(), e);
        }
    }

    private String callApiWithVision(String systemPrompt, List<Map<String, Object>> userContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.7);
            body.put("max_tokens", 2048);

            List<Object> messages = new ArrayList<>();

            Map<String, Object> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userContent);
            messages.add(userMsg);

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek Vision API调用失败: " + e.getMessage(), e);
        }
    }
}
