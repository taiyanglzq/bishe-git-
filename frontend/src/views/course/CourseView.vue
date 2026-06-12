<!-- 课程查询页面，支持按院系、学期筛选课程，查看课程详情和考试安排。 -->
<template>
  <div>
    <div class="page-header">
      <h2>课程查询</h2>
      <p>查看本学期开设课程信息与考试安排</p>
    </div>

    <!-- 课程检索区 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索课程名称或授课教师..."
        clearable
        style="width: 260px;"
        :prefix-icon="Search"
      />
      <el-select v-model="college" placeholder="院系筛选" clearable style="width: 160px;">
        <el-option label="计算机学院" value="计算机学院" />
        <el-option label="外国语学院" value="外国语学院" />
        <el-option label="数学学院" value="数学学院" />
      </el-select>
      <el-select v-model="semester" placeholder="学期筛选" clearable style="width: 160px;">
        <el-option label="2025-2026-2" value="2025-2026-2" />
        <el-option label="2025-2026-1" value="2025-2026-1" />
      </el-select>
      <el-button :icon="Refresh" @click="fetchCourses" circle />
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">
        共 {{ filteredCourses.length }} 门课程
      </span>
    </div>

    <!-- 课程卡片列表 -->
    <div v-if="pagedCourses.length" class="course-grid">
      <article
        v-for="item in pagedCourses"
        :key="item.id"
        class="course-card"
        @click="openCourseDetail(item.id)"
      >
        <div class="course-card-header">
          <h3 class="course-name">{{ item.name }}</h3>
          <span class="course-credit">{{ item.credit }} 学分</span>
        </div>
        <div class="course-card-body">
          <div class="course-info-row">
            <el-icon><User /></el-icon>
            <span>{{ item.teacherName || '待定' }}</span>
          </div>
          <div class="course-info-row">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ item.college }}</span>
          </div>
          <div class="course-info-row">
            <el-icon><Clock /></el-icon>
            <span>{{ item.scheduleInfo || '暂无' }}</span>
          </div>
          <div class="course-info-row">
            <el-icon><Location /></el-icon>
            <span>{{ item.classroom || '待定' }}</span>
          </div>
        </div>
        <div class="course-card-footer">
          <el-tag size="small" type="info">{{ item.semester }}</el-tag>
          <span class="course-capacity">容量 {{ item.capacity }} 人</span>
        </div>
      </article>
    </div>

    <el-pagination
      v-if="filteredCourses.length"
      class="notice-pagination"
      layout="prev, pager, next, total"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :total="filteredCourses.length"
      @current-change="(p) => currentPage = p"
    />

    <div v-if="!loading && !filteredCourses.length" class="empty-state">
      <div class="empty-state-icon">
        <el-icon><Document /></el-icon>
      </div>
      <p>{{ keyword || college || semester ? '未找到匹配的课程' : '暂无课程信息' }}</p>
    </div>

    <!-- 课程详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentCourse?.name || '课程详情'" width="680px" destroy-on-close>
      <div v-if="currentCourse" class="course-detail">
        <div class="course-detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">课程名称</span>
              <span class="detail-value">{{ currentCourse.name }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">授课教师</span>
              <span class="detail-value">{{ currentCourse.teacherName || '待定' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">开课院系</span>
              <span class="detail-value">{{ currentCourse.college }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">学期</span>
              <span class="detail-value">{{ currentCourse.semester }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">学分</span>
              <span class="detail-value">{{ currentCourse.credit }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">容量</span>
              <span class="detail-value">{{ currentCourse.capacity }} 人</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">上课时间</span>
              <span class="detail-value">{{ currentCourse.scheduleInfo || '暂无' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">上课地点</span>
              <span class="detail-value">{{ currentCourse.classroom || '待定' }}</span>
            </div>
          </div>
        </div>
        <div v-if="currentCourse.description" class="course-detail-section">
          <h4>课程简介</h4>
          <p class="course-description">{{ currentCourse.description }}</p>
        </div>

        <!-- 关联考试 -->
        <div class="course-detail-section">
          <h4>考试安排</h4>
          <div v-if="courseExams.length">
            <div v-for="exam in courseExams" :key="exam.id" class="exam-item">
              <div class="exam-item-header">
                <el-tag :type="exam.examType === '期末考试' ? 'danger' : 'warning'" size="small">
                  {{ exam.examType }}
                </el-tag>
                <span class="exam-date">{{ exam.examDate }}</span>
                <span>{{ exam.startTime }} - {{ exam.endTime }}</span>
              </div>
              <div class="exam-item-body">
                <span><el-icon><Location /></el-icon> {{ exam.location || '待定' }}</span>
                <span v-if="exam.seatNo">座位号：{{ exam.seatNo }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state" style="padding: 20px;">
            <p style="font-size: 13px;">暂无考试安排</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCoursePage, getExamPage } from '../../api/course'
import { Search, Refresh, User, OfficeBuilding, Clock, Location, Document } from '@element-plus/icons-vue'

const PAGE_SIZE = 12

const keyword = ref('')
const college = ref('')
const semester = ref('')
const currentPage = ref(1)
const loading = ref(false)

const courses = ref([])
const detailVisible = ref(false)
const currentCourse = ref(null)
const courseExams = ref([])

const filteredCourses = computed(() => {
  let list = courses.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(c =>
      (c.name && c.name.toLowerCase().includes(kw)) ||
      (c.teacherName && c.teacherName.toLowerCase().includes(kw))
    )
  }
  return list
})

const pagedCourses = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredCourses.value.slice(start, start + PAGE_SIZE)
})

async function fetchCourses() {
  loading.value = true
  try {
    const params = { current: 1, size: 200 }
    if (college.value) params.college = college.value
    if (semester.value) params.semester = semester.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getCoursePage(params)
    courses.value = res.records || []
  } catch {
    ElMessage.error('获取课程列表失败')
    courses.value = []
  } finally {
    loading.value = false
  }
}

async function openCourseDetail(courseId) {
  try {
    const courseRes = await getCoursePage({ current: 1, size: 200 })
    const allCourses = courseRes.records || []
    currentCourse.value = allCourses.find(c => c.id === courseId) || null

    const examRes = await getExamPage({ current: 1, size: 100 })
    const allExams = examRes.records || []
    courseExams.value = allExams.filter(e => e.courseId === courseId)

    detailVisible.value = true
  } catch {
    ElMessage.error('获取课程详情失败')
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.course-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
}
.course-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}

.course-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}
.course-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
.course-credit {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
  white-space: nowrap;
}

.course-card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}
.course-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.course-info-row .el-icon {
  font-size: 14px;
  color: var(--text-muted);
}

.course-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.course-capacity {
  font-size: 12px;
  color: var(--text-muted);
}

.course-detail-section {
  margin-bottom: 24px;
}
.course-detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.detail-label {
  font-size: 12px;
  color: var(--text-muted);
}
.detail-value {
  font-size: 14px;
  color: var(--text-primary);
}
.course-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.exam-item {
  padding: 12px;
  background: var(--bg-page);
  border-radius: 8px;
  margin-bottom: 10px;
}
.exam-item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  font-size: 14px;
}
.exam-date {
  font-weight: 600;
}
.exam-item-body {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
