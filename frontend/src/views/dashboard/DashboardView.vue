<template>
  <div>
    <section class="workbench-hero">
      <div>
        <p class="eyebrow">{{ workbench.roleName || '校园助手' }}</p>
        <h2>{{ workbench.welcomeText || '正在加载你的校园工作台' }}</h2>
      </div>
      <div class="workbench-actions">
        <el-button type="primary" @click="$router.push('/booking')">处理预约</el-button>
        <el-button @click="$router.push('/notification')">查看通知</el-button>
      </div>
    </section>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <div class="workbench-grid">
      <section class="workbench-card">
        <div class="section-title">
          <div>
            <p class="eyebrow">TODO</p>
            <h3>待办事项</h3>
          </div>
          <el-tag>{{ workbench.pendingBookingCount || 0 }} 条待审核预约</el-tag>
        </div>
        <div v-if="workbench.todos?.length" class="workbench-list">
          <article v-for="item in workbench.todos" :key="`${item.type}-${item.bizId}`" class="workbench-item">
            <el-tag size="small">{{ item.type }}</el-tag>
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.description }}</p>
              <small>{{ item.timeText }}</small>
            </div>
          </article>
        </div>
        <el-empty v-else description="当前没有待处理事项" />
      </section>

      <section class="workbench-card">
        <div class="section-title">
          <div>
            <p class="eyebrow">SCHEDULE</p>
            <h3>近期日程</h3>
          </div>
          <el-tag type="success">{{ workbench.upcomingActivityCount || 0 }} 个近期活动</el-tag>
        </div>
        <div v-if="workbench.schedules?.length" class="workbench-list">
          <article v-for="item in workbench.schedules" :key="`${item.type}-${item.bizId}`" class="workbench-item">
            <el-tag size="small" type="success">{{ item.type }}</el-tag>
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.description }}</p>
              <small>{{ item.timeText }}</small>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无近期活动安排" />
      </section>
    </div>

    <div class="dashboard-grid">
      <section class="chart-card">
        <h3>预约状态分布</h3>
        <div ref="bookingChartRef" class="chart-box"></div>
      </section>

      <section class="chart-card">
        <h3>活动报名排行</h3>
        <div ref="activityChartRef" class="chart-box"></div>
      </section>

      <section class="chart-card">
        <h3>热门场地预约量</h3>
        <div ref="venueChartRef" class="chart-box"></div>
      </section>

      <section class="chart-card checkin-card">
        <h3>签到完成率</h3>
        <strong>{{ stats.checkinRate || 0 }}%</strong>
        <p>报名 {{ stats.enrollCount || 0 }} 人次，签到 {{ stats.checkinCount || 0 }} 人次</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, getDashboardWorkbench } from '../../api/dashboard'

const stats = ref({})
const workbench = ref({})
const bookingChartRef = ref(null)
const activityChartRef = ref(null)
const venueChartRef = ref(null)
const chartInstances = []

const metrics = computed(() => {
  const summary = stats.value.summary || {}
  return [
    { label: '用户数', value: summary.userCount || 0 },
    { label: '今日预约', value: workbench.value.todayBookingCount || 0 },
    { label: '未读通知', value: workbench.value.unreadNotificationCount || 0 },
    { label: '待审预约', value: workbench.value.pendingBookingCount || 0 },
    { label: '活动数', value: summary.activityCount || 0 },
    { label: '签到数', value: summary.checkinCount || 0 }
  ]
})

function renderCharts() {
  disposeCharts()
  renderBookingChart()
  renderBarChart(activityChartRef.value, stats.value.activityEnrollRank || [], '报名人数')
  renderBarChart(venueChartRef.value, stats.value.venueBookingRank || [], '预约次数')
}

function renderBookingChart() {
  const chart = echarts.init(bookingChartRef.value)
  chartInstances.push(chart)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['42%', '70%'],
        data: stats.value.bookingStatus || [],
        label: { formatter: '{b}: {c}' }
      }
    ]
  })
}

function renderBarChart(dom, data, seriesName) {
  const chart = echarts.init(dom)
  chartInstances.push(chart)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 16, top: 24, bottom: 56 },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.name),
      axisLabel: { interval: 0, rotate: 25 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: seriesName,
        type: 'bar',
        data: data.map((item) => item.value),
        itemStyle: { color: '#2f6b4f', borderRadius: [8, 8, 0, 0] }
      }
    ]
  })
}

function resizeCharts() {
  chartInstances.forEach((chart) => chart.resize())
}

function disposeCharts() {
  while (chartInstances.length) {
    chartInstances.pop().dispose()
  }
}

onMounted(async () => {
  const [statsResult, workbenchResult] = await Promise.all([getDashboardStats(), getDashboardWorkbench()])
  stats.value = statsResult
  workbench.value = workbenchResult
  await nextTick()
  renderCharts()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>
