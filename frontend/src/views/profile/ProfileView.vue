<!-- ???? ???????????????????????? -->
<template>
  <div>
    <div class="page-header">
      <h2>个人中心</h2>
      <p>查看个人信息，管理账号安全</p>
    </div>

    <div class="profile-layout">
      <!-- 左侧：个人信息卡 -->
      <div class="panel-card">
        <div class="profile-avatar-section">
          <div class="profile-avatar">{{ userInitial }}</div>
          <h3>{{ authStore.user?.realName || '未设置姓名' }}</h3>
          <el-tag class="role-badge" size="small" :type="roleTagType" effect="light">
            {{ roleName }}
          </el-tag>
        </div>
        <div style="padding: 0 20px 20px;">
          <div style="display: grid; gap: 10px;">
            <div style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border);">
              <span style="color: var(--text-muted); font-size: 13px;">账号</span>
              <span style="font-weight: 500; font-size: 13px;">{{ authStore.user?.username || '-' }}</span>
            </div>
            <div style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border);">
              <span style="color: var(--text-muted); font-size: 13px;">学号/工号</span>
              <span style="font-weight: 500; font-size: 13px;">{{ authStore.user?.studentNo || authStore.user?.username || '-' }}</span>
            </div>
            <div style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border);">
              <span style="color: var(--text-muted); font-size: 13px;">院系</span>
              <span style="font-weight: 500; font-size: 13px;">{{ authStore.user?.college || '-' }}</span>
            </div>
            <div style="display: flex; justify-content: space-between; padding: 8px 0;">
              <span style="color: var(--text-muted); font-size: 13px;">初始密码</span>
              <el-tag size="small" :type="authStore.user?.initialPassword ? 'warning' : 'success'">
                {{ authStore.user?.initialPassword ? '建议修改' : '已修改' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：修改密码 -->
      <div class="panel-card">
        <div class="panel-card-header">
          <h3>修改密码</h3>
        </div>
        <div class="panel-card-body">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" style="max-width: 400px;">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="form.oldPassword"
                type="password"
                show-password
                placeholder="输入当前密码"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="form.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（至少 6 位）"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                show-password
                placeholder="再次输入新密码"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-button type="primary" :loading="loading" @click="submit">
              <el-icon><Check /></el-icon> 保存新密码
            </el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Lock } from '@element-plus/icons-vue'
import { updatePassword } from '../../api/auth'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const userInitial = computed(() => {
  const name = authStore.user?.realName || authStore.user?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const roleName = computed(() => {
  const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[authStore.user?.roleCode] || '学生'
})

const roleTagType = computed(() => {
  if (authStore.user?.roleCode === 'ADMIN') return 'danger'
  if (authStore.user?.roleCode === 'TEACHER') return 'warning'
  return 'success'
})

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await updatePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码修改成功，请牢记新密码')
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    formRef.value.clearValidate()
    await authStore.fetchCurrentUser()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!authStore.user) authStore.fetchCurrentUser().catch(() => {})
})
</script>
