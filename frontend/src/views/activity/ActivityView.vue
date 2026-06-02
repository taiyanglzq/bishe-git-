<template>
  <div>
    <div class="page-header">
      <h2>活动与签到</h2>
      <p>浏览校园活动，报名参加并完成签到</p>
    </div>

    <el-tabs v-model="activeTab" @tab-change="load">
      <el-tab-pane label="活动中心" name="activities">
        <div v-if="rows.length" class="activity-grid">
          <article v-for="item in rows" :key="item.id" class="activity-card">
            <div class="activity-card-top">
              <div>
                <h3>{{ item.title }}</h3>
                <div class="activity-card-location">
                  <el-icon><LocationFilled /></el-icon> {{ item.location || '未设置地点' }}
                </div>
              </div>
              <el-tag
                :type="item.checkedIn ? 'success' : item.enrolled ? 'warning' : 'info'"
                effect="light"
              >
                {{ item.checkedIn ? '已签到' : item.enrolled ? '已报名' : '未报名' }}
              </el-tag>
            </div>

            <div class="activity-card-desc">{{ item.content || '暂无活动说明' }}</div>

            <div class="activity-card-info">
              <div class="activity-card-info-item">
                活动时间<strong>{{ formatRange(item.startTime, item.endTime) }}</strong>
              </div>
              <div class="activity-card-info-item">
                签到时间<strong>{{ formatRange(item.checkinStartTime, item.checkinEndTime) }}</strong>
              </div>
              <div class="activity-card-info-item">
                已报名<strong>{{ item.enrolledCount || 0 }}/{{ item.capacity || 0 }}</strong>
              </div>
              <div class="activity-card-info-item">
                状态<strong>{{ item.checkedIn ? '已完成' : item.enrolled ? '待签到' : '可报名' }}</strong>
              </div>
            </div>

            <div class="activity-card-actions">
              <el-button v-if="!item.enrolled" type="primary" @click="enroll(item.id)">
                <el-icon><Plus /></el-icon> 报名
              </el-button>
              <el-button v-if="item.enrolled && !item.checkedIn" type="warning" @click="cancel(item.id)">
                取消报名
              </el-button>
              <el-button v-if="item.enrolled && !item.checkedIn" type="success" @click="checkin(item.id)">
                <el-icon><Check /></el-icon> 签到
              </el-button>
              <el-tag v-if="item.checkedIn" type="success" size="large">已完成签到</el-tag>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无活动" :image-size="80" />
      </el-tab-pane>

      <el-tab-pane label="我的报名" name="enrollments">
        <el-table :data="enrollRows" border>
          <el-table-column prop="activityTitle" label="活动" min-width="180" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="enrollStatusType(row.status)" size="small">
                {{ enrollStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="报名时间" width="210">
            <template #default="{ row }">
              {{ formatDateTime(row.enrollTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的签到" name="checkins">
        <el-table :data="checkinRows" border>
          <el-table-column prop="activityTitle" label="活动" min-width="180" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'CHECKED_IN' ? 'success' : 'info'" size="small">
                {{ row.status === 'CHECKED_IN' ? '已签到' : '未签到' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="签到时间" width="210">
            <template #default="{ row }">
              {{ formatDateTime(row.checkinTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { LocationFilled, Plus, Check } from '@element-plus/icons-vue'
import { cancelEnroll, checkinActivity, enrollActivity, getActivityPage, getMyCheckins, getMyEnrollments } from '../../api/activity'

const activeTab = ref('activities')
const rows = ref([])
const enrollRows = ref([])
const checkinRows = ref([])

async function load() {
  const [activityData, enrollData, checkinData] = await Promise.all([
    getActivityPage({ current: 1, size: 20 }),
    getMyEnrollments({ current: 1, size: 10 }),
    getMyCheckins({ current: 1, size: 10 })
  ])
  rows.value = activityData.records || []
  enrollRows.value = enrollData.records || []
  checkinRows.value = checkinData.records || []
}

async function enroll(id) {
  await enrollActivity({ activityId: id })
  ElMessage.success('报名成功')
  await load()
}

async function cancel(id) {
  await cancelEnroll(id)
  ElMessage.success('已取消报名')
  await load()
}

async function checkin(id) {
  await checkinActivity({ activityId: id })
  ElMessage.success('签到成功')
  await load()
}

function enrollStatusLabel(status) {
  const map = {
    ENROLLED: '已报名',
    CANCELLED: '已取消'
  }
  return map[status] || '未知状态'
}

function enrollStatusType(status) {
  if (status === 'ENROLLED') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'warning'
}

function formatRange(start, end) {
  if (!start && !end) return '未设置'
  return `${formatDateTime(start)} 至 ${formatDateTime(end)}`
}

function formatDateTime(value) {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return text
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}年${pad(date.getMonth() + 1)}月${pad(date.getDate())}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(load)
</script>
