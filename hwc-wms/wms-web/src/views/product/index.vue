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
      <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
      <el-table-column label="商品分类" width="100">
        <template #default="{ row }">
          {{ categoryMap[row.categoryId] || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="货主/客户" width="120" show-overflow-tooltip>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SKU编码" prop="skuCode">
              <el-input v-model="form.skuCode" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="货主/客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" style="width: 100%">
                <el-option
                  v-for="c in customerList"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="c in flatCategoryList"
                  :key="c.id"
                  :label="c.label"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计量单位">
              <el-input v-model="form.unit" placeholder="个/箱/件/托" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="条码">
              <el-input v-model="form.barcode" placeholder="商品条码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="重量(kg)">
              <el-input-number v-model="form.weight" :precision="3" :step="0.1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申报单价(元)">
              <el-input-number v-model="form.unitPrice" :precision="2" :step="0.01" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="长(cm)">
              <el-input-number v-model="form.length" :precision="2" :step="1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="宽(cm)">
              <el-input-number v-model="form.width" :precision="2" :step="1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="高(cm)">
              <el-input-number v-model="form.height" :precision="2" :step="1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="安全库存">
              <el-input-number v-model="form.safetyStock" :step="1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { pageProducts, addProduct, updateProduct, deleteProduct, getNextSkuCode } from '@/api/product'
import { listCustomers } from '@/api/customer'
import { getCategoryTree } from '@/api/category'

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

// ========== 弹窗 & 表单 ==========
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const customerList = ref([])
const flatCategoryList = ref([])

const form = reactive({
  id: null,
  skuCode: '',
  name: '',
  customerId: null,
  categoryId: null,
  unit: '个',
  barcode: '',
  weight: null,
  length: null,
  width: null,
  height: null,
  unitPrice: null,
  safetyStock: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }]
}

function resetForm() {
  form.id = null
  form.skuCode = ''
  form.name = ''
  form.customerId = null
  form.categoryId = null
  form.unit = '个'
  form.barcode = ''
  form.weight = null
  form.length = null
  form.width = null
  form.height = null
  form.unitPrice = null
  form.safetyStock = 0
  form.status = 1
}

function loadDialogRefData() {
  // 客户下拉选项
  listCustomers().then(res => {
    customerList.value = res.data
  })
  // 分类下拉选项（展平树为带缩进的列表）
  getCategoryTree().then(res => {
    const flat = []
    res.data.forEach(parent => {
      flat.push({ id: parent.id, label: parent.name })
      if (parent.children) {
        parent.children.forEach(child => {
          flat.push({ id: child.id, label: '　├ ' + child.name })
        })
      }
    })
    flatCategoryList.value = flat
  })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  loadDialogRefData()
  getNextSkuCode().then(res => {
    form.skuCode = res.data
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  loadDialogRefData()
  form.id = row.id
  form.skuCode = row.skuCode
  form.name = row.name
  form.customerId = row.customerId
  form.categoryId = row.categoryId
  form.unit = row.unit || '个'
  form.barcode = row.barcode || ''
  form.weight = row.weight
  form.length = row.length
  form.width = row.width
  form.height = row.height
  form.unitPrice = row.unitPrice
  form.safetyStock = row.safetyStock || 0
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  const api = isEdit.value ? updateProduct : addProduct
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
