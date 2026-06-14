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
              <el-table-column label="审核状态" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.status === 0" size="small" type="warning">待审核</el-tag>
                  <el-tag v-else-if="row.status === 1" size="small" type="success">已通过</el-tag>
                  <el-tag v-else-if="row.status === 2" size="small" type="danger">已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="scopeType" label="范围" width="90" />
              <el-table-column prop="scopeCollege" label="院系" width="140" />
              <el-table-column label="操作" min-width="200">
                <template #default="{ row }">
                  <template v-if="isAdmin && row.status === 0">
                    <el-button size="small" type="success" @click="handleApprove(row.id)">通过</el-button>
                    <el-button size="small" type="danger" @click="handleReject(row.id)">驳回</el-button>
                  </template>
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

      <el-tab-pane v-if="isAdmin" name="book">
        <template #label><span class="tab-label"><el-icon><Collection /></el-icon> 图书管理</span></template>
        <el-tabs v-model="bookSubTab">
          <el-tab-pane name="book-list">
            <template #label><span class="tab-label">图书列表</span></template>
            <div class="mgmt-form-row">
              <image-uploader label="封面图片" :url="bookForm.coverUrl" @uploaded="bookForm.coverUrl = $event" />
              <el-input v-model="bookForm.title" placeholder="书名" style="width: 200px;" />
              <el-input v-model="bookForm.author" placeholder="作者" style="width: 140px;" />
              <el-input v-model="bookForm.isbn" placeholder="ISBN" style="width: 160px;" />
              <el-input v-model="bookForm.publisher" placeholder="出版社" style="width: 160px;" />
              <el-input v-model="bookForm.publishYear" placeholder="出版年份" style="width: 110px;" />
              <el-select v-model="bookForm.category" placeholder="分类" style="width: 130px;">
                <el-option label="计算机科学" value="计算机科学" />
                <el-option label="数学" value="数学" />
                <el-option label="外语" value="外语" />
                <el-option label="文学" value="文学" />
              </el-select>
              <el-input v-model="bookForm.location" placeholder="馆藏位置" style="width: 180px;" />
              <el-input-number v-model="bookForm.totalCount" :min="1" style="width: 100px;" />
              <el-input v-model="bookForm.description" placeholder="图书简介" style="width: 280px;" />
              <el-button type="primary" @click="submitBook">{{ bookForm.id ? '更新' : '新增' }}</el-button>
              <el-button @click="resetBookForm">清空</el-button>
              <el-button @click="loadBooks">刷新</el-button>
            </div>
            <div class="panel-card" style="margin-top: 12px;">
              <div class="panel-card-body" style="padding: 0;">
                <el-table :data="books" border style="border: none;">
                  <el-table-column label="封面" width="80">
                    <template #default="{ row }">
                      <img v-if="row.coverUrl" :src="assetUrl(row.coverUrl)" class="table-thumb" alt="封面" />
                      <span v-else class="table-no-thumb">无</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="title" label="书名" min-width="150" show-overflow-tooltip />
                  <el-table-column prop="author" label="作者" width="100" />
                  <el-table-column prop="category" label="分类" width="110" />
                  <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
                  <el-table-column label="库存" width="100">
                    <template #default="{ row }">{{ row.availableCount || 0 }}/{{ row.totalCount || 0 }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button size="small" text type="primary" @click="editBook(row)">编辑</el-button>
                      <el-button size="small" text type="danger" @click="removeBook(row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination
                  class="mgmt-pagination"
                  layout="prev, pager, next, total"
                  :current-page="bookPage.current"
                  :page-size="PAGE_SIZE"
                  :total="bookPage.total"
                  @current-change="changeBookPage"
                />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane name="borrow-record">
            <template #label><span class="tab-label">借阅记录</span></template>
            <div class="mgmt-form-row">
              <el-input v-model="borrowKeyword" placeholder="搜索书名、作者或ISBN..." clearable style="width: 300px;" :prefix-icon="Search" @keyup.enter="searchBorrows" />
              <el-button type="primary" :icon="Search" @click="searchBorrows">搜索</el-button>
            </div>
            <div class="panel-card" style="margin-top: 12px;">
              <div class="panel-card-body" style="padding: 0;">
                <el-table :data="borrowRecords" border style="border: none;">
                  <el-table-column prop="bookTitle" label="书名" min-width="160" show-overflow-tooltip />
                  <el-table-column prop="bookAuthor" label="作者" width="100" />
                  <el-table-column prop="userRealName" label="借阅人" width="100" />
                  <el-table-column prop="userStudentNo" label="学号" width="140" />
                  <el-table-column label="借阅时间" width="160">
                    <template #default="{ row }">{{ formatTime(row.borrowTime) }}</template>
                  </el-table-column>
                  <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag v-if="row.status === 'BORROWED'" size="small" type="warning">借阅中</el-tag>
                      <el-tag v-else size="small" type="info">已归还</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination
                  class="mgmt-pagination"
                  layout="prev, pager, next, total"
                  :current-page="borrowPageInfo.current"
                  :page-size="PAGE_SIZE"
                  :total="borrowPageInfo.total"
                  @current-change="changeBorrowPage"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>
      <el-tab-pane v-if="isAdmin" name="exam">
        <template #label><span class="tab-label"><el-icon><AlarmClock /></el-icon> 考试管理</span></template>
        <el-tabs v-model="examSubTab">
          <el-tab-pane name="exam-list">
            <template #label><span class="tab-label">考试列表</span></template>
            <div class="mgmt-form-row">
              <el-input v-model="examForm.courseName" placeholder="考试科目" style="width: 200px;" />
              <el-input v-model="examForm.examDate" placeholder="考试日期 (2026-06-22)" style="width: 160px;" />
              <el-input v-model="examForm.startTime" placeholder="开始时间 (09:00)" style="width: 130px;" />
              <el-input v-model="examForm.endTime" placeholder="结束时间 (11:00)" style="width: 130px;" />
              <el-input v-model="examForm.location" placeholder="考试地点" style="width: 160px;" />
              <el-input v-model="examForm.invigilator" placeholder="监考老师（逗号分隔）" style="width: 200px;" />
              <el-select v-model="examForm.examType" placeholder="考试类型" style="width: 120px;">
                <el-option label="期末考试" value="期末考试" />
                <el-option label="期中考试" value="期中考试" />
                <el-option label="补考" value="补考" />
              </el-select>
              <el-select v-model="examForm.college" placeholder="院系" style="width: 130px;">
                <el-option label="计算机学院" value="计算机学院" />
                <el-option label="外国语学院" value="外国语学院" />
                <el-option label="数学学院" value="数学学院" />
              </el-select>
              <el-button type="primary" @click="submitExam">{{ examForm.id ? '更新' : '新增' }}</el-button>
              <el-button @click="resetExamForm">清空</el-button>
              <el-button @click="loadExams">刷新</el-button>
            </div>
            <div class="panel-card" style="margin-top: 12px;">
              <div class="panel-card-body" style="padding: 0;">
                <el-table :data="exams" border style="border: none;">
                  <el-table-column prop="courseName" label="考试科目" min-width="140" show-overflow-tooltip />
                  <el-table-column label="日期" width="110">
                    <template #default="{ row }">{{ row.examDate }}</template>
                  </el-table-column>
                  <el-table-column label="时间" width="160">
                    <template #default="{ row }">{{ row.startTime }}-{{ row.endTime }}</template>
                  </el-table-column>
                  <el-table-column prop="location" label="地点" width="120" />
                  <el-table-column prop="invigilator" label="监考老师" width="150" />
                  <el-table-column label="操作" min-width="200">
                    <template #default="{ row }">
                      <el-button size="small" text type="primary" @click="editExam(row)">编辑</el-button>
                      <el-button size="small" text type="danger" @click="removeExam(row.id)">删除</el-button>
                      <el-button size="small" text type="success" @click="showSeat(row)">座位</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination
                  class="mgmt-pagination"
                  layout="prev, pager, next, total"
                  :current-page="examPage.current"
                  :page-size="PAGE_SIZE"
                  :total="examPage.total"
                  @current-change="changeExamPage"
                />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane name="seat-manage">
            <template #label><span class="tab-label">座位管理</span></template>
            <div v-if="currentExamId" class="mgmt-form-row">
              <span style="font-size:14px;font-weight:600;">{{ currentExamName }} — 座位安排</span>
              <el-select v-model="seatMode" placeholder="生成模式" style="width: 160px;">
                <el-option label="按教室行列（A1,B1...）" value="CLASSROOM" />
                <el-option label="按学号排序" value="STUDENT_NO" />
                <el-option label="随机分配" value="RANDOM" />
              </el-select>
              <el-button type="primary" @click="handleGenerateSeats">生成预览</el-button>
              <el-button type="success" :disabled="!seats.length || seatsSaved" @click="handleSaveSeats">保存座位</el-button>
              <el-button :disabled="!seats.length || !seatsSaved" @click="exportSeats">导出座位表</el-button>
              <el-tag v-if="seats.length && !seatsSaved" size="small" type="warning">预览模式，尚未保存</el-tag>
              <el-tag v-if="seatsSaved" size="small" type="success">已保存</el-tag>
              <el-button @click="backToExamList">返回</el-button>
            </div>
            <div v-if="currentExamId" class="panel-card" style="margin-top: 12px;">
              <div class="panel-card-body" style="padding: 0;">
                <el-table :data="seats" border style="border: none;">
                  <el-table-column type="index" label="序号" width="60" />
                  <el-table-column prop="studentNo" label="学号" width="140" />
                  <el-table-column prop="studentName" label="姓名" width="100" />
                  <el-table-column prop="college" label="院系" width="120" />
                  <el-table-column label="座位号" width="120">
                    <template #default="{ row, $index }">
                      <el-input v-model="row.seatNo" size="small" style="width:90px;" @blur="handleUpdateSeat(row)" />
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div v-else class="empty-state">
              <p>请从考试列表中选择一场考试进行座位管理</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElButton, ElMessage, ElMessageBox, ElUpload } from 'element-plus'
