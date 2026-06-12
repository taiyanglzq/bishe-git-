<!-- 首页工作台，根据角色展示个性化欢迎和数据概览。 -->
<template>
  <div>
    <!-- Hero -->
    <div class="dashboard-hero">
      <div class="dashboard-hero-badge">{{ workbench.roleName || '校园助手' }}</div>
      <h2>{{ greeting }}</h2>
      <p>{{ workbench.welcomeText || '欢迎使用智慧校园助手，查看校园动态与个人事务。' }}</p>
      <div class="dashboard-hero-actions">
        <el-button type="primary" @click="$router.push('/course')">
          <el-icon><Reading /></el-icon> 查看课程
        </el-button>
        <el-button bg text-color="#fff" style="border-color: rgba(255,255,255,.3);" @click="$router.push('/notification')">
          <el-icon><Bell /></el-icon> 查看通知
        </el-button>
      </div>
    </div>

    <!-- 数据概览 -->
    <div class="stat-grid">
      <div v-for="item in metrics" :key="item.label" class="stat-card">
        <div class="stat-card-icon" :class="item.color">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-card-body">
          <div class="stat-card-label">{{ item.label }}</div>
          <div class="stat-card-value">{{ item.value }}</div>
          <div class="stat-card-sub">{{ item.sub }}</div>
        </div>
      </div>
    </div>

    <!-- 待办 & 日程 -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">
      <div class="panel-card">
        <div class="panel-card-header">
          <h3>待办事项</h3>
          <el-tag size="small" type="warning">{{ workbench.unreadNotificationCount || 0 }} 条未读</el-tag>
        </div>
        <div class="panel-card-body" style="max-height: 320px; overflow-y: auto;">
          <template v-if="workbench.todos?.length">
            <div v-for="item in workbench.todos" :key="`${item.type}-${item.bizId}`" class="notification-item" style="margin-bottom: 8px;">
              <div class="notification-item-dot"></div>
              <div class="notification-item-content">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
                <small>{{ item.timeText }}</small>
              </div>
              <el-tag size="small">{{ item.type }}</el-tag>
            </div>
          </template>
          <el-empty v-else description="暂无待处理事项" :image-size="60" />
        </div>
      </div>

      <div class="panel-card">
        <div class="panel-card-header">
          <h3>近期日程</h3>
          <el-tag size="small" type="success">考试提醒</el-tag>
        </div>
        <div class="panel-card-body" style="max-height: 320px; overflow-y: auto;">
          <template v-if="workbench.schedules?.length">
            <div v-for="item in workbench.schedules" :key="`${item.type}-${item.bizId}`" class="notification-item" style="margin-bottom: 8px;">
              <div class="notification-item-dot" style="background: var(--success);"></div>
              <div class="notification-item-content">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
                <small>{{ item.timeText }}</small>
              </div>
              <el-tag size="small" type="success">{{ item.type }}</el-tag>
            </div>
          </template>
          <el-empty v-else description="暂无近期考试" :image-size="60" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Bell, UserFilled, ChatDotRound, DataLine, Reading } from '@element-plus/icons-vue'
import { getDashboardStats, getDashboardWorkbench } from '../../api/dashboard'

const route = useRoute()
const stats = ref({})
const workbench = ref({})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，注意休息'
  if (h < 9) return '早上好，新的一天开始了'
  if (h < 12) return '上午好，精力充沛地开始工作吧'
  if (h < 14) return '中午好，别忘了休息一下'
  if (h < 18) return '下午好，保持专注'
  return '晚上好，回顾一下今天的收获吧'
})

const metrics = computed(() => {
  const s = stats.value.summary || {}
  return [
    { label: '用户总数', value: s.userCount || 0, sub: '系统注册用户', icon: UserFilled, color: 'green' },
    { label: '公告数量', value: s.noticeCount || 0, sub: '校园公告总数', icon: ChatDotRound, color: 'blue' },
    { label: '课程数量', value: s.courseCount || 0, sub: '开设课程总数', icon: Reading, color: 'amber' },
    { label: '考试场次', value: s.examCount || 0, sub: '考试安排总场次', icon: DataLine, color: 'green' },
    { label: '未读通知', value: workbench.value.unreadNotificationCount || 0, sub: '等待查看', icon: Bell, color: 'red' }
  ]
})

async function loadData() {
  const [s, w] = await Promise.all([getDashboardStats(), getDashboardWorkbench()])
  stats.value = s
  workbench.value = w
}

onMounted(loadData)

// 每次回到首页都重新加载数据
watch(() => route.path, (path) => {
  if (path === '/dashboard') loadData()
})
</script>
