import request from '@/utils/request'

// 后台文章管理
export const getAdminArticles = (params) => request.get('/admin/articles', { params })
export const getAdminArticle = (id) => request.get(`/admin/articles/${id}`)
export const saveArticle = (data) => request.post('/admin/articles', data)
export const deleteArticle = (id) => request.delete(`/admin/articles/${id}`)
export const batchDeleteArticles = (ids) => request.delete('/admin/articles/batch', { data: ids })
export const restoreArticle = (id) => request.post(`/admin/articles/${id}/restore`)
export const permanentDeleteArticle = (id) => request.delete(`/admin/articles/${id}/permanent`)
export const toggleTop = (id) => request.post(`/admin/articles/${id}/toggle-top`)

// 分类管理
export const getAdminCategories = () => request.get('/admin/categories')
export const createCategory = (params) => request.post('/admin/categories', null, { params })
export const updateCategory = (id, params) => request.put(`/admin/categories/${id}`, null, { params })
export const deleteCategory = (id) => request.delete(`/admin/categories/${id}`)

// 标签管理
export const getAdminTags = () => request.get('/admin/tags')
export const createTag = (params) => request.post('/admin/tags', null, { params })
export const updateTag = (id, params) => request.put(`/admin/tags/${id}`, null, { params })
export const deleteTag = (id) => request.delete(`/admin/tags/${id}`)

// 评论管理
export const getAdminComments = (params) => request.get('/admin/comments', { params })
export const replyComment = (params) => request.post('/admin/comments/reply', null, { params })
export const updateCommentStatus = (id, status) => request.post(`/admin/comments/${id}/status`, null, { params: { status } })
export const deleteComment = (id) => request.delete(`/admin/comments/${id}`)
export const batchDeleteComments = (ids) => request.delete('/admin/comments/batch', { data: ids })

// 友链管理
export const getAdminFriendLinks = () => request.get('/admin/friend-links')
export const createFriendLink = (params) => request.post('/admin/friend-links', null, { params })
export const updateFriendLink = (id, params) => request.put(`/admin/friend-links/${id}`, null, { params })
export const deleteFriendLink = (id) => request.delete(`/admin/friend-links/${id}`)

// 用户管理
export const getAllUsers = () => request.get('/admin/users')
export const getUserList = (params) => request.get('/admin/users/list', { params })
export const getUserDetail = (id) => request.get(`/admin/users/${id}/detail`)
export const getUserStatistics = () => request.get('/admin/users/statistics')
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const getCurrentAdmin = () => request.get('/admin/users/current')
export const updateAdminProfile = (data) => request.put('/admin/users/profile', data)
export const updateAdminPassword = (data) => request.put('/admin/users/password', data)

// 兼容旧后台页面：Profile.vue 仍使用 “博主信息” 接口名
export const getBloggerInfo = () => getCurrentAdmin()
export const updateBloggerInfo = (data) => updateAdminProfile(data)
export const updatePassword = (data) => updateAdminPassword(data)

// 媒体库
export const uploadFile = (file, category) => {
  const formData = new FormData()
  formData.append('file', file)
  if (category) formData.append('category', category)
  return request.post('/admin/media/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const getMediaList = (params) => request.get('/admin/media', { params })
export const deleteMedia = (id) => request.delete(`/admin/media/${id}`)

// 站点配置
export const getSiteConfig = () => request.get('/admin/config')
export const saveSiteConfig = (data) => request.post('/admin/config', data)
export const initSiteConfig = () => request.post('/admin/config/init')

// 统计数据
export const getStatistics = () => request.get('/admin/statistics')
export const getHotArticles = (limit) => request.get('/admin/statistics/hot-articles', { params: { limit } })

// 数据备份
export const exportArticlesMd = () => '/api/admin/backup/articles/markdown'
export const exportArticlesHtml = () => '/api/admin/backup/articles/html'
export const exportComments = () => '/api/admin/backup/comments'
export const exportAllData = () => '/api/admin/backup/all'

// 论坛管理
export const getAdminForumSections = () => request.get('/admin/forum/sections')
export const saveForumSection = (data) => request.post('/admin/forum/sections', data)
export const deleteForumSection = (id) => request.delete(`/admin/forum/sections/${id}`)

export const getAdminForumPosts = (params) => request.get('/admin/forum/posts', { params })
export const updateForumPostStatus = (id, status) => request.post(`/admin/forum/posts/${id}/status`, null, { params: { status } })
export const deleteForumPost = (id) => request.delete(`/admin/forum/posts/${id}`)
export const permanentDeleteForumPost = (id) => request.delete(`/admin/forum/posts/${id}/permanent`)
export const restoreForumPost = (id) => request.post(`/admin/forum/posts/${id}/restore`)
export const batchDeleteForumPosts = (ids) => request.delete('/admin/forum/posts/batch', { data: ids })
export const fixForumPostCounts = () => request.post('/admin/forum/fix-counts')
