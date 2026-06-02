<template>
  <el-tabs>
    <el-tab-pane label="活动中心">
      <div class="activity-grid">
        <article v-for="item in rows" :key="item.id" class="activity-card">
          <div class="activity-card-header">
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.location || '未设置地点' }}</p>
            </div>
            <el-tag :type="item.checkedIn ? 'success' : item.enrolled ? 'warning' : 'info'">
              {{ item.checkedIn ? '已签到' : item.enrolled ? '已报名' : '未报名' }}
            </el-tag>
          </div>

          <p class="activity-content">{{ item.content || '暂无活动说明' }}</p>

          <div class="activity-meta">
            <span>活动：{{ formatRange(item.startTime, item.endTime) }}</span>
            <span>签到：{{ formatRange(item.checkinStartTime, item.checkinEndTime) }}</span>
            <span>名额：{{ item.enrolledCount }}/{{ item.capacity }}</span>
          </div>

          <div class="activity-actions">
            <el-button v-if="!item.enrolled" type="primary" @click="enroll(item.id)">报名</el-button>
            <el-button v-if="item.enrolled && !item.checkedIn" type="warning" @click="cancel(item.id)">取消报名</el-button>
            <el-button v-if="item.enrolled && !item.checkedIn" type="success" @click="checkin(item.id)">签到</el-button>
            <el-button v-if="item.checkedIn" disabled>已完成签到</el-button>
          </div>
        </article>
      </div>
    </el-tab-pane>

    <el-tab-pane label="我的报名">
      <el-table :data="enrollRows" border>
        <el-table-column prop="activityTitle" label="活动" min-width="180" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="enrollTime" label="报名时间" width="180" />
      </el-table>
    </el-tab-pane>

    <el-tab-pane label="我的签到">
      <el-table :data="checkinRows" border>
        <el-table-column prop="activityTitle" label="活动" min-width="180" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="checkinTime" label="签到时间" width="180" />
      </el-table>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  cancelEnroll,
  checkinActivity,
  enrollActivity,
  getActivityPage,
  getMyCheckins,
  getMyEnrollments
} from '../../api/activity'

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

async function enroll(activityId) {
  await enrollActivity({ activityId })
  ElMessage.success('报名成功')
  await load()
}

async function cancel(activityId) {
  await cancelEnroll(activityId)
  ElMessage.success('已取消报名')
  await load()
}

async function checkin(activityId) {
  await checkinActivity({ activityId })
  ElMessage.success('签到成功')
  await load()
}

function formatRange(start, end) {
  if (!start && !end) return '未设置'
  return `${start || '-'} 至 ${end || '-'}`
}

onMounted(load)
</script>
