<template>
  <div class="post-audit-page">
    <div class="page-header">
      <h2>帖子审核</h2>
    </div>

    <el-card>
      <div class="toolbar" style="margin-bottom: 15px;">
        <el-button 
          type="success" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchApprove"
        >
          批量通过 ({{ selectedIds.length }})
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchReject"
        >
          批量驳回 ({{ selectedIds.length }})
        </el-button>
      </div>
      
      <el-table :data="posts" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
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
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="warning" @click="handleReject(row)">驳回</el-button>
            <el-dropdown @command="(cmd) => handleViolationAction(cmd, row)">
              <el-button size="small" type="danger">
                违规处理 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="warn">警告用户</el-dropdown-item>
                  <el-dropdown-item command="disable">禁用账号</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 警告对话框 -->
    <el-dialog v-model="warnDialogVisible" title="警告用户" width="500px">
      <el-form :model="warnForm" label-width="100px">
        <el-form-item label="警告内容" required>
          <el-input
            v-model="warnForm.message"
            type="textarea"
            :rows="4"
            placeholder="请输入警告内容，将发送给用户"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="warnDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmWarn">确定警告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPendingForumPosts, getAdminForumSections, approveForumPost, rejectForumPost, warnUser, disableUser } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const posts = ref([])
const sections = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const viewDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const warnDialogVisible = ref(false)
const currentPost = ref(null)
const selectedIds = ref([])

const rejectForm = reactive({
  postId: null,
  reason: ''
})

const warnForm = reactive({
  postId: null,
  message: '您发布的内容存在违规行为，请遵守社区规范。'
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
    // 如果是批量驳回（ID包含逗号）
    if (rejectForm.postId && rejectForm.postId.includes(',')) {
      const ids = rejectForm.postId.split(',').map(id => id.trim())
      for (const id of ids) {
        await rejectForumPost(id, rejectForm.reason)
      }
      ElMessage.success(`已批量驳回 ${ids.length} 个帖子`)
    } else {
      await rejectForumPost(rejectForm.postId, rejectForm.reason)
      ElMessage.success('已驳回')
    }
    rejectDialogVisible.value = false
    selectedIds.value = []
    loadPosts()
  } catch (e) {
    console.error('驳回失败:', e)
    ElMessage.error('驳回失败')
  }
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchApprove = async () => {
  if (selectedIds.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确定要通过选中的 ${selectedIds.value.length} 个帖子吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    for (const id of selectedIds.value) {
      await approveForumPost(id)
    }
    ElMessage.success('批量审核通过')
    selectedIds.value = []
    loadPosts()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('批量审核失败:', e)
      ElMessage.error('批量审核失败')
    }
  }
}

const handleBatchReject = () => {
  if (selectedIds.value.length === 0) return
  rejectForm.postId = selectedIds.value.join(',') // 用逗号分隔多个ID
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const handleViolationAction = (command, row) => {
  if (command === 'warn') {
    warnForm.postId = row.id
    warnForm.message = '您发布的内容存在违规行为，请遵守社区规范。'
    warnDialogVisible.value = true
  } else if (command === 'disable') {
    handleDisableUser(row)
  }
}

const confirmWarn = async () => {
  if (!warnForm.message.trim()) {
    ElMessage.warning('请输入警告内容')
    return
  }
  
  try {
    await warnUser(warnForm.postId, warnForm.message)
    ElMessage.success('警告已发送')
    warnDialogVisible.value = false
  } catch (e) {
    console.error('发送警告失败:', e)
    ElMessage.error('发送警告失败')
  }
}

const handleDisableUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用用户"${row.userNickname || `用户${row.userId}`}"的账号吗？禁用后该用户将无法登录。`,
      '警告',
      {
        confirmButtonText: '确定禁用',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await disableUser(row.id)
    ElMessage.success('用户账号已禁用')
  } catch (e) {
    if (e !== 'cancel') {
      console.error('禁用用户失败:', e)
      ElMessage.error('禁用用户失败')
    }
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

