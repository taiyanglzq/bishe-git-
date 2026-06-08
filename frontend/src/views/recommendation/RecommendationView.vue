<!-- ????? ????????????????????????? -->
<template>
  <div>
    <div class="page-header">
      <h2>个性化推荐</h2>
      <p>根据您的兴趣和校园行为，智能推荐公告、活动和场地</p>
    </div>

    <el-alert
      v-if="data.reason?.length"
      title="推荐依据"
      type="success"
      :closable="false"
      show-icon
      style="margin-bottom: 24px;"
    >
      <template #default>
        <div style="display: flex; gap: 6px; flex-wrap: wrap;">
          <el-tag v-for="(r, i) in data.reason" :key="i" size="small" effect="plain" type="success">{{ r }}</el-tag>
        </div>
      </template>
    </el-alert>

    <!-- 推荐公告 -->
    <div class="recommend-section">
      <div class="recommend-section-header">
        <h3>推荐公告</h3>
        <el-tag size="small" effect="plain">{{ (data.notices || []).length }} 条</el-tag>
      </div>
      <div v-if="data.notices?.length" class="recommend-cards">
        <div v-for="item in data.notices" :key="item.id" class="info-card">
          <div class="info-card-header">
            <span class="info-card-title">{{ item.title }}</span>
            <el-tag size="small">{{ item.category }}</el-tag>
          </div>
          <div class="info-card-meta">
            <span class="info-card-meta-item">
              <el-icon><View /></el-icon> {{ item.viewCount || 0 }} 浏览
            </span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无推荐公告" :image-size="48" />
    </div>

    <!-- 推荐活动 -->
    <div class="recommend-section">
      <div class="recommend-section-header">
        <h3>推荐活动</h3>
        <el-tag size="small" effect="plain" type="warning">{{ (data.activities || []).length }} 个</el-tag>
      </div>
      <div v-if="data.activities?.length" class="recommend-cards">
        <div v-for="item in data.activities" :key="item.id" class="info-card">
          <div class="info-card-header">
            <span class="info-card-title">{{ item.title }}</span>
            <el-tag size="small" type="warning">{{ item.enrolledCount || 0 }}/{{ item.capacity }}</el-tag>
          </div>
          <div class="info-card-desc" v-if="item.location">
            <el-icon><LocationFilled /></el-icon> {{ item.location }}
          </div>
          <div class="info-card-meta">
            <span class="info-card-meta-item">
              <el-icon><UserFilled /></el-icon> 已报名 {{ item.enrolledCount || 0 }} 人
            </span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无推荐活动" :image-size="48" />
    </div>

    <!-- 推荐场地 -->
    <div class="recommend-section">
      <div class="recommend-section-header">
        <h3>推荐场地</h3>
        <el-tag size="small" effect="plain" type="primary">{{ (data.venues || []).length }} 个</el-tag>
      </div>
      <div v-if="data.venues?.length" class="recommend-cards">
        <div v-for="item in data.venues" :key="item.id" class="info-card">
          <div class="info-card-header">
            <span class="info-card-title">{{ item.name }}</span>
            <el-tag size="small" type="primary">{{ item.capacity }} 人</el-tag>
          </div>
          <div class="info-card-desc" v-if="item.location">
            <el-icon><LocationFilled /></el-icon> {{ item.location }}
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无推荐场地" :image-size="48" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { View, LocationFilled, UserFilled } from '@element-plus/icons-vue'
import { getPersonalRecommendation } from '../../api/recommendation'

const data = ref({})

onMounted(async () => {
  data.value = await getPersonalRecommendation()
})
</script>
