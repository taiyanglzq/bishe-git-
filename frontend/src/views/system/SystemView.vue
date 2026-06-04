<template>
  <div>
    <div class="page-header">
      <h2>系统管理</h2>
      <p>管理用户、公告、场地、时间段库存和校园活动</p>
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadAll">
      <el-tab-pane v-if="isAdmin" name="user">
        <template #label><span class="tab-label"><el-icon><UserFilled /></el-icon> 用户管理</span></template>
        <div class="mgmt-form-row">
          <el-select v-model="userForm.roleCode" placeholder="角色" style="width: 110px;">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-input v-model="userForm.loginNo" :placeholder="loginNoPlaceholder" style="width: 180px;" />
          <el-input v-model="userForm.realName" placeholder="姓名" style="width: 130px;" />
          <el-input v-if="userForm.roleCode !== 'ADMIN'" v-model="userForm.college" placeholder="院系" style="width: 150px;" />
          <el-input v-model="userForm.password" placeholder="密码，默认123456" style="width: 160px;" />
          <el-button type="primary" @click="submitUser">{{ userForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetUserForm">清空</el-button>
          <el-button @click="loadUsers">刷新</el-button>
        </div>
        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="users" border style="border: none;">
              <el-table-column label="登录号" min-width="150">
                <template #default="{ row }">{{ loginNoLabel(row.roleCode) }}：{{ row.username }}</template>
              </el-table-column>
              <el-table-column prop="realName" label="姓名" width="110" />
              <el-table-column prop="college" label="院系" width="150" />
              <el-table-column label="角色" width="100">
                <template #default="{ row }"><el-tag size="small">{{ roleLabel(row.roleCode) }}</el-tag></template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editUser(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeUser(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="mgmt-pagination"
              layout="prev, pager, next, total"
              :current-page="userPage.current"
              :page-size="PAGE_SIZE"
              :total="userPage.total"
              @current-change="changeUserPage"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="notice">
        <template #label><span class="tab-label"><el-icon><ChatLineSquare /></el-icon> 公告管理</span></template>
        <div class="mgmt-form-row">
          <el-input v-model="noticeForm.title" placeholder="公告标题" style="width: 200px;" />
          <el-input v-model="noticeForm.category" placeholder="分类" style="width: 120px;" />
          <el-select v-model="noticeForm.scopeType" placeholder="发布范围" :disabled="!isAdmin" style="width: 110px;">
            <el-option label="全校" value="SCHOOL" />
            <el-option label="本院系" value="COLLEGE" />
          </el-select>
          <el-input v-if="noticeForm.scopeType === 'COLLEGE'" v-model="noticeForm.scopeCollege" placeholder="院系" style="width: 150px;" />
          <el-input v-model="noticeForm.content" placeholder="公告内容" style="width: 320px;" />
          <el-button type="primary" @click="submitNotice">{{ noticeForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetNoticeForm">清空</el-button>
          <el-button @click="loadNotices">刷新</el-button>
        </div>
        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="notices" border style="border: none;">
              <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
              <el-table-column prop="category" label="分类" width="110" />
              <el-table-column prop="scopeType" label="范围" width="90" />
              <el-table-column prop="scopeCollege" label="院系" width="140" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editNotice(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeNotice(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="mgmt-pagination"
              layout="prev, pager, next, total"
              :current-page="noticePage.current"
              :page-size="PAGE_SIZE"
              :total="noticePage.total"
              @current-change="changeNoticePage"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="isAdmin" name="venue">
        <template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon> 场地管理</span></template>
        <div class="mgmt-form-row">
          <image-uploader label="场地图片" :url="venueForm.imageUrl" @uploaded="venueForm.imageUrl = $event" />
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
              <el-table-column label="图片" width="92">
                <template #default="{ row }">
                  <img v-if="row.imageUrl" :src="assetUrl(row.imageUrl)" class="table-thumb" alt="场地图片" />
                  <span v-else class="text-muted">未上传</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="名称" min-width="140" />
              <el-table-column prop="location" label="位置" min-width="180" />
              <el-table-column prop="capacity" label="容量" width="90" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editVenue(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeVenue(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="mgmt-pagination"
              layout="prev, pager, next, total"
              :current-page="venuePage.current"
              :page-size="PAGE_SIZE"
              :total="venuePage.total"
              @current-change="changeVenuePage"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="isAdmin" name="slot">
        <template #label><span class="tab-label"><el-icon><Timer /></el-icon> 时间段库存</span></template>
        <div class="mgmt-form-row">
          <el-select v-model="slotForm.venueId" placeholder="场地" filterable style="width: 180px;">
            <el-option v-for="venue in venueOptions" :key="venue.id" :label="venue.name" :value="String(venue.id)" />
          </el-select>
          <el-date-picker v-model="slotForm.slotDate" value-format="YYYY-MM-DD" placeholder="日期" style="width: 150px;" />
          <el-input v-model="slotForm.timeRange" placeholder="09:00-10:00" style="width: 140px;" />
          <el-input-number v-model="slotForm.totalQuota" :min="1" placeholder="总名额" style="width: 120px;" />
          <el-input-number v-model="slotForm.remainingQuota" :min="0" placeholder="剩余" style="width: 120px;" />
          <el-button type="primary" @click="submitSlot">{{ slotForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetSlotForm">清空</el-button>
          <el-button @click="loadSlots">刷新</el-button>
        </div>
        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="slots" border style="border: none;">
              <el-table-column prop="venueId" label="场地ID" width="90" />
              <el-table-column prop="slotDate" label="日期" width="130" />
              <el-table-column prop="timeRange" label="时间段" width="140" />
              <el-table-column prop="totalQuota" label="总名额" width="90" />
              <el-table-column prop="remainingQuota" label="剩余" width="90" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editSlot(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeSlot(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="mgmt-pagination"
              layout="prev, pager, next, total"
              :current-page="slotPage.current"
              :page-size="PAGE_SIZE"
              :total="slotPage.total"
              @current-change="changeSlotPage"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="activity">
        <template #label><span class="tab-label"><el-icon><Star /></el-icon> 活动管理</span></template>
        <div class="mgmt-form-row">
          <image-uploader label="活动海报" :url="activityForm.coverUrl" @uploaded="activityForm.coverUrl = $event" />
          <el-input v-model="activityForm.title" placeholder="活动标题" style="width: 180px;" />
          <el-select v-model="activityForm.venueId" placeholder="场地" filterable style="width: 190px;" @change="handleActivityVenueChange">
            <el-option v-for="venue in venueOptions" :key="venue.id" :label="`${venue.name}（${venue.location || '未设置位置'}）`" :value="venue.id" />
          </el-select>
          <el-input-number v-model="activityForm.capacity" :min="1" style="width: 110px;" />
          <el-select v-model="activityForm.scopeType" placeholder="范围" :disabled="!isAdmin" style="width: 110px;">
            <el-option label="全校" value="SCHOOL" />
            <el-option label="本院系" value="COLLEGE" />
          </el-select>
          <el-date-picker v-model="activityTimeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="活动开始" end-placeholder="活动结束" style="width: 330px;" />
          <el-date-picker v-model="checkinTimeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="签到开始" end-placeholder="签到结束" style="width: 330px;" />
          <el-input v-model="activityForm.content" placeholder="活动说明" style="width: 240px;" />
          <el-button type="primary" @click="submitActivity">{{ activityForm.id ? '更新' : '新增' }}</el-button>
          <el-button @click="resetActivityForm">清空</el-button>
          <el-button @click="loadActivities">刷新</el-button>
        </div>
        <div class="panel-card">
          <div class="panel-card-body" style="padding: 0;">
            <el-table :data="activities" border style="border: none;">
              <el-table-column label="海报" width="92">
                <template #default="{ row }">
                  <img v-if="row.coverUrl" :src="assetUrl(row.coverUrl)" class="table-thumb" alt="活动海报" />
                  <span v-else class="text-muted">未上传</span>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
              <el-table-column prop="location" label="地点" width="150" show-overflow-tooltip />
              <el-table-column prop="capacity" label="容量" width="80" />
              <el-table-column prop="enrolledCount" label="已报名" width="90" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" text type="primary" @click="editActivity(row)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="removeActivity(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="mgmt-pagination"
              layout="prev, pager, next, total"
              :current-page="activityPage.current"
              :page-size="PAGE_SIZE"
              :total="activityPage.total"
              @current-change="changeActivityPage"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElButton, ElMessage, ElMessageBox, ElUpload } from 'element-plus'
import { UserFilled, ChatLineSquare, OfficeBuilding, Timer, Star } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { createActivity, deleteActivity, getActivityManagePage, updateActivity } from '../../api/activity'
import { createUser, deleteUser, getUserPage, updateUser } from '../../api/auth'
import { createNotice, deleteNotice, getNoticeManagePage, updateNotice } from '../../api/notice'
import { createVenue, createVenueSlot, deleteVenue, deleteVenueSlot, getVenuePage, getVenueSlotPage, updateVenue, updateVenueSlot, uploadImage } from '../../api/venue'

const ImageUploader = defineComponent({
  props: { label: String, url: String },
  emits: ['uploaded'],
  setup(props, { emit }) {
    async function beforeUpload(file) {
      const url = await uploadImage(file)
      emit('uploaded', url)
      ElMessage.success('图片上传成功')
      return false
    }
    return () => h('div', { class: 'image-upload-box' }, [
      props.url ? h('img', { src: assetUrl(props.url), class: 'image-upload-preview', alt: props.label }) : h('div', { class: 'image-upload-placeholder' }, props.label || '上传图片'),
      h(ElUpload, { showFileList: false, beforeUpload, accept: 'image/png,image/jpeg,image/webp' }, () => h(ElButton, { size: 'small' }, () => props.url ? '重新上传' : '上传图片'))
    ])
  }
})

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

const PAGE_SIZE = 10
const userPage = reactive({ current: 1, total: 0 })
const noticePage = reactive({ current: 1, total: 0 })
const venuePage = reactive({ current: 1, total: 0 })
const slotPage = reactive({ current: 1, total: 0 })
const activityPage = reactive({ current: 1, total: 0 })

const userForm = reactive({ id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 })
const noticeForm = reactive({ id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })
const venueForm = reactive({ id: null, name: '', location: '', imageUrl: '', capacity: 1, status: 1 })
const slotForm = reactive({ id: null, venueId: '', slotDate: '', timeRange: '09:00-10:00', totalQuota: 1, remainingQuota: 1, status: 1 })
const activityForm = reactive({ id: null, title: '', venueId: '', coverUrl: '', capacity: 30, content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })

const loginNoPlaceholder = computed(() => userForm.roleCode === 'STUDENT' ? '学号' : userForm.roleCode === 'TEACHER' ? '工号' : '管理员账号')

function assetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return url
}

function loginNoLabel(roleCode) { return roleCode === 'STUDENT' ? '学号' : roleCode === 'TEACHER' ? '工号' : '账号' }
function roleLabel(roleCode) { return { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }[roleCode] || roleCode }

const venueOptions = ref([])

async function loadUsers() {
  const data = await getUserPage({ current: userPage.current, size: PAGE_SIZE })
  users.value = data.records || []
  userPage.total = data.total || 0
}
async function loadNotices() {
  const data = await getNoticeManagePage({ current: noticePage.current, size: PAGE_SIZE })
  notices.value = data.records || []
  noticePage.total = data.total || 0
}
async function loadVenues() {
  const data = await getVenuePage({ current: venuePage.current, size: PAGE_SIZE })
  venues.value = data.records || []
  venuePage.total = data.total || 0
  await loadVenueOptions()
}
async function loadVenueOptions() {
  const data = await getVenuePage({ current: 1, size: 200 })
  venueOptions.value = data.records || []
}
async function loadSlots() {
  const data = await getVenueSlotPage({ current: slotPage.current, size: PAGE_SIZE })
  slots.value = data.records || []
  slotPage.total = data.total || 0
}
async function loadActivities() {
  const data = await getActivityManagePage({ current: activityPage.current, size: PAGE_SIZE })
  activities.value = data.records || []
  activityPage.total = data.total || 0
}

function changeUserPage(p) { userPage.current = p; loadUsers() }
function changeNoticePage(p) { noticePage.current = p; loadNotices() }
function changeVenuePage(p) { venuePage.current = p; loadVenues() }
function changeSlotPage(p) { slotPage.current = p; loadSlots() }
function changeActivityPage(p) { activityPage.current = p; loadActivities() }

async function loadAll() {
  if (!isAdmin.value && activeTab.value === 'user') activeTab.value = 'notice'
  const tasks = [loadNotices(), loadVenues(), loadActivities()]
  if (isAdmin.value) tasks.push(loadUsers(), loadSlots())
  await Promise.all(tasks)
}

async function submitUser() {
  if (!userForm.loginNo.trim()) { ElMessage.warning('请填写登录号'); return }
  if (!userForm.realName.trim()) { ElMessage.warning('请填写姓名'); return }
  if (!userForm.id && !userForm.password.trim()) { ElMessage.warning('请填写初始密码'); return }
  await (userForm.id ? updateUser(userForm) : createUser(userForm))
  ElMessage.success(userForm.id ? '用户更新成功' : '用户新增成功')
  resetUserForm(); await loadUsers()
}

async function submitNotice() {
  if (!noticeForm.title.trim()) { ElMessage.warning('请填写公告标题'); return }
  if (!noticeForm.content.trim()) { ElMessage.warning('请填写公告内容'); return }
  normalizeTeacherScope(noticeForm)
  await (noticeForm.id ? updateNotice(noticeForm) : createNotice(noticeForm))
  ElMessage.success(noticeForm.id ? '公告更新成功' : '公告新增成功')
  resetNoticeForm(); await loadNotices()
}

async function submitVenue() {
  if (!venueForm.name.trim()) { ElMessage.warning('请填写场地名称'); return }
  if (!venueForm.location.trim()) { ElMessage.warning('请填写场地位置'); return }
  await (venueForm.id ? updateVenue(venueForm) : createVenue(venueForm))
  ElMessage.success(venueForm.id ? '场地更新成功' : '场地新增成功')
  resetVenueForm(); await loadVenues()
}

async function submitSlot() {
  if (!slotForm.venueId) { ElMessage.warning('请选择场地'); return }
  if (!slotForm.slotDate) { ElMessage.warning('请选择日期'); return }
  if (!slotForm.timeRange.trim()) { ElMessage.warning('请填写时间段'); return }
  if (slotForm.remainingQuota > slotForm.totalQuota) { ElMessage.warning('剩余名额不能超过总名额'); return }
  const payload = { ...slotForm, venueId: Number(slotForm.venueId), remainingQuota: slotForm.remainingQuota ?? slotForm.totalQuota }
  await (slotForm.id ? updateVenueSlot(payload) : createVenueSlot(payload))
  ElMessage.success(slotForm.id ? '时间段更新成功' : '时间段新增成功')
  resetSlotForm(); await loadSlots()
}

async function submitActivity() {
  if (!activityForm.title.trim()) { ElMessage.warning('请填写活动标题'); return }
  if (!activityForm.venueId) { ElMessage.warning('请选择活动场地'); return }
  if (!activityTimeRange.value?.length) { ElMessage.warning('请选择活动时间'); return }
  const payload = {
    ...activityForm,
    venueId: Number(activityForm.venueId),
    startTime: activityTimeRange.value?.[0],
    endTime: activityTimeRange.value?.[1],
    checkinStartTime: checkinTimeRange.value?.[0],
    checkinEndTime: checkinTimeRange.value?.[1]
  }
  normalizeTeacherScope(payload)
  await (activityForm.id ? updateActivity(payload) : createActivity(payload))
  ElMessage.success(activityForm.id ? '活动更新成功' : '活动新增成功')
  resetActivityForm(); await loadActivities()
}

function normalizeTeacherScope(form) {
  if (!isAdmin.value) {
    form.scopeType = 'COLLEGE'
    form.scopeCollege = authStore.user?.college || form.scopeCollege || '计算机学院'
  }
}

function handleActivityVenueChange(id) {
  const venue = venueOptions.value.find((item) => item.id === id)
  if (venue) activityForm.capacity = venue.capacity
}

async function confirmDelete(name) {
  return ElMessageBox.confirm(`确定删除该${name}吗？此操作不可恢复。`, '删除确认', {
    type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消'
  }).then(() => true).catch(() => false)
}

async function removeUser(id) { if (!await confirmDelete('用户')) return; await deleteUser(id); ElMessage.success('用户已删除'); resetUserForm(); await loadUsers() }
async function removeNotice(id) { if (!await confirmDelete('公告')) return; await deleteNotice(id); ElMessage.success('公告已删除'); resetNoticeForm(); await loadNotices() }
async function removeVenue(id) { if (!await confirmDelete('场地')) return; await deleteVenue(id); ElMessage.success('场地已删除'); resetVenueForm(); await loadVenues() }
async function removeSlot(id) { if (!await confirmDelete('时间段')) return; await deleteVenueSlot(id); ElMessage.success('时间段已删除'); resetSlotForm(); await loadSlots() }
async function removeActivity(id) { if (!await confirmDelete('活动')) return; await deleteActivity(id); ElMessage.success('活动已删除'); resetActivityForm(); await loadActivities() }

function editUser(row) { Object.assign(userForm, { id: row.id, loginNo: row.username, realName: row.realName, college: row.college || '计算机学院', roleCode: row.roleCode, password: '', status: row.status }) }
function editNotice(row) { Object.assign(noticeForm, { id: row.id, title: row.title, category: row.category, content: row.content, scopeType: row.scopeType || 'COLLEGE', scopeCollege: row.scopeCollege || '计算机学院', status: row.status }) }
function editVenue(row) { Object.assign(venueForm, { id: row.id, name: row.name, location: row.location, imageUrl: row.imageUrl || '', capacity: row.capacity, status: row.status }) }
function editSlot(row) { Object.assign(slotForm, { id: row.id, venueId: String(row.venueId), slotDate: row.slotDate, timeRange: row.timeRange, totalQuota: row.totalQuota, remainingQuota: row.remainingQuota, status: row.status }) }
function editActivity(row) {
  Object.assign(activityForm, { id: row.id, title: row.title, venueId: row.venueId, coverUrl: row.coverUrl || '', capacity: row.capacity, content: row.content, scopeType: row.scopeType || 'COLLEGE', scopeCollege: row.scopeCollege || '计算机学院', status: row.status })
  activityTimeRange.value = row.startTime && row.endTime ? [row.startTime, row.endTime] : []
  checkinTimeRange.value = row.checkinStartTime && row.checkinEndTime ? [row.checkinStartTime, row.checkinEndTime] : []
}

function resetUserForm() { Object.assign(userForm, { id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 }) }
function resetNoticeForm() { Object.assign(noticeForm, { id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 }) }
function resetVenueForm() { Object.assign(venueForm, { id: null, name: '', location: '', imageUrl: '', capacity: 1, status: 1 }) }
function resetSlotForm() { Object.assign(slotForm, { id: null, venueId: '', slotDate: '', timeRange: '09:00-10:00', totalQuota: 1, remainingQuota: 1, status: 1 }) }
function resetActivityForm() {
  Object.assign(activityForm, { id: null, title: '', venueId: '', coverUrl: '', capacity: 30, content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })
  activityTimeRange.value = []
  checkinTimeRange.value = []
}

onMounted(() => {
  if (isAdmin.value) activeTab.value = 'user'
  loadAll()
})
</script>
