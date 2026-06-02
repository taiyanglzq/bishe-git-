<template>
  <div class="login-page">
    <section class="login-hero">
      <p class="eyebrow">Personalized Campus Assistant</p>
      <h1>个性化智慧校园助手</h1>
      <p>面向学生、教师和管理员，整合公告、预约、活动、签到、推荐和统计能力。</p>
    </section>

    <el-card class="login-card" shadow="never">
      <h2>账号登录</h2>
      <p class="hint">学生账号默认为学号，初始密码为身份证后 6 位。</p>
      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="例如 23050539414" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="默认身份证后 6 位" />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="login-button" @click="submit">
          登录系统
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
