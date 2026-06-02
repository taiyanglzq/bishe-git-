<template>
  <el-tabs v-model="activeTab" @tab-change="loadAll">
    <el-tab-pane v-if="isAdmin" label="用户管理" name="user">
      <el-form :model="userForm" inline class="toolbar">
        <el-select v-model="userForm.roleCode" placeholder="角色">
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-input v-model="userForm.loginNo" :placeholder="loginNoPlaceholder" />
        <el-input v-model="userForm.realName" :placeholder="namePlaceholder" />
        <el-input v-if="userForm.roleCode !== 'ADMIN'" v-model="userForm.college" placeholder="所属院系，例如 计算机学院" />
        <el-input v-model="userForm.password" placeholder="默认 123456" />
        <el-button type="primary" @click="submitUser">{{ userForm.id ? '更新用户' : '保存用户' }}</el-button>
        <el-button @click="resetUserForm">清空</el-button>
        <el-button @click="loadUsers">刷新</el-button>
      </el-form>
      <el-table :data="users" border>
        <el-table-column label="登录号">
          <template #default="{ row }">
            {{ row.roleCode === 'STUDENT' ? '学号' : row.roleCode === 'TEACHER' ? '工号' : '账号' }}：{{ row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="college" label="院系" />
        <el-table-column prop="roleCode" label="角色" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeUser(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <el-tab-pane label="公告管理" name="notice">
      <el-form :model="noticeForm" inline class="toolbar">
        <el-input v-model="noticeForm.title" placeholder="标题" />
        <el-input v-model="noticeForm.category" placeholder="分类" />
        <el-select v-model="noticeForm.scopeType" placeholder="发布范围" :disabled="!isAdmin">
          <el-option label="全校" value="SCHOOL" />
          <el-option label="本院系" value="COLLEGE" />
        </el-select>
        <el-input v-if="noticeForm.scopeType === 'COLLEGE'" v-model="noticeForm.scopeCollege" placeholder="院系，例如 计算机学院" />
        <el-input v-model="noticeForm.content" placeholder="内容" />
        <el-button type="primary" @click="submitNotice">{{ noticeForm.id ? '更新公告' : '保存公告' }}</el-button>
        <el-button @click="resetNoticeForm">清空</el-button>
        <el-button @click="loadNotices">刷新</el-button>
      </el-form>
      <el-table :data="notices" border>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" />
        <el-table-column prop="scopeType" label="范围" width="100" />
        <el-table-column prop="scopeCollege" label="院系" />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editNotice(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeNotice(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <el-tab-pane v-if="isAdmin" label="场地管理" name="venue">
      <el-form :model="venueForm" inline class="toolbar">
        <el-input v-model="venueForm.name" placeholder="名称" />
        <el-input v-model="venueForm.location" placeholder="位置" />
        <el-input-number v-model="venueForm.capacity" :min="1" />
        <el-button type="primary" @click="submitVenue">{{ venueForm.id ? '更新场地' : '保存场地' }}</el-button>
        <el-button @click="resetVenueForm">清空</el-button>
        <el-button @click="loadVenues">刷新</el-button>
      </el-form>
      <el-table :data="venues" border>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容量" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editVenue(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeVenue(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <el-tab-pane v-if="isAdmin" label="时间段库存" name="slot">
      <el-form :model="slotForm" inline class="toolbar">
        <el-input v-model="slotForm.venueId" placeholder="场地ID" />
        <el-date-picker v-model="slotForm.slotDate" value-format="YYYY-MM-DD" />
        <el-input v-model="slotForm.timeRange" placeholder="09:00-10:00" />
        <el-input-number v-model="slotForm.totalQuota" :min="1" />
        <el-input-number v-model="slotForm.remainingQuota" :min="0" />
        <el-button type="primary" @click="submitSlot">{{ slotForm.id ? '更新时间段' : '保存时间段' }}</el-button>
        <el-button @click="resetSlotForm">清空</el-button>
        <el-button @click="loadSlots">刷新</el-button>
      </el-form>
      <el-table :data="slots" border>
        <el-table-column prop="venueId" label="场地ID" width="100" />
        <el-table-column prop="slotDate" label="日期" width="140" />
        <el-table-column prop="timeRange" label="时间段" width="140" />
        <el-table-column prop="totalQuota" label="总名额" width="100" />
        <el-table-column prop="remainingQuota" label="剩余" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editSlot(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeSlot(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <el-tab-pane label="活动管理" name="activity">
      <el-form :model="activityForm" inline class="toolbar">
        <el-input v-model="activityForm.title" placeholder="标题" />
        <el-select v-model="activityForm.venueId" placeholder="请选择活动场地" filterable @change="handleActivityVenueChange">
          <el-option
            v-for="venue in venues"
            :key="venue.id"
            :label="`${venue.name}（${venue.location}，容量 ${venue.capacity}）`"
            :value="venue.id"
          />
        </el-select>
        <el-input-number v-model="activityForm.capacity" :min="1" />
        <el-select v-model="activityForm.scopeType" placeholder="发布范围" :disabled="!isAdmin">
          <el-option label="全校" value="SCHOOL" />
          <el-option label="本院系" value="COLLEGE" />
        </el-select>
        <el-input v-if="activityForm.scopeType === 'COLLEGE'" v-model="activityForm.scopeCollege" placeholder="院系，例如 计算机学院" />
        <el-date-picker
          v-model="activityTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="活动开始"
          end-placeholder="活动结束"
        />
        <el-date-picker
          v-model="checkinTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="签到开始"
          end-placeholder="签到结束"
        />
        <el-input v-model="activityForm.content" placeholder="内容" />
        <el-button type="primary" @click="submitActivity">{{ activityForm.id ? '更新活动' : '保存活动' }}</el-button>
        <el-button @click="resetActivityForm">清空</el-button>
        <el-button @click="loadActivities">刷新</el-button>
      </el-form>
      <el-table :data="activities" border>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="venueId" label="场地ID" width="90" />
        <el-table-column prop="location" label="地点" />
        <el-table-column prop="scopeType" label="范围" width="100" />
        <el-table-column prop="scopeCollege" label="院系" />
        <el-table-column prop="capacity" label="容量" width="100" />
        <el-table-column prop="enrolledCount" label="已报名" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editActivity(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeActivity(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { createActivity, deleteActivity, getActivityManagePage, updateActivity } from '../../api/activity'
import { createUser, deleteUser, getUserPage, updateUser } from '../../api/auth'
import { createNotice, deleteNotice, getNoticeManagePage, updateNotice } from '../../api/notice'
import { createVenue, createVenueSlot, deleteVenue, deleteVenueSlot, getVenuePage, getVenueSlotPage, updateVenue, updateVenueSlot } from '../../api/venue'

const activeTab = ref('user')
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

const loginNoPlaceholder = computed(() => {
  if (userForm.roleCode === 'STUDENT') return '学号，例如 23050539414'
  if (userForm.roleCode === 'TEACHER') return '工号，例如 T2026001'
  return '管理员账号，例如 admin'
})

const namePlaceholder = computed(() => {
  if (userForm.roleCode === 'STUDENT') return '学生姓名'
  if (userForm.roleCode === 'TEACHER') return '教师姓名'
  return '管理员姓名'
})

async function loadUsers() {
  const data = await getUserPage({ current: 1, size: 20 })
  users.value = data.records || []
}

async function loadNotices() {
  const data = await getNoticeManagePage({ current: 1, size: 20 })
  notices.value = data.records || []
}

async function loadVenues() {
  const data = await getVenuePage({ current: 1, size: 20 })
  venues.value = data.records || []
}

async function loadSlots() {
  const data = await getVenueSlotPage({ current: 1, size: 20 })
  slots.value = data.records || []
}

async function loadActivities() {
  const data = await getActivityManagePage({ current: 1, size: 20 })
  activities.value = data.records || []
}

async function loadAll() {
  if (!isAdmin.value && activeTab.value === 'user') {
    activeTab.value = 'notice'
  }
  const tasks = [loadNotices(), loadVenues(), loadActivities()]
  if (isAdmin.value) {
    tasks.push(loadUsers(), loadSlots())
  }
  await Promise.all(tasks)
}

async function submitUser() {
  if (userForm.id) {
    await updateUser(userForm)
  } else {
    await createUser(userForm)
  }
  ElMessage.success(userForm.id ? '用户更新成功' : '用户保存成功')
  resetUserForm()
  await loadUsers()
}

async function submitNotice() {
  normalizeTeacherScope(noticeForm)
  if (noticeForm.id) {
    await updateNotice(noticeForm)
  } else {
    await createNotice(noticeForm)
  }
  ElMessage.success(noticeForm.id ? '公告更新成功' : '公告保存成功')
  resetNoticeForm()
  await loadNotices()
}

async function submitVenue() {
  if (venueForm.id) {
    await updateVenue(venueForm)
  } else {
    await createVenue(venueForm)
  }
  ElMessage.success(venueForm.id ? '场地更新成功' : '场地保存成功')
  resetVenueForm()
  await loadVenues()
}

async function submitSlot() {
  if (slotForm.remainingQuota > slotForm.totalQuota) {
    ElMessage.warning('剩余名额不能超过总名额')
    return
  }
  const payload = { ...slotForm, venueId: Number(slotForm.venueId), remainingQuota: slotForm.remainingQuota ?? slotForm.totalQuota }
  if (slotForm.id) {
    await updateVenueSlot(payload)
  } else {
    await createVenueSlot(payload)
  }
  ElMessage.success(slotForm.id ? '时间段更新成功' : '时间段保存成功')
  resetSlotForm()
  await loadSlots()
}

async function submitActivity() {
  if (!activityForm.venueId) {
    ElMessage.warning('请选择活动场地')
    return
  }
  const payload = {
    ...activityForm,
    venueId: Number(activityForm.venueId),
    startTime: activityTimeRange.value?.[0],
    endTime: activityTimeRange.value?.[1],
    checkinStartTime: checkinTimeRange.value?.[0],
    checkinEndTime: checkinTimeRange.value?.[1]
  }
  normalizeTeacherScope(payload)
  if (activityForm.id) {
    await updateActivity(payload)
  } else {
    await createActivity(payload)
  }
  ElMessage.success(activityForm.id ? '活动更新成功' : '活动保存成功')
  resetActivityForm()
  await loadActivities()
}

function normalizeTeacherScope(form) {
  if (isAdmin.value) {
    return
  }
  form.scopeType = 'COLLEGE'
  form.scopeCollege = authStore.user?.college || form.scopeCollege || '计算机学院'
}

function handleActivityVenueChange(venueId) {
  const venue = venues.value.find((item) => item.id === venueId)
  if (venue) {
    activityForm.capacity = venue.capacity
  }
}

async function removeUser(id) {
  await deleteUser(id)
  resetUserForm()
  await loadUsers()
}

async function removeNotice(id) {
  await deleteNotice(id)
  resetNoticeForm()
  await loadNotices()
}

async function removeVenue(id) {
  await deleteVenue(id)
  resetVenueForm()
  await loadVenues()
}

async function removeSlot(id) {
  await deleteVenueSlot(id)
  resetSlotForm()
  await loadSlots()
}

async function removeActivity(id) {
  await deleteActivity(id)
  resetActivityForm()
  await loadActivities()
}

function editUser(row) {
  Object.assign(userForm, {
    id: row.id,
    loginNo: row.username,
    realName: row.realName,
    college: row.college || '计算机学院',
    roleCode: row.roleCode,
    password: '',
    status: row.status
  })
}

function editNotice(row) {
  Object.assign(noticeForm, {
    id: row.id,
    title: row.title,
    category: row.category,
    content: row.content,
    scopeType: row.scopeType || 'COLLEGE',
    scopeCollege: row.scopeCollege || '计算机学院',
    status: row.status
  })
}

function editVenue(row) {
  Object.assign(venueForm, {
    id: row.id,
    name: row.name,
    location: row.location,
    capacity: row.capacity,
    status: row.status
  })
}

function editSlot(row) {
  Object.assign(slotForm, {
    id: row.id,
    venueId: String(row.venueId),
    slotDate: row.slotDate,
    timeRange: row.timeRange,
    totalQuota: row.totalQuota,
    remainingQuota: row.remainingQuota,
    status: row.status
  })
}

function editActivity(row) {
  Object.assign(activityForm, {
    id: row.id,
    title: row.title,
    venueId: row.venueId,
    capacity: row.capacity,
    content: row.content,
    scopeType: row.scopeType || 'COLLEGE',
    scopeCollege: row.scopeCollege || '计算机学院',
    status: row.status
  })
  activityTimeRange.value = row.startTime && row.endTime ? [row.startTime, row.endTime] : []
  checkinTimeRange.value = row.checkinStartTime && row.checkinEndTime ? [row.checkinStartTime, row.checkinEndTime] : []
}

function resetUserForm() {
  Object.assign(userForm, { id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 })
}

function resetNoticeForm() {
  Object.assign(noticeForm, { id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })
}

function resetVenueForm() {
  Object.assign(venueForm, { id: null, name: '', location: '', capacity: 1, status: 1 })
}

function resetSlotForm() {
  Object.assign(slotForm, { id: null, venueId: '', slotDate: '', timeRange: '09:00-10:00', totalQuota: 1, remainingQuota: 1, status: 1 })
}

function resetActivityForm() {
  Object.assign(activityForm, { id: null, title: '', venueId: '', capacity: 30, content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })
  activityTimeRange.value = []
  checkinTimeRange.value = []
}

onMounted(loadAll)
</script>
