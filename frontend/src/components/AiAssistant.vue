<template>
  <Teleport to="body">
    <div v-if="visible" class="ai-assistant" :style="{ right: fabPosition.x + 'px', bottom: fabPosition.y + 'px' }">
      <transition name="ai-float">
        <div v-if="open" class="ai-assistant-panel" :style="panelStyle">
          <!-- 头部 - 添加拖动手柄 -->
          <div class="ai-assistant-header" @mousedown="startDragPanel">
            <div>
              <strong>AI 客服助手</strong>
              <p>支持多轮对话和个人状态查询</p>
            </div>
            <div class="ai-assistant-header-actions">
              <el-button size="small" text @click="newChat">新对话</el-button>
              <el-button size="small" text @click="open = false">收起</el-button>
            </div>
          </div>

          <!-- 会话列表 -->
          <div v-if="showSessions && sessions.length" class="ai-assistant-sessions">
            <div
              v-for="s in sessions"
              :key="s.id"
              class="ai-session-item"
              :class="{ active: sessionId === s.id }"
              @click="switchSession(s.id)"
            >
              <span class="ai-session-title">{{ s.title || '未命名' }}</span>
              <span class="ai-session-time">{{ s.createdAt }}</span>
              <el-button size="small" text type="danger" @click.stop="removeSession(s.id)">删除</el-button>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="ai-assistant-quick">
            <el-button size="small" plain @click="usePrompt('图书馆开放时间是几点？')">图书馆</el-button>
            <el-button size="small" plain @click="usePrompt('怎么查询我的考试成绩？')">考试查询</el-button>
            <el-button size="small" plain @click="showSessions = !showSessions">{{ showSessions ? '隐藏' : '历史' }}</el-button>
          </div>

          <!-- 消息列表 -->
          <div ref="messageRef" class="ai-assistant-messages">
            <article v-for="(item, index) in messages" :key="index" :class="['ai-message', item.role]">
              <div class="ai-message-bubble">
                <p>{{ item.content }}</p>
                <div v-if="item.images?.length" class="ai-message-images">
                  <img v-for="(img, i) in item.images" :key="i" :src="img" class="ai-msg-img" />
                </div>
                <div v-if="item.suggestedAction" class="ai-message-action">
                  <el-button size="small" type="primary" link @click="jumpTo(item.suggestedAction)">前往相关页面</el-button>
                </div>
              </div>
            </article>
          </div>

          <!-- 输入区 -->
          <div class="ai-assistant-input">
            <!-- 图片预览 -->
            <div v-if="images.length" class="ai-image-preview">
              <div v-for="(img, idx) in images" :key="idx" class="ai-image-thumb">
                <img :src="img" alt="预览图片" />
                <button class="ai-image-remove" @click="removeImage(idx)">×</button>
              </div>
            </div>
            <el-input
              v-model="question"
              type="textarea"
              :rows="2"
              maxlength="200"
              show-word-limit
              resize="none"
              placeholder="例如：图书馆开放时间？怎么查询考试成绩？"
              @keydown.enter.exact.prevent="submit"
            />
            <div class="ai-assistant-actions">
              <div class="ai-assistant-actions-left">
                <label class="ai-upload-btn">
                  <input
                    ref="fileInputRef"
                    type="file"
                    accept="image/*"
                    multiple
                    hidden
                    @change="handleImageSelect"
                  />
                  <el-button size="small" text type="primary" @click="triggerUpload">
                    <el-icon><Picture /></el-icon> 图片
                  </el-button>
                </label>
                <span class="ai-assistant-tip">按 Enter 发送，Shift + Enter 换行</span>
              </div>
              <el-button type="primary" :loading="loading" :disabled="!question.trim() && !images.length" @click="submit">发送</el-button>
            </div>
          </div>
        </div>
      </transition>

      <button
        class="ai-assistant-fab"
        @mousedown="startDragFab"
        @touchstart="startDragFabTouch"
        @click="toggleFab"
      >
        <span>AI</span>
        <small>助手</small>
      </button>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatWithAi, chatWithImage, getAiSessions, getAiSessionHistory, deleteAiSession } from '../api/ai'
