<template>
  <div>
    <div class="page-header">
      <h2>日志审计</h2>
      <p>记录系统操作的完整审计轨迹，仅管理员可查看</p>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索操作详情..." clearable style="width: 240px;" :prefix-icon="Search" />
      <el-select v-model="bizType" placeholder="业务类型" clearable style="width: 140px;">
        <el-option label="场地预约" value="BOOKING" />
        <el-option label="活动" value="ACTIVITY" />
        <el-option label="公告" value="NOTICE" />
        <el-option label="系统" value="SYSTEM" />
      </el-select>
      <el-button @click="load">刷新</el-button>
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">共 {{ rows.length }} 条记录</span>
    </div>

    <div class="panel-card">
      <div class="panel-card-body" style="padding: 0;">
        <el-table :data="rows" border style="border: none;">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="operatorId" label="操作人" width="100" />
          <el-table-column prop="operation" label="操作" width="160">
            <template #default="{ row }">
              <el-tag
                size="small"
                :type="operationType(row.operation)"
              >
                {{ row.operation }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="bizType" label="业务类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ bizTypeLabel(row.bizType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="bizId" label="业务ID" width="90" />
          <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
          <el-table-column prop="createTime" label="时间" width="170" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getLogPage } from '../../api/log'

const rows = ref([])
const keyword = ref('')
const bizType = ref('')

function operationType(op) {
  if (!op) return 'info'
  if (op.includes('创建') || op.includes('保存') || op.includes('报名') || op.includes('提交')) return 'success'
  if (op.includes('删除') || op.includes('取消') || op.includes('驳回')) return 'danger'
  if (op.includes('更新') || op.includes('编辑') || op.includes('修改')) return 'warning'
  if (op.includes('审核') || op.includes('通过')) return 'success'
  return 'info'
}

function bizTypeLabel(t) {
  const map = { BOOKING: '场地预约', ACTIVITY: '活动', NOTICE: '公告', SYSTEM: '系统' }
  return map[t] || t || '-'
}

async function load() {
  const data = await getLogPage({ current: 1, size: 50 })
  rows.value = data.records || []
}

onMounted(load)
</script>
