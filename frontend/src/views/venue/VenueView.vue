<!-- ?? ?????????????????????? -->
<template>
  <div>
    <div class="page-header">
      <h2>场地资源</h2>
      <p>浏览校园可用场地，了解容量与设施信息</p>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索场地名称..." clearable style="width: 240px;" :prefix-icon="Search" />
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">共 {{ filteredRows.length }} 个场地</span>
    </div>

    <div v-if="filteredRows.length" class="venue-grid">
      <div v-for="venue in filteredRows" :key="venue.id" class="venue-card">
        <div class="venue-card-header">
          <div>
            <div class="venue-card-name">{{ venue.name }}</div>
            <div class="venue-card-location">
              <el-icon><LocationFilled /></el-icon> {{ venue.location || '未设置位置' }}
            </div>
          </div>
          <el-tag :type="venue.status === 1 ? 'success' : 'info'" size="small" effect="light">
            {{ venue.status === 1 ? '可用' : '停用' }}
          </el-tag>
        </div>
        <div class="venue-card-stats">
          <div class="venue-card-stat">
            <strong>{{ venue.capacity || 0 }}</strong>
            <span>容量（人）</span>
          </div>
          <div class="venue-card-stat">
            <strong>
              <el-icon style="font-size: 14px; vertical-align: -2px;"><Star /></el-icon>
            </strong>
            <span>设施齐全</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-state-icon"><el-icon><OfficeBuilding /></el-icon></div>
      <p>暂无场地信息</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Search, LocationFilled, Star, OfficeBuilding } from '@element-plus/icons-vue'
import { getVenuePage } from '../../api/venue'

const rows = ref([])
const keyword = ref('')

const filteredRows = computed(() => {
  if (!keyword.value) return rows.value
  const kw = keyword.value.toLowerCase()
  return rows.value.filter(r => r.name && r.name.toLowerCase().includes(kw))
})

onMounted(async () => {
  const data = await getVenuePage({ current: 1, size: 50 })
  rows.value = data.records || []
})
</script>
