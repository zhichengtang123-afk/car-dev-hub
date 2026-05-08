<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1 class="page-title">我的收藏</h1>
    </div>
    <div class="favorites-list" v-loading="loading">
      <div class="favorite-item" v-for="item in favoriteList" :key="item.id" @click="$router.push(`/knowledge/detail/${item.id}`)">
        <div class="item-main">
          <div class="item-header">
            <el-tag size="small">{{ item.type }}</el-tag>
            <span class="domain">{{ item.domain }}</span>
          </div>
          <h3 class="item-title">{{ item.title }}</h3>
          <p class="item-summary">{{ item.summary }}</p>
        </div>
        <el-button type="danger" text @click.stop="handleRemove(item)">取消收藏</el-button>
      </div>
    </div>
    <el-empty v-if="!loading && !favoriteList.length" description="暂无收藏" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyCollections, uncollectKnowledge } from '@/api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const favoriteList = ref([])

async function loadFavorites() {
  loading.value = true
  try {
    const res = await getMyCollections({ page: 1, size: 100 })
    if (res?.data?.list) {
      favoriteList.value = res.data.list
    }
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

async function handleRemove(item) {
  try {
    await ElMessageBox.confirm('确定取消收藏吗？', '提示')
    await uncollectKnowledge(item.id)
    favoriteList.value = favoriteList.value.filter(f => f.id !== item.id)
    ElMessage.success('已取消收藏')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page { max-width: 1000px; margin: 0 auto; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 28px; font-weight: 700; color: var(--text-primary); }
.favorites-list { display: flex; flex-direction: column; gap: 16px; }
.favorite-item { display: flex; justify-content: space-between; padding: 20px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); cursor: pointer; transition: all var(--transition-normal); }
.favorite-item:hover { border-color: var(--primary-color); box-shadow: var(--shadow-glow); }
.item-main { flex: 1; }
.item-header { display: flex; gap: 12px; margin-bottom: 12px; }
.domain { font-size: 12px; color: var(--text-tertiary); }
.item-title { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 8px; }
.item-summary { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
</style>
