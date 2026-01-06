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
        <el-form-item label="网站公告">
          <el-input 
            v-model="config.site_announcement" 
            type="textarea" 
            :rows="3"
            placeholder="网站公告将在首页展示"
          />
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

    <el-card header="安全配置" style="margin-top: 20px;">
      <el-form :model="config" label-width="150px">
        <el-form-item label="JWT Token有效期">
          <el-input-number 
            v-model.number="jwtExpirationDays" 
            :min="1" 
            :max="365"
            @change="updateJwtExpiration"
          />
          <span style="margin-left: 10px; color: #909399;">天（当前：{{ jwtExpirationDays }}天）</span>
        </el-form-item>
        
        <el-divider content-position="left">密码强度要求</el-divider>
        
        <el-form-item label="最小长度">
          <el-input-number 
            v-model.number="config.password_min_length" 
            :min="6" 
            :max="20"
          />
          <span style="margin-left: 10px; color: #909399;">位</span>
        </el-form-item>
        <el-form-item label="要求大写字母">
          <el-switch 
            v-model="config.password_require_uppercase" 
            active-value="true" 
            inactive-value="false" 
          />
        </el-form-item>
        <el-form-item label="要求小写字母">
          <el-switch 
            v-model="config.password_require_lowercase" 
            active-value="true" 
            inactive-value="false" 
          />
        </el-form-item>
        <el-form-item label="要求数字">
          <el-switch 
            v-model="config.password_require_number" 
            active-value="true" 
            inactive-value="false" 
          />
        </el-form-item>
        <el-form-item label="要求特殊字符">
          <el-switch 
            v-model="config.password_require_special" 
            active-value="true" 
            inactive-value="false" 
          />
        </el-form-item>
        
        <el-divider content-position="left">敏感词库</el-divider>
        
        <el-form-item label="敏感词列表">
          <el-input 
            v-model="config.sensitive_words" 
            type="textarea" 
            :rows="8"
            placeholder="每行一个敏感词，用换行符分隔&#10;例如：&#10;敏感词1&#10;敏感词2"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            提示：敏感词将用于内容过滤，发布内容时自动检测并替换
          </div>
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
const jwtExpirationDays = ref(7)

const loadConfig = async () => {
  const res = await getSiteConfig()
  Object.assign(config, res.data || {})
  
  // 如果没有配置，初始化默认配置
  if (!res.data || Object.keys(res.data).length === 0) {
    await initSiteConfig()
    const res2 = await getSiteConfig()
    Object.assign(config, res2.data || {})
  }
  
  // 转换JWT有效期（毫秒转天）
  if (config.jwt_expiration) {
    jwtExpirationDays.value = Math.floor(parseInt(config.jwt_expiration) / (1000 * 60 * 60 * 24))
  }
}

const updateJwtExpiration = () => {
  // 将天数转换为毫秒
  config.jwt_expiration = String(jwtExpirationDays.value * 24 * 60 * 60 * 1000)
}

const handleSave = async () => {
  saving.value = true
  try {
    // 将数字转为字符串
    const data = { ...config }
    if (typeof data.article_page_size === 'number') {
      data.article_page_size = String(data.article_page_size)
    }
    if (typeof data.password_min_length === 'number') {
      data.password_min_length = String(data.password_min_length)
    }
    // 确保JWT有效期已更新
    updateJwtExpiration()
    await saveSiteConfig(data)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>
