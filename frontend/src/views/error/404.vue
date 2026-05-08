<template>
  <div class="not-found-page">
    <div class="content">
      <div class="error-code">404</div>
      <h1 class="title">页面不存在</h1>
      <p class="desc">抱歉，您访问的页面不存在或已被移除</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="$router.push('/')">返回首页</el-button>
        <el-button size="large" @click="$router.back()">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.error-code {
  font-size: 120px;
  font-weight: 900;
  background: linear-gradient(135deg, var(--primary-color) 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1;
  margin-bottom: 24px;
}
.title {
  font-size: 32px;
  color: var(--text-primary);
  margin-bottom: 16px;
}
.desc {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}
.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>
