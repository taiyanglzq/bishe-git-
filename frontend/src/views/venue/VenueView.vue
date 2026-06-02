<template>
  <el-table :data="rows" border>
    <el-table-column prop="name" label="场地名称" min-width="180" />
    <el-table-column prop="location" label="位置" min-width="160" />
    <el-table-column prop="capacity" label="容量" width="100" />
    <el-table-column prop="status" label="状态" width="100">
      <template #default="{ row }">{{ row.status === 1 ? '可用' : '停用' }}</template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getVenuePage } from '../../api/venue'

const rows = ref([])

onMounted(async () => {
  const data = await getVenuePage({ current: 1, size: 10 })
  rows.value = data.records || []
})
</script>
