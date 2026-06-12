<!-- SystemView ??????????SystemView?????????? -->
<template>
  <div>
    <div class="page-header">
      <h2>系统管理</h2>
      <p>管理用户和校园公告</p>
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
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElButton, ElMessage, ElMessageBox, ElUpload } from 'element-plus'
import { UserFilled, ChatLineSquare, OfficeBuilding, Timer, Star } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { createUser, deleteUser, getUserPage, updateUser } from '../../api/auth'
import { createNotice, deleteNotice, getNoticeManagePage, updateNotice } from '../../api/notice'
import { uploadImage } from '../../api/upload'

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
const checkinTimeRange = ref([])

const PAGE_SIZE = 10
const userPage = reactive({ current: 1, total: 0 })
const noticePage = reactive({ current: 1, total: 0 })

const userForm = reactive({ id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 })
const noticeForm = reactive({ id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })

const loginNoPlaceholder = computed(() => userForm.roleCode === 'STUDENT' ? '学号' : userForm.roleCode === 'TEACHER' ? '工号' : '管理员账号')

function assetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return url
}

function loginNoLabel(roleCode) { return roleCode === 'STUDENT' ? '学号' : roleCode === 'TEACHER' ? '工号' : '账号' }
function roleLabel(roleCode) { return { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }[roleCode] || roleCode }


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
function changeUserPage(p) { userPage.current = p; loadUsers() }
function changeNoticePage(p) { noticePage.current = p; loadNotices() }

async function loadAll() {
  if (!isAdmin.value && activeTab.value === 'user') activeTab.value = 'notice'
  const tasks = [loadNotices()]
  if (isAdmin.value) tasks.push(loadUsers())
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

function normalizeTeacherScope(form) {
  if (!isAdmin.value) {
    form.scopeType = 'COLLEGE'
    form.scopeCollege = authStore.user?.college || form.scopeCollege || '计算机学院'
  }
}

async function confirmDelete(name) {
  return ElMessageBox.confirm(`确定删除该${name}吗？此操作不可恢复。`, '删除确认', {
    type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消'
  }).then(() => true).catch(() => false)
}

async function removeUser(id) { if (!await confirmDelete('用户')) return; await deleteUser(id); ElMessage.success('用户已删除'); resetUserForm(); await loadUsers() }
async function removeNotice(id) { if (!await confirmDelete('公告')) return; await deleteNotice(id); ElMessage.success('公告已删除'); resetNoticeForm(); await loadNotices() }

function editUser(row) { Object.assign(userForm, { id: row.id, loginNo: row.username, realName: row.realName, college: row.college || '计算机学院', roleCode: row.roleCode, password: '', status: row.status }) }
function editNotice(row) { Object.assign(noticeForm, { id: row.id, title: row.title, category: row.category, content: row.content, scopeType: row.scopeType || 'COLLEGE', scopeCollege: row.scopeCollege || '计算机学院', status: row.status }) }
function resetUserForm() { Object.assign(userForm, { id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 }) }
function resetNoticeForm() { Object.assign(noticeForm, { id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 }) }
onMounted(() => {
  if (isAdmin.value) activeTab.value = 'user'
  loadAll()
})
</script>
