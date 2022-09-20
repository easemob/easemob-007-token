package com.easemob.agora;

import com.easemob.agora.utils.agoratools.chat.ChatTokenBuilder2;
import org.junit.Test;

public class AgoraTokenTest {

    @Test
    public void testGenerateToken() {
        String appId = "";
        String appCert = "";
        int expirePeriod = 86400;

        ChatTokenBuilder2 builder = new ChatTokenBuilder2();

        // 1. Generate a token with app privileges
        String tokenWithAppPrivileges = builder.buildAppToken(appId, appCert, expirePeriod);
        System.out.println("tokenWithAppPrivileges : " + tokenWithAppPrivileges);

        // 2.Register Agora Chat User And get the UUID
        String agoraChatUserUuid = "";

        // 3.Generate a token with user privileges
        String tokenWithUserPrivileges = builder.buildUserToken(appId, appCert, agoraChatUserUuid, expirePeriod);
        System.out.println("tokenWithUserPrivileges : " + tokenWithUserPrivileges);
    }
}
