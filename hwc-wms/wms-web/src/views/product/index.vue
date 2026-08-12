<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称、SKU编码或条码"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增商品
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="skuCode" label="SKU编码" width="130" />
      <el-table-column prop="name" label="商品名称" min-width="180" />
      <el-table-column label="商品分类" width="100">
        <template #default="{ row }">
          {{ categoryMap[row.categoryId] || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="货主/客户" width="160">
        <template #default="{ row }">
          {{ customerMap[row.customerId] || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="60" align="center" />
      <el-table-column prop="barcode" label="条码" width="130" />
      <el-table-column label="重量(kg)" width="90" align="right">
        <template #default="{ row }">{{ row.weight || '-' }}</template>
      </el-table-column>
      <el-table-column label="规格(cm)" width="140">
        <template #default="{ row }">
          <span v-if="row.length || row.width || row.height">
            {{ row.length || '-' }}×{{ row.width || '-' }}×{{ row.height || '-' }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="unitPrice" label="申报单价" width="100" align="right">
        <template #default="{ row }">{{ row.unitPrice || '-' }}</template>
      </el-table-column>
      <el-table-column prop="safetyStock" label="安全库存" width="85" align="center" />
      <el-table-column label="状态" width="70" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageProducts, deleteProduct } from '@/api/product'
import { listCustomers } from '@/api/customer'
import { getCategoryTree } from '@/api/category'

const router = useRouter()

// ========== 查询 & 分页 ==========
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// 关联数据缓存（用于表格显示中文名称）
const customerMap = ref({})
const categoryMap = ref({})

function loadData() {
  loading.value = true
  pageProducts({ page: page.value, pageSize: pageSize.value, keyword: keyword.value })
    .then(res => {
      tableData.value = res.data.records
      total.value = res.data.total
    })
    .finally(() => { loading.value = false })
}

function handleSearch() {
  page.value = 1
  loadData()
}

// ========== 加载关联数据 ==========
function loadRefData() {
  // 客户列表 → 构建 ID→Name 映射表
  listCustomers().then(res => {
    const map = {}
    res.data.forEach(c => { map[c.id] = c.name })
    customerMap.value = map
  })
  // 分类树 → 构建 ID→Name 映射表 + 展平为下拉选项
  getCategoryTree().then(res => {
    const map = {}
    res.data.forEach(parent => {
      map[parent.id] = parent.name
      if (parent.children) {
        parent.children.forEach(child => {
          map[child.id] = child.name
        })
      }
    })
    categoryMap.value = map
  })
}

// ========== 新增/编辑（跳转独立表单页） ==========
function handleAdd() {
  router.push('/product/edit')
}

function handleEdit(row) {
  router.push(`/product/edit/${row.id}`)
}

// ========== 删除 ==========
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    return deleteProduct(row.id)
  }).then(() => {
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
  loadRefData()
})
</script>

<style scoped>
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
