import request from '@/utils/request'

// 文章列表
export const getArticles = (params) => request.get('/front/articles', { params })
export const getArticle = (id) => request.get(`/front/articles/${id}`)
export const getArticlesByCategory = (categoryId, params) => request.get(`/front/articles/category/${categoryId}`, { params })
export const getArticlesByTag = (tagId, params) => request.get(`/front/articles/tag/${tagId}`, { params })
export const searchArticles = (params) => request.get('/front/articles/search', { params })
export const getHotArticles = (limit = 10) => request.get('/front/articles/hot', { params: { limit } })
export const getRelatedArticles = (id, limit = 5) => request.get(`/front/articles/${id}/related`, { params: { limit } })
export const verifyArticlePassword = (id, password) => request.post(`/front/articles/${id}/verify-password`, null, { params: { password } })

// 互动
export const likeArticle = (id) => request.post(`/front/articles/${id}/like`)
export const unlikeArticle = (id) => request.delete(`/front/articles/${id}/like`)
export const collectArticle = (id) => request.post(`/front/articles/${id}/collect`)
export const uncollectArticle = (id) => request.delete(`/front/articles/${id}/collect`)

// 归档
export const getArchives = () => request.get('/front/archives')
export const getArticlesByYearMonth = (year, month, params) => request.get(`/front/archives/${year}/${month}`, { params })

// 分类标签
export const getCategories = () => request.get('/front/categories')
export const getCategory = (id) => request.get(`/front/categories/${id}`)
export const getTags = () => request.get('/front/tags')
export const getTag = (id) => request.get(`/front/tags/${id}`)

// 评论
export const getArticleComments = (articleId) => request.get(`/front/articles/${articleId}/comments`)
export const getLatestComments = () => request.get('/front/comments/latest')
export const createComment = (data) => request.post('/front/comments', data)

// 其他
export const getAdminInfo = () => request.get('/front/admin-info')
// 兼容旧代码：历史页面仍调用 getBloggerInfo（实际返回站点管理员信息）
export const getBloggerInfo = () => request.get('/front/admin-info')
export const getFriendLinks = () => request.get('/front/friend-links')
export const getFrontConfig = () => request.get('/front/config')

// 论坛相关
export const getForumSections = () => request.get('/front/forum/sections')
export const getForumPosts = (params) => request.get('/front/forum/posts', { params })
export const getForumPostDetail = (id) => request.get(`/front/forum/posts/${id}`)

// 个人中心 (需要登录)
export const getUserProfile = () => request.get('/front/user/profile')
export const getMyArticles = (params) => request.get('/front/user/articles', { params })
export const getMyPosts = (params) => request.get('/front/user/posts', { params })
export const getMyNotifications = (params) => request.get('/front/user/notifications', { params })

// 用户互动 (需要登录)
export const savePost = (data) => request.post('/front/user/forum/posts', data)
export const deletePost = (id) => request.delete(`/front/user/forum/posts/${id}`)
export const createPostComment = (data) => request.post('/front/user/forum/comments', data)
export const likePost = (id) => request.post(`/front/user/forum/posts/${id}/like`)
export const collectPost = (id) => request.post(`/front/user/forum/posts/${id}/collect`)

// 用户信息 (需要登录)
export const getUserInfo = () => request.get('/front/user/profile')
export const updateUserInfo = (data) => request.put('/front/user/profile', data)
