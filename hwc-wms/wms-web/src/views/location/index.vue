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
      <el-table-column label="所属仓库" width="150" show-overflow-tooltip>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑库位' : '新增库位'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" style="width: 100%">
                <el-option
                  v-for="w in warehouseList"
                  :key="w.id"
                  :label="w.name"
                  :value="w.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库位编码" prop="code">
              <el-input v-model="form.code" placeholder="如 A-01-01-01">
                <template #append>
                  <el-button @click="fillNextCode">自动</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="区域">
              <el-input v-model="form.zone" placeholder="如 A区" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="货道">
              <el-input v-model="form.aisle" placeholder="如 01" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="货架">
              <el-input v-model="form.shelf" placeholder="如 01" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层">
              <el-input v-model="form.level" placeholder="如 01" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">空闲</el-radio>
            <el-radio :label="2">占用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="code-hint">
          <el-icon><InfoFilled /></el-icon>
          建议编码格式：区域-货道-货架-层，如 A-01-01-01
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageLocations, addLocation, updateLocation, deleteLocation, getNextLocationCode } from '@/api/location'
import { listWarehouses } from '@/api/warehouse'

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

// ========== 弹窗 & 表单 ==========
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  id: null,
  warehouseId: null,
  code: '',
  zone: '',
  aisle: '',
  shelf: '',
  level: '',
  status: 1
})

const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  code: [{ required: true, message: '请输入库位编码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function resetForm() {
  form.id = null
  form.warehouseId = null
  form.code = ''
  form.zone = ''
  form.aisle = ''
  form.shelf = ''
  form.level = ''
  form.status = 1
}

function fillNextCode() {
  getNextLocationCode().then(res => {
    form.code = res.data
  })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  // 预填编码
  getNextLocationCode().then(res => {
    form.code = res.data
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.warehouseId = row.warehouseId
  form.code = row.code
  form.zone = row.zone || ''
  form.aisle = row.aisle || ''
  form.shelf = row.shelf || ''
  form.level = row.level || ''
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  const api = isEdit.value ? updateLocation : addLocation
  api(form)
    .then(() => {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    })
    .finally(() => { submitLoading.value = false })
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
