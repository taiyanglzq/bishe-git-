<!-- ?? ?????????????????????? -->
<template>
  <div>
    <div class="page-header">
      <h2>校园公告</h2>
      <p>查看学校发布的最新通知与公告信息</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索公告标题..."
        clearable
        style="width: 260px;"
        :prefix-icon="Search"
      />
      <el-select v-model="category" placeholder="分类筛选" clearable style="width: 140px;">
        <el-option label="系统通知" value="系统通知" />
        <el-option label="学术活动" value="学术活动" />
        <el-option label="校园新闻" value="校园新闻" />
        <el-option label="考试信息" value="考试信息" />
      </el-select>
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">
        共 {{ filteredRows.length }} 条公告
      </span>
    </div>

    <div v-if="filteredRows.length" class="info-grid">
      <article v-for="item in pagedRows" :key="item.id" class="info-card clickable-card" @click="openDetail(item.id)">
        <div class="info-card-header">
          <span class="info-card-title">{{ item.title }}</span>
          <el-tag size="small" :type="categoryTagType(item.category)">
            {{ item.category || '公告' }}
          </el-tag>
        </div>
        <p v-if="item.content" class="info-card-desc">{{ item.content }}</p>
        <div class="info-card-meta">
          <span class="info-card-meta-item">
            <el-icon><View /></el-icon> {{ item.viewCount || 0 }} 浏览
          </span>
          <span class="info-card-meta-item">
            <el-icon><Clock /></el-icon> {{ formatDateTime(item.createTime) }}
          </span>
        </div>
      </article>
    </div>

    <el-pagination
      v-if="filteredRows.length"
      class="notice-pagination"
      layout="prev, pager, next, total"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :total="filteredRows.length"
      @current-change="(p) => currentPage = p"
    />

    <div v-else class="empty-state">
      <div class="empty-state-icon">
        <el-icon><Document /></el-icon>
      </div>
      <p>暂无公告信息</p>
    </div>

    <el-dialog v-model="detailVisible" title="公告详情" width="720px" destroy-on-close>
      <div v-if="detail" class="notice-detail">
        <div class="notice-detail-header">
          <el-tag size="small" :type="categoryTagType(detail.category)">
            {{ detail.category || '公告' }}
          </el-tag>
          <h3>{{ detail.title }}</h3>
          <p>
            {{ formatDateTime(detail.createTime) }}
            <span> · </span>
            {{ detail.viewCount || 0 }} 次浏览
          </p>
        </div>

        <div class="notice-detail-content">{{ detail.content }}</div>

        <div class="notice-comment-section">
          <div class="section-title">
            <h3>评论交流</h3>
            <span>{{ detail.comments?.length || 0 }} 条评论</span>
          </div>

          <div class="comment-input-row">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入评论内容，学生、教师和管理员均可参与讨论"
            />
            <el-button type="primary" :disabled="!commentContent.trim()" @click="submitComment">
              发表评论
            </el-button>
          </div>

          <div v-if="detail.comments?.length" class="comment-list">
            <article v-for="comment in detail.comments" :key="comment.id" class="comment-card">
              <div class="comment-avatar">{{ userInitial(comment.realName) }}</div>
              <div class="comment-body">
                <div class="comment-meta">
                  <strong>{{ comment.realName }}</strong>
                  <el-tag size="small" effect="plain">{{ roleLabel(comment.roleCode) }}</el-tag>
                  <span>{{ comment.college || '暂无院系' }}</span>
                  <em>{{ formatDateTime(comment.createTime) }}</em>
                </div>
                <p>{{ comment.content }}</p>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无评论，快来发表第一条评论" :image-size="60" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, View, Clock, Document } from '@element-plus/icons-vue'
import { createNoticeComment, getNoticeDetail, getNoticePage } from '../../api/notice'

const rows = ref([])
const keyword = ref('')
const category = ref('')
const detailVisible = ref(false)
const detail = ref(null)
const commentContent = ref('')

const PAGE_SIZE = 9
const currentPage = ref(1)

const filteredRows = computed(() => {
  let data = rows.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    data = data.filter((row) => row.title && row.title.toLowerCase().includes(kw))
  }
  if (category.value) {
    data = data.filter((row) => row.category === category.value)
  }
  return data
})

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredRows.value.slice(start, start + PAGE_SIZE)
})

watch([keyword, category], () => { currentPage.value = 1 })

function categoryTagType(cat) {
  if (cat === '系统通知') return 'danger'
  if (cat === '学术活动') return 'success'
  if (cat === '考试信息') return 'warning'
  if (cat === '校园新闻') return ''
  return 'info'
}

async function openDetail(id) {
  detail.value = await getNoticeDetail(id)
  commentContent.value = ''
  detailVisible.value = true
  await load()
}

async function submitComment() {
  if (!commentContent.value.trim() || !detail.value?.id) return
  await createNoticeComment({
    noticeId: detail.value.id,
    content: commentContent.value.trim()
  })
  ElMessage.success('评论发表成功')
  commentContent.value = ''
  detail.value = await getNoticeDetail(detail.value.id)
}

function roleLabel(roleCode) {
  const map = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return map[roleCode] || '用户'
}

function userInitial(name) {
  return (name || '用').charAt(0)
}

function formatDateTime(value) {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return text
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}年${pad(date.getMonth() + 1)}月${pad(date.getDate())}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function load() {
  const data = await getNoticePage({ current: 1, size: 50 })
  rows.value = data.records || []
}

onMounted(load)
</script>
