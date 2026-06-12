<!-- 校园导航页面，使用高德地图展示校园建筑位置，支持搜索和路径规划。 -->
<template>
  <div>
    <div class="page-header">
      <h2>校园导航</h2>
      <p>查找校园建筑位置，获取路径指引</p>
    </div>

    <div class="search-bar">
      <el-autocomplete
        v-model="searchText"
        :fetch-suggestions="searchBuildings"
        placeholder="搜索校园地点（如：图书馆、第一教学楼...）"
        clearable
        style="width: 360px;"
        :prefix-icon="Search"
        @select="onBuildingSelect"
      />
      <el-select v-model="selectedCategory" placeholder="建筑分类" clearable style="width: 150px;" @change="filterMarkers">
        <el-option label="教学楼" value="教学楼" />
        <el-option label="图书馆" value="图书馆" />
        <el-option label="宿舍楼" value="宿舍楼" />
        <el-option label="食堂餐厅" value="食堂餐厅" />
        <el-option label="体育场馆" value="体育场馆" />
        <el-option label="行政办公" value="行政办公" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="searchLocation">搜索</el-button>
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">
        共 {{ filteredBuildings.length }} 个地点
      </span>
    </div>

    <!-- 地图 + 侧边栏 -->
    <div class="map-container">
      <div id="campus-map" class="map-canvas"></div>
      <div class="map-sidebar">
        <h4>校园地点列表</h4>
        <div class="building-list">
          <div
            v-for="b in filteredBuildings"
            :key="b.id"
            class="building-item"
            :class="{ active: activeBuilding?.id === b.id }"
            @click="focusBuilding(b)"
          >
            <div class="building-item-icon">
              <el-icon><Location /></el-icon>
            </div>
            <div class="building-item-info">
              <strong>{{ b.name }}</strong>
              <span>{{ b.category }} · {{ b.address }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Search, Location } from '@element-plus/icons-vue'

// 高德地图 API Key（请替换为你自己的 Key）
const AMAP_KEY = 'YOUR_AMAP_KEY'
const AMAP_VERSION = '2.0'

const searchText = ref('')
const selectedCategory = ref('')
const activeBuilding = ref(null)

// 校园建筑预设数据
const buildings = [
  { id: 1, name: '第一教学楼', category: '教学楼', address: '校园东路与学思路交叉口', lng: 116.397428, lat: 39.909200, description: '主要教学楼，1-3层为阶梯教室' },
  { id: 2, name: '第二教学楼', category: '教学楼', address: '校园西门附近', lng: 116.396000, lat: 39.908800, description: '4层教学楼，配备多媒体教室' },
  { id: 3, name: '实验楼', category: '教学楼', address: '校园西南角', lng: 116.396500, lat: 39.908200, description: '计算机和物理实验中心' },
  { id: 4, name: '图书馆', category: '图书馆', address: '校园中心广场北侧', lng: 116.397000, lat: 39.909500, description: '5层，藏书50万册，自习座位800个' },
  { id: 5, name: '学生宿舍1号楼', category: '宿舍楼', address: '校园生活区北侧', lng: 116.398000, lat: 39.910000, description: '男生宿舍楼' },
  { id: 6, name: '学生宿舍2号楼', category: '宿舍楼', address: '校园生活区南侧', lng: 116.398200, lat: 39.909500, description: '女生宿舍楼' },
  { id: 7, name: '第一食堂', category: '食堂餐厅', address: '生活区中心', lng: 116.398500, lat: 39.909800, description: '一层为大众餐厅，二层为风味餐厅' },
  { id: 8, name: '第二食堂', category: '食堂餐厅', address: '教学区与生活区之间', lng: 116.397500, lat: 39.909700, description: '清真食堂' },
  { id: 9, name: '体育馆', category: '体育场馆', address: '校园东侧运动区', lng: 116.399000, lat: 39.909300, description: '含篮球场、羽毛球场、乒乓球室' },
  { id: 10, name: '田径场', category: '体育场馆', address: '体育馆东侧', lng: 116.399500, lat: 39.909200, description: '标准400米跑道，含足球场' },
  { id: 11, name: '行政办公楼', category: '行政办公', address: '校园正门入口处', lng: 116.396800, lat: 39.909900, description: '学校行政办公和教务大厅' },
  { id: 12, name: '大学生活动中心', category: '行政办公', address: '图书馆南侧', lng: 116.397200, lat: 39.909000, description: '三层，含社团办公室、多功能厅' }
]

