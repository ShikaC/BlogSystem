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
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="20"
                  placeholder="请输入文章内容（支持HTML）"
                />
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
              <el-input v-model="form.coverImage" placeholder="封面图URL" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminArticle, saveArticle, getAdminCategories, getAdminTags } from '@/api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const saving = ref(false)
const categories = ref([])
const tags = ref([])

const isEdit = computed(() => !!route.params.id)

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
    if (!isEdit.value) {
      router.push(`/admin/article/edit/${res.data.id}`)
    }
  } finally {
    saving.value = false
  }
}

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

.publish-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
