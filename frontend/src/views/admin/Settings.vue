<template>
  <div class="settings-page">
    <div class="page-header">
      <h1 class="page-title">系统配置</h1>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 知识分类管理 -->
      <el-tab-pane label="知识分类" name="category">
        <div class="tab-header">
          <span>管理知识的分类类型</span>
          <el-button type="primary" size="small" @click="showAddDialog('category')">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
        <el-table :data="categories" style="width: 100%">
          <el-table-column prop="name" label="分类名称" min-width="150" />
          <el-table-column prop="code" label="分类代码" width="120" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="count" label="知识数量" width="100" align="center" />
          <el-table-column prop="sort" label="排序" width="80" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="handleToggleStatus(row, 'category')" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row, 'category')">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row, 'category')">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 知识领域管理 -->
      <el-tab-pane label="知识领域" name="domain">
        <div class="tab-header">
          <span>管理知识的专业领域</span>
          <el-button type="primary" size="small" @click="showAddDialog('domain')">
            <el-icon><Plus /></el-icon>
            新增领域
          </el-button>
        </div>
        <el-table :data="domains" style="width: 100%">
          <el-table-column prop="name" label="领域名称" min-width="150" />
          <el-table-column prop="code" label="领域代码" width="120" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="count" label="知识数量" width="100" align="center" />
          <el-table-column label="颜色" width="100" align="center">
            <template #default="{ row }">
              <div class="color-preview" :style="{ background: row.color }"></div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row, 'domain')">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row, 'domain')">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 标签管理 -->
      <el-tab-pane label="标签管理" name="tag">
        <div class="tab-header">
          <span>管理知识标签</span>
          <el-button type="primary" size="small" @click="showAddDialog('tag')">
            <el-icon><Plus /></el-icon>
            新增标签
          </el-button>
        </div>
        <div class="tag-list">
          <div class="tag-group" v-for="group in tagGroups" :key="group.name">
            <h4 class="group-name">{{ group.name }}</h4>
            <div class="tags">
              <el-tag
                v-for="tag in group.tags"
                :key="tag.id"
                :type="tag.type"
                closable
                @close="handleDeleteTag(tag)"
              >
                {{ tag.name }} ({{ tag.count }})
              </el-tag>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 系统设置 -->
      <el-tab-pane label="系统设置" name="system">
        <el-form label-width="150px" class="settings-form">
          <el-divider content-position="left">基础设置</el-divider>
          <el-form-item label="平台名称">
            <el-input v-model="systemSettings.siteName" style="width: 400px" />
          </el-form-item>
          <el-form-item label="平台描述">
            <el-input v-model="systemSettings.siteDesc" type="textarea" rows="3" style="width: 400px" />
          </el-form-item>

          <el-divider content-position="left">知识设置</el-divider>
          <el-form-item label="知识审核">
            <el-switch v-model="systemSettings.enableAudit" />
            <span class="form-tip">开启后，用户上传的知识需要管理员审核后才能发布</span>
          </el-form-item>
          <el-form-item label="允许评论">
            <el-switch v-model="systemSettings.enableComment" />
          </el-form-item>
          <el-form-item label="附件大小限制">
            <el-input-number v-model="systemSettings.maxFileSize" :min="1" :max="100" />
            <span class="form-tip">MB，单个附件最大上传大小</span>
          </el-form-item>

          <el-divider content-position="left">积分设置</el-divider>
          <el-form-item label="上传知识积分">
            <el-input-number v-model="systemSettings.uploadScore" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="回答问题积分">
            <el-input-number v-model="systemSettings.answerScore" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="被采纳积分">
            <el-input-number v-model="systemSettings.acceptedScore" :min="0" :max="100" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSaveSettings">保存设置</el-button>
            <el-button @click="handleResetSettings">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="formData" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="代码" v-if="currentType !== 'tag'">
          <el-input v-model="formData.code" placeholder="请输入唯一代码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="颜色" v-if="currentType === 'domain'">
          <el-color-picker v-model="formData.color" />
        </el-form-item>
        <el-form-item label="排序" v-if="currentType === 'category'">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllSettings, saveAllSettings } from '@/api/admin'

