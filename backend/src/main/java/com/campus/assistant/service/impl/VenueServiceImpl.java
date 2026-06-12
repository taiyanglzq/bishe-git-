package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.dto.VenueSaveDTO;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.entity.VenueSlot;
import com.campus.assistant.mapper.VenueMapper;
import com.campus.assistant.mapper.VenueSlotMapper;
import com.campus.assistant.service.CacheEvictService;
import com.campus.assistant.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 场地服务实现，负责场地分页展示与场地管理业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private static final long VENUE_PAGE_TTL_MINUTES = 15L;

    private final VenueMapper venueMapper;
    private final VenueSlotMapper venueSlotMapper;
    private final CacheClient cacheClient;
    private final CacheEvictService cacheEvictService;

    @Override
    public Page<Venue> page(Long current, Long size) {
        String cacheKey = CacheKeyConstants.VENUE_PAGE + current + ":" + size + ":all";
        Page<Venue> cached = cacheClient.get(cacheKey, Page.class);
        if (cached != null) {
            return cached;
        }

        Page<Venue> result = venueMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<Venue>()
                .eq(Venue::getDeleted, 0)
                .orderByDesc(Venue::getCreateTime));
        cacheClient.set(cacheKey, result, VENUE_PAGE_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public Long save(VenueSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        Venue venue = new Venue();
        venue.setName(dto.getName());
        venue.setLocation(dto.getLocation());
        venue.setImageUrl(dto.getImageUrl());
        venue.setCapacity(dto.getCapacity());
        venue.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        venue.setDeleted(0);
        venue.setCreateTime(LocalDateTime.now());
        venue.setUpdateTime(LocalDateTime.now());
        venueMapper.insert(venue);
        evictVenueCaches();
        return venue.getId();
    }

    @Override
    public void update(VenueSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        Venue venue = venueMapper.selectById(dto.getId());
        if (venue == null) {
            throw new BusinessException(404, "场地不存在");
        }
        Long oversizedSlots = venueSlotMapper.selectCount(new LambdaQueryWrapper<VenueSlot>()
                .eq(VenueSlot::getVenueId, venue.getId())
                .eq(VenueSlot::getDeleted, 0)
                .gt(VenueSlot::getTotalQuota, dto.getCapacity()));
        if (oversizedSlots > 0) {
            throw new BusinessException(409, "已有开放时间段名额超过新容量，请先调整时间段库存");
        }
        venue.setName(dto.getName());
        venue.setLocation(dto.getLocation());
        venue.setImageUrl(dto.getImageUrl());
        venue.setCapacity(dto.getCapacity());
        venue.setStatus(dto.getStatus() == null ? venue.getStatus() : dto.getStatus());
        venue.setUpdateTime(LocalDateTime.now());
        venueMapper.updateById(venue);
        evictVenueCaches();
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("ADMIN");
        Venue venue = venueMapper.selectById(id);
        if (venue != null) {
            venue.setDeleted(1);
            venue.setUpdateTime(LocalDateTime.now());
            venueMapper.updateById(venue);
            evictVenueCaches();
        }
    }

    private void evictVenueCaches() {
        cacheEvictService.evictVenueCaches();
        cacheEvictService.evictRecommendationCaches();
        cacheEvictService.evictDashboardCaches();
    }
}
