<template>
  <div class="audit-page">
    <div class="page-header">
      <h1 class="page-title">知识审核</h1>
      <div class="header-actions">
        <el-radio-group v-model="auditStatus" @change="handleStatusChange">
          <el-radio-button label="pending">待审核 ({{ pendingCount }})</el-radio-button>
          <el-radio-button label="approved">已通过</el-radio-button>
          <el-radio-button label="rejected">已拒绝</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 审核列表 -->
    <div class="audit-list">
      <el-card v-for="item in auditList" :key="item.id" class="audit-card">
        <div class="audit-header">
          <div class="author-info">
            <el-avatar :size="40">{{ item.author?.charAt(0) }}</el-avatar>
            <div class="author-detail">
              <span class="author-name">{{ item.author }}</span>
              <span class="submit-time">{{ item.submitTime }} 提交</span>
            </div>
          </div>
          <el-tag :type="getStatusType(item.status)" size="large">{{ getStatusLabel(item.status) }}</el-tag>
        </div>

        <div class="audit-content">
          <h3 class="knowledge-title">{{ item.title }}</h3>
          <p class="knowledge-summary">{{ item.summary }}</p>
          <div class="knowledge-meta">
            <el-tag size="small">{{ item.type }}</el-tag>
            <el-tag size="small" type="info">{{ item.domain }}</el-tag>
            <span class="attachment-count" v-if="item.attachmentCount">
              <el-icon><Document /></el-icon> {{ item.attachmentCount }} 个附件
            </span>
          </div>
        </div>

        <div class="audit-actions" v-if="item.status === 'pending'">
          <el-button @click="previewKnowledge(item)">
            <el-icon><View /></el-icon>
            预览内容
          </el-button>
          <el-button type="success" @click="handleApprove(item)">
            <el-icon><Check /></el-icon>
            通过审核
          </el-button>
          <el-button type="danger" @click="handleReject(item)">
            <el-icon><Close /></el-icon>
            拒绝
          </el-button>
        </div>

        <div class="audit-result" v-else>
          <template v-if="item.status === 'approved'">
            <el-icon class="result-icon success"><CircleCheck /></el-icon>
            <span>由 {{ item.auditor }} 于 {{ item.auditTime }} 审核通过</span>
          </template>
          <template v-else>
            <el-icon class="result-icon danger"><CircleClose /></el-icon>
            <span>拒绝原因：{{ item.rejectReason }}</span>
          </template>
        </div>
      </el-card>
    </div>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="500px" destroy-on-close>
      <el-form>
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectReason" type="textarea" rows="4" placeholder="请输入拒绝原因，将通知作者" />
        </el-form-item>
        <el-form-item label="常用原因">
          <div class="quick-reasons">
            <el-tag v-for="reason in quickReasons" :key="reason" @click="rejectReason = reason">{{ reason }}</el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, View, Check, Close, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { getAuditList, approveKnowledge, rejectKnowledge } from '@/api/admin'
import { useRouter } from 'vue-router'

defineOptions({
  name: 'KnowledgeAudit'
})

const router = useRouter()

const auditStatus = ref('pending')
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentItem = ref(null)

const quickReasons = [
  '内容质量不达标',
  '信息不完整',
  '与已有知识重复',
  '涉及敏感信息',
  '格式需要调整'
]

// 数据
const auditList = ref([])
const pendingCount = ref(0)
const total = ref(0)
const currentPage = ref(1)

async function loadAuditList() {
  try {
    const res = await getAuditList(auditStatus.value, currentPage.value, 100) // load more for brevity
    if (res.code === 200) {
      auditList.value = res.data.list.map(item => ({
        id: item.id,
        title: item.title,
        summary: item.summary,
        type: item.type,
        domain: item.domain,
        author: item.authorName,
        submitTime: item.createTime, // actually createTime
        status: auditStatus.value, // binded string status
        attachmentCount: item.attachmentCount || 0,
        auditor: item.auditorName,
        auditTime: item.auditTime,
        rejectReason: item.rejectReason
      }))
      total.value = res.data.total
      // If we're on pending tab, update pending count
      if (auditStatus.value === 'pending') {
        pendingCount.value = res.data.total
      }
    }
  } catch (error) {
    ElMessage.error('查询数据失败: ' + error.message)
  }
}

function handleStatusChange() {
  currentPage.value = 1
  loadAuditList()
}

onMounted(() => {
  loadAuditList()
})

function getStatusType(status) {
  const types = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return types[status]
}

function getStatusLabel(status) {
  const labels = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  return labels[status]
}

function previewKnowledge(item) {
  const routeUrl = router.resolve(`/knowledge/detail/${item.id}`)
  window.open(routeUrl.href, '_blank')
}

async function handleApprove(item) {
  try {
    await ElMessageBox.confirm(`确定通过《${item.title}》的审核？`, '确认审核')
    const res = await approveKnowledge(item.id)
    if (res.code === 200) {
      ElMessage.success('审核通过')
      loadAuditList()
    } else {
      ElMessage.error(res.message)
    }
  } catch {}
}

function handleReject(item) {
  currentItem.value = item
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  const res = await rejectKnowledge(currentItem.value.id, rejectReason.value)
  if (res.code === 200) {
    ElMessage.success('已拒绝该知识')
    rejectDialogVisible.value = false
    loadAuditList()
  } else {
    ElMessage.error(res.message)
  }
}
</script>

<style scoped>
.audit-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.audit-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.audit-card {
  transition: all var(--transition-fast);
}

.audit-card:hover {
  border-color: var(--primary-color);
}

.audit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-weight: 600;
  color: var(--text-primary);
}

.submit-time {
  font-size: 13px;
  color: var(--text-tertiary);
}

.audit-content {
  padding: 16px 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.knowledge-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.knowledge-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.knowledge-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.attachment-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-tertiary);
}

.audit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.audit-result {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}

.result-icon {
  font-size: 18px;
}

.result-icon.success {
  color: var(--success-color);
}

.result-icon.danger {
  color: var(--danger-color);
}

.quick-reasons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-reasons .el-tag {
  cursor: pointer;
}

.quick-reasons .el-tag:hover {
  background: var(--primary-bg);
  border-color: var(--primary-color);
  color: var(--primary-color);
}
</style>