import { AlarmClock, UserFilled, ChatLineSquare, Collection, Search } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { createUser, deleteUser, getUserPage, updateUser } from '../../api/auth'
import { createBook, deleteBook, getBookManagePage, getBorrowPage, updateBook } from '../../api/book'
import { createExam, deleteExam, getExamManagePage, getExamSeats, generateExamSeats, saveExamSeats, getExamSeatsExportUrl, updateExam, updateExamSeat } from '../../api/course'
import { approveNotice, createNotice, deleteNotice, getNoticeManagePage, rejectNotice, updateNotice } from '../../api/notice'
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
const books = ref([])

const PAGE_SIZE = 10
const userPage = reactive({ current: 1, total: 0 })
const noticePage = reactive({ current: 1, total: 0 })
const bookPage = reactive({ current: 1, total: 0 })
const borrowRecords = ref([])
const borrowPageInfo = reactive({ current: 1, total: 0 })
const borrowKeyword = ref('')
const bookSubTab = ref('book-list')

// 考试管理
const exams = ref([])
const examPage = reactive({ current: 1, total: 0 })
const examForm = reactive({ id: null, courseId: null, courseName: '', examDate: '', startTime: '', endTime: '', location: '', invigilator: '', examType: '期末考试', college: '计算机学院', status: 1 })
const examSubTab = ref('exam-list')
const currentExamId = ref(null)
const currentExamName = ref('')
const seats = ref([])
const seatMode = ref('CLASSROOM')
const seatsSaved = ref(false)

