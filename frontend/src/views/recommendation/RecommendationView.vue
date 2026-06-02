<template>
  <div>
    <el-alert title="推荐说明" type="info" :closable="false">
      <p>{{ data.reason?.join('、') }}</p>
    </el-alert>

    <h3>推荐公告</h3>
    <el-table :data="data.notices || []" border>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="viewCount" label="热度" width="100" />
    </el-table>

    <h3>推荐活动</h3>
    <el-table :data="data.activities || []" border>
      <el-table-column prop="title" label="活动" min-width="180" />
      <el-table-column prop="location" label="地点" width="160" />
      <el-table-column label="名额" width="120">
        <template #default="{ row }">{{ row.enrolledCount }}/{{ row.capacity }}</template>
      </el-table-column>
    </el-table>

    <h3>推荐场地</h3>
    <el-table :data="data.venues || []" border>
      <el-table-column prop="name" label="场地" min-width="180" />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="capacity" label="容量" width="100" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getPersonalRecommendation } from '../../api/recommendation'

const data = ref({})

onMounted(async () => {
  data.value = await getPersonalRecommendation()
})
</script>
