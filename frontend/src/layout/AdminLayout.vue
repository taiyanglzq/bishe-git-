<!-- AdminLayout ??????????AdminLayout?????????? -->
<template>
  <div class="app-shell">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-brand">
        <div class="sidebar-brand-icon">CA</div>
        <div class="sidebar-brand-text">
          <strong>智慧校园助手</strong>
          <small>Campus Assistant</small>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div class="sidebar-nav-section">主导航</div>
        <RouterLink
          v-for="item in mainMenus"
          :key="item.path"
          :to="item.path"
          class="sidebar-nav-item"
          :class="{ active: route.path === item.path }"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.title }}</span>
        </RouterLink>

        <template v-if="adminMenus.length">
          <div class="sidebar-nav-section">系统管理</div>
          <RouterLink
            v-for="item in adminMenus"
            :key="item.path"
            :to="item.path"
            class="sidebar-nav-item"
            :class="{ active: route.path === item.path }"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span class="nav-label">{{ item.title }}</span>
          </RouterLink>
        </template>
      </nav>
    </aside>

    <main class="main-panel" :class="{ expanded: sidebarCollapsed }">
      <header class="topbar">
        <div class="topbar-left">
          <button class="topbar-collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
            <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
          </button>
          <div class="topbar-breadcrumb">
            <el-icon><HomeFilled /></el-icon>
            <span>{{ currentTitle }}</span>
          </div>
        </div>

        <div class="topbar-right">
          <button class="topbar-notification-btn" @click="router.push('/notification')">
            <el-icon><Bell /></el-icon>
            <span v-if="unreadCount" class="topbar-notification-dot"></span>
          </button>

          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="topbar-user">
              <div class="topbar-user-avatar">{{ userInitial }}</div>
              <div class="topbar-user-info">
                <strong>{{ authStore.user?.realName || authStore.user?.username || '用户' }}</strong>
                <span>{{ roleName }}</span>
              </div>
              <el-icon style="color: var(--text-muted); font-size: 12px;"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <section class="content-area">
        <RouterView v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  HomeFilled, Bell, User, SwitchButton, ArrowDown, Fold, Expand,
  DataBoard, ChatLineSquare, OfficeBuilding, Calendar,
  Star, MagicStick, Setting, Document, Tickets, Reading, AlarmClock, Collection, MapLocation
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const sidebarCollapsed = ref(false)
const unreadCount = ref(0)

const currentTitle = computed(() => route.meta.title || '智慧校园助手')
const roleName = computed(() => ({ ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }[authStore.user?.roleCode] || '学生'))
const userInitial = computed(() => (authStore.user?.realName || authStore.user?.username || 'U').charAt(0).toUpperCase())

const allMenus = [
  { path: '/dashboard', title: '首页工作台', icon: DataBoard, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/notice', title: '校园公告', icon: ChatLineSquare, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/venue', title: '场地资源', icon: OfficeBuilding, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/booking', title: '场地预约', icon: Calendar, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/activity', title: '活动签到', icon: Star, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/course', title: '课程查询', icon: Reading, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/exam', title: '考试安排', icon: AlarmClock, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/book', title: '图书检索', icon: Collection, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/navigation', title: '校园导航', icon: MapLocation, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/discussion', title: '讨论交流', icon: Tickets, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/recommendation', title: '个性化推荐', icon: MagicStick, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/notification', title: '通知中心', icon: Bell, roles: ['STUDENT', 'TEACHER', 'ADMIN'], group: 'main' },
  { path: '/log', title: '日志审计', icon: Document, roles: ['ADMIN'], group: 'admin' },
  { path: '/system', title: '系统管理', icon: Setting, roles: ['TEACHER', 'ADMIN'], group: 'admin' }
]

const visibleMenus = computed(() => {
  const role = authStore.user?.roleCode || 'STUDENT'
  return allMenus.filter((item) => item.roles.includes(role))
})
const mainMenus = computed(() => visibleMenus.value.filter((item) => item.group === 'main'))
const adminMenus = computed(() => visibleMenus.value.filter((item) => item.group === 'admin'))

function handleUserCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'logout') {
    authStore.logout()
    router.replace('/login')
  }
}

onMounted(() => {
  if (!authStore.user) {
    authStore.fetchCurrentUser().catch(() => {})
  }
})
</script>