const userForm = reactive({ id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 })
const noticeForm = reactive({ id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 })
const bookForm = reactive({ id: null, title: '', author: '', isbn: '', publisher: '', publishYear: '', category: '计算机科学', location: '', totalCount: 1, availableCount: 1, description: '', coverUrl: '', status: 1 })

const loginNoPlaceholder = computed(() => userForm.roleCode === 'STUDENT' ? '学号' : userForm.roleCode === 'TEACHER' ? '工号' : '管理员账号')

function assetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return url
}

function loginNoLabel(roleCode) { return roleCode === 'STUDENT' ? '学号' : roleCode === 'TEACHER' ? '工号' : '账号' }
function roleLabel(roleCode) { return { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }[roleCode] || roleCode }


async function loadUsers() {
  try {
    const data = await getUserPage({ current: userPage.current, size: PAGE_SIZE })
    users.value = data.records || []
    userPage.total = data.total || 0
  } catch { users.value = [] }
}
async function loadNotices() {
  try {
    const data = await getNoticeManagePage({ current: noticePage.current, size: PAGE_SIZE })
    notices.value = data.records || []
    noticePage.total = data.total || 0
  } catch { notices.value = [] }
}
function changeUserPage(p) { userPage.current = p; loadUsers() }
function changeNoticePage(p) { noticePage.current = p; loadNotices() }
function changeBookPage(p) { bookPage.current = p; loadBooks() }
function changeBorrowPage(p) { borrowPageInfo.current = p; loadBorrows() }

