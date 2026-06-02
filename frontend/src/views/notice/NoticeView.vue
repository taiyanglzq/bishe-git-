<template>
  <div>
    <div class="page-header">
      <h2>校园公告</h2>
      <p>查看学校发布的最新通知与公告信息</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索公告标题..."
        clearable
        style="width: 260px;"
        :prefix-icon="Search"
      />
      <el-select v-model="category" placeholder="分类筛选" clearable style="width: 140px;">
        <el-option label="系统通知" value="系统通知" />
        <el-option label="学术活动" value="学术活动" />
        <el-option label="校园新闻" value="校园新闻" />
        <el-option label="考试信息" value="考试信息" />
      </el-select>
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">
        共 {{ filteredRows.length }} 条公告
      </span>
    </div>

    <div v-if="filteredRows.length" class="info-grid">
      <article v-for="item in filteredRows" :key="item.id" class="info-card">
        <div class="info-card-header">
          <span class="info-card-title">{{ item.title }}</span>
          <el-tag size="small" :type="categoryTagType(item.category)">
            {{ item.category || '公告' }}
          </el-tag>
        </div>
        <p v-if="item.content" class="info-card-desc">{{ item.content }}</p>
        <div class="info-card-meta">
          <span class="info-card-meta-item">
            <el-icon><View /></el-icon> {{ item.viewCount || 0 }} 浏览
          </span>
          <span class="info-card-meta-item">
            <el-icon><Clock /></el-icon> {{ formatDateTime(item.createTime) }}
          </span>
        </div>
      </article>
    </div>

    <div v-else class="empty-state">
      <div class="empty-state-icon">
        <el-icon><Document /></el-icon>
      </div>
      <p>暂无公告信息</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Search, View, Clock, Document } from '@element-plus/icons-vue'
import { getNoticePage } from '../../api/notice'

const rows = ref([])
const keyword = ref('')
const category = ref('')

const filteredRows = computed(() => {
  let data = rows.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    data = data.filter((row) => row.title && row.title.toLowerCase().includes(kw))
  }
  if (category.value) {
    data = data.filter((row) => row.category === category.value)
  }
  return data
})

function categoryTagType(cat) {
  if (cat === '系统通知') return 'danger'
  if (cat === '学术活动') return 'success'
  if (cat === '考试信息') return 'warning'
  if (cat === '校园新闻') return ''
  return 'info'
}

function formatDateTime(value) {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return text
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}年${pad(date.getMonth() + 1)}月${pad(date.getDate())}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(async () => {
  const data = await getNoticePage({ current: 1, size: 50 })
  rows.value = data.records || []
})
</script>
