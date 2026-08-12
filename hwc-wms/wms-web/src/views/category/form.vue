<template>
  <div class="form-page">
    <el-card shadow="never">
      <template #header>
        <div class="form-header">
          <el-button link type="primary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>返回列表
          </el-button>
          <span class="form-title">{{ isEdit ? '编辑分类' : '新增分类' }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 640px">
        <el-divider content-position="left">分类信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="父分类" prop="parentId">
              <el-select v-model="form.parentId" placeholder="请选择父分类" style="width: 100%">
                <el-option :label="'一级分类（顶级）'" :value="0" />
                <el-option v-for="p in parentOptions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入分类名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="form-footer">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategory, getCategoryTree, addCategory, updateCategory } from '@/api/category'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const submitLoading = ref(false)

// 一级分类（可作为父分类）
const parentOptions = ref([])

const form = reactive({
  id: null,
  name: '',
  parentId: 0,
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

function goBack() {
  router.push('/category')
}

// 加载一级分类作为父分类选项（编辑时排除自身，避免自引用）
function loadParentOptions() {
  getCategoryTree().then(res => {
    parentOptions.value = res.data.filter(p => p.id !== form.id)
  })
}

onMounted(() => {
  loadParentOptions()
  if (isEdit.value) {
    getCategory(route.params.id).then(res => {
      Object.assign(form, res.data)
      loadParentOptions()
    })
  } else {
    // 新增：子分类页面跳转时携带 parentId
    form.parentId = route.query.parentId ? Number(route.query.parentId) : 0
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  const api = isEdit.value ? updateCategory : addCategory
  api(form)
    .then(() => {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      goBack()
    })
    .catch(() => { ElMessage.error('保存失败') })
    .finally(() => { submitLoading.value = false })
}
</script>

<style scoped>
.form-page {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.form-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.form-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}
.form-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
