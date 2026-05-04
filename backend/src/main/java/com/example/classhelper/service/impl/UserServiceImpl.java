package com.example.classhelper.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.classhelper.entity.User;
import com.example.classhelper.mapper.UserMapper;
import com.example.classhelper.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
