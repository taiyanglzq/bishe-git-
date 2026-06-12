<!-- 图书检索页面，支持按分类和关键字检索图书，查看图书详情和借阅状态。 -->
<template>
  <div>
    <div class="page-header">
      <h2>图书检索</h2>
      <p>搜索馆藏图书，查看馆藏位置和可借状态</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索书名、作者或ISBN..."
        clearable
        style="width: 300px;"
        :prefix-icon="Search"
        @keyup.enter="fetchBooks"
      />
      <el-select v-model="category" placeholder="全部分类" clearable style="width: 160px;" @change="fetchBooks">
        <el-option label="计算机科学" value="计算机科学" />
        <el-option label="数学" value="数学" />
        <el-option label="外语" value="外语" />
        <el-option label="文学" value="文学" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="fetchBooks">检索</el-button>
      <span style="margin-left: auto; font-size: 12px; color: var(--text-muted);">
        共 {{ filteredBooks.length }} 本图书
      </span>
    </div>

    <div v-if="pagedBooks.length" class="book-grid">
      <article
        v-for="item in pagedBooks"
        :key="item.id"
        class="book-card"
        @click="openDetail(item.id)"
      >
        <div class="book-cover">
          <div class="book-cover-placeholder">
            <el-icon><Reading /></el-icon>
          </div>
        </div>
        <div class="book-info">
          <h3 class="book-title">{{ item.title }}</h3>
          <p class="book-author">{{ item.author || '佚名' }}</p>
          <p class="book-meta">
            <span>{{ item.publisher }}</span>
            <span v-if="item.publishYear">{{ item.publishYear }}</span>
          </p>
          <div class="book-tags">
            <el-tag size="small" type="info">{{ item.category }}</el-tag>
            <el-tag size="small" :type="(item.availableCount || 0) > 0 ? 'success' : 'danger'">
              {{ (item.availableCount || 0) > 0 ? '可借 ' + item.availableCount + ' 册' : '已借完' }}
            </el-tag>
          </div>
        </div>
      </article>
    </div>

    <el-pagination
      v-if="filteredBooks.length"
      class="notice-pagination"
      layout="prev, pager, next, total"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :total="filteredBooks.length"
      @current-change="(p) => currentPage = p"
    />

    <div v-if="myBorrows.length" class="my-borrows-section">
      <h3 class="section-title">我的借阅</h3>
      <div class="my-borrows-grid">
        <article
          v-for="borrow in myBorrows"
          :key="borrow.id"
          class="my-borrow-card"
        >
          <div class="my-borrow-left">
            <div class="borrow-book-cover">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="my-borrow-info">
              <h4>{{ getBookTitle(borrow.bookId) }}</h4>
              <p>{{ getBookAuthor(borrow.bookId) }}</p>
              <p class="borrow-time">借阅时间：{{ formatBorrowTime(borrow.borrowTime) }}</p>
            </div>
          </div>
          <div class="my-borrow-right">
            <el-tag v-if="borrow.status === 'BORROWED'" type="warning" size="small">借阅中</el-tag>
            <el-tag v-else type="info" size="small">已归还</el-tag>
            <el-button
              v-if="borrow.status === 'BORROWED'"
              type="warning"
              size="small"
              :loading="returning"
              @click="handleReturn(borrow.bookId)"
            >
              归还
            </el-button>
          </div>
        </article>
      </div>
    </div>

    <div v-if="!loading && !filteredBooks.length" class="empty-state">
      <div class="empty-state-icon">
        <el-icon><Document /></el-icon>
      </div>
      <p>{{ keyword || category ? '未找到匹配的图书' : '暂无图书信息' }}</p>
    </div>

    <!-- 图书详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentBook?.title || '图书详情'" width="600px" destroy-on-close>
      <div v-if="currentBook" class="book-detail">
        <div class="book-detail-header">
          <div class="book-cover-placeholder large">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="book-detail-meta">
            <h3>{{ currentBook.title }}</h3>
            <p>作者：{{ currentBook.author || '佚名' }}</p>
            <p>出版社：{{ currentBook.publisher }} {{ currentBook.publishYear ? '(' + currentBook.publishYear + ')' : '' }}</p>
            <p>ISBN：{{ currentBook.isbn || '暂无' }}</p>
            <el-tag size="small" type="info">{{ currentBook.category }}</el-tag>
          </div>
        </div>
        <div class="book-detail-section">
          <h4>馆藏信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">馆藏位置</span>
              <span class="detail-value">{{ currentBook.location || '待定' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">总册数</span>
              <span class="detail-value">{{ currentBook.totalCount }} 册</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">可借册数</span>
              <span class="detail-value" :class="{ 'text-danger': !currentBook.availableCount }">
                {{ currentBook.availableCount || 0 }} 册
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">借阅状态</span>
              <span class="detail-value">{{ (currentBook.availableCount || 0) > 0 ? '可借阅' : '已全部借出' }}</span>
            </div>
          </div>
        </div>
        <div v-if="currentBook.description" class="book-detail-section">
          <h4>内容简介</h4>
          <p class="book-description">{{ currentBook.description }}</p>
        </div>
        <div class="book-detail-section">
          <h4>借阅操作</h4>
          <div class="borrow-area">
            <template v-if="borrowedBookIds.has(currentBook.id)">
              <el-button
                type="warning"
                :loading="returning"
                @click="handleReturn(currentBook.id)"
              >
                <el-icon><Check /></el-icon> 归还
              </el-button>
              <span class="borrow-tip">您已借阅此图书，点击归还</span>
            </template>
            <template v-else>
              <el-button
                type="primary"
                :disabled="!currentBook.availableCount"
                :loading="borrowing"
                @click="handleBorrow(currentBook.id)"
              >
                <el-icon><Plus /></el-icon> 借阅
              </el-button>
              <span v-if="!currentBook.availableCount" class="borrow-tip text-danger">
                该图书已全部借出
              </span>
              <span v-else class="borrow-tip">
                点击按钮即可借阅此图书
              </span>
            </template>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowBook, getBookPage, getMyBorrows, returnBook } from '../../api/book'
import { Check, Document, Plus, Reading, Search } from '@element-plus/icons-vue'

const PAGE_SIZE = 12

const keyword = ref('')
const category = ref('')
const currentPage = ref(1)
const loading = ref(false)
const borrowing = ref(false)
const returning = ref(false)
const books = ref([])
const myBorrows = ref([])
const detailVisible = ref(false)
const currentBook = ref(null)

const borrowedBookIds = computed(() => new Set(myBorrows.value.filter(b => b.status === 'BORROWED').map(b => b.bookId)))

const filteredBooks = computed(() => {
  let list = books.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(b =>
      (b.title && b.title.toLowerCase().includes(kw)) ||
      (b.author && b.author.toLowerCase().includes(kw)) ||
      (b.isbn && b.isbn.toLowerCase().includes(kw))
    )
  }
  return list
})

