package com.jm.oauth.service.impl;

import com.jm.oauth.service.UserLoginService;
import com.jm.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    //实现请求发送
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;





    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws Exception {
        //获取指定服务的注册数据
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");

        String url = serviceInstance.getUri()+"/oauth/token";
        // 请求提交的数据
        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<String, String>();
        parameterMap.add("username", username);
        parameterMap.add("password", password);
        parameterMap.add("grant_type", grant_type);

        //请求头封装
        String Authorization = "Basic " + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()), "UTF-8");
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<String, String>();
        headerMap.add("Authorization",Authorization);

        //HttpEntity 创建对象
        HttpEntity httpEntity = new HttpEntity(parameterMap, headerMap);

        //请求头
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        response.getBody();
        System.out.println(response.getBody());
        System.out.println(response.getBody());

        // 用户登录的令牌信息
        Map<String, String> map = response.getBody();

        //令牌登录信息转换成authToken 对象
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(map.get("access_token"));
        authToken.setRefreshToken(map.get("refresh_token"));
        authToken.setJti(map.get("jti"));
        return authToken;

    }
}
