<template>
  <div class="profile-page">
    <h2>个人信息</h2>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card header="基本信息">
          <el-form :model="profile" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="profile.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profile.nickname" />
            </el-form-item>
            <el-form-item label="头像URL">
              <el-input v-model="profile.avatar" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="profile.bio" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profile.email" />
            </el-form-item>
            <el-form-item label="GitHub">
              <el-input v-model="profile.github" />
            </el-form-item>
            <el-form-item label="知乎">
              <el-input v-model="profile.zhihu" />
            </el-form-item>
            <el-form-item label="微信">
              <el-input v-model="profile.weixin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveProfile" :loading="savingProfile">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card header="修改密码">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="savingPassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBloggerInfo, updateBloggerInfo, updatePassword } from '@/api/admin'
import { getUserInfo, updateUserInfo } from '@/api/front'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const profile = reactive({})
const savingProfile = ref(false)

const passwordFormRef = ref()
const passwordForm = reactive({ oldPassword: '', newPassword: '' })
const savingPassword = ref(false)

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
}

const loadProfile = async () => {
  const res = await getUserInfo()
  Object.assign(profile, res.data || {})
}

const handleSaveProfile = async () => {
  savingProfile.value = true
  try {
    await updateUserInfo(profile)
    ElMessage.success('保存成功')
    // 更新用户存储中的信息
    userStore.nickname = profile.nickname
    userStore.avatar = profile.avatar
  } finally {
    savingProfile.value = false
  }
}

const handleChangePassword = async () => {
  await passwordFormRef.value.validate()
  savingPassword.value = true
  try {
    await updatePassword(passwordForm)
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } finally {
    savingPassword.value = false
  }
}

onMounted(loadProfile)
</script>
