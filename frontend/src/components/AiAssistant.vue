<template>
  <Teleport to="body">
    <div v-if="visible" class="ai-assistant">
      <transition name="ai-float">
        <div v-if="open" class="ai-assistant-panel">
          <!-- 头部 -->
          <div class="ai-assistant-header">
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
            <el-button size="small" plain @click="usePrompt('怎么预约场地？')">预约流程</el-button>
            <el-button size="small" plain @click="usePrompt('我的场地预约状态怎么样？')">预约状态</el-button>
            <el-button size="small" plain @click="usePrompt('我的活动报名状态怎么样？')">报名状态</el-button>
            <el-button size="small" plain @click="showSessions = !showSessions">{{ showSessions ? '隐藏' : '历史' }}</el-button>
          </div>

          <!-- 消息列表 -->
          <div ref="messageRef" class="ai-assistant-messages">
            <article v-for="(item, index) in messages" :key="index" :class="['ai-message', item.role]">
              <div class="ai-message-bubble">
                <p>{{ item.content }}</p>
                <div v-if="item.suggestedAction" class="ai-message-action">
                  <el-button size="small" type="primary" link @click="jumpTo(item.suggestedAction)">前往相关页面</el-button>
                </div>
              </div>
            </article>
          </div>

          <!-- 输入区 -->
          <div class="ai-assistant-input">
            <el-input
              v-model="question"
              type="textarea"
              :rows="2"
              maxlength="200"
              show-word-limit
              resize="none"
              placeholder="例如：怎么预约场地？我的活动报名成功了吗？"
              @keydown.enter.exact.prevent="submit"
            />
            <div class="ai-assistant-actions">
              <span class="ai-assistant-tip">按 Enter 发送，Shift + Enter 换行</span>
              <el-button type="primary" :loading="loading" :disabled="!question.trim()" @click="submit">发送</el-button>
            </div>
          </div>
        </div>
      </transition>

      <button class="ai-assistant-fab" @click="toggle">
        <span>AI</span>
        <small>助手</small>
      </button>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatWithAi, getAiSessions, getAiSessionHistory, deleteAiSession } from '../api/ai'
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

function toggle() {
  open.value = !open.value
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
  resetMessages()
}

function resetMessages() {
  messages.value = [{
    role: 'assistant',
    content: '我是校园助手AI客服，可以回答场地预约、活动报名、签到和通知相关问题。开始新对话吧！'
  }]
}

async function submit() {
  const text = question.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  question.value = ''
  loading.value = true
  try {
    const data = await chatWithAi(text, sessionId.value)
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
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 28px;
  bottom: 24px;
  z-index: 5000;
}

.ai-assistant-panel {
  width: 380px;
  margin-bottom: 16px;
  border: 1px solid rgba(22, 67, 112, 0.12);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(247, 250, 255, 0.98), rgba(255, 255, 255, 0.98));
  box-shadow: 0 22px 48px rgba(20, 41, 78, 0.16);
  overflow: hidden;
}

.ai-assistant-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 18px 12px;
  border-bottom: 1px solid rgba(22, 67, 112, 0.08);
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
  cursor: pointer;
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
