<template>
  <div class="form-page">
    <el-card shadow="never">
      <template #header>
        <div class="form-header">
          <el-button link type="primary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>返回列表
          </el-button>
          <span class="form-title">{{ isEdit ? '编辑库位' : '新增库位' }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 960px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="所属仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" filterable style="width: 100%">
                <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
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
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">空闲</el-radio>
                <el-radio :label="2">占用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">库位位置</el-divider>
        <el-row :gutter="24">
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
        <div class="code-hint">
          <el-icon><InfoFilled /></el-icon>
          建议编码格式：区域-货道-货架-层，如 A-01-01-01
        </div>
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
import { getLocation, getNextLocationCode, addLocation, updateLocation } from '@/api/location'
import { listWarehouses } from '@/api/warehouse'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const submitLoading = ref(false)

const warehouseList = ref([])

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

function goBack() {
  router.push('/location')
}

function fillNextCode() {
  getNextLocationCode().then(res => {
    form.code = res.data
  })
}

onMounted(() => {
  listWarehouses().then(res => {
    warehouseList.value = res.data
  })
  if (isEdit.value) {
    getLocation(route.params.id).then(res => {
      Object.assign(form, res.data)
    })
  } else {
    // 新增时预填库位编码
    fillNextCode()
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  const api = isEdit.value ? updateLocation : addLocation
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
.code-hint {
  color: #909399;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
