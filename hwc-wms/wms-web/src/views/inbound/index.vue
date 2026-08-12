<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input v-model="query.orderNo" placeholder="入库单号" clearable style="width: 180px" @keyup.enter="handleSearch" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
        <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="query.warehouseId" placeholder="仓库" clearable filterable style="width: 160px">
        <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
      </el-select>
      <el-select v-model="query.customerId" placeholder="货主/客户" clearable filterable style="width: 160px">
        <el-option v-for="c in customerList" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-date-picker
        v-model="query.dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width: 360px"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button type="success" @click="handleAdd">
        <el-icon><Plus /></el-icon>创建入库单
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="入库单号" width="170" />
      <el-table-column label="仓库" width="130">
        <template #default="{ row }">{{ warehouseMap[row.warehouseId] || '-' }}</template>
      </el-table-column>
      <el-table-column label="货主/客户" width="170">
        <template #default="{ row }">{{ customerMap[row.customerId] || '-' }}</template>
      </el-table-column>
      <el-table-column label="入库类型" width="100" align="center">
        <template #default="{ row }">{{ typeMap[row.orderType] || row.orderType }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status] || 'info'" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expectArriveTime" label="预计到货" width="160" />
      <el-table-column prop="actualArriveTime" label="实际到货" width="160" />
      <el-table-column prop="createUser" label="创建人" width="80" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
          <template v-if="row.status === 'PENDING'">
            <el-button type="success" link size="small" @click="handleReceive(row)">收货</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
          </template>
          <template v-else-if="row.status === 'RECEIVED'">
            <el-button type="success" link size="small" @click="handlePutaway(row)">上架</el-button>
            <el-button type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
          </template>
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

    <!-- 创建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑入库单' : '创建入库单'"
      width="760px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入库类型" prop="orderType">
              <el-select v-model="form.orderType" style="width: 100%">
                <el-option v-for="(label, value) in typeMap" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="货主/客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
                <el-option v-for="c in customerList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" filterable style="width: 100%">
                <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预计到货时间">
              <el-date-picker
                v-model="form.expectArriveTime"
                type="datetime"
                placeholder="选择预计到货时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="入库明细" prop="items">
          <div style="width: 100%">
            <el-table :data="form.items" border size="small">
              <el-table-column label="商品" min-width="220">
                <template #default="{ row }">
                  <el-select v-model="row.productId" placeholder="选择商品" filterable style="width: 100%">
                    <el-option v-for="p in productList" :key="p.id" :label="`${p.name}（${p.skuCode}）`" :value="p.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="预计数量" width="140">
                <template #default="{ row }">
                  <el-input-number v-model="row.expectQuantity" :min="1" :step="1" style="width: 100%" />
                </template>
              </el-table-column>
              <el-table-column label="批次号" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.batchNo" placeholder="选填" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" link size="small" style="margin-top: 8px" @click="addItem">
              <el-icon><Plus /></el-icon>添加明细
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 收货弹窗 -->
    <el-dialog v-model="receiveVisible" title="收货确认" width="720px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>录入实际收货数量（默认等于预计数量），收货后库存暂不变更，上架时计入库存</template>
      </el-alert>
      <el-table :data="receiveItems" border size="small">
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">{{ productMap[row.productId] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="expectQuantity" label="预计数量" width="100" align="center" />
        <el-table-column label="实际收货" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.actualQuantity" :min="0" :step="1" style="width: 100%" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="primary" :loading="receiveLoading" @click="handleReceiveSubmit">确认收货</el-button>
      </template>
    </el-dialog>

    <!-- 上架弹窗 -->
    <el-dialog v-model="putawayVisible" title="上架确认" width="760px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>为已收货商品选择上架库位，确认后库存自动增加并生成入库流水</template>
      </el-alert>
      <el-table :data="putawayItems" border size="small">
        <el-table-column label="商品" min-width="180">
          <template #default="{ row }">{{ productMap[row.productId] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="actualQuantity" label="上架数量" width="90" align="center" />
        <el-table-column label="上架库位" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.locationId" placeholder="选择库位" filterable style="width: 100%" :disabled="row.actualQuantity <= 0">
              <el-option v-for="l in locationList" :key="l.id" :label="l.code" :value="l.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="100">
          <template #default="{ row }">{{ row.batchNo || '-' }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="putawayVisible = false">取消</el-button>
        <el-button type="primary" :loading="putawayLoading" @click="handlePutawaySubmit">确认上架</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="入库单详情" size="560px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="入库单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType[detail.status] || 'info'" size="small">{{ statusMap[detail.status] || detail.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="入库类型">{{ typeMap[detail.orderType] || detail.orderType }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ warehouseMap[detail.warehouseId] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="货主/客户">{{ customerMap[detail.customerId] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.createUser || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预计到货">{{ detail.expectArriveTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际到货">{{ detail.actualArriveTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">入库明细</h4>
        <el-table :data="detail.items" border size="small">
          <el-table-column label="商品" min-width="150">
            <template #default="{ row }">{{ productMap[row.productId] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="expectQuantity" label="预计" width="70" align="center" />
          <el-table-column prop="actualQuantity" label="实收" width="70" align="center" />
          <el-table-column label="上架库位" width="100">
            <template #default="{ row }">{{ locationMap[row.locationId] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次" width="90">
            <template #default="{ row }">{{ row.batchNo || '-' }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageInbounds, getInbound, addInbound, updateInbound, receiveInbound, putawayInbound, cancelInbound } from '@/api/inbound'
import { listCustomers } from '@/api/customer'
import { listWarehouses } from '@/api/warehouse'
import { listProducts } from '@/api/product'
import { listLocations } from '@/api/location'

// ========== 字典 ==========
const statusMap = {
  PENDING: '待收货',
  RECEIVED: '已收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}
const statusTagType = {
  PENDING: 'warning',
  RECEIVED: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'info'
}
const typeMap = {
  PURCHASE: '采购入库',
  RETURN: '退货入库',
  TRANSFER: '调拨入库'
}

// ========== 查询 & 分页 ==========
const query = reactive({
  orderNo: '',
  status: '',
  warehouseId: null,
  customerId: null,
  dateRange: []
})
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// 关联数据缓存
const warehouseMap = ref({})
const customerMap = ref({})
const productMap = ref({})
const locationMap = ref({})
const warehouseList = ref([])
const customerList = ref([])
const productList = ref([])
const locationList = ref([])

function loadData() {
  loading.value = true
  pageInbounds({
    page: page.value,
    pageSize: pageSize.value,
    orderNo: query.orderNo || undefined,
    status: query.status || undefined,
    warehouseId: query.warehouseId || undefined,
    customerId: query.customerId || undefined,
    startTime: query.dateRange?.[0] || undefined,
    endTime: query.dateRange?.[1] || undefined
  }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => { loading.value = false })
}

function handleSearch() {
  page.value = 1
  loadData()
}

// ========== 加载关联数据 ==========
function loadRefData() {
  listWarehouses().then(res => {
    warehouseList.value = res.data
    const map = {}
    res.data.forEach(w => { map[w.id] = w.name })
    warehouseMap.value = map
  })
  listCustomers().then(res => {
    customerList.value = res.data
    const map = {}
    res.data.forEach(c => { map[c.id] = c.name })
    customerMap.value = map
  })
  listProducts().then(res => {
    productList.value = res.data
    const map = {}
    res.data.forEach(p => { map[p.id] = p.name })
    productMap.value = map
  })
}

function loadLocations(warehouseId) {
  if (!warehouseId) {
    locationList.value = []
    return
  }
  listLocations(warehouseId).then(res => {
    locationList.value = res.data
    const map = {}
    res.data.forEach(l => { map[l.id] = l.code })
    locationMap.value = map
  })
}

// ========== 创建/编辑 ==========
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const editingId = ref(null)

const form = reactive({
  orderType: 'PURCHASE',
  customerId: null,
  warehouseId: null,
  expectArriveTime: null,
  remark: '',
  items: []
})

const rules = {
  orderType: [{ required: true, message: '请选择入库类型', trigger: 'change' }],
  customerId: [{ required: true, message: '请选择货主/客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择入库仓库', trigger: 'change' }],
  items: [{
    validator: (rule, value, callback) => {
      if (!value || value.length === 0) callback(new Error('请添加入库明细'))
      else if (value.some(i => !i.productId || !i.expectQuantity)) callback(new Error('明细商品或数量不完整'))
      else callback()
    },
    trigger: 'change'
  }]
}

function resetForm() {
  editingId.value = null
  form.orderType = 'PURCHASE'
  form.customerId = null
  form.warehouseId = null
  form.expectArriveTime = null
  form.remark = ''
  form.items = []
}

function addItem() {
  form.items.push({ productId: null, expectQuantity: 1, batchNo: '', remark: '' })
}

function removeItem(index) {
  form.items.splice(index, 1)
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  addItem()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  resetForm()
  editingId.value = row.id
  getInbound(row.id).then(res => {
    const detail = res.data
    form.orderType = detail.orderType
    form.customerId = detail.customerId
    form.warehouseId = detail.warehouseId
    form.expectArriveTime = detail.expectArriveTime
    form.remark = detail.remark
    form.items = (detail.items || []).map(i => ({
      id: i.id,
      productId: i.productId,
      expectQuantity: i.expectQuantity,
      batchNo: i.batchNo || '',
      remark: i.remark || ''
    }))
    if (form.items.length === 0) addItem()
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  const payload = {
    orderType: form.orderType,
    customerId: form.customerId,
    warehouseId: form.warehouseId,
    expectArriveTime: form.expectArriveTime,
    remark: form.remark,
    items: form.items
  }
  const api = isEdit.value ? () => updateInbound(editingId.value, payload) : () => addInbound(payload)
  api().then(() => {
    ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  }).finally(() => { submitLoading.value = false })
}

// ========== 收货 ==========
const receiveVisible = ref(false)
const receiveLoading = ref(false)
const receiveItems = ref([])
const receivingId = ref(null)

function handleReceive(row) {
  receivingId.value = row.id
  getInbound(row.id).then(res => {
    receiveItems.value = (res.data.items || []).map(i => ({
      id: i.id,
      productId: i.productId,
      expectQuantity: i.expectQuantity,
      actualQuantity: i.actualQuantity || i.expectQuantity
    }))
    receiveVisible.value = true
  })
}

function handleReceiveSubmit() {
  receiveLoading.value = true
  receiveInbound(receivingId.value, receiveItems.value.map(i => ({
    id: i.id,
    actualQuantity: i.actualQuantity
  }))).then(() => {
    ElMessage.success('收货成功')
    receiveVisible.value = false
    loadData()
  }).finally(() => { receiveLoading.value = false })
}

// ========== 上架 ==========
const putawayVisible = ref(false)
const putawayLoading = ref(false)
const putawayItems = ref([])
const putawayOrderId = ref(null)

function handlePutaway(row) {
  putawayOrderId.value = row.id
  getInbound(row.id).then(res => {
    loadLocations(row.warehouseId)
    putawayItems.value = (res.data.items || []).map(i => ({
      id: i.id,
      productId: i.productId,
      actualQuantity: i.actualQuantity || 0,
      locationId: i.locationId || null,
      batchNo: i.batchNo
    }))
    putawayVisible.value = true
  })
}

function handlePutawaySubmit() {
  const needLocation = putawayItems.value.some(i => i.actualQuantity > 0 && !i.locationId)
  if (needLocation) {
    ElMessage.warning('请为已收货商品选择上架库位')
    return
  }
  putawayLoading.value = true
  putawayInbound(putawayOrderId.value, putawayItems.value.map(i => ({
    id: i.id,
    locationId: i.locationId
  }))).then(() => {
    ElMessage.success('上架成功，库存已更新')
    putawayVisible.value = false
    loadData()
  }).finally(() => { putawayLoading.value = false })
}

// ========== 详情 ==========
const detailVisible = ref(false)
const detail = ref(null)

function handleDetail(row) {
  getInbound(row.id).then(res => {
    detail.value = res.data
    if (res.data.warehouseId) loadLocations(res.data.warehouseId)
    detailVisible.value = true
  })
}

// ========== 取消 ==========
function handleCancel(row) {
  ElMessageBox.confirm(`确定取消入库单「${row.orderNo}」吗？`, '取消确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '再想想'
  }).then(() => {
    return cancelInbound(row.id)
  }).then(() => {
    ElMessage.success('已取消')
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
  flex-wrap: wrap;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
