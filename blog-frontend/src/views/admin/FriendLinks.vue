<template>
  <div class="friend-links-page">
    <div class="page-header">
      <h2>友情链接</h2>
      <el-button type="primary" @click="showAddDialog"><el-icon><Plus /></el-icon> 添加友链</el-button>
    </div>

    <el-card>
      <el-table :data="links" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="名称" width="150">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-avatar :size="32" :src="row.logo || undefined">{{ row.name?.charAt(0) }}</el-avatar>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="网址" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="显示" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isVisible ? 'success' : 'info'">{{ row.isVisible ? '显示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑友链' : '添加友链'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="网址">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-input v-model="form.logo" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="显示">
          <el-switch v-model="form.isVisible" />
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
import { getAdminFriendLinks, createFriendLink, updateFriendLink, deleteFriendLink } from '@/api/admin'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const links = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '',
  url: '',
  logo: '',
  description: '',
  sortOrder: 0,
  isVisible: true
})

const loadLinks = async () => {
  loading.value = true
  try {
    const res = await getAdminFriendLinks()
    links.value = res.data || []
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  editingId.value = null
  Object.assign(form, { name: '', url: '', logo: '', description: '', sortOrder: 0, isVisible: true })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim() || !form.url.trim()) {
    ElMessage.warning('请填写名称和网址')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateFriendLink(editingId.value, form)
    } else {
      await createFriendLink(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadLinks()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除这个友链吗？', '提示')
  await deleteFriendLink(row.id)
  ElMessage.success('删除成功')
  loadLinks()
}

onMounted(loadLinks)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
