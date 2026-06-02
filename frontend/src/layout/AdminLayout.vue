<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">CA</span>
        <div>
          <strong>智慧校园助手</strong>
          <small>Campus Assistant</small>
        </div>
      </div>
      <nav>
        <RouterLink v-for="item in visibleMenus" :key="item.path" :to="item.path">
          {{ item.title }}
        </RouterLink>
      </nav>
    </aside>

    <main class="main-panel">
      <header class="topbar">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>当前用户：{{ authStore.user?.realName || authStore.user?.username || '未加载' }} · {{ roleName }}</p>
        </div>
        <div class="top-actions">
          <el-button @click="router.push('/profile')">个人中心</el-button>
          <el-button @click="logout">退出登录</el-button>
        </div>
      </header>
      <section class="content-card">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menus = [
  { path: '/dashboard', title: '首页工作台', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { path: '/notice', title: '校园公告', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { path: '/venue', title: '场地资源', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { path: '/booking', title: '场地预约', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { path: '/activity', title: '活动签到', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { path: '/recommendation', title: '个性化推荐', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { path: '/notification', title: '通知中心', roles: ['STUDENT', 'TEACHER', 'ADMIN'] },
  { path: '/log', title: '日志审计', roles: ['ADMIN'] },
  { path: '/system', title: '系统管理', roles: ['TEACHER', 'ADMIN'] }
]

const currentTitle = computed(() => route.meta.title || '智慧校园助手')
const roleName = computed(() => {
  const roleCode = authStore.user?.roleCode
  if (roleCode === 'ADMIN') return '管理员'
  if (roleCode === 'TEACHER') return '教师'
  return '学生'
})
const visibleMenus = computed(() => {
  const roleCode = authStore.user?.roleCode || 'STUDENT'
  return menus.filter((item) => item.roles.includes(roleCode))
})

function logout() {
  authStore.logout()
  router.replace('/login')
}

onMounted(() => {
  if (!authStore.user) {
    authStore.fetchCurrentUser().catch(() => {})
  }
})
</script>