async function loadAll() {
  if (!isAdmin.value && activeTab.value === 'user') activeTab.value = 'notice'
  const tasks = [loadNotices(), loadExams()]
  if (isAdmin.value) tasks.push(loadUsers(), loadBooks(), loadBorrows())
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
async function handleApprove(id) { await approveNotice(id); ElMessage.success('公告已通过审核'); await loadNotices() }
async function handleReject(id) { await rejectNotice(id); ElMessage.success('公告已驳回'); await loadNotices() }

function editUser(row) { Object.assign(userForm, { id: row.id, loginNo: row.username, realName: row.realName, college: row.college || '计算机学院', roleCode: row.roleCode, password: '', status: row.status }) }
function editNotice(row) { Object.assign(noticeForm, { id: row.id, title: row.title, category: row.category, content: row.content, scopeType: row.scopeType || 'COLLEGE', scopeCollege: row.scopeCollege || '计算机学院', status: row.status }) }
function resetUserForm() { Object.assign(userForm, { id: null, loginNo: '', realName: '', college: '计算机学院', roleCode: 'STUDENT', password: '123456', status: 1 }) }
function resetNoticeForm() { Object.assign(noticeForm, { id: null, title: '', category: '系统通知', content: '', scopeType: 'COLLEGE', scopeCollege: '计算机学院', status: 1 }) }

async function loadBooks() {
  try {
    const data = await getBookManagePage({ current: bookPage.current, size: PAGE_SIZE })
    books.value = data.records || []
    bookPage.total = data.total || 0
  } catch { books.value = [] }
}

async function submitBook() {
  if (!bookForm.title.trim()) { ElMessage.warning('请填写书名'); return }
  await (bookForm.id ? updateBook(bookForm) : createBook(bookForm))
  ElMessage.success(bookForm.id ? '图书更新成功' : '图书新增成功')
  resetBookForm(); await loadBooks()
}

async function removeBook(id) { if (!await confirmDelete('图书')) return; await deleteBook(id); ElMessage.success('图书已删除'); resetBookForm(); await loadBooks() }

function editBook(row) { Object.assign(bookForm, { id: row.id, title: row.title, author: row.author || '', isbn: row.isbn || '', publisher: row.publisher || '', publishYear: row.publishYear || '', category: row.category || '计算机科学', location: row.location || '', totalCount: row.totalCount || 1, availableCount: row.availableCount ?? 1, description: row.description || '', coverUrl: row.coverUrl || '', status: row.status ?? 1 }) }
function resetBookForm() { Object.assign(bookForm, { id: null, title: '', author: '', isbn: '', publisher: '', publishYear: '', category: '计算机科学', location: '', totalCount: 1, availableCount: 1, description: '', coverUrl: '', status: 1 }) }

function searchBorrows() { borrowPageInfo.current = 1; loadBorrows() }

async function loadBorrows() {
  try {
    const params = { current: borrowPageInfo.current, size: PAGE_SIZE }
    if (borrowKeyword.value) params.keyword = borrowKeyword.value
    const data = await getBorrowPage(params)
    borrowRecords.value = data.records || []
    borrowPageInfo.total = data.total || 0
  } catch { borrowRecords.value = [] }
}

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return time
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ===== 考试管理 =====
function changeExamPage(p) { examPage.current = p; loadExams() }

async function loadExams() {
  try {
    const data = await getExamManagePage({ current: examPage.current, size: PAGE_SIZE })
    exams.value = data.records || []
    examPage.total = data.total || 0
  } catch {
    exams.value = []
  }
}

async function submitExam() {
  if (!examForm.courseName.trim()) { ElMessage.warning('请填写考试科目'); return }
  await (examForm.id ? updateExam(examForm) : createExam(examForm))
  ElMessage.success(examForm.id ? '考试更新成功' : '考试新增成功')
  resetExamForm(); await loadExams()
}

async function removeExam(id) { if (!await confirmDelete('考试')) return; await deleteExam(id); ElMessage.success('考试已删除'); resetExamForm(); await loadExams() }

function editExam(row) {
  Object.assign(examForm, {
    id: row.id, courseId: row.courseId, courseName: row.courseName,
    examDate: row.examDate, startTime: row.startTime, endTime: row.endTime,
    location: row.location || '', invigilator: row.invigilator || '',
    examType: row.examType, college: row.college || '计算机学院', status: row.status ?? 1
  })
}
function resetExamForm() { Object.assign(examForm, { id: null, courseId: null, courseName: '', examDate: '', startTime: '', endTime: '', location: '', invigilator: '', examType: '期末考试', college: '计算机学院', status: 1 }) }

function showSeat(row) {
  currentExamId.value = row.id
  currentExamName.value = row.courseName
  loadSeats(row.id)
  examSubTab.value = 'seat-manage'
}


async function loadSeats(examId) {
  try {
    const data = await getExamSeats(examId)
    seats.value = Array.isArray(data) ? data : []
    seatsSaved.value = seats.value.length > 0
  } catch { seats.value = []; seatsSaved.value = false }
}

function backToExamList() { currentExamId.value = null; examSubTab.value = 'exam-list'; seatsSaved.value = false }

async function handleGenerateSeats() {
  if (!currentExamId.value) return
  try {
    const data = await generateExamSeats(currentExamId.value, { mode: seatMode.value })
    seats.value = Array.isArray(data) ? data : []
    seatsSaved.value = false
    ElMessage.success('座位生成预览')
  } catch { ElMessage.error('座位生成失败') }
}

async function handleSaveSeats() {
  if (!currentExamId.value || !seats.value.length) return
  try {
    await saveExamSeats(currentExamId.value, seats.value)
    seatsSaved.value = true
    ElMessage.success('座位保存成功')
  } catch { ElMessage.error('座位保存失败') }
}

async function handleUpdateSeat(row) {
  if (!row.id || !row.seatNo) return
  await updateExamSeat(row.id, row.seatNo)
}

function exportSeats() {
  if (!currentExamId.value) return
  const url = getExamSeatsExportUrl(currentExamId.value)
  window.open(url, '_blank')
}

onMounted(() => {
  if (isAdmin.value) activeTab.value = 'user'
  loadAll().catch(() => {})
})
</script>
