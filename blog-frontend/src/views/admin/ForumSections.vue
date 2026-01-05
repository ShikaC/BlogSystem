<template>
  <div class="forum-sections-page">
    <div class="page-header">
      <h2>论坛板块管理</h2>
      <el-button type="primary" @click="showAddDialog"><el-icon><Plus /></el-icon> 添加板块</el-button>
    </div>

    <el-card>
      <el-table :data="sections" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑板块' : '添加板块'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminForumSections, saveForumSection, deleteForumSection } from '@/api/admin'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const sections = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)

const form = reactive({
  id: null,
  name: '',
  description: '',
  sortOrder: 0
})

const loadSections = async () => {
  loading.value = true
  try {
    const res = await getAdminForumSections()
    sections.value = res.data || []
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  form.id = null
  form.name = ''
  form.description = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入板块名称')
    return
  }
  saving.value = true
  try {
    await saveForumSection(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadSections()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个板块吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteForumSection(row.id)
    ElMessage.success('删除成功')
    loadSections()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '删除失败'
      ElMessage.error(errorMsg)
    }
  }
}

onMounted(loadSections)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