import { Picture } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { getToken } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const open = ref(false)
const loading = ref(false)
const question = ref('')
const messageRef = ref(null)
const messages = ref([])
const sessionId = ref(null)
const sessions = ref([])
const showSessions = ref(false)
const images = ref([])
const fileInputRef = ref(null)

// 拖动相关状态
const fabPosition = reactive({ x: 28, y: 24 })
const panelPosition = reactive({ x: 0, y: 0 })
const isDraggingFab = ref(false)
const isDraggingPanel = ref(false)
const fabDragDistance = ref(0)
const dragStartPos = reactive({ x: 0, y: 0 })
const dragStartElementPos = reactive({ x: 0, y: 0 })

// 计算面板样式
const panelStyle = computed(() => {
  if (panelPosition.x === 0 && panelPosition.y === 0) return {}
  return {
    transform: `translate(${panelPosition.x}px, ${panelPosition.y}px)`
  }
})

// FAB拖动处理
function startDragFab(e) {
  e.preventDefault()
  fabDragDistance.value = 0
  isDraggingFab.value = true
  dragStartPos.x = e.clientX
  dragStartPos.y = e.clientY
  dragStartElementPos.x = fabPosition.x
  dragStartElementPos.y = fabPosition.y

  document.addEventListener('mousemove', handleDragFab)
  document.addEventListener('mouseup', stopDragFab)
}

function handleDragFab(e) {
  if (!isDraggingFab.value) return
  const deltaX = dragStartPos.x - e.clientX
  const deltaY = dragStartPos.y - e.clientY
  fabDragDistance.value = Math.max(fabDragDistance.value, Math.abs(deltaX), Math.abs(deltaY))
  fabPosition.x = Math.max(0, dragStartElementPos.x + deltaX)
  fabPosition.y = Math.max(0, dragStartElementPos.y + deltaY)
}

function stopDragFab() {
  isDraggingFab.value = false
  document.removeEventListener('mousemove', handleDragFab)
  document.removeEventListener('mouseup', stopDragFab)

  // 保存位置到localStorage
  savePositions()
}

// FAB触摸拖动处理（移动端）
function startDragFabTouch(e) {
  const touch = e.touches[0]
  fabDragDistance.value = 0
  isDraggingFab.value = true
  dragStartPos.x = touch.clientX
  dragStartPos.y = touch.clientY
  dragStartElementPos.x = fabPosition.x
  dragStartElementPos.y = fabPosition.y

  document.addEventListener('touchmove', handleDragFabTouch, { passive: false })
  document.addEventListener('touchend', stopDragFabTouch)
}

function handleDragFabTouch(e) {
  if (!isDraggingFab.value) return
  e.preventDefault()
  const touch = e.touches[0]
  const deltaX = dragStartPos.x - touch.clientX
  const deltaY = dragStartPos.y - touch.clientY
  fabPosition.x = Math.max(0, dragStartElementPos.x + deltaX)
  fabPosition.y = Math.max(0, dragStartElementPos.y + deltaY)
}

function stopDragFabTouch() {
  isDraggingFab.value = false
  document.removeEventListener('touchmove', handleDragFabTouch)
  document.removeEventListener('touchend', stopDragFabTouch)
  savePositions()
}

// 面板拖动处理
function startDragPanel(e) {
  if (e.target.tagName === 'BUTTON' || e.target.closest('button')) return
  e.preventDefault()
  isDraggingPanel.value = true
  dragStartPos.x = e.clientX
  dragStartPos.y = e.clientY
  dragStartElementPos.x = panelPosition.x
  dragStartElementPos.y = panelPosition.y

  document.addEventListener('mousemove', handleDragPanel)
  document.addEventListener('mouseup', stopDragPanel)
}

function handleDragPanel(e) {
  if (!isDraggingPanel.value) return
  const deltaX = e.clientX - dragStartPos.x
  const deltaY = e.clientY - dragStartPos.y
  panelPosition.x = dragStartElementPos.x + deltaX
  panelPosition.y = dragStartElementPos.y + deltaY
}

