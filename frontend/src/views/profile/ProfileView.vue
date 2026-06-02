<template>
  <div class="profile-grid">
    <el-card shadow="never">
      <template #header>当前登录信息</template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="账号">{{ authStore.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ authStore.user?.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ authStore.user?.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ authStore.user?.roleCode }}</el-descriptions-item>
        <el-descriptions-item label="初始密码">
          {{ authStore.user?.initialPassword ? '是，建议修改' : '否' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never">
      <template #header>修改密码</template>
      <el-form :model="form" label-position="top">
        <el-form-item label="旧密码">
          <el-input v-model="form.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password />
        </el-form-item>
        <el-button type="primary" @click="submit">保存新密码</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePassword } from '../../api/auth'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const form = reactive({
  oldPassword: '',
  newPassword: ''
})

async function submit() {
  await updatePassword(form)
  ElMessage.success('密码修改成功，请牢记新密码')
  form.oldPassword = ''
  form.newPassword = ''
  await authStore.fetchCurrentUser()
}

onMounted(() => {
  if (!authStore.user) {
    authStore.fetchCurrentUser().catch(() => {})
  }
})
</script>
