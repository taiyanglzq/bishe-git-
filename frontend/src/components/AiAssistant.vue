<!-- AiAssistant ??????????AiAssistant?????????? -->
<template>
  <div v-if="visible" class="ai-assistant">
    <transition name="ai-float">
      <div v-if="open" class="ai-assistant-panel">
        <div class="ai-assistant-header">
          <div>
            <strong>AI 客服助手</strong>
            <p>支持流程指导和个人状态查询</p>
          </div>
          <el-button text @click="open = false">收起</el-button>
        </div>

        <div class="ai-assistant-quick">
          <el-button size="small" plain @click="usePrompt('怎么预约场地？')">预约流程</el-button>
          <el-button size="small" plain @click="usePrompt('我的场地预约状态怎么样？')">预约状态</el-button>
          <el-button size="small" plain @click="usePrompt('我的活动报名状态怎么样？')">报名状态</el-button>
        </div>

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
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatWithAi } from '../api/ai'
import { getToken } from '../utils/auth'

const router = useRouter()
const route = useRoute()

const open = ref(false)
const loading = ref(false)
const question = ref('')
const messageRef = ref(null)
const messages = ref([
  {
    role: 'assistant',
    content: '我是校园助手 AI 客服，可以回答场地预约、活动报名、签到和通知相关问题。'
  }
])

const visible = computed(() => Boolean(getToken()) && route.path !== '/login')

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

async function submit() {
  const text = question.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  question.value = ''
  loading.value = true
  try {
    const data = await chatWithAi(text)
    messages.value.push({
      role: 'assistant',
      content: data.answer || '当前没有获取到可用回复。',
      suggestedAction: data.suggestedAction || ''
    })
  } catch (error) {
    ElMessage.error(error?.message || 'AI 助手暂时不可用')
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
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 28px;
  bottom: 24px;
  z-index: 1200;
}

.ai-assistant-panel {
  width: 360px;
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

.ai-assistant-quick {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 16px 0;
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
