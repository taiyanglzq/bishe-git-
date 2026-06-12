<!-- ????? ????????????????????????? -->
<template>
  <div>
    <div class="page-header">
      <h2>个性化推荐</h2>
      <p>根据您的兴趣和校园行为，智能推荐公告、课程和图书资源</p>
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

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { View } from '@element-plus/icons-vue'
import { getPersonalRecommendation } from '../../api/recommendation'

const data = ref({})

onMounted(async () => {
  data.value = await getPersonalRecommendation()
})
</script>
