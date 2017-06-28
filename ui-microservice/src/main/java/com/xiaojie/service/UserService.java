package com.xiaojie.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.xiaojie.models.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by hadoop on 17-6-21.
 */

@Service
public class UserService {
    private RestTemplate restTemplate;
    final String SERVICE_NAME="user"; //对应相应的service的spring.application.name

    @HystrixCommand(fallbackMethod = "fallbackSearchAll")
    public UserInfo getUserInfo(String userid) {
        restTemplate = new RestTemplate();
        return restTemplate.getForObject("http://"+SERVICE_NAME+"/user"+"?userid="+userid, UserInfo.class);
    }
    private UserInfo fallbackSearchAll() {
        System.out.println("HystrixCommand fallbackMethod handle!");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserid("TestHystrixCommand");
        return userInfo;
    }
}