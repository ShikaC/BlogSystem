<template>
  <div class="users-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="stats-row" v-if="statistics">
        <el-tag type="info">总用户 {{ statistics.total }}</el-tag>
        <el-tag type="success">启用 {{ statistics.active }}</el-tag>
        <el-tag type="danger">禁用 {{ statistics.disabled }}</el-tag>
        <el-tag type="warning">管理员 {{ statistics.admins }}</el-tag>
      </div>
    </div>

    <el-card>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-input 
          v-model="keyword" 
          placeholder="搜索用户名/昵称" 
          clearable
          style="width: 200px; margin-right: 12px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="roleFilter" placeholder="角色" clearable style="width: 120px; margin-right: 12px;">
          <el-option label="全部角色" value="" />
          <el-option label="管理员" value="ADMIN" />
          <el-option label="普通用户" value="USER" />
        </el-select>
        
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px; margin-right: 12px;">
          <el-option label="全部状态" value="" />
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 用户列表 -->
      <el-table :data="users" v-loading="loading" stripe class="user-table">
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="40" :src="row.avatar || undefined">
                {{ (row.nickname || row.username)?.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <span class="nickname">{{ row.nickname || row.username }}</span>
                <span class="username">@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="email" label="邮箱" width="180">
          <template #default="{ row }">
            <span>{{ row.email || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="发布统计" width="150">
          <template #default="{ row }">
            <div class="publish-stats">
              <span><el-icon><Document /></el-icon> {{ row.articleCount || 0 }}</span>
              <span><el-icon><ChatLineSquare /></el-icon> {{ row.postCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="注册时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button 
              v-if="row.role !== 'ADMIN'"
              size="small" 
              :type="row.status === 1 ? 'danger' : 'success'" 
              link
              @click="handleToggleStatus(row)"
            >
              <el-icon><Lock v-if="row.status === 1" /><Unlock v-else /></el-icon>
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="table-footer">
        <el-pagination
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="用户详情"
      size="600px"
      direction="rtl"
    >
      <div v-if="selectedUser" class="user-detail">
        <!-- 用户基本信息 -->
        <div class="detail-section">
          <div class="user-profile">
            <el-avatar :size="80" :src="selectedUser.avatar || undefined">
              {{ (selectedUser.nickname || selectedUser.username)?.charAt(0) }}
            </el-avatar>
            <div class="profile-info">
              <h3>{{ selectedUser.nickname || selectedUser.username }}</h3>
              <p class="username">@{{ selectedUser.username }}</p>
              <div class="profile-tags">
                <el-tag :type="selectedUser.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
                  {{ selectedUser.role === 'ADMIN' ? '管理员' : '用户' }}
                </el-tag>
                <el-tag :type="selectedUser.status === 1 ? 'success' : 'danger'" size="small">
                  {{ selectedUser.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="detail-section">
          <h4>发布统计</h4>
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-value">{{ selectedUser.articleCount || 0 }}</div>
              <div class="stat-label">文章</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ selectedUser.postCount || 0 }}</div>
              <div class="stat-label">帖子</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ selectedUser.commentCount || 0 }}</div>
              <div class="stat-label">评论</div>
            </div>
          </div>
        </div>

        <!-- 用户资料 -->
        <div class="detail-section">
          <h4>个人资料</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="邮箱">{{ selectedUser.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="简介">{{ selectedUser.bio || '-' }}</el-descriptions-item>
            <el-descriptions-item label="GitHub">{{ selectedUser.github || '-' }}</el-descriptions-item>
            <el-descriptions-item label="知乎">{{ selectedUser.zhihu || '-' }}</el-descriptions-item>
            <el-descriptions-item label="微信">{{ selectedUser.weixin || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDateTime(selectedUser.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(selectedUser.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 发布的文章 -->
        <div class="detail-section" v-if="selectedUser.articles?.length">
          <h4>最近发布的文章</h4>
          <el-table :data="selectedUser.articles" size="small" max-height="250">
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="articleStatusMap[row.status]?.type" size="small">
                  {{ articleStatusMap[row.status]?.text }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="阅读" width="70" />
            <el-table-column label="日期" width="100">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 发布的帖子 -->
        <div class="detail-section" v-if="selectedUser.posts?.length">
          <h4>最近发布的帖子</h4>
          <el-table :data="selectedUser.posts" size="small" max-height="250">
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="postStatusMap[row.status]?.type" size="small">
                  {{ postStatusMap[row.status]?.text }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览" width="70" />
            <el-table-column label="日期" width="100">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, getUserDetail, getUserStatistics, updateUserStatus } from '@/api/admin'
import { Search, Document, ChatLineSquare, View, Lock, Unlock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statistics = ref(null)

const detailDrawerVisible = ref(false)
const selectedUser = ref(null)

const articleStatusMap = {
  0: { text: '草稿', type: 'info' },
  1: { text: '已发布', type: 'success' },
  2: { text: '私密', type: 'warning' },
  3: { text: '回收站', type: 'danger' }
}

const postStatusMap = {
  0: { text: '草稿', type: 'info' },
  1: { text: '已发布', type: 'success' },
  2: { text: '私密', type: 'warning' },
  3: { text: '回收站', type: 'danger' }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    if (keyword.value) params.keyword = keyword.value
    if (roleFilter.value) params.role = roleFilter.value
    if (statusFilter.value !== '' && statusFilter.value !== null) params.status = statusFilter.value
    
    const res = await getUserList(params)
    users.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await getUserStatistics()
    statistics.value = res.data
  } catch (e) {
    console.error('Failed to load statistics', e)
  }
}

const handleSearch = () => {
  page.value = 1
  loadUsers()
}

const handleReset = () => {
  keyword.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
  page.value = 1
  loadUsers()
}

const handlePageChange = (p) => {
  page.value = p
  loadUsers()
}

const handleSizeChange = (s) => {
  pageSize.value = s
  page.value = 1
  loadUsers()
}

const handleViewDetail = async (row) => {
  try {
    const res = await getUserDetail(row.id)
    selectedUser.value = res.data
    detailDrawerVisible.value = true
  } catch (e) {
    ElMessage.error('获取用户详情失败')
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  await ElMessageBox.confirm(
    `确定要${action}用户 "${row.nickname || row.username}" 吗？${newStatus === 0 ? '禁用后该用户将无法登录。' : ''}`,
    '提示',
    { type: newStatus === 0 ? 'warning' : 'info' }
  )
  
  try {
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`已${action}用户`)
    loadUsers()
    loadStatistics()
  } catch (e) {
    ElMessage.error(`${action}失败`)
  }
}

onMounted(() => {
  loadUsers()
  loadStatistics()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-row {
  display: flex;
  gap: 10px;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 8px;
}

.user-table {
  margin-bottom: 20px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-info .nickname {
  font-weight: 500;
  color: #303133;
}

.user-info .username {
  font-size: 12px;
  color: #909399;
}

.publish-stats {
  display: flex;
  gap: 16px;
}

.publish-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 13px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
}

/* 用户详情样式 */
.user-detail {
  padding: 0 10px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e6f0ff 100%);
  border-radius: 8px;
}

.profile-info h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.profile-info .username {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.profile-tags {
  display: flex;
  gap: 8px;
}

.stats-cards {
  display: flex;
  gap: 16px;
}

.stat-card {
  flex: 1;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
}

.stat-card .stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-card .stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
