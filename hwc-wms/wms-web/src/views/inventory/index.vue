<template>
  <div class="page-container">
    <el-tabs v-model="activeTab">
      <!-- ==================== 库存列表 ==================== -->
      <el-tab-pane label="库存列表" name="list">
        <div class="search-bar">
          <el-input v-model="query.keyword" placeholder="商品名称、SKU或条码" clearable style="width: 220px" @keyup.enter="handleSearch" />
          <el-select v-model="query.warehouseId" placeholder="仓库" clearable filterable style="width: 150px">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
          <el-select v-model="query.customerId" placeholder="货主/客户" clearable filterable style="width: 150px">
            <el-option v-for="c in customerList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-select v-model="query.locationId" placeholder="库位" clearable filterable style="width: 150px">
            <el-option v-for="l in locationList" :key="l.id" :label="l.code" :value="l.id" />
          </el-select>
          <el-checkbox v-model="query.warnOnly">仅看库存预警</el-checkbox>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button type="success" @click="handleAdjust">
            <el-icon><EditPen /></el-icon>库存调整
          </el-button>
        </div>

        <el-table :data="tableData" v-loading="loading" border stripe>
          <el-table-column label="商品" min-width="180">
            <template #default="{ row }">{{ productMap[row.productId]?.name || '-' }}</template>
          </el-table-column>
          <el-table-column label="SKU" width="120">
            <template #default="{ row }">{{ productMap[row.productId]?.skuCode || '-' }}</template>
          </el-table-column>
          <el-table-column label="货主/客户" width="160">
            <template #default="{ row }">{{ customerMap[row.customerId] || '-' }}</template>
          </el-table-column>
          <el-table-column label="仓库" width="120">
            <template #default="{ row }">{{ warehouseMap[row.warehouseId] || '-' }}</template>
          </el-table-column>
          <el-table-column label="库位" width="100">
            <template #default="{ row }">{{ locationMap[row.locationId] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次" width="90">
            <template #default="{ row }">{{ row.batchNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="在库" width="80" align="center" />
          <el-table-column prop="lockedQuantity" label="锁定" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.lockedQuantity > 0" class="locked-num">{{ row.lockedQuantity }}</span>
              <span v-else>0</span>
            </template>
          </el-table-column>
          <el-table-column prop="availableQuantity" label="可用" width="80" align="center" />
          <el-table-column label="预警" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="isWarn(row)" type="danger" size="small">预警</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="160" />
        </el-table>

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

        <!-- 库存调整弹窗 -->
        <el-dialog v-model="adjustVisible" title="库存调整" width="560px" :close-on-click-modal="false">
          <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px">
            <template #title>调整将直接变更库存并生成调整流水。若该商品库存有批次号，请填写相同批次，否则将新增一条库存记录</template>
          </el-alert>
          <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="90px">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="商品" prop="productId">
                  <el-select v-model="adjustForm.productId" placeholder="选择商品" filterable style="width: 100%">
                    <el-option v-for="p in productList" :key="p.id" :label="`${p.name}（${p.skuCode}）`" :value="p.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="货主/客户" prop="customerId">
                  <el-select v-model="adjustForm.customerId" placeholder="选择客户" filterable style="width: 100%">
                    <el-option v-for="c in customerList" :key="c.id" :label="c.name" :value="c.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="仓库" prop="warehouseId">
                  <el-select v-model="adjustForm.warehouseId" placeholder="选择仓库" filterable style="width: 100%" @change="onAdjustWarehouseChange">
                    <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="库位" prop="locationId">
                  <el-select v-model="adjustForm.locationId" placeholder="选择库位" filterable style="width: 100%">
                    <el-option v-for="l in adjustLocationList" :key="l.id" :label="l.code" :value="l.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="批次号">
                  <el-input v-model="adjustForm.batchNo" placeholder="留空仅匹配无批次库存" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="调整方式" prop="type">
                  <el-radio-group v-model="adjustForm.type">
                    <el-radio label="INCREASE">增加</el-radio>
                    <el-radio label="DECREASE">减少</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="调整数量" prop="quantity">
                  <el-input-number v-model="adjustForm.quantity" :min="1" :step="1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="调整原因" prop="reason">
                  <el-select v-model="adjustForm.reason" placeholder="选择原因" style="width: 100%">
                    <el-option label="盘点调整" value="盘点调整" />
                    <el-option label="报损" value="报损" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <template #footer>
            <el-button @click="adjustVisible = false">取消</el-button>
            <el-button type="primary" :loading="adjustLoading" @click="handleAdjustSubmit">确定调整</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ==================== 库存流水 ==================== -->
      <el-tab-pane label="库存流水" name="logs">
        <div class="search-bar">
          <el-select v-model="logQuery.changeType" placeholder="变更类型" clearable style="width: 140px">
            <el-option v-for="(label, value) in logTypeMap" :key="value" :label="label" :value="value" />
          </el-select>
          <el-input v-model="logQuery.orderNo" placeholder="关联单号" clearable style="width: 180px" @keyup.enter="handleLogSearch" />
          <el-input v-model="logQuery.productKeyword" placeholder="商品名称、SKU或条码" clearable style="width: 200px" @keyup.enter="handleLogSearch" />
          <el-date-picker
            v-model="logQuery.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
          <el-button type="primary" @click="handleLogSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
        </div>

        <el-table :data="logData" v-loading="logLoading" border stripe>
          <el-table-column label="商品" min-width="180">
            <template #default="{ row }">{{ productMap[row.productId]?.name || '-' }}</template>
          </el-table-column>
          <el-table-column label="SKU" width="120">
            <template #default="{ row }">{{ productMap[row.productId]?.skuCode || '-' }}</template>
          </el-table-column>
          <el-table-column label="变更类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="logTagType[row.changeType] || 'info'" size="small">{{ logTypeMap[row.changeType] || row.changeType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="变动数量" width="100" align="center">
            <template #default="{ row }">
              <span :class="row.changeQuantity >= 0 ? 'change-in' : 'change-out'">
                {{ row.changeQuantity >= 0 ? '+' : '' }}{{ row.changeQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="beforeQuantity" label="变动前" width="80" align="center" />
          <el-table-column prop="afterQuantity" label="变动后" width="80" align="center" />
          <el-table-column prop="orderNo" label="关联单号" width="170" show-overflow-tooltip />
          <el-table-column prop="remark" label="备注" min-width="160" />
          <el-table-column prop="createTime" label="时间" width="160" />
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="logPage"
            v-model:page-size="logPageSize"
            :page-sizes="[10, 20, 50]"
            :total="logTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLogs"
            @current-change="loadLogs"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { pageInventory, adjustInventory, pageInventoryLogs } from '@/api/inventory'
import { listWarehouses } from '@/api/warehouse'
import { listCustomers } from '@/api/customer'
import { listProducts } from '@/api/product'
import { listLocations } from '@/api/location'

// ========== 字典 ==========
const logTypeMap = {
  INBOUND: '入库',
  OUTBOUND: '出库',
  ADJUST: '盘点调整',
  LOCK: '锁定',
  UNLOCK: '解锁'
}
const logTagType = {
  INBOUND: 'success',
  OUTBOUND: 'danger',
  ADJUST: 'warning',
  LOCK: 'primary',
  UNLOCK: 'info'
}

// ========== 库存列表 ==========
const activeTab = ref('list')
const query = reactive({
  keyword: '',
  warehouseId: null,
  customerId: null,
  locationId: null,
  warnOnly: false
})
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// 关联数据
const warehouseMap = ref({})
const customerMap = ref({})
const locationMap = ref({})
const productMap = ref({})
const warehouseList = ref([])
const customerList = ref([])
const locationList = ref([])
const productList = ref([])

function isWarn(row) {
  const safety = productMap.value[row.productId]?.safetyStock
  return safety != null && safety > 0 && row.quantity < safety
}

function loadData() {
  loading.value = true
  pageInventory({
    page: page.value,
    pageSize: pageSize.value,
    keyword: query.keyword || undefined,
    warehouseId: query.warehouseId || undefined,
    customerId: query.customerId || undefined,
    locationId: query.locationId || undefined,
    warnOnly: query.warnOnly || undefined
  }).then(res => {
    tableData.value = res.data.records
    total.value = res.data.total
  }).finally(() => { loading.value = false })
}

function handleSearch() {
  page.value = 1
  loadData()
}

// ========== 库存调整 ==========
const adjustVisible = ref(false)
const adjustLoading = ref(false)
const adjustFormRef = ref(null)
const adjustLocationList = ref([])
const adjustForm = reactive({
  productId: null,
  customerId: null,
  warehouseId: null,
  locationId: null,
  batchNo: '',
  type: 'INCREASE',
  quantity: 1,
  reason: ''
})

const adjustRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  customerId: [{ required: true, message: '请选择货主/客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  locationId: [{ required: true, message: '请选择库位', trigger: 'change' }],
  type: [{ required: true, message: '请选择调整方式', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入调整数量', trigger: 'change' }],
  reason: [{ required: true, message: '请选择调整原因', trigger: 'change' }]
}

function handleAdjust() {
  adjustForm.productId = null
  adjustForm.customerId = null
  adjustForm.warehouseId = null
  adjustForm.locationId = null
  adjustForm.batchNo = ''
  adjustForm.type = 'INCREASE'
  adjustForm.quantity = 1
  adjustForm.reason = ''
  adjustLocationList.value = []
  adjustVisible.value = true
}

function onAdjustWarehouseChange(warehouseId) {
  adjustForm.locationId = null
  if (!warehouseId) {
    adjustLocationList.value = []
    return
  }
  listLocations(warehouseId).then(res => {
    adjustLocationList.value = res.data
  })
}

async function handleAdjustSubmit() {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return
  adjustLoading.value = true
  adjustInventory({
    productId: adjustForm.productId,
    customerId: adjustForm.customerId,
    warehouseId: adjustForm.warehouseId,
    locationId: adjustForm.locationId,
    batchNo: adjustForm.batchNo || null,
    type: adjustForm.type,
    quantity: adjustForm.quantity,
    reason: adjustForm.reason
  }).then(() => {
    ElMessage.success('调整成功')
    adjustVisible.value = false
    loadData()
  }).finally(() => { adjustLoading.value = false })
}

// ========== 库存流水 ==========
const logQuery = reactive({
  changeType: '',
  orderNo: '',
  productKeyword: '',
  dateRange: []
})
const logPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)
const logData = ref([])
const logLoading = ref(false)

function loadLogs() {
  logLoading.value = true
  pageInventoryLogs({
    page: logPage.value,
    pageSize: logPageSize.value,
    changeType: logQuery.changeType || undefined,
    orderNo: logQuery.orderNo || undefined,
    productKeyword: logQuery.productKeyword || undefined,
    startTime: logQuery.dateRange?.[0] || undefined,
    endTime: logQuery.dateRange?.[1] || undefined
  }).then(res => {
    logData.value = res.data.records
    logTotal.value = res.data.total
  }).finally(() => { logLoading.value = false })
}

function handleLogSearch() {
  logPage.value = 1
  loadLogs()
}

// 切换 tab 时若首次进入流水页则加载
watch(activeTab, tab => {
  if (tab === 'logs' && logData.value.length === 0) {
    loadLogs()
  }
})

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
    res.data.forEach(p => { map[p.id] = p } )
    productMap.value = map
  })
  listLocations().then(res => {
    locationList.value = res.data
    const map = {}
    res.data.forEach(l => { map[l.id] = l.code })
    locationMap.value = map
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
  align-items: center;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.locked-num {
  color: #e6a23c;
  font-weight: bold;
}
.change-in {
  color: #67c23a;
  font-weight: bold;
}
.change-out {
  color: #f56c6c;
  font-weight: bold;
}
</style>
