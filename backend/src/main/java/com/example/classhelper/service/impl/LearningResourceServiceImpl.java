package com.example.classhelper.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.classhelper.entity.LearningResource;
import com.example.classhelper.mapper.LearningResourceMapper;
import com.example.classhelper.service.LearningResourceService;
import org.springframework.stereotype.Service;

@Service
public class LearningResourceServiceImpl extends ServiceImpl<LearningResourceMapper, LearningResource> implements LearningResourceService {

}
