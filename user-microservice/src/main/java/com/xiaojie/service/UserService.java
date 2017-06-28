package com.xiaojie.service;

import com.xiaojie.model.UserInfo;


/**
 * Created by hadoop on 17-6-21.
 */

public interface UserService {
    UserInfo findUser(String userid);

    void updatePassword(String userid,String password);

    void save(UserInfo userInfo);

    Iterable<UserInfo> findAll();

    void delete(Long id);

    void deleteAll();
}