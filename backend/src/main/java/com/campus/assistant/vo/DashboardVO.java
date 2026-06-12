package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

/**
 * ???? VO???????????????????
 */
@Data
@Builder
public class DashboardVO {

    private Long userCount;
    private Long noticeCount;
    private Long courseCount;
    private Long examCount;
}
