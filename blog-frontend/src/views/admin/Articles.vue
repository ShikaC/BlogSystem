<template>
  <div class="articles-page">
    <div class="page-header">
      <h2>文章管理</h2>
      <el-button type="primary" @click="$router.push('/admin/article/edit')">
        <el-icon><Plus /></el-icon> 写文章
      </el-button>
    </div>

    <el-card>
      <div class="filter-bar">
        <el-radio-group v-model="status" @change="loadArticles">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="1">已发布</el-radio-button>
          <el-radio-button :value="0">草稿</el-radio-button>
          <el-radio-button :value="2">私密</el-radio-button>
          <el-radio-button :value="3">回收站</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="articles" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" type="danger" size="small">置顶</el-tag>
            {{ row.title }}
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="70" />
        <el-table-column prop="likeCount" label="点赞" width="70" />
        <el-table-column label="创建时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/admin/article/edit/${row.id}`)">编辑</el-button>
            <el-button size="small" @click="handleToggleTop(row)">{{ row.isTop ? '取消置顶' : '置顶' }}</el-button>
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
import { getAdminArticles, deleteArticle, restoreArticle, permanentDeleteArticle, toggleTop, batchDeleteArticles } from '@/api/admin'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const articles = ref([])
const loading = ref(false)
const status = ref(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

const statusMap = {
  0: { text: '草稿', type: 'info' },
  1: { text: '已发布', type: 'success' },
  2: { text: '私密', type: 'warning' },
  3: { text: '回收站', type: 'danger' }
}

const formatDate = (dateStr) => new Date(dateStr).toLocaleDateString('zh-CN')

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getAdminArticles({ status: status.value, page: page.value, size: pageSize.value })
    articles.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handlePageChange = (p) => {
  page.value = p
  loadArticles()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleToggleTop = async (row) => {
  await toggleTop(row.id)
  ElMessage.success(row.isTop ? '已取消置顶' : '已置顶')
  loadArticles()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除这篇文章吗？删除后会移到回收站。', '提示')
  await deleteArticle(row.id)
  ElMessage.success('已移到回收站')
  loadArticles()
}

const handleRestore = async (row) => {
  await restoreArticle(row.id)
  ElMessage.success('已恢复')
  loadArticles()
}

const handlePermanentDelete = async (row) => {
  await ElMessageBox.confirm('彻底删除后无法恢复，确定要删除吗？', '警告', { type: 'warning' })
  await permanentDeleteArticle(row.id)
  ElMessage.success('已彻底删除')
  loadArticles()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 篇文章吗？`, '提示')
  await batchDeleteArticles(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadArticles()
}

onMounted(loadArticles)
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

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}
</style>
