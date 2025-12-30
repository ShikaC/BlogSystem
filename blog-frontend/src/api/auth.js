import request from '@/utils/request'

// 认证相关
export const login = (data) => request.post('/auth/login', data)
export const initBlogger = (params) => request.post('/auth/init', null, { params })

// 验证码
export const getCaptcha = () => request.get('/captcha')