function stopDragPanel() {
  isDraggingPanel.value = false
  document.removeEventListener('mousemove', handleDragPanel)
  document.removeEventListener('mouseup', stopDragPanel)
  savePositions()
}

// 保存和加载位置
function savePositions() {
  localStorage.setItem('ai_assistant_fab_position', JSON.stringify(fabPosition))
  localStorage.setItem('ai_assistant_panel_position', JSON.stringify(panelPosition))
}

function loadPositions() {
  try {
    const savedFab = localStorage.getItem('ai_assistant_fab_position')
    const savedPanel = localStorage.getItem('ai_assistant_panel_position')
    if (savedFab) Object.assign(fabPosition, JSON.parse(savedFab))
    if (savedPanel) Object.assign(panelPosition, JSON.parse(savedPanel))
  } catch {
    // ignore
  }
}

const visible = computed(() => {
  const hasToken = Boolean(getToken())
  const hasUser = Boolean(authStore.user)
  return (hasToken || hasUser) && route.path !== '/login'
})

watch(() => route.path, () => {
  if (route.path === '/login') {
    open.value = false
  }
})

watch(messages, async () => {
  await nextTick()
  if (messageRef.value) {
    messageRef.value.scrollTop = messageRef.value.scrollHeight
  }
}, { deep: true })

function toggleFab() {
  // 拖动距离小于 5px 视为点击，否则忽略
  if (fabDragDistance.value < 5) {
    open.value = !open.value
  }
}

function usePrompt(text) {
  question.value = text
  submit()
}

async function loadSessions() {
  try {
    const res = await getAiSessions()
    sessions.value = res.data || []
  } catch { /* ignore */ }
}

async function switchSession(sid) {
  sessionId.value = sid
  showSessions.value = false
  try {
    const res = await getAiSessionHistory(sid)
    const history = res.data || []
    messages.value = history.map(h => ({
      role: h.role || 'assistant',
      content: h.content || ''
    }))
  } catch {
    messages.value = []
  }
}

async function removeSession(sid) {
  try {
    await deleteAiSession(sid)
    if (sessionId.value === sid) {
      sessionId.value = null
      resetMessages()
    }
    sessions.value = sessions.value.filter(s => s.id !== sid)
  } catch {
    ElMessage.error('删除会话失败')
  }
}

function newChat() {
  sessionId.value = null
  images.value = []
  resetMessages()
}

function resetMessages() {
  messages.value = [{
    role: 'assistant',
    content: '我是校园助手AI客服，可以回答课程查询、考试安排、图书借阅、校园通知和公告相关问题。开始新对话吧！'
  }]
}

function triggerUpload() {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

function handleImageSelect(e) {
  const files = Array.from(e.target.files || [])
  files.forEach(file => {
    if (images.value.length >= 4) {
      ElMessage.warning('最多上传4张图片')
      return
    }
    const reader = new FileReader()
    reader.onload = (ev) => {
      images.value.push(ev.target.result)
    }
    reader.readAsDataURL(file)
  })
}

function removeImage(idx) {
  images.value.splice(idx, 1)
}

async function submit() {
  const text = question.value.trim()
  const hasImages = images.value.length > 0
  if ((!text && !hasImages) || loading.value) return

  const displayContent = text || '[图片消息]'
  messages.value.push({ role: 'user', content: displayContent, images: [...images.value] })
  const currentImages = [...images.value]
  question.value = ''
  images.value = []
  loading.value = true
  try {
    let data
    if (currentImages.length) {
      const pureImages = currentImages.map(img => img.split(',')[1])
      data = await chatWithImage(text || '请描述这张图片', pureImages, sessionId.value)
    } else {
      data = await chatWithAi(text, sessionId.value)
    }
    if (data.sessionId && !sessionId.value) {
      sessionId.value = data.sessionId
    }
    messages.value.push({
      role: 'assistant',
      content: data.answer || '当前没有获取到可用回复。',
      suggestedAction: data.suggestedAction || ''
    })
    loadSessions()
  } catch (error) {
    ElMessage.error(error?.message || 'AI助手暂时不可用')
  } finally {
    loading.value = false
    open.value = true
  }
}

function jumpTo(path) {
  if (!path) return
  router.push(path)
  open.value = false
}

onMounted(() => {
  resetMessages()
  loadSessions()
  loadPositions()
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  z-index: 5000;
  transition: none;
}

.ai-assistant-panel {
  width: 380px;
  margin-bottom: 16px;
  border: 1px solid rgba(22, 67, 112, 0.12);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(247, 250, 255, 0.98), rgba(255, 255, 255, 0.98));
  box-shadow: 0 22px 48px rgba(20, 41, 78, 0.16);
  overflow: hidden;
  cursor: move;
}

.ai-assistant-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 18px 12px;
  border-bottom: 1px solid rgba(22, 67, 112, 0.08);
  cursor: grab;
  user-select: none;
}

