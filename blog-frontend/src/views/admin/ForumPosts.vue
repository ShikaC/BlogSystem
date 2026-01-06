<template>
  <div class="forum-posts-page">
    <div class="page-header">
      <h2>论坛帖子管理</h2>
      <el-button type="warning" plain @click="handleFixCounts">校正评论数</el-button>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-select v-model="sectionId" placeholder="选择板块" clearable @change="loadPosts" class="filter-select">
          <el-option label="全部板块" :value="null" />
          <el-option v-for="section in sections" :key="section.id" :label="section.name" :value="section.id" />
        </el-select>
        <el-radio-group v-model="status" @change="loadPosts">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="1">已发布</el-radio-button>
          <el-radio-button :value="0">待审核</el-radio-button>
          <el-radio-button :value="2">已驳回</el-radio-button>
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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <!-- 待审核状态：显示审核按钮 -->
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="warning" @click="handleReject(row)">驳回</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <!-- 已驳回状态：显示审核按钮 -->
            <template v-else-if="row.status === 2">
              <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <!-- 回收站状态 -->
            <template v-else-if="row.status === 3">
              <el-button size="small" @click="handleRestore(row)">恢复</el-button>
              <el-button size="small" type="danger" @click="handlePermanentDelete(row)">彻底删除</el-button>
            </template>
            <!-- 其他状态：只显示删除 -->
            <template v-else>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回帖子" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="驳回理由" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminForumPosts, getAdminForumSections, deleteForumPost, restoreForumPost, permanentDeleteForumPost, batchDeleteForumPosts, fixForumPostCounts, approveForumPost, rejectForumPost } from '@/api/admin'
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
  1: { text: '已发布', type: 'success' },
  2: { text: '已驳回', type: 'danger' },
  3: { text: '回收站', type: 'warning' }
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

const handleFixCounts = async () => {
  try {
    await ElMessageBox.confirm('确定要重新计算所有帖子的评论数吗？这可能需要一点时间。', '提示')
    await fixForumPostCounts()
    ElMessage.success('校正完成')
    loadPosts()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('校正失败')
    }
  }
}

const rejectForm = reactive({
  postId: null,
  reason: ''
})

const rejectDialogVisible = ref(false)

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要通过这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await approveForumPost(row.id)
    ElMessage.success('审核通过')
    loadPosts()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('审核失败:', e)
      ElMessage.error('审核失败')
    }
  }
}

const handleReject = (row) => {
  rejectForm.postId = row.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入驳回理由')
    return
  }
  
  try {
    await rejectForumPost(rejectForm.postId, rejectForm.reason)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    loadPosts()
  } catch (e) {
    console.error('驳回失败:', e)
    ElMessage.error('驳回失败')
  }
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
