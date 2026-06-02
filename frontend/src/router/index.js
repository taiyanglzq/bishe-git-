import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'
import LoginView from '../views/login/LoginView.vue'
import AdminLayout from '../layout/AdminLayout.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import NoticeView from '../views/notice/NoticeView.vue'
import VenueView from '../views/venue/VenueView.vue'
import BookingView from '../views/booking/BookingView.vue'
import ActivityView from '../views/activity/ActivityView.vue'
import RecommendationView from '../views/recommendation/RecommendationView.vue'
import ProfileView from '../views/profile/ProfileView.vue'
import NotificationView from '../views/notification/NotificationView.vue'
import LogView from '../views/log/LogView.vue'
import SystemView from '../views/system/SystemView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: DashboardView, meta: { title: '首页工作台' } },
        { path: 'notice', component: NoticeView, meta: { title: '校园公告' } },
        { path: 'venue', component: VenueView, meta: { title: '场地资源' } },
        { path: 'booking', component: BookingView, meta: { title: '场地预约' } },
        { path: 'activity', component: ActivityView, meta: { title: '活动与签到' } },
        { path: 'recommendation', component: RecommendationView, meta: { title: '个性化推荐' } },
        { path: 'profile', component: ProfileView, meta: { title: '个人中心' } },
        { path: 'notification', component: NotificationView, meta: { title: '通知中心' } },
        { path: 'log', component: LogView, meta: { title: '日志审计' } },
        { path: 'system', component: SystemView, meta: { title: '系统管理' } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !getToken()) {
    return '/login'
  }
  if (to.path === '/login' && getToken()) {
    return '/dashboard'
  }
  return true
})

export default router
