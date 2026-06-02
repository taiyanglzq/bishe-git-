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

        <el-form :model="form" label-position="top" @keyup.enter="submit">
          <el-form-item label="账号">
            <el-input
              v-model="form.username"
              placeholder="例如 23050539414"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="默认身份证后 6 位"
              size="large"
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-submit-btn"
            @click="submit"
          >
            登录系统
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Calendar, Star, MagicStick } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: '23050539414',
  password: '123456'
})

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const user = await authStore.login(form)
    if (user.initialPassword) {
      ElMessage.warning('当前仍为初始密码，建议登录后尽快修改')
    }
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>
