package com.xiaojie.clients;

import com.xiaojie.models.User;
import com.xiaojie.models.UserInfo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("user")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/findUser")
    UserInfo findUser(@RequestParam(value = "userid")String userid);

    @RequestMapping(method = RequestMethod.GET, value = "/users")
    PagedResources<User> findAll();

    @RequestMapping(method = RequestMethod.GET, value = "/users/{id}")
    List<User> findById(@PathVariable("id") String id);

    @RequestMapping(method = RequestMethod.POST, value = "/users",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    void createUser(@RequestBody User user);
}