defineOptions({
  name: 'SystemSettings'
})

const activeTab = ref('category')
const dialogVisible = ref(false)
const currentType = ref('')
const isEdit = ref(false)
const formData = ref({})

const dialogTitle = computed(() => {
  const typeNames = { category: '分类', domain: '领域', tag: '标签' }
  return (isEdit.value ? '编辑' : '新增') + typeNames[currentType.value]
})

// 知识分类
const categories = ref([])

// 知识领域
const domains = ref([])

// 标签分组
const tagGroups = ref([])

// 系统设置
const systemSettings = ref({
  siteName: '汽车研发知识共享平台',
  siteDesc: '整合研发全流程知识资源，打造汽车行业知识共享生态',
  enableAudit: true,
  enableComment: true,
  maxFileSize: 50,
  uploadScore: 10,
  answerScore: 5,
  acceptedScore: 20
})

const defaultCategories = [
  { id: 1, name: '技术文档', code: 'tech_doc', description: '技术规范、设计文档、开发指南等', count: 485, sort: 1, enabled: true },
  { id: 2, name: '设计方案', code: 'design', description: '系统设计方案、架构设计、详细设计等', count: 286, sort: 2, enabled: true },
  { id: 3, name: '测试报告', code: 'test_report', description: '测试用例、测试报告、验证报告等', count: 198, sort: 3, enabled: true },
  { id: 4, name: '故障解决方案', code: 'solution', description: '故障分析、问题解决、经验总结等', count: 156, sort: 4, enabled: true },
  { id: 5, name: '仿真数据', code: 'simulation', description: '仿真模型、仿真结果、分析报告等', count: 86, sort: 5, enabled: true }
]

const defaultDomains = [
  { id: 1, name: '软件算法', code: 'software', description: '嵌入式软件、算法、控制策略等', count: 320, color: '#667eea' },
  { id: 2, name: '电子电控', code: 'electric', description: '电子电气架构、控制器、线束等', count: 280, color: '#38ef7d' },
  { id: 3, name: '动力系统', code: 'powertrain', description: '电机、电池、变速箱、发动机等', count: 256, color: '#f5576c' },
  { id: 4, name: '底盘工程', code: 'chassis', description: '悬架、转向、制动等底盘系统', count: 210, color: '#4facfe' },
  { id: 5, name: '整车设计', code: 'vehicle', description: '整车集成、NVH、热管理等', count: 190, color: '#fa8231' }
]

const defaultTagGroups = [
  { name: '技术标签', tags: [
    { id: 1, name: 'AUTOSAR', count: 45, type: '' },
    { id: 2, name: '机器学习', count: 38, type: '' },
    { id: 3, name: '车联网', count: 32, type: '' },
    { id: 4, name: 'CAN总线', count: 28, type: '' },
    { id: 5, name: '以太网', count: 25, type: '' }
  ]},
  { name: '产品标签', tags: [
    { id: 6, name: '新能源', count: 120, type: 'success' },
    { id: 7, name: '智能驾驶', count: 95, type: 'success' },
    { id: 8, name: '电动化', count: 78, type: 'success' }
  ]},
  { name: '状态标签', tags: [
    { id: 9, name: '已验证', count: 56, type: 'info' },
    { id: 10, name: '最佳实践', count: 34, type: 'warning' },
    { id: 11, name: '需更新', count: 12, type: 'danger' }
  ]}
]

onMounted(async () => {
  await fetchAllSettings()
})

