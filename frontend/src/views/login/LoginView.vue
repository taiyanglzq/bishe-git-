<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="login-hero">
        <div class="login-hero-badge">Campus Assistant</div>
        <h1>个性化<br />智慧校园助手</h1>
        <p>整合公告、预约、活动、签到、推荐与统计分析能力，为学生、教师和管理员提供一站式校园服务。</p>
        <div class="login-hero-features">
          <div class="login-hero-feature">
            <el-icon><Calendar /></el-icon> 场地预约
          </div>
          <div class="login-hero-feature">
            <el-icon><Star /></el-icon> 活动签到
          </div>
          <div class="login-hero-feature">
            <el-icon><MagicStick /></el-icon> 智能推荐
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-panel">
        <h2>账号登录</h2>
        <p class="login-form-hint">学生账号默认为学号，初始密码为身份证后 6 位</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              placeholder="例如 23050539414"
              size="large"
              clearable
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="默认身份证后 6 位"
              size="large"
              :prefix-icon="Lock"
            />
          </el-form-item>
          <div class="login-form-options">
            <el-checkbox v-model="rememberAccount">记住账号</el-checkbox>
          </div>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-submit-btn"
            @click="submit"
          >
            {{ loading ? '登录中…' : '登录系统' }}
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Calendar, Star, MagicStick } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref(null)
const rememberAccount = ref(false)

const REMEMBER_KEY = 'campus_remembered_account'

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, message: '账号长度至少 3 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ]
}

async function submit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const user = await authStore.login(form)
    if (rememberAccount.value) {
      localStorage.setItem(REMEMBER_KEY, form.username)
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
    if (user.initialPassword) {
      ElMessage.warning('当前仍为初始密码，建议登录后尽快修改')
    }
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const remembered = localStorage.getItem(REMEMBER_KEY)
  if (remembered) {
    form.username = remembered
    rememberAccount.value = true
  } else {
    form.username = '23050539414'
  }
  form.password = '123456'
})
</script>
