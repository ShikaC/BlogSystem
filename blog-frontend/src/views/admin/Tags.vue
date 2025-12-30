<template>
  <div class="tags-page">
    <div class="page-header">
      <h2>标签管理</h2>
      <el-button type="primary" @click="showAddDialog"><el-icon><Plus /></el-icon> 添加标签</el-button>
    </div>

    <el-card>
      <el-table :data="tags" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="articleCount" label="文章数" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑标签' : '添加标签'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
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
import { getAdminTags, createTag, updateTag, deleteTag } from '@/api/admin'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const tags = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const form = reactive({ name: '' })

const loadTags = async () => {
  loading.value = true
  try {
    const res = await getAdminTags()
    tags.value = res.data || []
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  editingId.value = null
  form.name = ''
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  editingId.value = row.id
  form.name = row.name
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateTag(editingId.value, form)
    } else {
      await createTag(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadTags()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除这个标签吗？', '提示')
  await deleteTag(row.id)
  ElMessage.success('删除成功')
  loadTags()
}

onMounted(loadTags)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
