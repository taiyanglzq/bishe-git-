<template>
  <div class="booking-page booking-page-compact">
    <section class="booking-workbench">
      <div class="booking-column venue-column">
        <div class="compact-title">
          <div>
            <p class="eyebrow">Venue Reservation</p>
            <h2>场地选择</h2>
          </div>
          <el-button size="small" @click="load">刷新</el-button>
        </div>

        <div class="venue-list">
          <button
            v-for="venue in venues"
            :key="venue.id"
            class="venue-row"
            :class="{ active: Number(form.venueId) === venue.id }"
            @click="selectVenue(venue)"
          >
            <span>
              <strong>{{ venue.name }}</strong>
              <small>{{ venue.location }}</small>
            </span>
            <em>{{ venue.capacity }} 人</em>
          </button>
        </div>
      </div>

      <div class="booking-column slot-column">
        <div class="compact-title">
          <div>
            <p class="eyebrow">Time Slot</p>
            <h2>日期与时间段</h2>
          </div>
          <el-date-picker v-model="selectedDate" value-format="YYYY-MM-DD" size="small" @change="loadSlots" />
        </div>

        <div v-if="slots.length" class="slot-strip">
          <button
            v-for="slot in slots"
            :key="slot.id"
            class="slot-pill"
            :class="{ active: form.timeRange === slot.timeRange, disabled: slot.remainingQuota <= 0 }"
            :disabled="slot.remainingQuota <= 0"
            @click="selectSlot(slot)"
          >
            <strong>{{ slot.timeRange }}</strong>
            <span>{{ slot.remainingQuota }}/{{ slot.totalQuota }}</span>
          </button>
        </div>
        <el-empty v-else class="compact-empty" description="当前场地暂无开放时间段" />

        <div class="reason-row">
          <el-input v-model="form.reason" placeholder="预约原因，例如学习自习、社团活动、训练" />
          <el-button type="primary" :disabled="!canSubmit" @click="submit">提交预约</el-button>
        </div>
      </div>
    </section>

    <section class="record-panel compact-record">
      <div class="section-title">
        <h3>{{ canAudit ? '预约审核列表' : '我的预约记录' }}</h3>
        <div class="record-actions">
          <el-select v-model="status" clearable placeholder="按状态筛选" size="small" @change="loadBookings">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-button size="small" @click="loadBookings">刷新</el-button>
        </div>
      </div>

      <el-table :data="rows" border size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="studentId" label="学生ID" width="90" />
        <el-table-column prop="venueId" label="场地ID" width="90" />
        <el-table-column prop="bookingDate" label="日期" width="120" />
        <el-table-column prop="timeRange" label="时间段" width="125" />
        <el-table-column prop="status" label="状态" width="105" />
        <el-table-column prop="reason" label="原因" min-width="150" />
        <el-table-column label="操作" width="210">
          <template #default="{ row }">
            <el-button v-if="canAudit" size="small" type="success" :disabled="row.status !== 'PENDING'" @click="approve(row.id)">通过</el-button>
            <el-button v-if="canAudit" size="small" type="warning" :disabled="row.status !== 'PENDING'" @click="reject(row.id)">驳回</el-button>
            <el-button v-if="!canAudit" size="small" type="danger" :disabled="row.status === 'CANCELLED'" @click="cancel(row.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
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

async function loadVenues() {
  const data = await getVenuePage({ current: 1, size: 20 })
  venues.value = data.records || []
  if (!form.venueId && venues.value.length) {
    selectVenue(venues.value[0])
  }
}

async function loadSlots() {
  form.bookingDate = selectedDate.value
  if (!form.venueId || !selectedDate.value) {
    slots.value = []
    return
  }
  const data = await getVenueSlotPage({
    current: 1,
    size: 50,
    venueId: Number(form.venueId),
    slotDate: selectedDate.value
  })
  slots.value = data.records || []
  if (!slots.value.some((slot) => slot.timeRange === form.timeRange)) {
    form.timeRange = ''
  }
}

async function loadBookings() {
  const bookingData = await getBookingPage({ current: 1, size: 10, status: status.value || undefined })
  rows.value = bookingData.records || []
}

async function load() {
  await loadVenues()
  await Promise.all([loadSlots(), loadBookings()])
}

function selectVenue(venue) {
  form.venueId = String(venue.id)
  form.timeRange = ''
  loadSlots()
}

function selectSlot(slot) {
  form.timeRange = slot.timeRange
}

async function submit() {
  if (!canSubmit.value) {
    ElMessage.warning('请选择场地、日期和时间段，并填写预约原因')
    return
  }
  await createBooking({
    venueId: Number(form.venueId),
    bookingDate: selectedDate.value,
    timeRange: form.timeRange,
    reason: form.reason
  })
  ElMessage.success('预约提交成功，等待审核')
  await Promise.all([loadSlots(), loadBookings()])
}

async function approve(id) {
  await approveBooking({ bookingId: id, remark: '审核通过' })
  ElMessage.success('已通过预约')
  await Promise.all([loadSlots(), loadBookings()])
}

async function reject(id) {
  await rejectBooking({ bookingId: id, remark: '名额或时间不符合要求' })
  ElMessage.success('已驳回预约')
  await Promise.all([loadSlots(), loadBookings()])
}

async function cancel(id) {
  await cancelBooking(id)
  ElMessage.success('已取消预约')
  await Promise.all([loadSlots(), loadBookings()])
}

onMounted(load)
</script>
