<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" @click="handleAdd(null)">
        <el-icon><Plus /></el-icon>新增一级分类
      </el-button>
    </div>

    <el-table
      :data="tableData"
      v-loading="loading"
      border
      stripe
      row-key="id"
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="name" label="分类名称" min-width="200">
        <template #default="{ row }">
          <span>{{ row.level === 1 ? '📁' : '📄' }} {{ row.name }}</span>
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

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryTree, deleteCategory } from '@/api/category'

const router = useRouter()

const tableData = ref([])
const loading = ref(false)

function loadData() {
  loading.value = true
  getCategoryTree()
    .then(res => { tableData.value = res.data })
    .catch(() => { ElMessage.error('加载分类失败') })
    .finally(() => { loading.value = false })
}

// ========== 新增/编辑（跳转独立表单页） ==========
function handleAdd(parent) {
  router.push({ path: '/category/edit', query: { parentId: parent ? parent.id : 0 } })
}

function handleEdit(row) {
  router.push(`/category/edit/${row.id}`)
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
