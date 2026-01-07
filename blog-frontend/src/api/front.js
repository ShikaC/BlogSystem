import request from '@/utils/request'

// 前台内容获取
export const getArticles = (params) => request.get('/front/articles', { params })
export const getArticle = (id) => request.get(`/front/articles/${id}`)
export const getRelatedArticles = (id, limit) => request.get(`/front/articles/${id}/related`, { params: { limit } })
export const getArticleComments = (id) => request.get(`/front/articles/${id}/comments`)
export const getCategories = () => request.get('/front/categories')
export const getCategory = (id) => request.get(`/front/categories/${id}`)
export const getTags = () => request.get('/front/tags')
export const getTag = (id) => request.get(`/front/tags/${id}`)

export const getArchives = () => request.get('/front/archives')
export const getArticlesByYearMonth = (year, month, params) => request.get(`/front/archives/${year}/${month}`, { params })
export const getFriendLinks = () => request.get('/front/friend-links')
// 修复：添加确实的分类和标签文章获取接口
export const getArticlesByCategory = (id, params) => request.get(`/front/articles/category/${id}`, { params })
export const getArticlesByTag = (id, params) => request.get(`/front/articles/tag/${id}`, { params })

export const getHotArticles = (limit) => request.get('/front/articles/hot', { params: { limit } })
export const getBloggerInfo = () => request.get('/front/blogger')
export const getUserInfo = () => request.get('/front/user/info')
export const getFrontConfig = () => request.get('/front/config')

// 互动功能
export const likeArticle = (id) => request.post(`/front/articles/${id}/like`)
export const unlikeArticle = (id) => request.delete(`/front/articles/${id}/like`)
export const collectArticle = (id) => request.post(`/front/articles/${id}/collect`)
export const uncollectArticle = (id) => request.delete(`/front/articles/${id}/collect`)
export const checkArticleStatus = (id) => request.get(`/front/articles/${id}/status`)

// 评论功能
export const createComment = (data) => request.post('/front/comments', data)
export const deleteComment = (id) => request.delete(`/front/comments/${id}`)
export const likeComment = (id) => request.post(`/front/comments/${id}/like`)
export const unlikeComment = (id) => request.delete(`/front/comments/${id}/like`)

// 用户相关
export const getMyArticles = (params) => request.get('/front/user/articles', { params })
export const getMyArticlesFiltered = (params) => request.get('/front/user/articles/filter', { params })
export const getMyPosts = (params) => request.get('/front/user/posts', { params })
export const getMyNotifications = (params) => request.get('/front/user/notifications', { params })
export const getUserProfile = () => request.get('/front/user/profile')
export const updateUserProfile = (data) => request.put('/front/user/profile', data)
export const getMyLikedArticles = (params) => request.get('/front/user/likes', { params: { ...params, type: 'ARTICLE' } })
export const getMyCollectedArticles = (params) => request.get('/front/user/favorites', { params: { ...params, type: 'ARTICLE' } })

// 用户统计
export const getUserStatistics = () => request.get('/front/user/statistics')

// 用户图片上传
export const uploadUserImage = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/front/user/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 批量操作
export const batchUnpublishArticles = (ids) => request.post('/front/user/articles/batch-unpublish', ids)
export const batchDeleteMyArticles = (ids) => request.post('/front/user/articles/batch-delete', ids)

// 公开用户主页
export const getPublicUserInfo = (userId) => request.get(`/front/user/public/${userId}`)
export const getUserPublicArticles = (userId, params) => request.get(`/front/user/public/${userId}/articles`, { params })
export const getUserPublicLikes = (userId, params) => request.get(`/front/user/public/${userId}/likes`, { params })
export const getUserPublicFavorites = (userId, params) => request.get(`/front/user/public/${userId}/favorites`, { params })


// 论坛相关
export const getForumSections = () => request.get('/front/forum/sections')
export const getForumPosts = (params) => request.get('/front/forum/posts', { params })
export const getForumPost = (id) => request.get(`/front/forum/posts/${id}`)
export const getForumPostComments = (id, params) => request.get(`/front/forum/posts/${id}/comments`, { params })
// 论坛互动 - 需要登录，路径在 /front/user/forum
export const savePost = (data) => request.post('/front/user/forum/posts', data)
export const deletePost = (id) => request.delete(`/front/user/forum/posts/${id}`)
export const likePost = (id) => request.post(`/front/user/forum/posts/${id}/like`)
export const unlikePost = (id) => request.delete(`/front/user/forum/posts/${id}/like`)
export const collectPost = (id) => request.post(`/front/user/forum/posts/${id}/collect`)
export const uncollectPost = (id) => request.delete(`/front/user/forum/posts/${id}/collect`)
export const checkPostStatus = (id) => request.get(`/front/user/forum/posts/${id}/status`)
export const createPostComment = (data) => request.post('/front/user/forum/comments', data)
export const deletePostComment = (id) => request.delete(`/front/user/forum/comments/${id}`)

// 搜索
export const searchContent = (params) => request.get('/front/search', { params })