const filteredBuildings = computed(() => {
  if (!selectedCategory.value) return buildings
  return buildings.filter(b => b.category === selectedCategory.value)
})

function searchBuildings(queryString, cb) {
  const results = queryString
    ? buildings.filter(b => b.name.includes(queryString) || b.address.includes(queryString))
        .map(b => ({ value: b.name, building: b }))
    : buildings.map(b => ({ value: b.name, building: b }))
  cb(results)
}

function onBuildingSelect(item) {
  activeBuilding.value = item.building
  focusBuilding(item.building)
}

function focusBuilding(building) {
  activeBuilding.value = building
  if (window._campusMap) {
    window._campusMap.setCenter([building.lng, building.lat])
    window._campusMap.setZoom(17)
    // 显示信息窗体
    const infoWindow = new window.AMap.InfoWindow({
      content: `<div style="padding:8px;"><strong>${building.name}</strong><br/>
        <span style="font-size:12px;color:#666;">${building.category} · ${building.address}</span><br/>
        <span style="font-size:12px;color:#999;">${building.description}</span></div>`,
      offset: new window.AMap.Pixel(0, -30)
    })
    infoWindow.open(window._campusMap, [building.lng, building.lat])
  }
}

function searchLocation() {
  if (!searchText.value) return
  const found = buildings.find(b => b.name.includes(searchText.value) || b.address.includes(searchText.value))
  if (found) {
    focusBuilding(found)
  } else if (window._campusMap && window.AMap) {
    // 使用高德地理编码搜索
    const geocoder = new window.AMap.Geocoder({ city: '北京' })
    geocoder.getLocation(searchText.value, (status, result) => {
      if (status === 'complete' && result.info === 'OK') {
        const { lng, lat } = result.geocodes[0].location
        window._campusMap.setCenter([lng, lat])
        window._campusMap.setZoom(16)
        new window.AMap.Marker({ position: [lng, lat], map: window._campusMap })
      }
    })
  }
}

function filterMarkers() {
  // filteredBuildings 变化时刷新地图标记
  initMarkers()
}

function initMarkers() {
  if (!window._campusMap || !window.AMap) return
  window._campusMap.clearMap()
  filteredBuildings.value.forEach(b => {
    const marker = new window.AMap.Marker({
      position: [b.lng, b.lat],
      title: b.name,
      map: window._campusMap
    })
    marker.on('click', () => focusBuilding(b))
  })
}

onMounted(() => {
  // 动态加载高德地图
  if (window.AMap) {
    initMap()
    return
  }
  const script = document.createElement('script')
  script.src = `https://webapi.amap.com/maps?v=${AMAP_VERSION}&key=${AMAP_KEY}`
  script.onload = initMap
  script.onerror = () => {
    // 降级：使用静态地图展示
    document.getElementById('campus-map').innerHTML =
      `<div style="display:flex;align-items:center;justify-content:center;height:100%;flex-direction:column;gap:8px;">
        <span style="font-size:48px;">&#127759;</span>
        <p style="color:var(--text-muted);">请配置高德地图 API Key 后查看实时地图</p>
        <p style="font-size:12px;color:var(--text-muted);">API Key 配置位置：CampusMapView.vue 中的 AMAP_KEY</p>
      </div>`
  }
  document.head.appendChild(script)
})

function initMap() {
  if (!window.AMap) return
  const map = new window.AMap.Map('campus-map', {
    center: [116.397428, 39.909200],
    zoom: 15,
    mapStyle: 'amap://styles/light'
  })
  window._campusMap = map
  initMarkers()
}
</script>

<style scoped>
.map-container {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  height: 520px;
}

.map-canvas {
  flex: 1;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  min-width: 0;
}

.map-sidebar {
  width: 280px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  overflow-y: auto;
  flex-shrink: 0;
}

.map-sidebar h4 {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
}

.building-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.building-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.building-item:hover {
  background: var(--bg-page);
}
.building-item.active {
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-5);
}

.building-item-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.building-item-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.building-item-info strong {
  font-size: 13px;
}
.building-item-info span {
  font-size: 11px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
