<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="handleAdd(null)">
        <el-icon><Plus /></el-icon>新增一级分类
      </el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="name" label="分类名称" min-width="200">
        <template #default="{ row }">
          <span :style="{ paddingLeft: (row.level - 1) * 24 + 'px' }">
            {{ row.level === 1 ? '📁' : '📄' }} {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="层级" width="80" align="center" />
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleAdd(row)">添加子分类</el-button>
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="父分类">
          <el-input :model-value="parentName" disabled />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/category'

const tableData = ref([])
const loading = ref(false)

function flattenTree(treeList) {
  const rows = []
  if (Array.isArray(treeList)) {
    treeList.forEach(p => {
      rows.push({ id: p.id, name: p.name, level: p.level, sort: p.sort, _parentId: null })
      if (Array.isArray(p.children)) {
        p.children.forEach(c => {
          rows.push({ id: c.id, name: c.name, level: c.level, sort: c.sort, _parentId: c.parentId || p.id })
        })
      }
    })
  }
  return rows
}

function loadData() {
  loading.value = true
  getCategoryTree()
    .then(res => { tableData.value = flattenTree(res.data) })
    .catch(() => { ElMessage.error('加载分类失败') })
    .finally(() => { loading.value = false })
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const editParent = ref(null)

const form = reactive({ id: null, name: '', parentId: 0, sort: 0 })

const parentName = computed(() => {
  if (isEdit.value) {
    const row = tableData.value.find(r => r.id === form.id)
    if (row && row._parentId) {
      const p = tableData.value.find(r => r.id === row._parentId)
      return p ? p.name : '一级分类'
    }
    return '一级分类'
  }
  return editParent.value ? editParent.value.name : '一级分类'
})

const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

function handleAdd(parent) {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.parentId = parent ? parent.id : 0
  form.sort = 0
  editParent.value = parent
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.parentId = row._parentId || 0
  form.sort = row.sort || 0
  editParent.value = null
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }
  submitLoading.value = true
  const api = isEdit.value ? updateCategory : addCategory
  api(form)
    .then(() => { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; loadData() })
    .catch(() => { ElMessage.error('保存失败') })
    .finally(() => { submitLoading.value = false })
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除分类「' + row.name + '」吗？', '删除确认', { type: 'warning' })
    .then(() => deleteCategory(row.id))
    .then(() => { ElMessage.success('已删除'); loadData() })
    .catch(() => {})
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { background: #fff; border-radius: 8px; padding: 20px; }
.search-bar { display: flex; gap: 10px; margin-bottom: 20px; }
</style>
