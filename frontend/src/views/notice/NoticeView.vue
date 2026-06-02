<template>
  <el-table :data="rows" border>
    <el-table-column prop="title" label="标题" min-width="180" />
    <el-table-column prop="category" label="分类" width="120" />
    <el-table-column prop="viewCount" label="浏览量" width="100" />
    <el-table-column prop="createTime" label="发布时间" width="180" />
  </el-table>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getNoticePage } from '../../api/notice'

const rows = ref([])

onMounted(async () => {
  const data = await getNoticePage({ current: 1, size: 10 })
  rows.value = data.records || []
})
</script>
