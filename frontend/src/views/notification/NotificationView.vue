<template>
  <div>
    <div class="page-header">
      <h2>通知中心</h2>
      <p>查看系统推送的预约、活动、公告等业务通知</p>
    </div>

    <div class="panel-card">
      <div class="panel-card-header">
        <h3>通知列表</h3>
        <div style="display: flex; gap: 10px; align-items: center;">
          <el-select v-model="readStatus" clearable placeholder="阅读状态" size="small" @change="load" style="width: 110px;">
            <el-option label="未读" :value="0" />
            <el-option label="已读" :value="1" />
          </el-select>
          <el-button size="small" @click="load">刷新</el-button>
          <el-button size="small" type="primary" @click="readAll">全部已读</el-button>
        </div>
      </div>

      <div class="panel-card-body">
        <div v-if="rows.length" class="notification-list">
          <div
            v-for="item in rows"
            :key="item.id"
            class="notification-item"
            :class="{ unread: item.readStatus === 0 }"
          >
            <div v-if="item.readStatus === 0" class="notification-item-dot"></div>
            <el-icon v-else style="color: var(--text-muted); flex-shrink: 0;">
              <Check />
            </el-icon>

            <div class="notification-item-content">
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
              <small>{{ formatBizType(item.bizType) }} · {{ formatDateTime(item.createTime) }}</small>
            </div>

            <el-button
              v-if="item.readStatus === 0"
              size="small"
              type="primary"
              @click="read(item.id)"
            >
              标记已读
            </el-button>
            <el-tag v-else size="small" type="info">已读</el-tag>
          </div>
        </div>
        <el-empty v-else description="暂无通知" :image-size="60" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { getNotificationPage, markAllNotificationRead, markNotificationRead } from '../../api/notification'

const rows = ref([])
const readStatus = ref(null)

async function load() {
  const data = await getNotificationPage({
    current: 1,
    size: 30,
    readStatus: readStatus.value ?? undefined
  })
  rows.value = data.records || []
}

async function read(id) {
  await markNotificationRead(id)
  await load()
}

async function readAll() {
  await markAllNotificationRead()
  await load()
}

function formatBizType(type) {
  const map = {
    BOOKING: '场地预约',
    ACTIVITY: '活动通知',
    NOTICE: '校园公告',
    SYSTEM: '系统通知'
  }
  return map[type] || '业务通知'
}

function formatDateTime(value) {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return text
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}年${pad(date.getMonth() + 1)}月${pad(date.getDate())}日 ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(load)
</script>
