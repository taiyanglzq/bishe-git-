<template>
  <div>
    <div class="page-header">
      <h2>场地预约</h2>
      <p>选择场地、日期和时间段，提交预约申请</p>
    </div>

    <!-- 预约工作台 -->
    <div class="booking-workbench">
      <!-- 场地选择 -->
      <div class="booking-column">
        <div class="booking-column-header">
          <h3>选择场地</h3>
          <el-button size="small" text @click="loadVenues">刷新</el-button>
        </div>
        <div class="venue-select-list">
          <button
            v-for="venue in venues"
            :key="venue.id"
            class="venue-select-item"
            :class="{ active: Number(form.venueId) === venue.id }"
            @click="selectVenue(venue)"
          >
            <span>
              <span class="venue-select-item-name">{{ venue.name }}</span>
              <span class="venue-select-item-location">{{ venue.location }}</span>
            </span>
            <span class="venue-select-item-capacity">{{ venue.capacity }} 人</span>
          </button>
        </div>
      </div>

      <!-- 时间段选择 -->
      <div class="booking-column">
        <div class="booking-column-header">
          <h3>日期与时间段</h3>
          <el-date-picker
            v-model="selectedDate"
            value-format="YYYY-MM-DD"
            size="small"
            @change="loadSlots"
          />
        </div>

        <div v-if="slots.length" class="slot-grid">
          <button
            v-for="slot in slots"
            :key="slot.id"
            class="slot-pill"
            :class="{
              active: form.timeRange === slot.timeRange,
              disabled: slot.remainingQuota <= 0
            }"
            :disabled="slot.remainingQuota <= 0"
            @click="selectSlot(slot)"
          >
            <span class="slot-pill-time">{{ slot.timeRange }}</span>
            <span class="slot-pill-quota">剩余 {{ slot.remainingQuota }}/{{ slot.totalQuota }}</span>
          </button>
        </div>
        <el-empty v-else description="当前场地暂无开放时间段" :image-size="48" />

        <div style="display: flex; gap: 10px; margin-top: 12px;">
          <el-input v-model="form.reason" placeholder="预约原因，例如学习自习、社团活动" size="small" style="flex: 1;" />
          <el-button type="primary" size="small" :disabled="!canSubmit" @click="submit">提交预约</el-button>
        </div>
      </div>
    </div>

    <!-- 预约记录 -->
    <div class="panel-card">
      <div class="panel-card-header">
        <h3>{{ canAudit ? '预约审核列表' : '我的预约记录' }}</h3>
        <div style="display: flex; gap: 10px; align-items: center;">
          <el-select v-model="status" clearable placeholder="状态筛选" size="small" @change="loadBookings" style="width: 120px;">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-button size="small" @click="loadBookings">刷新</el-button>
        </div>
      </div>
      <div class="panel-card-body" style="padding: 0;">
        <el-table :data="rows" border style="border: none;">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="studentId" label="学生ID" width="90" />
          <el-table-column prop="venueId" label="场地ID" width="90" />
          <el-table-column prop="bookingDate" label="日期" width="120" />
          <el-table-column prop="timeRange" label="时间段" width="130" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag
                size="small"
                :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : row.status === 'CANCELLED' ? 'info' : 'warning'"
              >
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="150" />
          <el-table-column label="操作" width="210">
            <template #default="{ row }">
              <el-button v-if="canAudit" size="small" type="success" :disabled="row.status !== 'PENDING'" @click="approve(row.id)">通过</el-button>
              <el-button v-if="canAudit" size="small" type="warning" :disabled="row.status !== 'PENDING'" @click="reject(row.id)">驳回</el-button>
              <el-button v-if="!canAudit" size="small" type="danger" :disabled="row.status === 'CANCELLED'" @click="cancel(row.id)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { approveBooking, cancelBooking, createBooking, getBookingPage, rejectBooking } from '../../api/booking'
import { useAuthStore } from '../../stores/auth'
import { getVenuePage, getVenueSlotPage } from '../../api/venue'

const rows = ref([])
const venues = ref([])
const slots = ref([])
const status = ref('')
const selectedDate = ref('2026-06-10')
const authStore = useAuthStore()
const canAudit = computed(() => ['TEACHER', 'ADMIN'].includes(authStore.user?.roleCode))
const canSubmit = computed(() => form.venueId && selectedDate.value && form.timeRange && form.reason)

const form = reactive({
  venueId: '',
  bookingDate: '2026-06-10',
  timeRange: '',
  reason: '学习自习'
})

function statusLabel(s) {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回', CANCELLED: '已取消' }
  return map[s] || s
}

async function loadVenues() {
  const data = await getVenuePage({ current: 1, size: 20 })
  venues.value = data.records || []
  if (!form.venueId && venues.value.length) selectVenue(venues.value[0])
}

async function loadSlots() {
  form.bookingDate = selectedDate.value
  if (!form.venueId || !selectedDate.value) { slots.value = []; return }
  const data = await getVenueSlotPage({ current: 1, size: 50, venueId: Number(form.venueId), slotDate: selectedDate.value })
  slots.value = data.records || []
  if (!slots.value.some(s => s.timeRange === form.timeRange)) form.timeRange = ''
}

async function loadBookings() {
  const d = await getBookingPage({ current: 1, size: 10, status: status.value || undefined })
  rows.value = d.records || []
}

async function load() {
  await loadVenues()
  await Promise.all([loadSlots(), loadBookings()])
}

function selectVenue(venue) { form.venueId = String(venue.id); form.timeRange = ''; loadSlots() }
function selectSlot(slot) { form.timeRange = slot.timeRange }

async function submit() {
  if (!canSubmit.value) { ElMessage.warning('请选择场地、日期和时间段，并填写预约原因'); return }
  await createBooking({ venueId: Number(form.venueId), bookingDate: selectedDate.value, timeRange: form.timeRange, reason: form.reason })
  ElMessage.success('预约提交成功，等待审核')
  await Promise.all([loadSlots(), loadBookings()])
}

async function approve(id) { await approveBooking({ bookingId: id, remark: '审核通过' }); ElMessage.success('已通过预约'); await loadBookings() }
async function reject(id) { await rejectBooking({ bookingId: id, remark: '名额或时间不符合要求' }); ElMessage.success('已驳回预约'); await loadBookings() }
async function cancel(id) { await cancelBooking(id); ElMessage.success('已取消预约'); await Promise.all([loadSlots(), loadBookings()]) }

onMounted(load)
</script>
