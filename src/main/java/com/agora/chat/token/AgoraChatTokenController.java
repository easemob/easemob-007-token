package com.agora.chat.token;

import io.agora.chat.ChatTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class AgoraChatTokenController {

    @Value("${appid}")
    private String appid;

    @Value("${appcert}")
    private String appcert;

    @Value("${expire.second}")
    private int expirePeriod;

    /**
     *
     * get app privilege token
     * @return app privilege token
     */
    @GetMapping("/chat/app/token")
    public String getAppToken() {
        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            throw new IllegalArgumentException("appid or appcert is not empty.");
        }

        // Use agora App Id、App Cert to generate agora app privilege token
        ChatTokenBuilder2 builder = new ChatTokenBuilder2();
        return builder.buildAppToken(appid, appcert, expirePeriod);
    }

    /**
     * get user privilege token
     * @param chatUserName chat username,
     * Chat Username need to comply with the rules for username in the document link,
     * <a href="https://docs.agora.io/en/agora-chat/restful-api/user-system-registration?platform=react-native#common-parameters">Username Rules</a>
     * @return user privilege token
     */
    @GetMapping("/chat/user/{chatUserName}/token")
    public String getChatUserToken(@PathVariable String chatUserName) {
        if (!StringUtils.hasText(appid) || !StringUtils.hasText(appcert)) {
            throw new IllegalArgumentException("appid or appcert is not empty.");
        }

        if (!StringUtils.hasText(chatUserName)) {
            throw new IllegalArgumentException("chatUserName is not empty.");
        }

        // Use agora App Id、App Cert、chat username to generate agora user privilege token
        ChatTokenBuilder2 builder = new ChatTokenBuilder2();
        return builder.buildUserToken(appid, appcert, chatUserName, expirePeriod);
    }

}