.ai-assistant-header:active {
  cursor: grabbing;
}

.ai-assistant-header strong {
  display: block;
  font-size: 16px;
  color: #16345f;
}

.ai-assistant-header p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6a7c96;
}

.ai-assistant-header-actions {
  display: flex;
  gap: 4px;
}

.ai-assistant-sessions {
  max-height: 160px;
  overflow-y: auto;
  padding: 8px 14px;
  border-bottom: 1px solid rgba(22, 67, 112, 0.06);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ai-session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
}
.ai-session-item:hover {
  background: rgba(24, 75, 125, 0.05);
}
.ai-session-item.active {
  background: rgba(24, 75, 125, 0.08);
}

.ai-session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #21344d;
}
.ai-session-time {
  color: #8a97a8;
  white-space: nowrap;
}

.ai-assistant-quick {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding: 10px 16px 0;
}

.ai-assistant-messages {
  max-height: 320px;
  min-height: 200px;
  overflow-y: auto;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-message {
  display: flex;
}

.ai-message.user {
  justify-content: flex-end;
}

.ai-message.assistant {
  justify-content: flex-start;
}

.ai-message-bubble {
  max-width: 86%;
  padding: 11px 13px;
  border-radius: 14px;
  line-height: 1.6;
  font-size: 13px;
}

.ai-message.user .ai-message-bubble {
  background: #204b7a;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-message.assistant .ai-message-bubble {
  background: #eef4fb;
  color: #21344d;
  border-bottom-left-radius: 4px;
}

.ai-message-bubble p {
  margin: 0;
  white-space: pre-wrap;
}

.ai-message-action {
  margin-top: 6px;
}

.ai-assistant-input {
  padding: 12px 16px 16px;
  border-top: 1px solid rgba(22, 67, 112, 0.08);
}

.ai-assistant-actions {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.ai-assistant-tip {
  font-size: 12px;
  color: #7a8799;
}

.ai-assistant-actions-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-upload-btn {
  cursor: pointer;
  display: inline-flex;
}

.ai-image-preview {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.ai-image-thumb {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(22, 67, 112, 0.1);
}

.ai-image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ai-image-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-message-images {
  display: flex;
  gap: 4px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.ai-msg-img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
}

.ai-assistant-fab {
  width: 68px;
  height: 68px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #184b7d, #3d77b5);
  color: #fff;
  box-shadow: 0 14px 30px rgba(24, 75, 125, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: grab;
  user-select: none;
  transition: transform 0.1s;
}

.ai-assistant-fab:hover {
  transform: scale(1.05);
}

.ai-assistant-fab:active {
  cursor: grabbing;
  transform: scale(0.95);
}

.ai-assistant-fab span {
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}

.ai-assistant-fab small {
  margin-top: 4px;
  font-size: 11px;
  line-height: 1;
}

.ai-float-enter-active,
.ai-float-leave-active {
  transition: all 0.2s ease;
}

.ai-float-enter-from,
.ai-float-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 768px) {
  .ai-assistant {
    right: 14px;
    left: 14px;
    bottom: 14px;
  }

  .ai-assistant-panel {
    width: 100%;
  }

  .ai-assistant-fab {
    margin-left: auto;
  }
}
</style>
