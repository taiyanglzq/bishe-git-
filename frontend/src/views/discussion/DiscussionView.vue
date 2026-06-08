<!-- ???? ???????????????????????? -->
<template>
  <div>
    <div class="page-header">
      <h2>讨论交流</h2>
      <p>面向学生与教师的校园互动空间，支持发帖、配图、评论、点赞和内容管理</p>
    </div>

    <div class="discussion-layout">
      <section class="panel-card discussion-editor">
        <div class="panel-card-header">
          <h3>发布帖子</h3>
          <el-tag type="success" size="small">校园互动</el-tag>
        </div>
        <div class="panel-card-body">
          <div class="discussion-post-compose">
            <div class="discussion-image-upload">
              <img v-if="postForm.imageUrl" :src="assetUrl(postForm.imageUrl)" class="discussion-image-preview" alt="帖子图片" />
              <div v-else class="discussion-image-placeholder">帖子配图</div>
              <el-upload :show-file-list="false" :before-upload="uploadPostImage" accept="image/png,image/jpeg,image/webp">
                <el-button size="small">{{ postForm.imageUrl ? '重新上传' : '上传图片' }}</el-button>
              </el-upload>
            </div>
            <div>
              <el-input v-model="postForm.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
              <el-input
                v-model="postForm.content"
                type="textarea"
                :rows="5"
                maxlength="2000"
                show-word-limit
                placeholder="分享校园生活、课程经验、活动感受或问题求助"
                class="mt-12"
              />
            </div>
          </div>
          <el-button type="primary" class="mt-12" :disabled="!canSubmitPost" @click="submitPost">发布帖子</el-button>
          <el-button class="mt-12" @click="resetPostForm">清空</el-button>
        </div>
      </section>

      <section class="panel-card discussion-search">
        <div class="panel-card-header">
          <h3>帖子检索</h3>
        </div>
        <div class="panel-card-body">
          <el-input v-model="keyword" placeholder="按标题搜索" clearable @keyup.enter="search" />
          <el-button class="mt-12" @click="search">搜索/刷新</el-button>
        </div>
      </section>
    </div>

    <div v-if="posts.length" class="discussion-list">
      <article v-for="post in posts" :key="post.id" class="discussion-card discussion-card-clickable" @click="openPostDetail(post)">
        <div class="discussion-card-header">
          <div>
            <div class="discussion-badges">
              <el-tag v-if="isEnabled(post.pinned)" type="danger" size="small">置顶</el-tag>
              <el-tag v-if="isEnabled(post.featured)" type="warning" size="small">精华</el-tag>
              <el-tag size="small" effect="plain">{{ roleLabel(post.authorRole) }}</el-tag>
            </div>
            <h3>{{ post.title }}</h3>
            <p>{{ post.authorName }} · {{ post.college || '暂无院系' }} · {{ formatDateTime(post.createTime) }}</p>
          </div>
          <div class="discussion-actions">
            <el-button size="small" :type="post.liked ? 'primary' : 'default'" @click.stop="like(post)">
              {{ post.liked ? '已点赞' : '点赞' }} {{ post.likeCount || 0 }}
            </el-button>
            <el-button v-if="post.canDelete" size="small" type="danger" plain @click.stop="removePost(post.id)">删除</el-button>
          </div>
        </div>

        <div class="discussion-list-preview">
          <div>
            <p class="discussion-summary">{{ postSummary(post.content) }}</p>
            <div class="discussion-list-meta">
              <span>评论 {{ post.commentCount || 0 }}</span>
              <span>点赞 {{ post.likeCount || 0 }}</span>
              <span>点击查看详情和评论</span>
            </div>
          </div>
          <img v-if="post.imageUrl" :src="assetUrl(post.imageUrl)" class="discussion-list-thumb" alt="帖子图片" />
        </div>

        <div v-if="post.canManage" class="discussion-manage">
          <el-button size="small" @click.stop="setPin(post)">{{ isEnabled(post.pinned) ? '取消置顶' : '置顶' }}</el-button>
          <el-button size="small" @click.stop="setFeature(post)">{{ isEnabled(post.featured) ? '取消精华' : '设为精华' }}</el-button>
          <el-button v-if="isAdmin" size="small" type="warning" @click.stop="banUser(post.authorId)">封禁作者发帖</el-button>
        </div>
      </article>
    </div>

    <el-pagination
      v-if="posts.length"
      class="discussion-pagination"
      layout="prev, pager, next, total"
      :current-page="page.current"
      :page-size="PAGE_SIZE"
      :total="page.total"
      @current-change="changePage"
    />

    <div v-else class="friendly-empty">
      <div class="friendly-empty-icon">论</div>
      <h3>还没有讨论内容</h3>
      <p>可以发布第一条帖子，分享校园生活或提出你关心的问题。</p>
    </div>

    <el-dialog v-model="detailVisible" width="760px" class="discussion-detail-dialog" destroy-on-close>
      <template #header>
        <div v-if="selectedPost" class="discussion-detail-header">
          <div class="discussion-badges">
            <el-tag v-if="isEnabled(selectedPost.pinned)" type="danger" size="small">置顶</el-tag>
            <el-tag v-if="isEnabled(selectedPost.featured)" type="warning" size="small">精华</el-tag>
            <el-tag size="small" effect="plain">{{ roleLabel(selectedPost.authorRole) }}</el-tag>
          </div>
          <h2>{{ selectedPost.title }}</h2>
          <p>{{ selectedPost.authorName }} · {{ selectedPost.college || '暂无院系' }} · {{ formatDateTime(selectedPost.createTime) }}</p>
        </div>
      </template>

      <div v-if="selectedPost" class="discussion-detail-body">
        <div v-if="selectedPost.imageUrl" class="discussion-post-image-wrap">
          <img :src="assetUrl(selectedPost.imageUrl)" class="discussion-post-image" alt="帖子图片" />
        </div>
        <p class="discussion-content">{{ selectedPost.content }}</p>

        <div class="discussion-detail-actions">
          <el-button size="small" :type="selectedPost.liked ? 'primary' : 'default'" @click="like(selectedPost)">
            {{ selectedPost.liked ? '已点赞' : '点赞' }} {{ selectedPost.likeCount || 0 }}
          </el-button>
          <el-button v-if="selectedPost.canDelete" size="small" type="danger" plain @click="removePost(selectedPost.id)">删除帖子</el-button>
        </div>

        <div v-if="selectedPost.canManage" class="discussion-manage">
          <el-button size="small" @click="setPin(selectedPost)">{{ isEnabled(selectedPost.pinned) ? '取消置顶' : '置顶' }}</el-button>
          <el-button size="small" @click="setFeature(selectedPost)">{{ isEnabled(selectedPost.featured) ? '取消精华' : '设为精华' }}</el-button>
          <el-button v-if="isAdmin" size="small" type="warning" @click="banUser(selectedPost.authorId)">封禁作者发帖</el-button>
        </div>

        <div class="discussion-comments">
          <div class="discussion-comment-title">全部评论 {{ selectedPost.commentCount || 0 }}</div>
          <div v-if="selectedPost.comments?.length" class="discussion-comment-list">
            <div v-for="comment in selectedPost.comments" :key="comment.id" class="discussion-comment">
              <div>
                <strong>{{ comment.realName }}</strong>
                <span>{{ roleLabel(comment.roleCode) }} · {{ formatDateTime(comment.createTime) }}</span>
                <p>{{ comment.content }}</p>
              </div>
              <div class="discussion-comment-actions">
                <el-button size="small" text :type="comment.liked ? 'primary' : 'default'" @click="likeComment(comment.id)">
                  {{ comment.liked ? '已点赞' : '点赞' }} {{ comment.likeCount || 0 }}
                </el-button>
                <el-button v-if="comment.canDelete" size="small" text type="danger" @click="removeComment(comment.id)">删除</el-button>
              </div>
            </div>
          </div>
          <div v-else class="discussion-comment-empty">还没有评论，可以发表第一条看法。</div>
          <div class="discussion-comment-input">
            <el-input v-model="detailComment" placeholder="写下你的评论，参与校园讨论" maxlength="500" />
            <el-button type="primary" :disabled="!detailComment.trim()" @click="submitComment(selectedPost.id)">发表评论</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { previewModeration } from '../../api/ai'
