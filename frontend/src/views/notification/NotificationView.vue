<template>
  <div>
    <div class="section-title">
      <h3>通知中心</h3>
      <div class="record-actions">
        <el-select v-model="readStatus" clearable placeholder="阅读状态" size="small" @change="load">
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-button size="small" @click="load">刷新</el-button>
        <el-button size="small" type="primary" @click="readAll">全部已读</el-button>
      </div>
    </div>

    <div class="notification-list">
      <article v-for="item in rows" :key="item.id" class="notification-card" :class="{ unread: item.readStatus === 0 }">
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
          <small>{{ formatBizType(item.bizType) }} #{{ item.bizId }} · {{ formatDateTime(item.createTime) }}</small>
        </div>
        <el-button v-if="item.readStatus === 0" size="small" @click="read(item.id)">标记已读</el-button>
        <el-tag v-else type="info">已读</el-tag>
      </article>
    </div>

    <el-empty v-if="rows.length === 0" description="暂无通知" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getNotificationPage, markAllNotificationRead, markNotificationRead } from '../../api/notification'

const rows = ref([])
const readStatus = ref(null)

async function load() {
  const data = await getNotificationPage({ current: 1, size: 20, readStatus: readStatus.value ?? undefined })
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
    ACTIVITY: '活动',
    NOTICE: '公告',
    SYSTEM: '系统'
  }
  return map[type] || '业务'
}

function formatDateTime(value) {
  if (!value) return '-'
  const normalized = String(value).replace('T', ' ')
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) {
    return normalized
  }
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}年${pad(date.getMonth() + 1)}月${pad(date.getDate())}日 ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(load)
</script>
