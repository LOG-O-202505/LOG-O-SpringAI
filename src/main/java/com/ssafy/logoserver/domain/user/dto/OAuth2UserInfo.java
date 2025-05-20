// src/main/java/com/ssafy/logoserver/domain/user/dto/OAuth2UserInfo.java
package com.ssafy.logoserver.domain.user.dto;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
    public abstract String getProvider();
}