const pagedBooks = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredBooks.value.slice(start, start + PAGE_SIZE)
})

async function fetchBooks() {
  loading.value = true
  currentPage.value = 1
  try {
    const params = { current: 1, size: 200 }
    if (category.value) params.category = category.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getBookPage(params)
    books.value = res.records || []
  } catch {
    ElMessage.error('获取图书列表失败')
    books.value = []
  } finally {
    loading.value = false
  }
}

function openDetail(bookId) {
  currentBook.value = books.value.find(b => b.id === bookId) || null
  detailVisible.value = true
}

async function handleBorrow(bookId) {
  borrowing.value = true
  try {
    await borrowBook({ bookId })
    ElMessage.success('借阅成功')
    detailVisible.value = false
    await Promise.all([fetchBooks(), fetchMyBorrows()])
  } catch {
    // error message is already shown by request interceptor
  } finally {
    borrowing.value = false
  }
}

async function handleReturn(bookId) {
  returning.value = true
  try {
    await returnBook(bookId)
    ElMessage.success('归还成功')
    await Promise.all([fetchBooks(), fetchMyBorrows()])
  } catch {
    // error message is already shown by request interceptor
  } finally {
    returning.value = false
  }
}

async function fetchMyBorrows() {
  try {
    const data = await getMyBorrows()
    myBorrows.value = Array.isArray(data) ? data : []
  } catch {
    myBorrows.value = []
  }
}

function getBookTitle(bookId) {
  const book = books.value.find(b => b.id === bookId)
  return book?.title || '未知图书'
}

function getBookAuthor(bookId) {
  const book = books.value.find(b => b.id === bookId)
  return book?.author || '佚名'
}

function formatBorrowTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return time
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(() => {
  fetchBooks()
  fetchMyBorrows()
})
</script>

<style scoped>
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.book-card {
  display: flex;
  gap: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.book-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}

.book-cover-placeholder {
  width: 80px;
  height: 110px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  flex-shrink: 0;
}
.book-cover-placeholder.large {
  width: 100px;
  height: 140px;
  font-size: 36px;
}

.book-info {
  flex: 1;
  min-width: 0;
}
.book-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 4px;
}
.book-meta {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0 0 8px;
  display: flex;
  gap: 8px;
}
.book-tags {
  display: flex;
  gap: 8px;
}

.book-detail-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.book-detail-meta h3 {
  margin: 0 0 8px;
  font-size: 18px;
}
.book-detail-meta p {
  margin: 0 0 4px;
  font-size: 14px;
  color: var(--text-secondary);
}
.book-detail-section {
  margin-bottom: 20px;
}
.book-detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}
.book-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
}
.text-danger {
  color: var(--el-color-danger);
  font-weight: 600;
}

.borrow-area {
  display: flex;
  gap: 12px;
  align-items: center;
}

.borrow-tip {
  font-size: 13px;
  color: var(--text-secondary);
}

.my-borrows-section {
  margin-top: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.my-borrows-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.my-borrow-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 12px 16px;
  transition: all 0.2s;
}
.my-borrow-card:hover {
  border-color: var(--primary-color);
}

.my-borrow-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.borrow-book-cover {
  width: 48px;
  height: 66px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.my-borrow-info h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 2px;
}
.my-borrow-info p {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0 0 2px;
}
.borrow-time {
  color: var(--text-muted) !important;
}

.my-borrow-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
</style>
