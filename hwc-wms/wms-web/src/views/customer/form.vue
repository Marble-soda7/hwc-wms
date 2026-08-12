<template>
  <div class="form-page">
    <el-card shadow="never">
      <template #header>
        <div class="form-header">
          <el-button link type="primary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>返回列表
          </el-button>
          <span class="form-title">{{ isEdit ? '编辑客户' : '新增客户' }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 960px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="客户编码" prop="code">
              <el-input v-model="form.code" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入客户名称" />
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

        <el-divider content-position="left">联系信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contact" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址">
              <el-input v-model="form.address" placeholder="请输入地址" />
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
import { getCustomer, getNextCode, addCustomer, updateCustomer } from '@/api/customer'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const submitLoading = ref(false)

const form = reactive({
  id: null,
  code: '',
  name: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function goBack() {
  router.push('/customer')
}

onMounted(() => {
  if (isEdit.value) {
    getCustomer(route.params.id).then(res => {
      Object.assign(form, res.data)
    })
  } else {
    // 新增时自动生成客户编码
    getNextCode().then(res => {
      form.code = res.data
    })
  }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  const api = isEdit.value ? updateCustomer : addCustomer
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