import { useAuthStore } from '../../stores/auth'
import { uploadImage } from '../../api/venue'
import {
  banDiscussionUser,
  createDiscussionComment,
  createDiscussionPost,
  deleteDiscussionComment,
  deleteDiscussionPost,
  getDiscussionPage,
  toggleDiscussionCommentLike,
  toggleDiscussionLike,
  updateDiscussionFeature,
  updateDiscussionPin
} from '../../api/discussion'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.user?.roleCode === 'ADMIN')
const keyword = ref('')
const posts = ref([])
const detailVisible = ref(false)
const selectedPost = ref(null)
const detailComment = ref('')
const postForm = reactive({ title: '', content: '', imageUrl: '' })
const canSubmitPost = computed(() => postForm.title.trim() && postForm.content.trim())

const PAGE_SIZE = 10
const page = reactive({ current: 1, total: 0 })

function assetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return url
}

async function uploadPostImage(file) {
  const url = await uploadImage(file)
  postForm.imageUrl = url
  ElMessage.success('图片上传成功')
  return false
}

async function load() {
  const data = await getDiscussionPage({ current: page.current, size: PAGE_SIZE, keyword: keyword.value || undefined })
  posts.value = data.records || []
  page.total = data.total || 0
  refreshSelectedPost()
}

function changePage(p) {
  page.current = p
  load()
}

