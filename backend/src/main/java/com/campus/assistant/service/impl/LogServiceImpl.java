package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.entity.OperationLog;
import com.campus.assistant.mapper.OperationLogMapper;
import com.campus.assistant.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 日志服务实现，负责日志分页和详情查询逻辑。
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public Page<OperationLog> page(Long current, Long size) {
        return operationLogMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime));
    }

    @Override
    public OperationLog detail(Long id) {
        return operationLogMapper.selectById(id);
    }
}
