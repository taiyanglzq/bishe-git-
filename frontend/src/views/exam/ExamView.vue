<!-- 考试安排页面，展示即将到来的考试列表，支持按院系和类型筛选。 -->
<template>
  <div>
    <div class="page-header">
      <h2>考试安排</h2>
      <p>查看本学期考试时间、地点和座位安排</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索考试科目..."
        clearable
        style="width: 260px;"
        :prefix-icon="Search"
      />
      <el-select v-model="college" placeholder="院系筛选" clearable style="width: 160px;">
        <el-option label="计算机学院" value="计算机学院" />
        <el-option label="外国语学院" value="外国语学院" />
        <el-option label="数学学院" value="数学学院" />
      </el-select>
      <el-select v-model="examType" placeholder="考试类型" clearable style="width: 140px;">
        <el-option label="期末考试" value="期末考试" />
        <el-option label="期中考试" value="期中考试" />
        <el-option label="补考" value="补考" />
      </el-select>
      <el-button :icon="Refresh" @click="fetchExams" circle />
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">
        共 {{ filteredExams.length }} 场考试
      </span>
    </div>

    <!-- 即将开始提醒 -->
    <el-alert
      v-if="upcomingExams.length"
      :title="'未来 3 天有 ' + upcomingExams.length + ' 场考试，请做好准备'"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 16px;"
    />

    <div v-if="pagedExams.length" class="exam-list">
      <div
        v-for="item in pagedExams"
        :key="item.id"
        class="exam-card"
        :class="{ 'exam-upcoming': isUpcoming(item) }"
      >
        <div class="exam-card-left">
          <div class="exam-date-badge">
            <span class="exam-date-day">{{ formatDay(item.examDate) }}</span>
            <span class="exam-date-month">{{ formatMonth(item.examDate) }}</span>
          </div>
        </div>
        <div class="exam-card-body">
          <div class="exam-card-header">
            <h3>{{ item.courseName }}</h3>
            <el-tag
              :type="item.examType === '期末考试' ? 'danger' : item.examType === '期中考试' ? 'warning' : 'info'"
              size="small"
            >
              {{ item.examType }}
            </el-tag>
          </div>
          <div class="exam-info-grid">
            <div class="exam-info-item">
              <el-icon><Clock /></el-icon>
              <span>{{ item.startTime }} - {{ item.endTime }}</span>
            </div>
            <div class="exam-info-item">
              <el-icon><Location /></el-icon>
              <span>{{ item.location || '待定' }}</span>
            </div>
            <div class="exam-info-item">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ item.college }}</span>
            </div>
            <div v-if="item.invigilator" class="exam-info-item">
              <el-icon><UserFilled /></el-icon>
              <span>监考：{{ item.invigilator }}</span>
            </div>
            <div v-if="seatsMap[item.id] || item.seatNo" class="exam-info-item">
              <el-icon><Tickets /></el-icon>
              <span>座位号：{{ seatsMap[item.id] || item.seatNo }}</span>
            </div>
          </div>
        </div>
        <div class="exam-card-right">
          <el-tag :type="isUpcoming(item) ? 'warning' : 'info'" effect="dark" size="small">
            {{ countdownText(item) }}
          </el-tag>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="filteredExams.length"
      class="notice-pagination"
      layout="prev, pager, next, total"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :total="filteredExams.length"
      @current-change="(p) => currentPage = p"
    />

    <div v-if="!loading && !filteredExams.length" class="empty-state">
      <div class="empty-state-icon">
        <el-icon><Document /></el-icon>
      </div>
      <p>暂无考试安排</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getExamPage, getMyExamSeats } from '../../api/course'
import { Search, Refresh, Clock, Location, OfficeBuilding, Tickets, UserFilled, Document } from '@element-plus/icons-vue'

const PAGE_SIZE = 10

const keyword = ref('')
const college = ref('')
const examType = ref('')
const currentPage = ref(1)
const loading = ref(false)
const exams = ref([])
const seatsMap = ref({})

const filteredExams = computed(() => {
  let list = exams.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(e => e.courseName && e.courseName.toLowerCase().includes(kw))
  }
  return list
})

const pagedExams = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredExams.value.slice(start, start + PAGE_SIZE)
})

const upcomingExams = computed(() => {
  const now = new Date()
  const threeDaysLater = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000)
  return exams.value.filter(e => {
    if (!e.examDate) return false
    const examDate = new Date(e.examDate)
    return examDate >= now && examDate <= threeDaysLater
  })
})

function isUpcoming(exam) {
  if (!exam.examDate) return false
  const now = new Date()
  const examDate = new Date(exam.examDate)
  const diff = examDate.getTime() - now.getTime()
  return diff > 0 && diff < 3 * 24 * 60 * 60 * 1000
}

function countdownText(exam) {
  if (!exam.examDate) return ''
  const now = new Date()
  const examDate = new Date(exam.examDate)
  const diffDays = Math.ceil((examDate.getTime() - now.getTime()) / (24 * 60 * 60 * 1000))
  if (diffDays < 0) return '已结束'
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '明天'
  return diffDays + ' 天后'
}

function formatDay(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.getDate()
}

function formatMonth(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return (d.getMonth() + 1) + '月'
}

async function fetchExams() {
  loading.value = true
  try {
    const params = { current: 1, size: 200 }
    if (college.value) params.college = college.value
    if (examType.value) params.examType = examType.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getExamPage(params)
    exams.value = res.records || []
    // 获取当前用户的座位信息
    try {
      const mySeats = await getMyExamSeats()
      const map = {}
      if (Array.isArray(mySeats)) {
        mySeats.forEach(s => { map[s.examId] = s.seatNo })
      }
      seatsMap.value = map
    } catch { /* ignore */ }
  } catch {
    ElMessage.error('获取考试安排失败')
    exams.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchExams()
})
</script>

<style scoped>
.exam-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.exam-card {
  display: flex;
  align-items: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px 20px;
  gap: 20px;
  transition: all 0.2s;
}
.exam-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.exam-card.exam-upcoming {
  border-left: 4px solid var(--el-color-warning);
}

.exam-date-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: var(--bg-page);
  border-radius: 10px;
}
.exam-date-day {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}
.exam-date-month {
  font-size: 12px;
  color: var(--text-muted);
}

.exam-card-body {
  flex: 1;
}
.exam-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.exam-card-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.exam-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}
.exam-info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.exam-info-item .el-icon {
  font-size: 14px;
  color: var(--text-muted);
}

.exam-card-right {
  flex-shrink: 0;
}
</style>
