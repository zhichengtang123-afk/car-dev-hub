<template>
  <div class="knowledge-create-page">
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '编辑知识' : '上传知识' }}</h1>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      size="large"
    >
      <el-row :gutter="24">
        <!-- 左侧主要内容 -->
        <el-col :span="16">
          <el-card class="form-card">
            <template #header>
              <span>基本信息</span>
            </template>

            <el-form-item label="知识标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入知识标题"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="知识摘要" prop="summary">
              <el-input
                v-model="form.summary"
                type="textarea"
                placeholder="简要描述知识内容，便于检索和理解"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="知识内容" prop="content">
              <div class="editor-wrapper">
                <MdEditor
                  v-model="form.content"
                  :theme="editorTheme"
                  :preview-theme="'github'"
                  :code-theme="appStore.isDark ? 'atom' : 'github'"
                  :toolbars-exclude="['github', 'htmlPreview']"
                  @on-upload-img="handleUploadImg"
                />
              </div>
            </el-form-item>
          </el-card>

          <el-card class="form-card">
            <template #header>
              <span>附件上传</span>
            </template>

            <el-upload
              class="file-upload"
              drag
              multiple
              :action="uploadUrl"
              :headers="uploadHeaders"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-remove="handleUploadRemove"
              :file-list="form.attachments"
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处，或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 PDF、Word、Excel、PPT、图片、压缩包，单文件不超过 100MB
                </div>
              </template>
            </el-upload>
          </el-card>
        </el-col>

        <!-- 右侧分类信息 -->
        <el-col :span="8">
          <el-card class="form-card sticky-card">
            <template #header>
              <span>分类信息</span>
            </template>

            <el-form-item label="知识类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择知识类型" style="width: 100%">
                <el-option
                  v-for="item in knowledgeTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="所属领域" prop="domain">
              <el-select v-model="form.domain" placeholder="请选择所属领域" style="width: 100%">
                <el-option
                  v-for="item in domains"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="标签" prop="tags">
              <div class="tags-input">
                <el-tag
                  v-for="tag in form.tags"
                  :key="tag"
                  closable
                  @close="removeTag(tag)"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="tagInputVisible"
                  ref="tagInputRef"
                  v-model="tagInputValue"
                  size="small"
                  class="tag-input"
                  @keyup.enter="addTag"
                  @blur="addTag"
                />
                <el-button
                  v-else
                  size="small"
                  @click="showTagInput"
                >
                  + 添加标签
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="关联项目">
              <el-select
                v-model="form.projectIds"
                multiple
                placeholder="选择关联项目（可选）"
                style="width: 100%"
              >
                <el-option
                  v-for="item in projects"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-divider />

            <div class="action-buttons">
              <el-button size="large" @click="saveDraft" :loading="saving">
                保存草稿
              </el-button>
              <el-button size="large" type="primary" @click="handleSubmit" :loading="submitting">
                {{ isEdit ? '更新发布' : '立即发布' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { createKnowledge, updateKnowledge, getKnowledgeDetail, uploadAttachment, getKnowledgeTypes, getKnowledgeDomains } from '@/api/knowledge'
import { getProjectList } from '@/api/project'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { UploadFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 编辑器主题跟随系统
const editorTheme = computed(() => appStore.isDark ? 'dark' : 'light')

const isEdit = computed(() => !!route.params.id)
const formRef = ref()
const tagInputRef = ref()
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const saving = ref(false)
const submitting = ref(false)

async function loadKnowledgeDetail() {
  try {
    const res = await getKnowledgeDetail(route.params.id)
    if (res?.data) {
      const data = res.data
      Object.assign(form, {
        title: data.title,
        summary: data.summary,
        content: data.content,
        type: data.type,
        domain: data.domain,
        tags: data.tags || [],
        projectIds: data.projects?.map(p => p.id) || [],
        attachments: data.attachments || []
      })
    }
  } catch (error) {
    ElMessage.error('加载知识详情失败')
  }
}

// 表单数据
const form = reactive({
  title: '',
  summary: '',
  content: '',
  type: '',
  domain: '',
  tags: [],
  projectIds: [],
  attachments: []
})

// 验证规则
const rules = {
  title: [
    { required: true, message: '请输入知识标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  summary: [
    { required: true, message: '请输入知识摘要', trigger: 'blur' },
    { min: 20, max: 500, message: '摘要长度在 20 到 500 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入知识内容', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择知识类型', trigger: 'change' }
  ],
  domain: [
    { required: true, message: '请选择所属领域', trigger: 'change' }
  ]
}

// 知识类型选项
const knowledgeTypes = ref([])

// 领域选项
const domains = ref([])

// 项目列表
const projects = ref([])

onMounted(async () => {
  // 加载类型、领域和项目
  try {
    const [typesRes, domainsRes, projectsRes] = await Promise.allSettled([
      getKnowledgeTypes(),
      getKnowledgeDomains(),
      getProjectList({ page: 1, size: 50 })
    ])

    if (typesRes.status === 'fulfilled' && typesRes.value?.data) {
      knowledgeTypes.value = typesRes.value.data.map(item => ({ label: item, value: item }))
    }
    if (domainsRes.status === 'fulfilled' && domainsRes.value?.data) {
      domains.value = domainsRes.value.data.map(item => ({ label: item, value: item }))
    }
    if (projectsRes.status === 'fulfilled' && projectsRes.value?.data) {
      projects.value = projectsRes.value.data.list || projectsRes.value.data
    }
  } catch (error) {
    console.error('加载选项失败:', error)
  }

  // 如果是编辑模式，加载知识详情
  if (isEdit.value) {
    loadKnowledgeDetail()
  }
})

// 上传相关
const uploadUrl = computed(() => import.meta.env.VITE_API_BASE_URL + '/knowledge/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 标签操作
function showTagInput() {
  tagInputVisible.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

function addTag() {
  const value = tagInputValue.value.trim()
  if (value && !form.tags.includes(value)) {
    form.tags.push(value)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

function removeTag(tag) {
  form.tags = form.tags.filter(t => t !== tag)
}

// 上传前检查
function beforeUpload(file) {
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB')
    return false
  }
  return true
}

// 图片上传（Markdown 编辑器）
async function handleUploadImg(files, callback) {
  const results = await Promise.all(
    files.map(async (file) => {
      try {
        const res = await uploadAttachment(file)
        return res.data.url
      } catch (error) {
        return ''
      }
    })
  )
  callback(results.filter(Boolean))
}

// 文件上传成功
function handleUploadSuccess(response, file, fileList) {
  form.attachments = fileList.map(f => ({
    name: f.name,
    url: f.response?.data?.url || f.url,
    id: f.response?.data?.id || f.uid
  }))
}

// 文件删除
function handleUploadRemove(file, fileList) {
  form.attachments = fileList.map(f => ({
    name: f.name,
    url: f.response?.data?.url || f.url,
    id: f.response?.data?.id || f.uid
  }))
}

// 保存草稿
async function saveDraft() {
  saving.value = true
  try {
    form.status = 0
    if (isEdit.value) {
      await updateKnowledge(route.params.id, form)
      ElMessage.success('草稿更新成功')
    } else {
      await createKnowledge(form)
      ElMessage.success('草稿保存成功')
    }
    router.push('/knowledge/list')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 提交发布
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    form.status = 1 // 提交审核
    if (isEdit.value) {
      await updateKnowledge(route.params.id, form)
      ElMessage.success('更新成功，已提交审核')
    } else {
      await createKnowledge(form)
      ElMessage.success('发布成功，已提交审核')
    }
    router.push('/knowledge/list')
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
  } finally {
    submitting.value = false
  }
}

// 加载知识详情（编辑模式）
async function loadKnowledge() {
  if (!isEdit.value) return
  
  try {
    const res = await getKnowledgeDetail(route.params.id)
    if (res.code === 200) {
      Object.assign(form, res.data)
    }
  } catch (error) {
    ElMessage.error('加载知识详情失败')
    router.back()
  }
}

// 加载项目列表
async function loadProjects() {
  try {
    // TODO: 调用真实 API
    // const res = await getProjectList()
    // projects.value = res.data.list
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

onMounted(() => {
  loadKnowledge()
  loadProjects()
})
</script>

<style scoped>
.knowledge-create-page {
  max-width: 1400px;
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

.form-card {
  margin-bottom: 24px;
}

.sticky-card {
  position: sticky;
  top: 24px;
}

/* 编辑器 */
.editor-wrapper {
  width: 100%;
  min-height: 500px;
}

.editor-wrapper :deep(.md-editor) {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.editor-wrapper :deep(.md-editor-toolbar) {
  border-bottom: 1px solid var(--border-color);
}

.editor-wrapper :deep(.md-editor-footer) {
  border-top: 1px solid var(--border-color);
  padding: 8px 12px;
  min-height: 36px;
  line-height: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 文件上传 */
.file-upload :deep(.el-upload-dragger) {
  background: var(--bg-tertiary);
  border: 2px dashed var(--border-color);
  transition: all var(--transition-fast);
}

.file-upload :deep(.el-upload-dragger):hover {
  border-color: var(--primary-color);
}

.upload-icon {
  font-size: 48px;
  color: var(--text-tertiary);
  margin-bottom: 16px;
}

/* 标签输入 */
.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-input {
  width: 100px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: row;
  gap: 12px;
  justify-content: stretch;
}

.action-buttons .el-button {
  flex: 1;
}
</style>
