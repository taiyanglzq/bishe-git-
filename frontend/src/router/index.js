import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const LoginView = () => import('../views/login/LoginView.vue')
const AdminLayout = () => import('../layout/AdminLayout.vue')
const DashboardView = () => import('../views/dashboard/DashboardView.vue')
const NoticeView = () => import('../views/notice/NoticeView.vue')
const VenueView = () => import('../views/venue/VenueView.vue')
const BookingView = () => import('../views/booking/BookingView.vue')
const ActivityView = () => import('../views/activity/ActivityView.vue')
const DiscussionView = () => import('../views/discussion/DiscussionView.vue')
const RecommendationView = () => import('../views/recommendation/RecommendationView.vue')
const ProfileView = () => import('../views/profile/ProfileView.vue')
const NotificationView = () => import('../views/notification/NotificationView.vue')
const LogView = () => import('../views/log/LogView.vue')
const SystemView = () => import('../views/system/SystemView.vue')

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
        { path: 'discussion', component: DiscussionView, meta: { title: '讨论交流' } },
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
