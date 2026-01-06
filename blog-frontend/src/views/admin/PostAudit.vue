<template>
  <div class="post-audit-page">
    <div class="page-header">
      <h2>帖子审核</h2>
    </div>

    <el-card>
      <el-table :data="posts" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <div>
              <el-link :href="`/forum/post/${row.id}`" target="_blank" type="primary">
                {{ row.title }}
              </el-link>
            </div>
            <div v-if="row.rejectReason" class="reject-reason">
              <el-tag type="danger" size="small">驳回理由：{{ row.rejectReason }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="板块" width="100">
          <template #default="{ row }">
            {{ getSectionName(row.sectionId) }}
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            {{ row.userNickname || `用户${row.userId}` }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- 查看帖子详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="帖子详情" width="70%">
      <div v-if="currentPost">
        <h3>{{ currentPost.title }}</h3>
        <div class="post-meta">
          <span>作者：{{ currentPost.userNickname || `用户${currentPost.userId}` }}</span>
          <span>发布时间：{{ formatDate(currentPost.createdAt) }}</span>
        </div>
        <div class="post-content" v-html="currentPost.content"></div>
      </div>
    </el-dialog>

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
import { getPendingForumPosts, getAdminForumSections, approveForumPost, rejectForumPost } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const posts = ref([])
const sections = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const viewDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentPost = ref(null)

const rejectForm = reactive({
  postId: null,
  reason: ''
})

const statusMap = {
  0: { text: '待审核', type: 'info' },
  1: { text: '已发布', type: 'success' },
  2: { text: '已驳回', type: 'danger' }
}

const formatDate = (dateStr) => new Date(dateStr).toLocaleString()

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
    const res = await getPendingForumPosts({ page: page.value, size: pageSize.value })
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

const handleView = (row) => {
  currentPost.value = row
  viewDialogVisible.value = true
}

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
  margin-bottom: 20px;
}

.reject-reason {
  margin-top: 5px;
}

.post-meta {
  font-size: 14px;
  color: #909399;
  margin: 10px 0;
  display: flex;
  gap: 20px;
}

.post-content {
  margin-top: 20px;
  line-height: 1.8;
  color: #606266;
}
</style>

