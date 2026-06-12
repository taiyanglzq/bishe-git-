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
        placeholder="搜索校园地点（如：图书馆、主教楼、一食堂...）"
        clearable
        style="width: 360px;"
        :prefix-icon="Search"
        @select="onBuildingSelect"
      />
      <el-select v-model="selectedCategory" placeholder="建筑分类" clearable style="width: 150px;" @change="filterMarkers">
        <el-option label="教学楼" value="教学楼" />
        <el-option label="图书馆" value="图书馆" />
        <el-option label="食堂餐厅" value="食堂餐厅" />
        <el-option label="体育场馆" value="体育场馆" />
        <el-option label="行政办公" value="行政办公" />
        <el-option label="校门" value="校门" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="searchLocation">搜索</el-button>
      <el-button :icon="Aim" :loading="locating" @click="locateMe">定位</el-button>
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Aim, Search, Location } from '@element-plus/icons-vue'

// 高德地图 API Key（请替换为你自己的 Key）
const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY'
const AMAP_VERSION = '2.0'

const searchText = ref('')
const selectedCategory = ref('')
const activeBuilding = ref(null)
const locating = ref(false)
let currentLocationMarker = null

// 宜春学院校园建筑数据
const buildings = [
  { id: 1, name: '主教楼', category: '教学楼', address: '校园中心', lng: 114.357326, lat: 27.790557, description: '宜春学院主要教学楼' },
  { id: 2, name: '医农大', category: '教学楼', address: '校园东侧', lng: 114.359098, lat: 27.788761, description: '医学与农业教学楼' },
  { id: 3, name: '人文楼', category: '教学楼', address: '校园东侧', lng: 114.359239, lat: 27.789591, description: '人文学院教学楼' },
  { id: 4, name: '二教楼', category: '教学楼', address: '校园中部', lng: 114.358708, lat: 27.790560, description: '第二教学楼' },
  { id: 5, name: '理工楼', category: '教学楼', address: '校园中部', lng: 114.358728, lat: 27.791470, description: '理工科教学楼' },
  { id: 6, name: '实训楼', category: '教学楼', address: '校园北侧', lng: 114.357266, lat: 27.792697, description: '实验实训中心' },
  { id: 7, name: '艺术楼', category: '教学楼', address: '校园南侧', lng: 114.355784, lat: 27.787945, description: '艺术类教学楼' },
  { id: 8, name: '图书馆', category: '图书馆', address: '校园中部', lng: 114.357306, lat: 27.791927, description: '宜春学院图书馆' },
  { id: 9, name: '一食堂', category: '食堂餐厅', address: '校园北侧', lng: 114.358538, lat: 27.794509, description: '第一食堂' },
  { id: 10, name: '二食堂', category: '食堂餐厅', address: '校园北侧', lng: 114.356866, lat: 27.794296, description: '第二食堂' },
  { id: 11, name: '北田径场', category: '体育场馆', address: '校园北侧', lng: 114.358458, lat: 27.795529, description: '北田径场' },
  { id: 12, name: '南田径场', category: '体育场馆', address: '校园中部', lng: 114.355443, lat: 27.790984, description: '南田径场' },
  { id: 13, name: '气膜球馆', category: '体育场馆', address: '校园西侧', lng: 114.355644, lat: 27.792424, description: '气膜球馆' },
  { id: 14, name: '体育馆', category: '体育场馆', address: '校园西侧', lng: 114.355554, lat: 27.789315, description: '宜春学院体育馆' },
  { id: 15, name: '大学生活动中心', category: '行政办公', address: '校园北侧', lng: 114.357707, lat: 27.794777, description: '大学生活动中心' },
  { id: 16, name: '派出所', category: '行政办公', address: '校园北侧', lng: 114.357146, lat: 27.795816, description: '宜春学院派出所' },
  { id: 17, name: '西门', category: '校门', address: '校园西侧', lng: 114.350627, lat: 27.793335, description: '宜春学院西门' },
  { id: 18, name: '东门', category: '校门', address: '校园东侧', lng: 114.359409, lat: 27.793090, description: '宜春学院东门' },
  { id: 19, name: '南门', category: '校门', address: '校园南侧', lng: 114.357606, lat: 27.787568, description: '宜春学院南门' }
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
    const geocoder = new window.AMap.Geocoder({ city: '宜春' })
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

function locateMe() {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持定位功能')
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      locating.value = false
      const { longitude, latitude } = pos.coords
      if (!window._campusMap || !window.AMap) return
      // 移除旧定位标记
      if (currentLocationMarker) {
        window._campusMap.remove(currentLocationMarker)
      }
      // 添加当前位置标记（蓝色圆点）
      currentLocationMarker = new window.AMap.Marker({
        position: [longitude, latitude],
        icon: new window.AMap.Icon({
          size: new window.AMap.Size(24, 24),
          image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
          imageSize: new window.AMap.Size(24, 24)
        }),
        offset: new window.AMap.Pixel(-12, -12),
        zIndex: 999,
        map: window._campusMap
      })
      // 添加定位圆圈
      const circle = new window.AMap.Circle({
        center: [longitude, latitude],
        radius: 50,
        fillColor: '#1890ff',
        fillOpacity: 0.15,
        strokeColor: '#1890ff',
        strokeWeight: 1,
        strokeOpacity: 0.5,
        map: window._campusMap
      })
      currentLocationMarker._circle = circle
      window._campusMap.setCenter([longitude, latitude])
      window._campusMap.setZoom(17)
      ElMessage.success('定位成功')
    },
    () => {
      locating.value = false
      ElMessage.error('定位失败，请检查浏览器位置权限')
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  )
}

function filterMarkers() {
  // filteredBuildings 变化时刷新地图标记
  initMarkers()
}

function initMarkers() {
  if (!window._campusMap || !window.AMap) return
  window._campusMap.clearMap()
  currentLocationMarker = null
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
    center: [114.357306, 27.791927],
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
