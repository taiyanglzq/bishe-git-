package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.AuditDTO;
import com.campus.assistant.dto.BookingCreateDTO;
import com.campus.assistant.entity.Booking;

public interface BookingService {

    Long create(BookingCreateDTO dto);

    void approve(AuditDTO dto);

    void reject(AuditDTO dto);

    void cancel(Long bookingId);

    Page<Booking> page(Long current, Long size, String status);
}