function search() {
  page.current = 1
  load()
}

async function submitPost() {
  const passed = await checkModerationPreview(`${postForm.title.trim()}\n${postForm.content.trim()}`, '帖子')
  if (!passed) return
  await createDiscussionPost({
    title: postForm.title.trim(),
    content: postForm.content.trim(),
    imageUrl: postForm.imageUrl
  })
  ElMessage.success('帖子发布成功')
  resetPostForm()
  await load()
}

function resetPostForm() {
  postForm.title = ''
  postForm.content = ''
  postForm.imageUrl = ''
}

async function submitComment(postId) {
  const content = detailComment.value.trim()
  if (!content) return
  const passed = await checkModerationPreview(content, '评论')
  if (!passed) return
  await createDiscussionComment({ postId, content })
  ElMessage.success('评论成功')
  detailComment.value = ''
  await load()
}

async function checkModerationPreview(content, scene) {
  try {
    const result = await previewModeration(content, scene)
    if (result?.result === 'BLOCK') {
      ElMessage.error(result.suggestion || `${scene}内容包含违规信息，请修改后再提交`)
      return false
    }
    if (result?.result === 'FLAG') {
      return await ElMessageBox.confirm(
        result.suggestion || `${scene}内容疑似存在敏感或不文明表达，是否继续提交？`,
        'AI 内容提醒',
        {
          type: 'warning',
          confirmButtonText: '继续提交',
          cancelButtonText: '返回修改'
        }
      ).then(() => true).catch(() => false)
    }
    return true
  } catch (error) {
    return true
  }
}

async function like(post) {
  await toggleDiscussionLike(post.id)
  await load()
}

async function likeComment(commentId) {
  await toggleDiscussionCommentLike(commentId)
  await load()
}

async function removePost(id) {
  const ok = await ElMessageBox.confirm('确定删除该帖子吗？此操作不可恢复。', '删除确认', {
    type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消'
  }).then(() => true).catch(() => false)
  if (!ok) return
  await deleteDiscussionPost(id)
  ElMessage.success('帖子已删除')
  if (selectedPost.value?.id === id) {
    detailVisible.value = false
    selectedPost.value = null
  }
  await load()
}

async function removeComment(id) {
  const ok = await ElMessageBox.confirm('确定删除该评论吗？', '删除确认', {
    type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消'
  }).then(() => true).catch(() => false)
  if (!ok) return
  await deleteDiscussionComment(id)
  ElMessage.success('评论已删除')
  await load()
}

function isEnabled(value) {
  return Number(value) === 1
}

function openPostDetail(post) {
  selectedPost.value = post
  detailComment.value = ''
  detailVisible.value = true
}

function refreshSelectedPost() {
  if (!selectedPost.value) return
  selectedPost.value = posts.value.find((post) => post.id === selectedPost.value.id) || selectedPost.value
}

function postSummary(content) {
  const text = String(content || '').replace(/\s+/g, ' ').trim()
  if (!text) return '暂无正文内容'
  return text.length > 96 ? `${text.slice(0, 96)}...` : text
}

async function setPin(post) {
  const enabled = isEnabled(post.pinned)
  await updateDiscussionPin({ postId: post.id, value: enabled ? 0 : 1 })
  ElMessage.success(enabled ? '已取消置顶' : '已置顶')
  await load()
}

async function setFeature(post) {
  const enabled = isEnabled(post.featured)
  await updateDiscussionFeature({ postId: post.id, value: enabled ? 0 : 1 })
  ElMessage.success(enabled ? '已取消精华' : '已设为精华')
  await load()
}

async function banUser(userId) {
  const ok = await ElMessageBox.confirm('确定封禁该用户的发帖和评论权限吗？', '封禁确认', {
    type: 'warning', confirmButtonText: '确定封禁', cancelButtonText: '取消'
  }).then(() => true).catch(() => false)
  if (!ok) return
  await banDiscussionUser({ userId, reason: '讨论区违规发言' })
  ElMessage.success('已限制该用户发帖和评论')
}

function roleLabel(roleCode) {
  return { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }[roleCode] || '用户'
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(String(value).replace('T', ' '))
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ')
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}年${pad(date.getMonth() + 1)}月${pad(date.getDate())}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(load)
</script>
