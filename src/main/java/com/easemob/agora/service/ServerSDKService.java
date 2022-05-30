package com.easemob.agora.service;

import com.easemob.im.server.api.token.Token;

public interface ServerSDKService {
    /**
     * 为用户注册 chat 用户名
     * @param chatUserName chat用户名
     */
    void registerChatUserName(String chatUserName);

    /**
     * 检查 chat 用户名是否存在
     * @param chatUserName chat用户名
     * @return boolean
     */
    boolean checkIfChatUserNameExists(String chatUserName);

    /**
     * 获取 chat 用户id
     * @param chatUserName chat用户名
     * @return uuid
     */
    String getChatUserId(String chatUserName);

    /**
     * 生成声网 appToken
     * @return Token
     */
    Token generateAppToken();

    /**
     * 生成声网 chatUserToken
     * @param chatUserName chat用户名
     * @param chatUserId chat用户id
     * @return chatUserToken
     */
    String generateAgoraChatUserToken(String chatUserName, String chatUserId);

    /**
     * 生成声网 rtcToken
     * @param channelName 频道名称
     * @param agorauid 声网uid
     * @return rtcToken
     */
    String generateAgoraRtcToken(String channelName, Integer agorauid);
}