async function fetchAllSettings() {
  try {
    const res = await getAllSettings()
    if (res.code === 200 && res.data) {
      categories.value = res.data.categories || defaultCategories
      domains.value = res.data.domains || defaultDomains
      tagGroups.value = res.data.tagGroups || defaultTagGroups
      if (res.data.systemSettings) {
        systemSettings.value = { ...systemSettings.value, ...res.data.systemSettings }
      }
    }
  } catch (error) {
    console.error('获取设置失败', error)
  }
}

async function doSaveAllSettings(quiet = false) {
  try {
    await saveAllSettings({
      categories: categories.value,
      domains: domains.value,
      tagGroups: tagGroups.value,
      systemSettings: systemSettings.value
    })
    if (!quiet) {
      ElMessage.success('设置已保存')
    }
  } catch (error) {
    if (!quiet) {
      ElMessage.error('保存失败')
    }
  }
}

function showAddDialog(type) {
  currentType.value = type
  isEdit.value = false
  formData.value = { name: '', code: '', description: '', color: '#1890ff', sort: 0 }
  dialogVisible.value = true
}

function handleEdit(row, type) {
  currentType.value = type
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleDelete(row, type) {
  ElMessageBox.confirm(`确定删除 "${row.name}" 吗？`, '确认删除', { type: 'warning' })
    .then(() => {
      if (type === 'category') {
        categories.value = categories.value.filter(item => item.id !== row.id)
      } else if (type === 'domain') {
        domains.value = domains.value.filter(item => item.id !== row.id)
      }
      doSaveAllSettings(true)
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

function handleDeleteTag(tag) {
  tagGroups.value.forEach(group => {
    group.tags = group.tags.filter(t => t.id !== tag.id)
  })
  doSaveAllSettings(true)
  ElMessage.success(`已删除标签: ${tag.name}`)
}

function handleToggleStatus(row, type) {
  doSaveAllSettings(true)
  ElMessage.success(row.enabled ? '已启用' : '已禁用')
}

function handleSubmit() {
  if (!formData.value.name) {
    ElMessage.warning('请输入名称')
    return
  }
  
  if (currentType.value === 'category') {
    if (isEdit.value) {
      const index = categories.value.findIndex(c => c.id === formData.value.id)
      if (index !== -1) categories.value[index] = { ...formData.value }
    } else {
      categories.value.push({ ...formData.value, id: Date.now(), count: 0, enabled: true })
    }
  } else if (currentType.value === 'domain') {
    if (isEdit.value) {
      const index = domains.value.findIndex(d => d.id === formData.value.id)
      if (index !== -1) domains.value[index] = { ...formData.value }
    } else {
      domains.value.push({ ...formData.value, id: Date.now(), count: 0 })
    }
  } else if (currentType.value === 'tag') {
    // 简化的标签添加逻辑，实际中可能需要选分组
    if (!isEdit.value) {
      tagGroups.value[0].tags.push({ ...formData.value, id: Date.now(), count: 0, type: '' })
    }
  }

  doSaveAllSettings(true)
  ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
  dialogVisible.value = false
}

async function handleSaveSettings() {
  await doSaveAllSettings(false)
}

function handleResetSettings() {
  ElMessageBox.confirm('确定恢复默认设置吗？', '提示')
    .then(async () => {
      systemSettings.value = {
        siteName: '汽车研发知识共享平台',
        siteDesc: '整合研发全流程知识资源，打造汽车行业知识共享生态',
        enableAudit: true,
        enableComment: true,
        maxFileSize: 50,
        uploadScore: 10,
        answerScore: 5,
        acceptedScore: 20
      }
      categories.value = [...defaultCategories]
      domains.value = [...defaultDomains]
      tagGroups.value = [...defaultTagGroups]
      
      await doSaveAllSettings(true)
      ElMessage.success('已恢复默认设置')
    })
    .catch(() => {})
}
</script>

<style scoped>
.settings-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  color: var(--text-secondary);
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  margin: 0 auto;
}

.tag-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.tag-group {
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.group-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.settings-form {
  max-width: 600px;
  padding: 20px;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--text-tertiary);
}
</style>
