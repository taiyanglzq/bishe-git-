<template>
  <el-table :data="rows" border>
    <el-table-column prop="id" label="ID" width="80" />
    <el-table-column prop="operatorId" label="操作人" width="100" />
    <el-table-column prop="operation" label="操作" width="180" />
    <el-table-column prop="bizType" label="业务类型" width="120" />
    <el-table-column prop="bizId" label="业务ID" width="100" />
    <el-table-column prop="detail" label="详情" min-width="220" />
    <el-table-column prop="createTime" label="时间" width="180" />
  </el-table>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getLogPage } from '../../api/log'

const rows = ref([])

onMounted(async () => {
  const data = await getLogPage({ current: 1, size: 20 })
  rows.value = data.records || []
})
</script>
