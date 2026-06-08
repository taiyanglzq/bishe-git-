package com.campus.assistant.service;

import com.campus.assistant.vo.DashboardVO;
import com.campus.assistant.vo.DashboardStatsVO;
import com.campus.assistant.vo.DashboardWorkbenchVO;/**
 * ???? ???????????????????????
 */
public interface DashboardService {

    DashboardVO summary();

    DashboardStatsVO stats();

    DashboardWorkbenchVO workbench();
}
