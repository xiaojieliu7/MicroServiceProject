package com.xiaojie.repository;

import com.xiaojie.model.UserInfo;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by hadoop on 17-6-25.
 */

public interface UserRepository2 extends CrudRepository<UserInfo,Long> {
    UserInfo findByUserid(String userid);
}
