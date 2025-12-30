<template>
  <div class="media-page">
    <div class="page-header">
      <h2>媒体库</h2>
      <el-upload
        :show-file-list="false"
        :before-upload="handleUpload"
        accept="image/*"
      >
        <el-button type="primary"><el-icon><Upload /></el-icon> 上传图片</el-button>
      </el-upload>
    </div>

    <el-card>
      <div class="media-grid" v-loading="loading">
        <div v-for="media in mediaList" :key="media.id" class="media-item">
          <img :src="media.fileUrl" :alt="media.originalName" />
          <div class="media-overlay">
            <el-button size="small" @click="copyUrl(media.fileUrl)">复制URL</el-button>
            <el-button size="small" type="danger" @click="handleDelete(media)">删除</el-button>
          </div>
          <p class="media-name">{{ media.originalName }}</p>
        </div>
      </div>

      <el-pagination
        v-if="total > pageSize"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMediaList, uploadFile, deleteMedia } from '@/api/admin'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const mediaList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadMedia = async () => {
  loading.value = true
  try {
    const res = await getMediaList({ page: page.value, size: pageSize.value })
    mediaList.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleUpload = async (file) => {
  try {
    await uploadFile(file)
    ElMessage.success('上传成功')
    loadMedia()
  } catch (e) {
    ElMessage.error('上传失败')
  }
  return false
}

const handleDelete = async (media) => {
  await ElMessageBox.confirm('确定要删除这个文件吗？', '提示')
  await deleteMedia(media.id)
  ElMessage.success('删除成功')
  loadMedia()
}

const copyUrl = (url) => {
  navigator.clipboard.writeText(url)
  ElMessage.success('已复制到剪贴板')
}

const handlePageChange = (p) => {
  page.value = p
  loadMedia()
}

onMounted(loadMedia)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.media-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.media-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.media-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 30px;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.media-item:hover .media-overlay {
  opacity: 1;
}

.media-name {
  padding: 8px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0;
}
</style>
