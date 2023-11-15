package com.agora.chat.token;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.agora.chat.ChatTokenBuilder2;
import io.agora.media.AccessToken2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
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
     * 获取 app 权限 token
     * @return app 权限 token
     */
    @GetMapping("/chat/app/token")
    public String getAppToken() {

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            return "appid or appcert is not empty";
        }

        return getAgoraAppToken();
    }

    /**
     * 获取 user 权限 token
     * @param chatUserName chat 用户名
     * @return user 权限 token
     */
    @GetMapping("/chat/user/{chatUserName}/token")
    public String getChatToken(@PathVariable String chatUserName) {

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            return "appid or appcert is not empty";
        }

        if (!StringUtils.hasText(appkey) || !StringUtils.hasText(domain)) {
            return "appkey or domain is not empty";
        }

        if (!appkey.contains("#")) {
            return "appkey is illegal";
        }

        if (!StringUtils.hasText(chatUserName)) {
            return "chatUserName is not empty";
        }

        String chatUserUuid = getChatUserUuid(chatUserName);

        if (chatUserUuid == null) {
            chatUserUuid = registerChatUser(chatUserName);
        }

        ChatTokenBuilder2 builder = new ChatTokenBuilder2();
        return builder.buildUserToken(appid, appcert, chatUserUuid, expirePeriod);
    }

    /**
     * 根据用户名和密码在 agora chat 服务器上注册一个 user，并获取到此用户的 uuid 用于生成 user 权限 token
     * 这里密码默认使用 "123"
     *
     * @param chatUserName 用户名
     * @return uuid
     */
    private String registerChatUser(String chatUserName) {

        String orgName = appkey.split("#")[0];

        String appName = appkey.split("#")[1];

        String url = "http://" + domain + "/" + orgName + "/" + appName + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(getAgoraChatAppTokenFromCache());

        Map<String, String> body = new HashMap<>();
        body.put("username", chatUserName);
        body.put("password", "123");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        } catch (Exception e) {
            throw new RestClientException("register chat user error : " + e.getMessage());
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("entities");

        return (String) results.get(0).get("uuid");
    }

    /**
     * 根据用户名到 agora chat 服务器上获取此用户，如用户存在则获取此用户的 uuid，用户不存在返回 null
     *
     * @param chatUserName 用户名
     * @return uuid
     */
    private String getChatUserUuid(String chatUserName) {

        String orgName = appkey.split("#")[0];

        String appName = appkey.split("#")[1];

        String url = "http://" + domain + "/" + orgName + "/" + appName + "/users/" + chatUserName;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(getAgoraChatAppTokenFromCache());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Map> responseEntity = null;

        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        } catch (Exception e) {
            System.out.println("get chat user error : " + e.getMessage());
        }

        if (responseEntity != null) {

            List<Map<String, Object>> results = (List<Map<String, Object>>) responseEntity.getBody().get("entities");

            return (String) results.get(0).get("uuid");
        }

        return null;
    }

    /**
     * 生成 Agora Chat app token
     * @return Agora Chat app token
     */
    private String getAgoraAppToken() {
        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            throw new IllegalArgumentException("appid or appcert is not empty");
        }

        // Use agora App Id、App Cert to generate agora app token
        ChatTokenBuilder2 builder = new ChatTokenBuilder2();
        return builder.buildAppToken(appid, appcert, expirePeriod);
    }

    /**
     * 从缓存中获取 Agora Chat App Token
     * @return Agora Chat App Token
     */
    private String getAgoraChatAppTokenFromCache() {
        try {
            return agoraChatAppTokenCache.get("agora-chat-app-token", () -> {
                return getAgoraAppToken();
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Get Agora Chat app token from cache error");
        }
    }

}
