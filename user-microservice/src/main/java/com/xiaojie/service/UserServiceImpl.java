package com.xiaojie.service;

import com.xiaojie.model.UserInfo;
import com.xiaojie.repository.UserRepository2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Created by hadoop on 17-6-25.
 */

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository2 userRepository;

    RowMapper<UserInfo> rm = BeanPropertyRowMapper.newInstance(UserInfo.class);
    @Override
    public UserInfo findUser(String userid) {
//        return jdbcTemplate.queryForObject("select * from user where userid=?",new Object[]{userid},rm);
        return userRepository.findByUserid(userid);
    }

    @Override
    public void updatePassword(String userid,String password) {
        jdbcTemplate.update("UPDATE USER SET password=? WHERE userid=?",password,userid);
    }

    @Override
    public void save(UserInfo userInfo) {
        userRepository.save(userInfo);
    }

    @Override
    public Iterable<UserInfo> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }

    @Override
    public void deleteAll() {
         userRepository.deleteAll();
    }
}
