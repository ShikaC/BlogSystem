import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    /**
     * 通过拆分 vendor chunk 降低单 chunk 体积，同时把 chunk 警告阈值调高以避免构建时噪音警告。
     * 这里只影响构建输出的分包与提示，不影响运行逻辑。
     */
    chunkSizeWarningLimit: 2000,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('element-plus')) return 'element-plus'
          if (id.includes('highlight.js')) return 'highlight'
          if (id.includes('marked')) return 'marked'
          if (id.includes('/vue/') || id.includes('/@vue/')) return 'vue-vendor'
          return 'vendor'
        }
      }
    }
  }
})
