package com.xiaojie.Controller;

import com.xiaojie.model.UserInfo;
import com.xiaojie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hadoop on 17-6-21.
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @return
     */
    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String register(@RequestParam(value = "userInfo")UserInfo userInfo){
        // 内存数据库操作
        userService.save(userInfo);
        return "save ok";
    }


    /**
     * 根据userid查找用户
     * @param userid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/findUser")
    public UserInfo findUser(@RequestParam(value = "userid")String userid){
        // 数据库无数据,初始化数据
        Iterable<UserInfo> userInfos = userService.findAll();
        if (!userInfos.iterator().hasNext())
            save();
        return userService.findUser(userid);
    }


    @RequestMapping("/save")
    public String save(){
        // 内存数据库操作
        userService.save(new UserInfo("4","123456","23", "male"));
        userService.save(new UserInfo("1001","1234","24", "male"));
        userService.save(new UserInfo("1002","1234","24", "female"));
        userService.save(new UserInfo("1003","1234","23", "female"));
        userService.save(new UserInfo("1004","1234","34", "female"));
        userService.save(new UserInfo("1005","1234","14", "male"));
        return "save ok";
    }

    /**
     * 获取所有用户数据.
     * @return
     */
    @RequestMapping("/findAll")
    public Iterable<UserInfo> findAll(){
        // 数据库无数据,初始化数据
        Iterable<UserInfo> userInfos = userService.findAll();
        if (!userInfos.iterator().hasNext())
            save();
        // 内存数据库操作
        return userService.findAll();
    }

    /**
     * 根据用户id删除用户
     * @param id
     */
    @RequestMapping("/delete")
    public void delete(@RequestParam(value = "id")Long id){
        userService.delete(id);
    }

    @RequestMapping("/deleteAll")
    public void delete(){
        userService.deleteAll();
    }

    /**
     * 修改用户密码
     * @param userid
     * @param password
     */
    @RequestMapping("/updatePassword")
    public void updatePassword(@RequestParam(value = "userid")String userid,@RequestParam(value = "password")String password){
        userService.updatePassword(userid,password);
    }
}