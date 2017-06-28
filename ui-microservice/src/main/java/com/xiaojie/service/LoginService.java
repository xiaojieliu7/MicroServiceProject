package com.xiaojie.service;

import com.xiaojie.models.UserInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by hadoop on 17-6-22.
 */
@Component
@FeignClient(value = "user") //这里的name对应调用服务的spring.applicatoin.name
public interface LoginService {

    @RequestMapping(value = "/user")
    UserInfo getUserInfo(@RequestParam("userid") String id);
}
