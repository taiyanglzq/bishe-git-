package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.entity.OperationLog;

/**
 * 日志服务接口，定义日志分页和日志详情查询相关业务能力。
 */
public interface LogService {

    Page<OperationLog> page(Long current, Long size);

    OperationLog detail(Long id);
}
