<template>
  <div class="login-page">
    <div class="login-card">
      <h2>{{ isRegister ? '新用户注册' : '欢迎登录' }}</h2>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleSubmit">
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
        <el-form-item v-if="isRegister" prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item v-if="!isRegister">
          <el-checkbox v-model="form.rememberMe">记住登录状态</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit" style="width: 100%">
            {{ isRegister ? '立即注册' : '登录' }}
          </el-button>
        </el-form-item>
        <div class="auth-footer">
          <el-link type="primary" @click="toggleMode">{{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}</el-link>
          <div class="init-link" v-if="!isRegister">
            <el-link type="info" @click="showInitDialog = true">初始化管理员账号</el-link>
          </div>
        </div>
      </el-form>
    </div>

    <!-- 初始化管理员账号对话框 -->
    <el-dialog v-model="showInitDialog" title="初始化系统管理员" width="400px" destroy-on-close>
      <el-form :model="initForm" :rules="initRules" ref="initFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="initForm.username" placeholder="管理员用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="initForm.password" type="password" placeholder="管理密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="initForm.nickname" placeholder="管理员昵称" />
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login, register, initAdmin } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const isRegister = ref(route.path === '/register')

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  rememberMe: false
})

// 重要：登录模式不校验 nickname，否则会导致 validate 失败，从而“点击无反应/不跳转”
const rules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ],
  ...(isRegister.value ? { nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }] } : {})
}))

const toggleMode = () => {
  isRegister.value = !isRegister.value
  router.push(isRegister.value ? '/register' : '/login')
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isRegister.value) {
      await register({
        username: form.username,
        password: form.password,
        nickname: form.nickname
      })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
      router.push('/login')
    } else {
      const res = await login(form)
      userStore.setUser(res.data)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || (userStore.role === 'ADMIN' ? '/admin' : '/')
      router.push(redirect)
    }
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
    await initAdmin(initForm)
    ElMessage.success('系统初始化成功，请登录')
    showInitDialog.value = false
    form.username = initForm.username
  } catch (e) {
    console.error(e)
  } finally {
    initLoading.value = false
  }
}

onMounted(() => {
  isRegister.value = route.path === '/register'
})

watch(
  () => route.path,
  (p) => {
    isRegister.value = p === '/register'
  }
)
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
