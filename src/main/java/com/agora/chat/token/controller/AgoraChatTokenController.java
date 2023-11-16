package com.agora.chat.token.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.agora.chat.ChatTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
public class AgoraChatTokenController {

    @Value("${appid}")
    private String appid;

    @Value("${appcert}")
    private String appcert;

    @Value("${expire.second}")
    private int expirePeriod;

    @Value("${appkey}")
    private String appkey;

    @Value("${domain}")
    private String domain;

    private final RestTemplate restTemplate = new RestTemplate();

    private Cache<String, String> agoraChatAppTokenCache;

    @PostConstruct
    public void init() {
        agoraChatAppTokenCache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(expirePeriod, TimeUnit.SECONDS).build();
    }

    /**
     *
     * Get app privilege token
     * @return app privilege token
     */
    @GetMapping("/chat/app/token")
    public ResponseEntity getAppToken() {

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            throw new IllegalArgumentException("appid or appcert is not empty");
        }

        String appToken = generateAgoraAppToken();
        return ResponseEntity.ok(new AgoraChatTokenResponse(appToken));
    }

    /**
     * Get user privilege token
     * @param chatUserName chat username
     * @return user privilege token
     */
    @GetMapping("/chat/user/{chatUserName}/token")
    public ResponseEntity getUserToken(@PathVariable String chatUserName) {

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            throw new IllegalArgumentException("appid or appcert is not empty");
        }

        if (!StringUtils.hasText(chatUserName)) {
            throw new IllegalArgumentException("chatUserName is not empty");
        }

        ChatTokenBuilder2 builder = new ChatTokenBuilder2();
        String userToken = builder.buildUserToken(appid, appcert, chatUserName, expirePeriod);
        return ResponseEntity.ok(new AgoraChatTokenResponse(userToken));
    }

    /**
     * Register users on the Agora Chat server based on the username.
     *
     * @param registerChatUserRequest register users request
     */
    @PostMapping("/chat/user/register")
    public ResponseEntity registerChatUser(@RequestBody RegisterChatUserRequest registerChatUserRequest) {

        if (!StringUtils.hasText(appkey)) {
            throw new IllegalArgumentException("appkey is not empty");
        }

        if (!appkey.contains("#")) {
            throw new IllegalArgumentException("appkey is illegal");
        }

        List<String> chatUserNameList = registerChatUserRequest.getUsernames();
        if (chatUserNameList == null || chatUserNameList.isEmpty()) {
            throw new IllegalArgumentException("register chat usernames is not empty");
        }

        String orgName = appkey.split("#")[0];
        String appName = appkey.split("#")[1];
        String url = "http://" + domain + "/" + orgName + "/" + appName + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(getAgoraChatAppTokenFromCache());

        List<Map<String, String>> requestBody = new ArrayList<>();
        chatUserNameList.forEach(chatUserName -> {
            Map<String, String> chatUserNameEntity = new HashMap<>();
            chatUserNameEntity.put("username", chatUserName);
            requestBody.add(chatUserNameEntity);
        });

        HttpEntity<List<Map<String, String>>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        } catch (Exception e) {
            throw new RestClientException("register chat user error : " + e.getMessage());
        }

        return ResponseEntity.ok(new AgoraChatTokenResponse("register successful"));
    }

    /**
     * Generate agora chat app token
     * @return Agora Chat app token
     */
    private String generateAgoraAppToken() {

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            throw new IllegalArgumentException("appid or appcert is not empty");
        }

        // Use agora App Id、App Cert to generate agora app token
        ChatTokenBuilder2 builder = new ChatTokenBuilder2();
        return builder.buildAppToken(appid, appcert, expirePeriod);
    }

    /**
     * Get agora chat app token from cache
     * @return Agora Chat App Token
     */
    private String getAgoraChatAppTokenFromCache() {

        try {
            return agoraChatAppTokenCache.get("agora-chat-app-token", () -> {
                return generateAgoraAppToken();
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Get Agora Chat app token from cache error");
        }
    }

}
