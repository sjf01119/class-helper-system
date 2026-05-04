package com.example.classhelper.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.classhelper.entity.OperationLog;
import com.example.classhelper.mapper.OperationLogMapper;
import com.example.classhelper.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

}
