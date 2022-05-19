package com.agora.chat.token;

import com.agora.chat.token.io.agora.chat.ChatTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@CrossOrigin
public class AgoraChatTokenController {

    @Value("${appid}")
    private String appid;

    @Value("${appcert}")
    private String appcert;

    @Value("${expire.second}")
    private int expire;

    @Value("${appkey}")
    private String appkey;

    @Value("${domain}")
    private String domain;

    private final RestTemplate restTemplate = new RestTemplate();


    /**
     *
     * 获取 agora chat app token
     * @return app token
     */
    @GetMapping("/chat/app/token")
    public String getAppToken() {

        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            return "appid or appcert is not empty";
        }

        ChatTokenBuilder2 builder = new ChatTokenBuilder2();

        return builder.buildAppToken(appid, appcert, expire);
    }

    /**
     * 获取 agora chat user token
     * @param chatUserName chat 用户名
     * @return user token
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

        ChatTokenBuilder2 builder = new ChatTokenBuilder2();

        String chatUserUuid = getChatUserUuid(chatUserName);

        if (chatUserUuid == null) {
            chatUserUuid = registerChatUser(chatUserName);
        }

        return builder.buildUserToken(appid, appcert, chatUserUuid, expire);
    }

    /**
     * 将 agora accessToken 置换成可以调用 agora chat 服务的 token
     * @return token
     */
    private String exchangeToken() {

        String orgName = appkey.split("#")[0];

        String appName = appkey.split("#")[1];

        String url = "http://" + domain + "/" + orgName + "/" + appName + "/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(getAppToken());

        System.out.println("app token = " + getAppToken());

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "agora");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        } catch (Exception e) {
            throw new RestClientException("exchange token error : " + e.getMessage());
        }

        return (String) Objects.requireNonNull(response.getBody()).get("access_token");
    }

    /**
     * 根据用户名和密码在 agora chat 服务器上注册一个 user，并获取到此用户的 uuid 用于生成 agora chat user token
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
        headers.setBearerAuth(exchangeToken());

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
        headers.setBearerAuth(exchangeToken());

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

}
