<template>
  <div class="auth-layout">
    <!-- 背景动画 -->
    <div class="bg-animation">
      <div class="particle" v-for="i in 20" :key="i" :style="getParticleStyle(i)"></div>
    </div>

    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="48"><Van /></el-icon>
        </div>
        <h1 class="brand-title">汽车研发知识共享平台</h1>
        <p class="brand-desc">
          整合全流程知识资源，优化知识沉淀、共享与复用流程<br>
          提升研发效率、降低试错成本
        </p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>知识沉淀</span>
          </div>
          <div class="feature-item">
            <el-icon><Share /></el-icon>
            <span>高效共享</span>
          </div>
          <div class="feature-item">
            <el-icon><Search /></el-icon>
            <span>智能检索</span>
          </div>
          <div class="feature-item">
            <el-icon><TrendCharts /></el-icon>
            <span>数据分析</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-section">
      <div class="form-container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Van, Document, Share, Search, TrendCharts } from '@element-plus/icons-vue'

// 生成粒子动画样式
function getParticleStyle(index) {
  const size = Math.random() * 4 + 2
  const x = Math.random() * 100
  const y = Math.random() * 100
  const duration = Math.random() * 20 + 10
  const delay = Math.random() * 10

  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${x}%`,
    top: `${y}%`,
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`
  }
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
}

/* 背景动画 */
.bg-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.particle {
  position: absolute;
  background: var(--primary-color);
  border-radius: 50%;
  opacity: 0.3;
  animation: float linear infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0.3;
  }
  25% {
    transform: translateY(-30px) translateX(20px);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-10px) translateX(-20px);
    opacity: 0.3;
  }
  75% {
    transform: translateY(-50px) translateX(10px);
    opacity: 0.4;
  }
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-primary) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(ellipse at 30% 20%, rgba(24, 144, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(139, 92, 246, 0.12) 0%, transparent 50%);
  pointer-events: none;
}

.brand-content {
  text-align: center;
  max-width: 480px;
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #8b5cf6 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin: 0 auto 32px;
  box-shadow: 0 20px 60px rgba(24, 144, 255, 0.4);
  animation: pulse-glow 3s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 20px 60px rgba(24, 144, 255, 0.4);
  }
  50% {
    box-shadow: 0 20px 80px rgba(24, 144, 255, 0.6);
  }
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--primary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-desc {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 48px;
}

.features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  font-size: 14px;
  backdrop-filter: blur(10px);
  transition: all var(--transition-normal);
}

.feature-item:hover {
  background: var(--primary-bg);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-2px);
}

.feature-item .el-icon {
  font-size: 20px;
}

/* 右侧表单区 */
.form-section {
  width: 520px;
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  border-left: 1px solid var(--border-color);
  position: relative;
  z-index: 1;
}

.form-container {
  width: 100%;
  max-width: 360px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 响应式 */
@media (max-width: 1024px) {
  .brand-section {
    display: none;
  }
  
  .form-section {
    width: 100%;
    border-left: none;
  }
}
</style>
