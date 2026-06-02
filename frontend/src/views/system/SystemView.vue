<template>
  <div>
    <div class="page-header">
      <h2>系统管理</h2>
      <p>管理用户、公告、场地、时间段和活动</p>
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadAll">
      <!-- 用户管理 -->
      <el-tab-pane v-if="isAdmin" name="user">
        <template #label>
          <span style="display: flex; align-items: center; gap: 6px;">
            <el-icon><UserFilled /></el-icon> 用户管理
          </span>
        </template>

        <div class="mgmt-form-row">
          <el-select v-model="userForm.roleCode" placeholder="角色" style="width: 100px;">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-input v-model="userForm.loginNo" :placeholder="loginNoPlaceholder" style="width: 180px;" />
          <el-input v-model="userForm.realName" :placeholder="namePlaceholder" style="width: 130px;" />
          <el-input v-if="userForm.roleCode !== 'ADMIN'" v-model="userForm.college" placeholder="院系" style="width: 150px;" />
          <el-input v-model="userForm.password" placeholder="密码 (默认123456)" style="width: 150px;" />
          <el-button type="primary" @click="submitUser">{{ userForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetUserForm">清空</el-button>
          <el-button @click="loadUsers">刷新</el-button>
        </div>

        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="users" border style="border: none;">
              <el-table-column label="登录号" min-width="140">
                <template #default="{ row }">
                  {{ row.roleCode === 'STUDENT' ? '学号' : row.roleCode === 'TEACHER' ? '工号' : '账号' }}：{{ row.username }}
                </template>
              </el-table-column>
              <el-table-column prop="realName" label="姓名" width="100" />
              <el-table-column prop="college" label="院系" width="140" />
              <el-table-column prop="roleCode" label="角色" width="80">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.roleCode === 'ADMIN' ? 'danger' : row.roleCode === 'TEACHER' ? 'warning' : 'success'">
                    {{ roleLabel(row.roleCode) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editUser(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeUser(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 公告管理 -->
      <el-tab-pane name="notice">
        <template #label>
          <span style="display: flex; align-items: center; gap: 6px;">
            <el-icon><ChatLineSquare /></el-icon> 公告管理
          </span>
        </template>

        <div class="mgmt-form-row">
          <el-input v-model="noticeForm.title" placeholder="公告标题" style="width: 200px;" />
          <el-input v-model="noticeForm.category" placeholder="分类" style="width: 120px;" />
          <el-select v-model="noticeForm.scopeType" placeholder="发布范围" :disabled="!isAdmin" style="width: 100px;">
            <el-option label="全校" value="SCHOOL" />
            <el-option label="本院系" value="COLLEGE" />
          </el-select>
          <el-input v-if="noticeForm.scopeType === 'COLLEGE'" v-model="noticeForm.scopeCollege" placeholder="院系" style="width: 140px;" />
          <el-input v-model="noticeForm.content" placeholder="公告内容" style="width: 300px;" />
          <el-button type="primary" @click="submitNotice">{{ noticeForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetNoticeForm">清空</el-button>
          <el-button @click="loadNotices">刷新</el-button>
        </div>

        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="notices" border style="border: none;">
              <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
              <el-table-column prop="category" label="分类" width="100">
                <template #default="{ row }">
                  <el-tag size="small" effect="plain">{{ row.category }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="scopeType" label="范围" width="80" />
              <el-table-column prop="scopeCollege" label="院系" width="120" />
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editNotice(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeNotice(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 场地管理 -->
      <el-tab-pane v-if="isAdmin" name="venue">
        <template #label>
          <span style="display: flex; align-items: center; gap: 6px;">
            <el-icon><OfficeBuilding /></el-icon> 场地管理
          </span>
        </template>

        <div class="mgmt-form-row">
          <el-input v-model="venueForm.name" placeholder="场地名称" style="width: 160px;" />
          <el-input v-model="venueForm.location" placeholder="位置" style="width: 180px;" />
          <el-input-number v-model="venueForm.capacity" :min="1" placeholder="容量" style="width: 120px;" />
          <el-button type="primary" @click="submitVenue">{{ venueForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetVenueForm">清空</el-button>
          <el-button @click="loadVenues">刷新</el-button>
        </div>

        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="venues" border style="border: none;">
              <el-table-column prop="name" label="名称" min-width="140" />
              <el-table-column prop="location" label="位置" min-width="180" />
              <el-table-column prop="capacity" label="容量" width="80" />
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editVenue(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeVenue(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 时间段库存 -->
      <el-tab-pane v-if="isAdmin" name="slot">
        <template #label>
          <span style="display: flex; align-items: center; gap: 6px;">
            <el-icon><Timer /></el-icon> 时间段库存
          </span>
        </template>

        <div class="mgmt-form-row">
          <el-input v-model="slotForm.venueId" placeholder="场地ID" style="width: 100px;" />
          <el-date-picker v-model="slotForm.slotDate" value-format="YYYY-MM-DD" placeholder="日期" style="width: 150px;" />
          <el-input v-model="slotForm.timeRange" placeholder="09:00-10:00" style="width: 140px;" />
          <el-input-number v-model="slotForm.totalQuota" :min="1" placeholder="总名额" style="width: 110px;" />
          <el-input-number v-model="slotForm.remainingQuota" :min="0" placeholder="剩余" style="width: 110px;" />
          <el-button type="primary" @click="submitSlot">{{ slotForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetSlotForm">清空</el-button>
          <el-button @click="loadSlots">刷新</el-button>
        </div>

        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="slots" border style="border: none;">
              <el-table-column prop="venueId" label="场地ID" width="80" />
              <el-table-column prop="slotDate" label="日期" width="130" />
              <el-table-column prop="timeRange" label="时间段" width="140" />
              <el-table-column prop="totalQuota" label="总名额" width="80" />
              <el-table-column prop="remainingQuota" label="剩余" width="80" />
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editSlot(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeSlot(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 活动管理 -->
      <el-tab-pane name="activity">
        <template #label>
          <span style="display: flex; align-items: center; gap: 6px;">
            <el-icon><Star /></el-icon> 活动管理
          </span>
        </template>

        <div class="mgmt-form-row">
          <el-input v-model="activityForm.title" placeholder="活动标题" style="width: 180px;" />
          <el-select v-model="activityForm.venueId" placeholder="场地" filterable style="width: 180px;" @change="handleActivityVenueChange">
            <el-option v-for="v in venues" :key="v.id" :label="`${v.name} (${v.location})`" :value="v.id" />
          </el-select>
          <el-input-number v-model="activityForm.capacity" :min="1" style="width: 100px;" />
          <el-select v-model="activityForm.scopeType" placeholder="范围" :disabled="!isAdmin" style="width: 100px;">
            <el-option label="全校" value="SCHOOL" />
            <el-option label="本院系" value="COLLEGE" />
          </el-select>
          <el-date-picker v-model="activityTimeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="活动开始" end-placeholder="活动结束" style="width: 300px;" />
          <el-date-picker v-model="checkinTimeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="签到开始" end-placeholder="签到结束" style="width: 300px;" />
          <el-input v-model="activityForm.content" placeholder="活动说明" style="width: 200px;" />
          <el-button type="primary" @click="submitActivity">{{ activityForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetActivityForm">清空</el-button>
          <el-button @click="loadActivities">刷新</el-button>
        </div>

        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="activities" border style="border: none;">
              <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
              <el-table-column prop="venueId" label="场地ID" width="80" />
              <el-table-column prop="location" label="地点" width="120" />
              <el-table-column prop="capacity" label="容量" width="70" />
              <el-table-column prop="enrolledCount" label="已报名" width="80" />
              <el-table-column label="操作" width="140">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editActivity(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeActivity(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, ChatLineSquare, OfficeBuilding, Timer, Star } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { createActivity, deleteActivity, getActivityManagePage, updateActivity } from '../../api/activity'
import { createUser, deleteUser, getUserPage, updateUser } from '../../api/auth'
import { createNotice, deleteNotice, getNoticeManagePage, updateNotice } from '../../api/notice'
import { createVenue, createVenueSlot, deleteVenue, deleteVenueSlot, getVenuePage, getVenueSlotPage, updateVenue, updateVenueSlot } from '../../api/venue'

const activeTab = ref('notice')
const authStore = useAuthStore()
const isAdmin = computed(() => authStore.user?.roleCode === 'ADMIN')
const users = ref([])
const notices = ref([])
const venues = ref([])
const slots = ref([])
const activities = ref([])
const activityTimeRange = ref([])
const checkinTimeRange = ref([])

const userForm = reactive({ id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 })
const noticeForm = reactive({ id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })
const venueForm = reactive({ id: null, name: '', location: '', capacity: 1, status: 1 })
const slotForm = reactive({ id: null, venueId: '', slotDate: '', timeRange: '09:00-10:00', totalQuota: 1, remainingQuota: 1, status: 1 })
const activityForm = reactive({ id: null, title: '', venueId: '', capacity: 30, content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })

const loginNoPlaceholder = computed(() => userForm.roleCode === 'STUDENT' ? '学号' : userForm.roleCode === 'TEACHER' ? '工号' : '管理员账号')
const namePlaceholder = computed(() => userForm.roleCode === 'STUDENT' ? '姓名' : userForm.roleCode === 'TEACHER' ? '姓名' : '姓名')

function roleLabel(r) { return { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }[r] || r }

// --- Data loading ---
async function loadUsers() { const d = await getUserPage({ current: 1, size: 20 }); users.value = d.records || [] }
async function loadNotices() { const d = await getNoticeManagePage({ current: 1, size: 20 }); notices.value = d.records || [] }
async function loadVenues() { const d = await getVenuePage({ current: 1, size: 20 }); venues.value = d.records || [] }
async function loadSlots() { const d = await getVenueSlotPage({ current: 1, size: 20 }); slots.value = d.records || [] }
async function loadActivities() { const d = await getActivityManagePage({ current: 1, size: 20 }); activities.value = d.records || [] }

async function loadAll() {
  if (!isAdmin.value && activeTab.value === 'user') activeTab.value = 'notice'
  const tasks = [loadNotices(), loadVenues(), loadActivities()]
  if (isAdmin.value) tasks.push(loadUsers(), loadSlots())
  await Promise.all(tasks)
}

// --- Submissions ---
async function submitUser() {
  await (userForm.id ? updateUser(userForm) : createUser(userForm))
  ElMessage.success(userForm.id ? '用户更新成功' : '用户新增成功')
  resetUserForm(); await loadUsers()
}

async function submitNotice() {
  normalizeTeacherScope(noticeForm)
  await (noticeForm.id ? updateNotice(noticeForm) : createNotice(noticeForm))
  ElMessage.success(noticeForm.id ? '公告更新成功' : '公告新增成功')
  resetNoticeForm(); await loadNotices()
}

async function submitVenue() {
  await (venueForm.id ? updateVenue(venueForm) : createVenue(venueForm))
  ElMessage.success(venueForm.id ? '场地更新成功' : '场地新增成功')
  resetVenueForm(); await loadVenues()
}

async function submitSlot() {
  if (slotForm.remainingQuota > slotForm.totalQuota) { ElMessage.warning('剩余名额不能超过总名额'); return }
  const p = { ...slotForm, venueId: Number(slotForm.venueId), remainingQuota: slotForm.remainingQuota ?? slotForm.totalQuota }
  await (slotForm.id ? updateVenueSlot(p) : createVenueSlot(p))
  ElMessage.success(slotForm.id ? '时间段更新成功' : '时间段新增成功')
  resetSlotForm(); await loadSlots()
}

async function submitActivity() {
  if (!activityForm.venueId) { ElMessage.warning('请选择活动场地'); return }
  const p = { ...activityForm, venueId: Number(activityForm.venueId), startTime: activityTimeRange.value?.[0], endTime: activityTimeRange.value?.[1], checkinStartTime: checkinTimeRange.value?.[0], checkinEndTime: checkinTimeRange.value?.[1] }
  normalizeTeacherScope(p)
  await (activityForm.id ? updateActivity(p) : createActivity(p))
  ElMessage.success(activityForm.id ? '活动更新成功' : '活动新增成功')
  resetActivityForm(); await loadActivities()
}

function normalizeTeacherScope(f) {
  if (!isAdmin.value) { f.scopeType = 'COLLEGE'; f.scopeCollege = authStore.user?.college || f.scopeCollege || '计算机学院' }
}

function handleActivityVenueChange(id) {
  const v = venues.value.find(x => x.id === id)
  if (v) activityForm.capacity = v.capacity
}

// --- Deletions ---
async function removeUser(id) { await deleteUser(id); resetUserForm(); await loadUsers() }
async function removeNotice(id) { await deleteNotice(id); resetNoticeForm(); await loadNotices() }
async function removeVenue(id) { await deleteVenue(id); resetVenueForm(); await loadVenues() }
async function removeSlot(id) { await deleteVenueSlot(id); resetSlotForm(); await loadSlots() }
async function removeActivity(id) { await deleteActivity(id); resetActivityForm(); await loadActivities() }

// --- Edit ---
function editUser(r) { Object.assign(userForm, { id: r.id, loginNo: r.username, realName: r.realName, college: r.college || '计算机学院', roleCode: r.roleCode, password: '', status: r.status }) }
function editNotice(r) { Object.assign(noticeForm, { id: r.id, title: r.title, category: r.category, content: r.content, scopeType: r.scopeType || 'COLLEGE', scopeCollege: r.scopeCollege || '计算机学院', status: r.status }) }
function editVenue(r) { Object.assign(venueForm, { id: r.id, name: r.name, location: r.location, capacity: r.capacity, status: r.status }) }
function editSlot(r) { Object.assign(slotForm, { id: r.id, venueId: String(r.venueId), slotDate: r.slotDate, timeRange: r.timeRange, totalQuota: r.totalQuota, remainingQuota: r.remainingQuota, status: r.status }) }
function editActivity(r) {
  Object.assign(activityForm, { id: r.id, title: r.title, venueId: r.venueId, capacity: r.capacity, content: r.content, scopeType: r.scopeType || 'COLLEGE', scopeCollege: r.scopeCollege || '计算机学院', status: r.status })
  activityTimeRange.value = r.startTime && r.endTime ? [r.startTime, r.endTime] : []
  checkinTimeRange.value = r.checkinStartTime && r.checkinEndTime ? [r.checkinStartTime, r.checkinEndTime] : []
}

// --- Reset ---
function resetUserForm() { Object.assign(userForm, { id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 }) }
function resetNoticeForm() { Object.assign(noticeForm, { id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 }) }
function resetVenueForm() { Object.assign(venueForm, { id: null, name: '', location: '', capacity: 1, status: 1 }) }
function resetSlotForm() { Object.assign(slotForm, { id: null, venueId: '', slotDate: '', timeRange: '09:00-10:00', totalQuota: 1, remainingQuota: 1, status: 1 }) }
function resetActivityForm() {
  Object.assign(activityForm, { id: null, title: '', venueId: '', capacity: 30, content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })
  activityTimeRange.value = []
  checkinTimeRange.value = []
}

// --- Init ---
onMounted(() => {
  if (isAdmin.value) activeTab.value = 'user'
  loadAll()
})
</script>
