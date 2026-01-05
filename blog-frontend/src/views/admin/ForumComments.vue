<template>
  <div class="comments-page">
    <div class="page-header">
      <h2>论坛评论管理</h2>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-radio-group v-model="status" @change="loadComments">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="1">已通过</el-radio-button>
          <el-radio-button :value="0">待审核</el-radio-button>
          <el-radio-button :value="2">已拒绝</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="comments" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="评论者" width="140">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-avatar :size="32" :src="row.avatar || undefined">{{ row.nickname?.charAt(0) || '匿' }}</el-avatar>
              <span>{{ row.nickname || '匿名' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="帖子" width="120">
          <template #default="{ row }">
            <router-link v-if="row.targetId" :to="`/forum/post/${row.targetId}`" target="_blank" class="post-link">
              查看帖子
            </router-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleReply(row)">回复</el-button>
            <el-button v-if="row.status !== 1" size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status !== 2" size="small" type="warning" @click="handleReject(row)">拒绝</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div>
          <el-button v-if="selectedIds.length" type="success" @click="handleBatchApprove">批量通过</el-button>
          <el-button v-if="selectedIds.length" type="danger" @click="handleBatchDelete">批量删除</el-button>
        </div>
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评论" width="500px">
      <el-form :model="replyForm">
        <el-form-item>
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="replyLoading" @click="submitReply">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminComments, updateCommentStatus, deleteComment, batchDeleteComments, replyComment } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const status = ref(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const replyForm = ref({
  targetType: 'FORUM_POST',
  targetId: null,
  parentId: null,
  replyToId: null,
  content: ''
})

const statusMap = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已拒绝', type: 'danger' }
}

const formatDate = (dateStr) => new Date(dateStr).toLocaleDateString('zh-CN')

const loadComments = async () => {
  loading.value = true
  try {
    const res = await getAdminComments({ 
      targetType: 'FORUM_POST', 
      status: status.value, 
      page: page.value, 
      size: pageSize.value 
    })
    comments.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handlePageChange = (p) => {
  page.value = p
  loadComments()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleApprove = async (row) => {
  await updateCommentStatus(row.id, 1)
  ElMessage.success('已通过')
  loadComments()
}

const handleReject = async (row) => {
  await updateCommentStatus(row.id, 2)
  ElMessage.success('已拒绝')
  loadComments()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除这条评论吗？', '提示')
  await deleteComment(row.id)
  ElMessage.success('已删除')
  loadComments()
}

const handleBatchApprove = async () => {
  for (const id of selectedIds.value) {
    await updateCommentStatus(id, 1)
  }
  ElMessage.success('批量通过成功')
  loadComments()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条评论吗？`, '提示')
  await batchDeleteComments(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadComments()
}

const handleReply = (row) => {
  replyForm.value = {
    targetType: 'FORUM_POST',
    targetId: row.targetId,
    parentId: row.parentId || row.id,
    replyToId: row.id,
    content: ''
  }
  replyDialogVisible.value = true
}

const submitReply = async () => {
  if (!replyForm.value.content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replyLoading.value = true
  try {
    await replyComment(replyForm.value)
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadComments()
  } finally {
    replyLoading.value = false
  }
}

onMounted(loadComments)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-bar {
  margin-bottom: 20px;
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
