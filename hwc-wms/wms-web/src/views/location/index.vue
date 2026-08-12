<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-select
        v-model="filterWarehouseId"
        placeholder="全部仓库"
        clearable
        style="width: 200px"
        @change="handleSearch"
      >
        <el-option
          v-for="w in warehouseList"
          :key="w.id"
          :label="w.name"
          :value="w.id"
        />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索库位编码、区域或货道"
        clearable
        style="width: 260px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增库位
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="所属仓库" width="150">
        <template #default="{ row }">
          {{ warehouseMap[row.warehouseId] || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="code" label="库位编码" width="140" />
      <el-table-column prop="zone" label="区域" width="100" />
      <el-table-column prop="aisle" label="货道" width="100" />
      <el-table-column prop="shelf" label="货架" width="100" />
      <el-table-column prop="level" label="层" width="80" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'"
            size="small"
          >
            {{ statusMap[row.status] || '未知' }}
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
import { pageLocations, deleteLocation } from '@/api/location'
import { listWarehouses } from '@/api/warehouse'

const router = useRouter()

// ========== 状态映射 ==========
const statusMap = { 1: '空闲', 2: '占用', 0: '禁用' }

// ========== 查询 & 分页 ==========
const keyword = ref('')
const filterWarehouseId = ref(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// 关联数据缓存
const warehouseList = ref([])
const warehouseMap = ref({})

function loadData() {
  loading.value = true
  pageLocations({
    page: page.value,
    pageSize: pageSize.value,
    warehouseId: filterWarehouseId.value || undefined,
    keyword: keyword.value
  })
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

// ========== 加载仓库数据 ==========
function loadWarehouses() {
  listWarehouses().then(res => {
    warehouseList.value = res.data
    const map = {}
    res.data.forEach(w => { map[w.id] = w.name })
    warehouseMap.value = map
  })
}

// ========== 新增/编辑（跳转独立表单页） ==========
function handleAdd() {
  router.push('/location/edit')
}

function handleEdit(row) {
  router.push(`/location/edit/${row.id}`)
}

// ========== 删除 ==========
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除库位「${row.code}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    return deleteLocation(row.id)
  }).then(() => {
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
  loadWarehouses()
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
.code-hint {
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
}
</style>
