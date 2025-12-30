<template>
  <div class="settings-page">
    <h2>系统设置</h2>

    <el-card header="站点信息">
      <el-form :model="config" label-width="120px">
        <el-form-item label="博客名称">
          <el-input v-model="config.site_name" />
        </el-form-item>
        <el-form-item label="博客副标题">
          <el-input v-model="config.site_subtitle" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="config.site_logo" />
        </el-form-item>
        <el-form-item label="页脚版权信息">
          <el-input v-model="config.site_footer" type="textarea" />
        </el-form-item>
        <el-form-item label="备案信息">
          <el-input v-model="config.site_icp" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card header="功能开关" style="margin-top: 20px;">
      <el-form :model="config" label-width="120px">
        <el-form-item label="开启评论">
          <el-switch v-model="config.comment_enabled" active-value="true" inactive-value="false" />
        </el-form-item>
        <el-form-item label="显示阅读量">
          <el-switch v-model="config.view_count_enabled" active-value="true" inactive-value="false" />
        </el-form-item>
        <el-form-item label="显示点赞量">
          <el-switch v-model="config.like_count_enabled" active-value="true" inactive-value="false" />
        </el-form-item>
        <el-form-item label="首页文章条数">
          <el-input-number v-model.number="config.article_page_size" :min="5" :max="50" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card header="SEO设置" style="margin-top: 20px;">
      <el-form :model="config" label-width="120px">
        <el-form-item label="SEO标题">
          <el-input v-model="config.seo_title" />
        </el-form-item>
        <el-form-item label="SEO关键词">
          <el-input v-model="config.seo_keywords" />
        </el-form-item>
        <el-form-item label="SEO描述">
          <el-input v-model="config.seo_description" type="textarea" />
        </el-form-item>
      </el-form>
    </el-card>

    <div style="margin-top: 20px; text-align: right;">
      <el-button type="primary" @click="handleSave" :loading="saving">保存设置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSiteConfig, saveSiteConfig, initSiteConfig } from '@/api/admin'
import { ElMessage } from 'element-plus'

const config = reactive({})
const saving = ref(false)

const loadConfig = async () => {
  const res = await getSiteConfig()
  Object.assign(config, res.data || {})
  
  // 如果没有配置，初始化默认配置
  if (!res.data || Object.keys(res.data).length === 0) {
    await initSiteConfig()
    const res2 = await getSiteConfig()
    Object.assign(config, res2.data || {})
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    // 将数字转为字符串
    const data = { ...config }
    if (typeof data.article_page_size === 'number') {
      data.article_page_size = String(data.article_page_size)
    }
    await saveSiteConfig(data)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>
