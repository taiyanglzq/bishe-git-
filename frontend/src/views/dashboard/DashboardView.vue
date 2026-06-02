<template>
  <div>
    <!-- Hero -->
    <div class="dashboard-hero">
      <div class="dashboard-hero-badge">{{ workbench.roleName || '校园助手' }}</div>
      <h2>{{ greeting }}</h2>
      <p>{{ workbench.welcomeText || '欢迎使用智慧校园助手，查看校园动态与个人事务。' }}</p>
      <div class="dashboard-hero-actions">
        <el-button type="primary" @click="$router.push('/booking')">
          <el-icon><Calendar /></el-icon> 处理预约
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
          <el-tag size="small" type="warning">{{ workbench.pendingBookingCount || 0 }} 条待审核</el-tag>
        </div>
        <div class="panel-card-body">
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
          <el-tag size="small" type="success">{{ workbench.upcomingActivityCount || 0 }} 个活动</el-tag>
        </div>
        <div class="panel-card-body">
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
          <el-empty v-else description="暂无近期活动" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px;">
      <div class="panel-card">
        <div class="panel-card-header"><h3>预约状态分布</h3></div>
        <div class="panel-card-body">
          <div ref="bookingChartRef" style="height: 260px;"></div>
        </div>
      </div>

      <div class="panel-card">
        <div class="panel-card-header"><h3>活动报名排行</h3></div>
        <div class="panel-card-body">
          <div ref="activityChartRef" style="height: 260px;"></div>
        </div>
      </div>

      <div class="panel-card">
        <div class="panel-card-header"><h3>热门场地预约量</h3></div>
        <div class="panel-card-body">
          <div ref="venueChartRef" style="height: 260px;"></div>
        </div>
      </div>

      <div class="panel-card">
        <div class="panel-card-header"><h3>签到完成率</h3></div>
        <div class="panel-card-body" style="text-align: center; padding-top: 32px; padding-bottom: 32px;">
          <div ref="checkinChartRef" style="height: 200px;"></div>
          <p style="color: var(--text-muted); margin-top: 8px; font-size: 13px;">
            报名 {{ stats.enrollCount || 0 }} 人次，签到 {{ stats.checkinCount || 0 }} 人次
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Calendar, Bell, UserFilled, Tickets, ChatDotRound, DataLine, Medal, Notebook } from '@element-plus/icons-vue'
import { getDashboardStats, getDashboardWorkbench } from '../../api/dashboard'

const stats = ref({})
const workbench = ref({})
const bookingChartRef = ref(null)
const activityChartRef = ref(null)
const venueChartRef = ref(null)
const checkinChartRef = ref(null)
const chartInstances = []

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
    { label: '今日预约', value: workbench.value.todayBookingCount || 0, sub: '今日提交的预约', icon: Tickets, color: 'blue' },
    { label: '未读通知', value: workbench.value.unreadNotificationCount || 0, sub: '等待查看', icon: ChatDotRound, color: 'amber' },
    { label: '待审预约', value: workbench.value.pendingBookingCount || 0, sub: '需要审核处理', icon: Notebook, color: 'red' },
    { label: '活动总数', value: s.activityCount || 0, sub: '进行中的校园活动', icon: Medal, color: 'green' },
    { label: '签到人次', value: s.checkinCount || 0, sub: '累计签到记录', icon: DataLine, color: 'blue' }
  ]
})

const chartColors = ['#059669', '#10b981', '#34d399', '#6ee7b7', '#a7f3d0', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6']

function renderCharts() {
  disposeCharts()
  renderBookingPie()
  renderBarChart(activityChartRef.value, stats.value.activityEnrollRank || [], '报名人数', chartColors[0])
  renderBarChart(venueChartRef.value, stats.value.venueBookingRank || [], '预约次数', chartColors[5])
  renderCheckinGauge()
}

function renderBookingPie() {
  const chart = echarts.init(bookingChartRef.value)
  chartInstances.push(chart)
  const data = stats.value.bookingStatus || []
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data,
      color: chartColors
    }]
  })
}

function renderBarChart(dom, data, name, color) {
  if (!dom) return
  const chart = echarts.init(dom)
  chartInstances.push(chart)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 30, right: 16, top: 16, bottom: 48 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: { interval: 0, rotate: 25, fontSize: 11, color: '#94a3b8' },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { fontSize: 11, color: '#94a3b8' }
    },
    series: [{
      name,
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: {
        color,
        borderRadius: [6, 6, 0, 0]
      },
      barWidth: '50%'
    }]
  })
}

function renderCheckinGauge() {
  const chart = echarts.init(checkinChartRef.value)
  chartInstances.push(chart)
  const rate = stats.value.checkinRate || 0
  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 210,
      endAngle: -30,
      center: ['50%', '55%'],
      radius: '90%',
      min: 0,
      max: 100,
      splitNumber: 10,
      axisLine: {
        show: true,
        lineStyle: {
          width: 18,
          color: [
            [rate / 100, '#059669'],
            [1, '#f1f5f9']
          ]
        }
      },
      pointer: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: {
        valueAnimation: true,
        formatter: '{value}%',
        fontSize: 36,
        fontWeight: 700,
        color: '#0f172a',
        offsetCenter: [0, '10%']
      },
      data: [{ value: rate }]
    }]
  })
}

function resizeCharts() {
  chartInstances.forEach(c => c.resize())
}

function disposeCharts() {
  while (chartInstances.length) {
    chartInstances.pop().dispose()
  }
}

onMounted(async () => {
  const [s, w] = await Promise.all([getDashboardStats(), getDashboardWorkbench()])
  stats.value = s
  workbench.value = w
  await nextTick()
  renderCharts()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>
