<template>
  <div class="discussion-create-page">
    <div class="page-header">
      <h1 class="page-title">发起提问</h1>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="问题标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请简洁清晰地描述您的问题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="问题描述" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="详细描述您的问题，包括背景、已尝试的方法、期望的结果等"
            :rows="8"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="问题标签">
          <div style="width: 100%; display: flex; flex-direction: column;">
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
            <div class="tag-suggestions">
              <span class="label">热门标签：</span>
              <el-tag
                v-for="tag in hotTags"
                :key="tag"
                size="small"
                type="info"
                @click="addSuggestedTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="关联知识">
          <el-select
            v-model="form.knowledgeId"
            placeholder="选择相关的知识（可选）"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in knowledgeList"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联项目">
          <el-select
            v-model="form.projectId"
            placeholder="选择相关的项目（可选）"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in projectList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              发布问题
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 提问指南 -->
    <el-card class="guide-card">
      <template #header>
        <span>💡 提问指南</span>
      </template>
      <ul class="guide-list">
        <li>标题请简洁明了，概括问题核心</li>
        <li>描述问题时请包含必要的背景信息</li>
        <li>说明您已经尝试过的解决方案</li>
        <li>如果是代码问题，请提供相关代码片段</li>
        <li>添加合适的标签有助于问题被更快发现</li>
        <li>关联相关知识或项目可以提供更好的上下文</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createDiscussion } from '@/api/discussion'
import { getProjectList } from '@/api/project'
import { getKnowledgeList, getHotTags } from '@/api/knowledge'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const tagInputRef = ref()
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const submitting = ref(false)

const form = reactive({
  title: '',
  content: '',
  tags: [],
  knowledgeId: null,
  projectId: null
})

const rules = {
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 10, max: 200, message: '标题长度在 10 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 20, max: 2000, message: '描述长度在 20 到 2000 个字符', trigger: 'blur' }
  ]
}

// 读取状态的数组
const hotTags = ref([])
const knowledgeList = ref([])
const projectList = ref([])

onMounted(async () => {
  try {
    const [tagsRes, knowledgeRes, projectsRes] = await Promise.allSettled([
      getHotTags({ limit: 8 }),
      getKnowledgeList({ page: 1, size: 50 }),
      getProjectList({ page: 1, size: 50 })
    ])
    if (tagsRes.status === 'fulfilled' && tagsRes.value?.data) {
      hotTags.value = tagsRes.value.data.map(t => t.name || t)
    }
    if (knowledgeRes.status === 'fulfilled' && knowledgeRes.value?.data) {
      knowledgeList.value = knowledgeRes.value.data.list || knowledgeRes.value.data
    }
    if (projectsRes.status === 'fulfilled' && projectsRes.value?.data) {
      projectList.value = projectsRes.value.data.list || projectsRes.value.data
    }
  } catch (error) {
    console.error('加载选项失败:', error)
  }
})

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

function addSuggestedTag(tag) {
  if (!form.tags.includes(tag)) {
    form.tags.push(tag)
  }
}

// 提交
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createDiscussion(form)
    ElMessage.success('发布成功')
    router.push('/discussion/list')
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.discussion-create-page {
  max-width: 800px;
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

.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag-input {
  width: 100px;
}

.tag-suggestions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-suggestions .label {
  color: var(--text-tertiary);
  font-size: 13px;
}

.tag-suggestions .el-tag {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tag-suggestions .el-tag:hover {
  background: var(--primary-bg);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

/* 提问指南 */
.guide-card {
  background: var(--bg-tertiary);
}

.guide-list {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
  line-height: 2;
}

.guide-list li {
  margin-bottom: 4px;
}
</style>
