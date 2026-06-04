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
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" class="activity-cover" alt="活动海报" />
            <div v-else class="activity-cover activity-cover-empty">活动海报</div>
            <div class="activity-card-top">
              <div>
                <h3>{{ item.title }}</h3>
                <div class="activity-card-location">
                  <el-icon><LocationFilled /></el-icon> {{ item.location || '未设置地点' }}
                </div>
              </div>
              <el-tag :type="item.checkedIn ? 'success' : item.enrolled ? 'warning' : 'info'" effect="light">
                {{ item.checkedIn ? '已签到' : item.enrolled ? '已报名' : '未报名' }}
              </el-tag>
            </div>

            <div class="activity-card-desc">{{ item.content || '暂无活动说明' }}</div>

            <div class="activity-card-info">
              <div class="activity-card-info-item">活动时间<strong>{{ formatRange(item.startTime, item.endTime) }}</strong></div>
              <div class="activity-card-info-item">签到时间<strong>{{ formatRange(item.checkinStartTime, item.checkinEndTime) }}</strong></div>
              <div class="activity-card-info-item">已报名<strong>{{ item.enrolledCount || 0 }}/{{ item.capacity || 0 }}</strong></div>
              <div class="activity-card-info-item">状态<strong>{{ item.checkedIn ? '已完成' : item.enrolled ? '待签到' : '可报名' }}</strong></div>
            </div>

            <div class="activity-card-actions">
              <el-button v-if="!item.enrolled" type="primary" @click="enroll(item.id)">
                <el-icon><Plus /></el-icon> 报名
              </el-button>
              <el-button v-if="item.enrolled && !item.checkedIn" type="warning" @click="cancel(item.id)">取消报名</el-button>
              <el-button v-if="item.enrolled && !item.checkedIn" type="success" :disabled="!canCheckinNow(item)" @click="checkin(item.id)">
                <el-icon><Check /></el-icon> {{ canCheckinNow(item) ? '签到' : '未到签到时间' }}
              </el-button>
              <el-tag v-if="item.checkedIn" type="success" size="large">已完成签到</el-tag>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无活动" :image-size="80" />
      </el-tab-pane>

      <el-tab-pane label="待签到活动" name="pendingCheckin">
        <div v-if="pendingCheckinRows.length" class="checked-activity-grid">
          <article v-for="item in pendingCheckinRows" :key="item.id" class="checked-activity-card pending-checkin-card">
            <img v-if="item.coverUrl" :src="assetUrl(item.coverUrl)" class="checked-activity-thumb" alt="活动海报" />
            <div>
              <el-tag type="warning" size="small">可以签到</el-tag>
              <h3>{{ item.title }}</h3>
              <p>地点：{{ item.location || '未设置地点' }}</p>
              <p>签到时间：{{ formatRange(item.checkinStartTime, item.checkinEndTime) }}</p>
            </div>
            <el-button type="success" @click="checkin(item.id)">
              <el-icon><Check /></el-icon> 立即签到
            </el-button>
          </article>
        </div>
        <div v-else class="friendly-empty">
          <div class="friendly-empty-icon"><el-icon><Clock /></el-icon></div>
          <h3>当前还没有活动可签到</h3>
          <p>你已报名的活动还没有进入签到时间，或者已经完成签到。可以稍后再来查看。</p>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的报名" name="enrollments">
        <el-table :data="enrollRows" border empty-text="暂无报名记录">
          <el-table-column prop="activityTitle" label="活动" min-width="180" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }"><el-tag :type="enrollStatusType(row.status)" size="small">{{ enrollStatusLabel(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="报名时间" width="210">
            <template #default="{ row }">{{ formatDateTime(row.enrollTime) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="已签到活动" name="checked">
        <div v-if="checkinRows.length" class="checked-activity-grid">
          <article v-for="row in checkinRows" :key="row.id" class="checked-activity-card">
            <div>
              <el-tag type="success" size="small">已签到</el-tag>
              <h3>{{ row.activityTitle }}</h3>
              <p>签到时间：{{ formatDateTime(row.checkinTime) }}</p>
            </div>
            <el-icon><Check /></el-icon>
          </article>
        </div>
        <div v-else class="friendly-empty">
          <div class="friendly-empty-icon"><el-icon><Check /></el-icon></div>
          <h3>暂时还没有已签到活动</h3>
          <p>完成活动签到后，记录会显示在这里，方便你回顾参与过的校园活动。</p>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { LocationFilled, Plus, Check, Clock } from '@element-plus/icons-vue'
import { cancelEnroll, checkinActivity, enrollActivity, getActivityPage, getMyCheckins, getMyEnrollments } from '../../api/activity'

const activeTab = ref('activities')
const rows = ref([])
const enrollRows = ref([])
const checkinRows = ref([])
const pendingCheckinRows = computed(() => rows.value.filter((item) => canCheckinNow(item)))

function assetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return url
}

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

async function enroll(id) { await enrollActivity({ activityId: id }); ElMessage.success('报名成功'); await load() }
async function cancel(id) { await cancelEnroll(id); ElMessage.success('已取消报名'); await load() }
async function checkin(id) { await checkinActivity({ activityId: id }); ElMessage.success('签到成功'); await load() }

function canCheckinNow(item) {
  if (!item.enrolled || item.checkedIn) return false
  const now = new Date()
  const start = parseDate(item.checkinStartTime)
  const end = parseDate(item.checkinEndTime)
  if (start && now < start) return false
  if (end && now > end) return false
  return true
}

function enrollStatusLabel(status) {
  return { ENROLLED: '已报名', CANCELLED: '已取消' }[status] || '未知状态'
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
  const date = parseDate(value)
  if (!date) return String(value).replace('T', ' ')
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}年${pad(date.getMonth() + 1)}月${pad(date.getDate())}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function parseDate(value) {
  if (!value) return null
  const date = new Date(String(value).replace('T', ' '))
  return Number.isNaN(date.getTime()) ? null : date
}

onMounted(load)
</script>
