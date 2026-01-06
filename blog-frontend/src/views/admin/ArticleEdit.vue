<template>
  <div class="article-edit">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card>
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>

            <el-form-item label="文章内容" prop="content">
              <div class="editor-wrapper">
                <div class="editor-toolbar">
                  <span class="toolbar-tip">📝 支持 Markdown 语法</span>
                  <el-button size="small" @click="showPreview = !showPreview">
                    {{ showPreview ? '隐藏预览' : '显示预览' }}
                  </el-button>
                </div>
                <div class="editor-container" :class="{ 'split-view': showPreview }">
                  <el-input
                    v-model="form.content"
                    type="textarea"
                    :rows="20"
                    placeholder="请输入文章内容（支持Markdown）&#10;&#10;# 标题1&#10;## 标题2&#10;**粗体** *斜体*&#10;[链接](https://example.com)&#10;![图片](url)&#10;```代码块```"
                    class="markdown-editor"
                  />
                  <div v-if="showPreview" class="markdown-preview" v-html="markdownPreview"></div>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="文章摘要">
              <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="文章摘要，不填则自动截取" />
            </el-form-item>
          </el-card>

          <el-card header="SEO设置" style="margin-top: 20px;">
            <el-form-item label="SEO标题">
              <el-input v-model="form.seoTitle" placeholder="留空则使用文章标题" />
            </el-form-item>
            <el-form-item label="SEO关键词">
              <el-input v-model="form.seoKeywords" placeholder="多个关键词用逗号分隔" />
            </el-form-item>
            <el-form-item label="SEO描述">
              <el-input v-model="form.seoDescription" type="textarea" :rows="2" placeholder="留空则使用文章摘要" />
            </el-form-item>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card header="发布设置">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">草稿</el-radio>
                <el-radio :value="1">发布</el-radio>
                <el-radio :value="2">私密</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="置顶">
              <el-switch v-model="form.isTop" />
            </el-form-item>

            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width: 100%;">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="标签">
              <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width: 100%;">
                <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
              </el-select>
            </el-form-item>

            <el-form-item label="封面图">
              <div class="cover-upload">
                <el-input v-model="form.coverImage" placeholder="封面图URL" style="flex: 1;" />
                <el-upload
                  :show-file-list="false"
                  :http-request="handleCoverUpload"
                  accept="image/*"
                >
                  <el-button size="small" type="primary">上传</el-button>
                </el-upload>
              </div>
              <el-image v-if="form.coverImage" :src="form.coverImage" fit="cover" class="cover-preview" />
            </el-form-item>

            <el-form-item label="访问密码">
              <el-input v-model="form.password" placeholder="留空则无需密码" />
            </el-form-item>

            <div class="publish-actions">
              <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
              <el-button type="primary" @click="publishArticle" :loading="saving">
                {{ isEdit ? '更新文章' : '发布文章' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { getAdminArticle, saveArticle, getAdminCategories, getAdminTags, createTag, uploadFile } from '@/api/admin'
import { uploadUserImage } from '@/api/front'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import { useUserStore } from '@/stores/user'

// 配置 marked
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

const route = useRoute()
const router = useRouter()

const formRef = ref()
const saving = ref(false)
const categories = ref([])
const tags = ref([])
const showPreview = ref(false)

const isEdit = computed(() => !!route.params.id)

// Markdown 预览
const markdownPreview = computed(() => {
  if (!form.content) return ''
  try {
    return marked.parse(form.content)
  } catch (e) {
    return '预览解析失败'
  }
})


const form = reactive({
  id: null,
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  status: 0,
  isTop: false,
  categoryId: null,
  tagIds: [],
  seoTitle: '',
  seoKeywords: '',
  seoDescription: '',
  password: ''
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }]
}

const saveDraft = async () => {
  form.status = 0
  await doSave()
}

const publishArticle = async () => {
  await formRef.value.validate()
  form.status = 1
  await doSave()
}

const doSave = async () => {
  saving.value = true
  try {
    const res = await saveArticle(form)
    ElMessage.success(isEdit.value ? '更新成功' : '保存成功')
    hasUnsavedChanges.value = false
    if (!isEdit.value) {
      router.push(`/admin/article/edit/${res.data.id}`)
    }
  } finally {
    saving.value = false
  }
}

// 封面图上传
const userStore = useUserStore()
const handleCoverUpload = async ({ file }) => {
  try {
    let res
    if (userStore.isAdmin) {
      res = await uploadFile(file, 'cover')
    } else {
      res = await uploadUserImage(file)
    }
    form.coverImage = res.data.fileUrl
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

// 自动保存和未保存提示
const hasUnsavedChanges = ref(false)
const originalContent = ref('')

watch(() => form.content, (newVal) => {
  if (originalContent.value && newVal !== originalContent.value) {
    hasUnsavedChanges.value = true
  }
})

watch(() => form.title, () => {
  hasUnsavedChanges.value = true
})

// 自动保存草稿（每60秒）
let autoSaveTimer = null
const autoSaveDraft = async () => {
  if (form.title && form.content && hasUnsavedChanges.value && form.status === 0) {
    try {
      await saveArticle(form)
      hasUnsavedChanges.value = false
      console.log('草稿已自动保存')
    } catch (e) {
      console.error('自动保存失败', e)
    }
  }
}

// 页面离开前提示
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm('您有未保存的内容，确定要离开吗？', '提示', {
      confirmButtonText: '离开',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      next()
    }).catch(() => {
      next(false)
    })
  } else {
    next()
  }
})

onMounted(async () => {
  // 加载分类和标签
  const [catRes, tagRes] = await Promise.all([
    getAdminCategories(),
    getAdminTags()
  ])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []

  // 编辑模式加载文章
  if (route.params.id) {
    const res = await getAdminArticle(route.params.id)
    const article = res.data
    form.id = article.id
    form.title = article.title
    form.content = article.content || ''
    form.summary = article.summary || ''
    form.coverImage = article.coverImage || ''
    form.status = article.status
    form.isTop = article.isTop
    form.categoryId = article.categoryId
    form.tagIds = article.tags?.map(t => t.id) || []
    form.seoTitle = article.seoTitle || ''
    form.seoKeywords = article.seoKeywords || ''
    form.seoDescription = article.seoDescription || ''
    originalContent.value = article.content || ''
  }
  
  // 启动自动保存定时器
  autoSaveTimer = setInterval(autoSaveDraft, 60000)
})

onBeforeUnmount(() => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
  }
})
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.editor-wrapper {
  width: 100%;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.toolbar-tip {
  font-size: 14px;
  color: #606266;
}

.editor-container {
  display: flex;
  gap: 10px;
}

.editor-container.split-view .markdown-editor {
  width: 50%;
}

.markdown-editor :deep(textarea) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  line-height: 1.6;
}

.markdown-preview {
  width: 50%;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  max-height: 500px;
  overflow-y: auto;
  line-height: 1.8;
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3) {
  margin-top: 20px;
  margin-bottom: 10px;
}

.markdown-preview :deep(pre) {
  background: #f6f8fa;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-preview :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 15px;
  margin: 15px 0;
  color: #666;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}

.markdown-preview :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 15px 0;
}

.markdown-preview :deep(table th),
.markdown-preview :deep(table td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
}

.markdown-preview :deep(table th) {
  background: #f5f7fa;
  font-weight: bold;
}

.publish-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.cover-upload {
  display: flex;
  gap: 10px;
  align-items: center;
}

.cover-preview {
  margin-top: 10px;
  width: 100%;
  max-height: 150px;
  border-radius: 4px;
}
</style>

