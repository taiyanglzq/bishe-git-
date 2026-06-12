/** 路由配置模块，负责维护前端页面访问路径与登录守卫逻辑。 */
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { canShowAuthExpiredTip, clearAuthStorage, getToken, isTokenExpired } from '../utils/auth'

const LoginView = () => import('../views/login/LoginView.vue')
const AdminLayout = () => import('../layout/AdminLayout.vue')
const DashboardView = () => import('../views/dashboard/DashboardView.vue')
const NoticeView = () => import('../views/notice/NoticeView.vue')
const DiscussionView = () => import('../views/discussion/DiscussionView.vue')
const CourseView = () => import('../views/course/CourseView.vue')
const ExamView = () => import('../views/exam/ExamView.vue')
const BookView = () => import('../views/book/BookView.vue')
const CampusMapView = () => import('../views/navigation/CampusMapView.vue')
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
        { path: 'discussion', component: DiscussionView, meta: { title: '讨论交流' } },
        { path: 'course', component: CourseView, meta: { title: '课程查询' } },
        { path: 'exam', component: ExamView, meta: { title: '考试安排' } },
        { path: 'book', component: BookView, meta: { title: '图书检索' } },
        { path: 'navigation', component: CampusMapView, meta: { title: '校园导航' } },
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
  const token = getToken()
  if (token && isTokenExpired(token)) {
    if (canShowAuthExpiredTip()) {
      ElMessage.error('登录已过期，请重新登录')
    }
    clearAuthStorage()
    if (to.path !== '/login') {
      return '/login'
    }
  }
  if (to.path !== '/login' && !getToken()) {
    return '/login'
  }
  if (to.path === '/login' && getToken()) {
    return '/dashboard'
  }
  return true
})

export default router
