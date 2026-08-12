<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input v-model="query.orderNo" placeholder="出库单号" clearable style="width: 180px" @keyup.enter="handleSearch" />
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
        <el-icon><Plus /></el-icon>创建出库单
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="出库单号" width="170" />
      <el-table-column label="仓库" width="130">
        <template #default="{ row }">{{ warehouseMap[row.warehouseId] || '-' }}</template>
      </el-table-column>
      <el-table-column label="货主/客户" width="170">
        <template #default="{ row }">{{ customerMap[row.customerId] || '-' }}</template>
      </el-table-column>
      <el-table-column label="出库类型" width="100" align="center">
        <template #default="{ row }">{{ typeMap[row.orderType] || row.orderType }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status] || 'info'" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="90" align="center" />
      <el-table-column prop="expressCompany" label="快递公司" width="120" />
      <el-table-column prop="expressNo" label="快递单号" width="140" show-overflow-tooltip />
      <el-table-column prop="createUser" label="创建人" width="80" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
          <template v-if="row.status === 'WAIT_PICK'">
            <el-button type="success" link size="small" @click="handlePick(row)">拣货</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
          </template>
          <template v-else-if="row.status === 'PICKING'">
            <el-button type="success" link size="small" @click="handlePick(row)">继续拣货</el-button>
            <el-button type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
          </template>
          <template v-else-if="row.status === 'PICKED'">
            <el-button type="success" link size="small" @click="handleShip(row)">发货</el-button>
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
      :title="isEdit ? '编辑出库单' : '创建出库单'"
      width="820px"
      :close-on-click-modal="false"
    >
      <el-alert v-if="!isEdit" type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>创建出库单将自动锁定库存（可用数量减少），库存不足时无法创建</template>
      </el-alert>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="出库类型" prop="orderType">
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
            <el-form-item label="出库仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" filterable style="width: 100%">
                <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="收货人姓名" prop="receiverName">
              <el-input v-model="form.receiverName" placeholder="收货人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收货人电话">
              <el-input v-model="form.receiverPhone" placeholder="收货人电话" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="收货人地址">
          <el-input v-model="form.receiverAddress" placeholder="收货人详细地址（海外地址）" />
        </el-form-item>
        <el-form-item label="出库明细" prop="items">
          <div style="width: 100%">
            <el-table :data="form.items" border size="small">
              <el-table-column label="商品" min-width="220">
                <template #default="{ row }">
                  <el-select v-model="row.productId" placeholder="选择商品" filterable style="width: 100%">
                    <el-option v-for="p in productList" :key="p.id" :label="`${p.name}（${p.skuCode}）`" :value="p.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="出库数量" width="140">
                <template #default="{ row }">
                  <el-input-number v-model="row.quantity" :min="1" :step="1" style="width: 100%" />
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

    <!-- 拣货弹窗 -->
    <el-dialog v-model="pickVisible" title="拣货确认" width="720px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>全量拣货：每项商品拣货数量必须等于下单数量；可分批拣货，全部拣完后进入发货</template>
      </el-alert>
      <div style="margin-bottom: 10px">
        <el-button type="primary" link size="small" @click="pickAll">
          <el-icon><Check /></el-icon>一键全部拣货
        </el-button>
      </div>
      <el-table :data="pickItems" border size="small">
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">{{ productMap[row.productId] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="下单数量" width="100" align="center" />
        <el-table-column label="拣货数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.pickedQuantity" :min="0" :max="row.quantity" :step="1" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="拣货库位" width="110">
          <template #default="{ row }">{{ locationMap[row.locationId] || '-' }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="pickVisible = false">取消</el-button>
        <el-button type="primary" :loading="pickLoading" @click="handlePickSubmit">确认拣货</el-button>
      </template>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipVisible" title="发货确认" width="480px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>填写快递信息后确认发货，系统将扣减库存并生成出库流水</template>
      </el-alert>
      <el-form label-width="100px">
        <el-form-item label="快递公司" required>
          <el-input v-model="shipForm.expressCompany" placeholder="如: UPS/FedEx/DHL" />
        </el-form-item>
        <el-form-item label="快递单号" required>
          <el-input v-model="shipForm.expressNo" placeholder="物流跟踪单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="handleShipSubmit">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="出库单详情" size="580px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="出库单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType[detail.status] || 'info'" size="small">{{ statusMap[detail.status] || detail.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出库类型">{{ typeMap[detail.orderType] || detail.orderType }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ warehouseMap[detail.warehouseId] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="货主/客户">{{ customerMap[detail.customerId] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.createUser || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detail.receiverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.receiverPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detail.receiverAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="快递公司">{{ detail.expressCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ detail.expressNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">出库明细</h4>
        <el-table :data="detail.items" border size="small">
          <el-table-column label="商品" min-width="150">
            <template #default="{ row }">{{ productMap[row.productId] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="下单" width="70" align="center" />
          <el-table-column prop="pickedQuantity" label="已拣" width="70" align="center" />
          <el-table-column label="拣货库位" width="100">
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
import { pageOutbounds, getOutbound, addOutbound, updateOutbound, pickOutbound, shipOutbound, cancelOutbound } from '@/api/outbound'
import { listCustomers } from '@/api/customer'
import { listWarehouses } from '@/api/warehouse'
import { listProducts } from '@/api/product'
import { listLocations } from '@/api/location'

// ========== 字典 ==========
const statusMap = {
  WAIT_PICK: '待拣货',
  PICKING: '拣货中',
  PICKED: '已拣货',
  SHIPPED: '已发货',
  CANCELLED: '已取消'
}
const statusTagType = {
  WAIT_PICK: 'warning',
  PICKING: 'primary',
  PICKED: 'success',
  SHIPPED: 'info',
  CANCELLED: 'info'
}
const typeMap = {
  SALE: '销售出库',
  TRANSFER: '调拨出库',
  RETURN: '退货出库'
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

function loadData() {
  loading.value = true
  pageOutbounds({
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
    locationMap.value = {}
    return
  }
  listLocations(warehouseId).then(res => {
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
  orderType: 'SALE',
  customerId: null,
  warehouseId: null,
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: '',
  items: []
})

const rules = {
  orderType: [{ required: true, message: '请选择出库类型', trigger: 'change' }],
  customerId: [{ required: true, message: '请选择货主/客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择出库仓库', trigger: 'change' }],
  receiverName: [{ required: true, message: '请填写收货人姓名', trigger: 'blur' }],
  items: [{
    validator: (rule, value, callback) => {
      if (!value || value.length === 0) callback(new Error('请添加出库明细'))
      else if (value.some(i => !i.productId || !i.quantity)) callback(new Error('明细商品或数量不完整'))
      else callback()
    },
    trigger: 'change'
  }]
}

function resetForm() {
  editingId.value = null
  form.orderType = 'SALE'
  form.customerId = null
  form.warehouseId = null
  form.receiverName = ''
  form.receiverPhone = ''
  form.receiverAddress = ''
  form.remark = ''
  form.items = []
}

function addItem() {
  form.items.push({ productId: null, quantity: 1, batchNo: '', remark: '' })
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
  getOutbound(row.id).then(res => {
    const detail = res.data
    form.orderType = detail.orderType
    form.customerId = detail.customerId
    form.warehouseId = detail.warehouseId
    form.receiverName = detail.receiverName || ''
    form.receiverPhone = detail.receiverPhone || ''
    form.receiverAddress = detail.receiverAddress || ''
    form.remark = detail.remark || ''
    form.items = (detail.items || []).map(i => ({
      id: i.id,
      productId: i.productId,
      quantity: i.quantity,
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
    receiverName: form.receiverName,
    receiverPhone: form.receiverPhone,
    receiverAddress: form.receiverAddress,
    remark: form.remark,
    items: form.items
  }
  const api = isEdit.value ? () => updateOutbound(editingId.value, payload) : () => addOutbound(payload)
  api().then(() => {
    ElMessage.success(isEdit.value ? '修改成功，库存已重新锁定' : '创建成功，库存已锁定')
    dialogVisible.value = false
    loadData()
  }).finally(() => { submitLoading.value = false })
}

// ========== 拣货 ==========
const pickVisible = ref(false)
const pickLoading = ref(false)
const pickItems = ref([])
const pickingId = ref(null)

function handlePick(row) {
  pickingId.value = row.id
  getOutbound(row.id).then(res => {
    loadLocations(row.warehouseId)
    pickItems.value = (res.data.items || []).map(i => ({
      id: i.id,
      productId: i.productId,
      quantity: i.quantity,
      pickedQuantity: i.pickedQuantity || 0,
      locationId: i.locationId
    }))
    pickVisible.value = true
  })
}

function pickAll() {
  pickItems.value.forEach(i => { i.pickedQuantity = i.quantity })
}

function handlePickSubmit() {
  const invalid = pickItems.value.some(i => i.pickedQuantity > 0 && i.pickedQuantity !== i.quantity)
  if (invalid) {
    ElMessage.warning('拣货数量必须等于下单数量（全量拣货）')
    return
  }
  const submitted = pickItems.value.filter(i => i.pickedQuantity > 0)
  if (submitted.length === 0) {
    ElMessage.warning('请先选择要拣货的商品')
    return
  }
  pickLoading.value = true
  pickOutbound(pickingId.value, submitted.map(i => ({
    id: i.id,
    pickedQuantity: i.pickedQuantity
  }))).then(() => {
    ElMessage.success('拣货成功')
    pickVisible.value = false
    loadData()
  }).finally(() => { pickLoading.value = false })
}

// ========== 发货 ==========
const shipVisible = ref(false)
const shipLoading = ref(false)
const shipForm = reactive({ expressCompany: '', expressNo: '' })
const shippingId = ref(null)

function handleShip(row) {
  shippingId.value = row.id
  shipForm.expressCompany = row.expressCompany || ''
  shipForm.expressNo = row.expressNo || ''
  shipVisible.value = true
}

function handleShipSubmit() {
  if (!shipForm.expressCompany || !shipForm.expressNo) {
    ElMessage.warning('请填写快递公司和快递单号')
    return
  }
  shipLoading.value = true
  shipOutbound(shippingId.value, {
    expressCompany: shipForm.expressCompany,
    expressNo: shipForm.expressNo
  }).then(() => {
    ElMessage.success('发货成功，库存已扣减')
    shipVisible.value = false
    loadData()
  }).finally(() => { shipLoading.value = false })
}

// ========== 详情 ==========
const detailVisible = ref(false)
const detail = ref(null)

function handleDetail(row) {
  getOutbound(row.id).then(res => {
    detail.value = res.data
    if (res.data.warehouseId) loadLocations(res.data.warehouseId)
    detailVisible.value = true
  })
}

// ========== 取消 ==========
function handleCancel(row) {
  ElMessageBox.confirm(`确定取消出库单「${row.orderNo}」吗？取消后将解锁已锁定库存`, '取消确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '再想想'
  }).then(() => {
    return cancelOutbound(row.id)
  }).then(() => {
    ElMessage.success('已取消，库存已解锁')
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
