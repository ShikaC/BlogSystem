<template>
  <div class="login-page">
    <div class="login-card">
      <h2>博客后台登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.rememberMe">记住登录状态</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <div class="init-link">
          <el-link type="info" @click="showInitDialog = true">初始化博主账号</el-link>
        </div>
      </el-form>
    </div>

    <!-- 初始化博主账号对话框 -->
    <el-dialog v-model="showInitDialog" title="初始化博主账号" width="400px" destroy-on-close>
      <el-form :model="initForm" :rules="initRules" ref="initFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="initForm.username" placeholder="设置后台登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="initForm.password" type="password" placeholder="设置后台登录密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="initForm.nickname" placeholder="设置博主昵称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showInitDialog = false">取消</el-button>
        <el-button type="primary" :loading="initLoading" @click="handleInit">立即初始化</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login, initBlogger } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setUser(res.data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/admin'
    router.push(redirect)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 初始化相关
const showInitDialog = ref(false)
const initLoading = ref(false)
const initFormRef = ref()
const initForm = reactive({
  username: '',
  password: '',
  nickname: ''
})

const initRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const handleInit = async () => {
  await initFormRef.value.validate()
  initLoading.value = true
  try {
    await initBlogger(initForm)
    ElMessage.success('账号初始化成功，请登录')
    showInitDialog.value = false
    form.username = initForm.username
  } catch (e) {
    console.error(e)
  } finally {
    initLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: #fff;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  width: 100%;
  max-width: 400px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.init-link {
  text-align: center;
  margin-top: 15px;
}
</style>
