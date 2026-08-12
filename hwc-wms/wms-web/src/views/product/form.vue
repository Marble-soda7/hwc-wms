<template>
  <div class="form-page">
    <el-card shadow="never">
      <template #header>
        <div class="form-header">
          <el-button link type="primary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>返回列表
          </el-button>
          <span class="form-title">{{ isEdit ? '编辑商品' : '新增商品' }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 960px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="24">
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
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">归属信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="货主/客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
                <el-option v-for="c in customerList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" filterable style="width: 100%">
                <el-option v-for="c in flatCategoryList" :key="c.id" :label="c.label" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">规格信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="重量(kg)">
              <el-input-number v-model="form.weight" :precision="3" :step="0.1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
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
          <el-col :span="8">
            <el-form-item label="申报单价(元)">
              <el-input-number v-model="form.unitPrice" :precision="2" :step="0.01" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="安全库存">
              <el-input-number v-model="form.safetyStock" :step="1" :min="0" style="width: 100%" />
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
import { getProduct, getNextSkuCode, addProduct, updateProduct } from '@/api/product'
import { listCustomers } from '@/api/customer'
import { getCategoryTree } from '@/api/category'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
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
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function goBack() {
  router.push('/product')
}

// 加载下拉数据：客户 + 分类（展平为带缩进的两级选项）
function loadRefData() {
  listCustomers().then(res => {
    customerList.value = res.data
  })
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

onMounted(() => {
  loadRefData()
  if (isEdit.value) {
    getProduct(route.params.id).then(res => {
      Object.assign(form, res.data)
    })
  } else {
    // 新增时自动生成 SKU 编码
    getNextSkuCode().then(res => {
      form.skuCode = res.data
    })
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  const api = isEdit.value ? updateProduct : addProduct
  api(form)
    .then(() => {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      goBack()
    })
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
