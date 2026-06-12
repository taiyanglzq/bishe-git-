package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.ActivityEnrollDTO;
import com.campus.assistant.dto.ActivitySaveDTO;
import com.campus.assistant.dto.CheckinDTO;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.vo.ActivityRecordVO;
import com.campus.assistant.vo.ActivityVO;/**
 * ?? ?????????????????????
 */
public interface ActivityBizService {

    Page<ActivityVO> page(Long current, Long size);

    Page<Activity> managePage(Long current, Long size);

    Long save(ActivitySaveDTO dto);

    void update(ActivitySaveDTO dto);

    void delete(Long id);

    void enroll(ActivityEnrollDTO dto);

    void cancelEnroll(Long activityId);

    void checkin(CheckinDTO dto);

    Page<ActivityRecordVO> myEnrollments(Long current, Long size);

    Page<ActivityRecordVO> myCheckins(Long current, Long size);
}
