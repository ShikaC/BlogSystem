<template>
  <div class="forum-posts-page">
    <div class="page-header">
      <h2>论坛帖子管理</h2>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-select v-model="sectionId" placeholder="选择板块" clearable @change="loadPosts" class="filter-select">
          <el-option label="全部板块" :value="null" />
          <el-option v-for="section in sections" :key="section.id" :label="section.name" :value="section.id" />
        </el-select>
        <el-radio-group v-model="status" @change="loadPosts">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="1">正常</el-radio-button>
          <el-radio-button :value="0">待审核</el-radio-button>
          <el-radio-button :value="3">回收站</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="posts" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/forum/post/${row.id}`" target="_blank" class="post-link">
              {{ row.title }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column label="板块" width="100">
          <template #default="{ row }">
            {{ getSectionName(row.sectionId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" />
        <el-table-column prop="likeCount" label="点赞" width="70" />
        <el-table-column prop="commentCount" label="回帖" width="70" />
        <el-table-column label="发布时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 3" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <template v-else>
              <el-button size="small" @click="handleRestore(row)">恢复</el-button>
              <el-button size="small" type="danger" @click="handlePermanentDelete(row)">彻底删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-button v-if="selectedIds.length" type="danger" @click="handleBatchDelete">
          批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminForumPosts, getAdminForumSections, deleteForumPost, restoreForumPost, permanentDeleteForumPost, batchDeleteForumPosts } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const posts = ref([])
const sections = ref([])
const loading = ref(false)
const sectionId = ref(null)
const status = ref(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

const statusMap = {
  0: { text: '待审核', type: 'info' },
  1: { text: '正常', type: 'success' },
  3: { text: '回收站', type: 'danger' }
}

const formatDate = (dateStr) => new Date(dateStr).toLocaleDateString('zh-CN')

const getSectionName = (id) => {
  const section = sections.value.find(s => s.id === id)
  return section?.name || '未知'
}

const loadSections = async () => {
  try {
    const res = await getAdminForumSections()
    sections.value = res.data || []
  } catch (e) {
    console.error('加载板块失败', e)
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (sectionId.value !== null) params.sectionId = sectionId.value
    if (status.value !== null) params.status = status.value
    const res = await getAdminForumPosts(params)
    posts.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handlePageChange = (p) => {
  page.value = p
  loadPosts()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后会移到回收站。', '提示')
  await deleteForumPost(row.id)
  ElMessage.success('已移到回收站')
  loadPosts()
}

const handleRestore = async (row) => {
  await restoreForumPost(row.id)
  ElMessage.success('已恢复')
  loadPosts()
}

const handlePermanentDelete = async (row) => {
  await ElMessageBox.confirm('彻底删除后无法恢复，确定要删除吗？', '警告', { type: 'warning' })
  await permanentDeleteForumPost(row.id)
  ElMessage.success('已彻底删除')
  loadPosts()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 篇帖子吗？`, '提示')
  await batchDeleteForumPosts(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadPosts()
}

onMounted(() => {
  loadSections()
  loadPosts()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  align-items: center;
}

.filter-select {
  width: 150px;
}

.post-link {
  color: #409eff;
  text-decoration: none;
}

.post-link:hover {
  text-decoration: underline;
}

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}
</style>
