package com.jm.oauth.service;

import com.jm.oauth.util.AuthToken;

public interface UserLoginService {
    AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws Exception;
}
