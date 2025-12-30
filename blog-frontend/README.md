# 前端（blog-frontend）

本目录为 Vue 3 + Vite 前端工程，对接后端 `server.servlet.context-path=/api`（默认后端地址：`http://localhost:8080/api`）。

## 启动

```bash
cd blog-frontend
npm install
npm run dev
```

## 构建

```bash
npm run build
```

## 说明

- 本次一体化改造后，后端新增了统一入口（不影响旧接口）：
  - **全站搜索**：`GET /front/search`
  - **统一分类/版块导航**：`GET /front/unified/categories`
  - **个人中心聚合**：`GET /front/user/favorites` / `GET /front/user/likes` / `GET /front/user/comments`
- 旧接口仍保留，可按需逐步迁移前端请求。
