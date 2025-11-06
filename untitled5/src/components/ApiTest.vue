<template>
  <div class="api-test">
    <el-card>
      <template #header>
        <span>API接口测试</span>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 认证接口测试 -->
        <el-tab-pane label="认证接口" name="auth">
          <div class="test-section">
            <h4>发送验证码</h4>
            <el-form inline>
              <el-form-item label="邮箱">
                <el-input v-model="testData.email" placeholder="test@example.com" />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="testData.codeType">
                  <el-option label="注册" value="REGISTER" />
                  <el-option label="重置密码" value="RESET_PASSWORD" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button @click="testSendCode">发送验证码</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="test-section">
            <h4>用户注册</h4>
            <el-form inline>
              <el-form-item label="邮箱">
                <el-input v-model="testData.registerEmail" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="testData.registerPassword" type="password" />
              </el-form-item>
              <el-form-item label="验证码">
                <el-input v-model="testData.registerCode" />
              </el-form-item>
              <el-form-item>
                <el-button @click="testRegister">注册</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 科室接口测试 -->
        <el-tab-pane label="科室接口" name="department">
          <div class="test-section">
            <el-button @click="testGetDepartments">获取科室列表</el-button>
            <div v-if="departments.length" class="result">
              <h5>科室列表：</h5>
              <ul>
                <li v-for="dept in departments" :key="dept.id">
                  {{ dept.id }} - {{ dept.name }} ({{ dept.description }})
                </li>
              </ul>
            </div>
          </div>
        </el-tab-pane>

        <!-- 医生接口测试 -->
        <el-tab-pane label="医生接口" name="doctor">
          <div class="test-section">
            <el-button @click="testGetDoctors">获取医生列表</el-button>
            <div v-if="doctors.length" class="result">
              <h5>医生列表：</h5>
              <ul>
                <li v-for="doctor in doctors" :key="doctor.id">
                  {{ doctor.id }} - {{ doctor.name }} ({{ doctor.title }}) - 科室ID: {{ doctor.departmentId }}
                </li>
              </ul>
            </div>
          </div>
        </el-tab-pane>

        <!-- 挂号接口测试 -->
        <el-tab-pane label="挂号接口" name="registration">
          <div class="test-section">
            <h4>创建挂号</h4>
            <el-form inline>
              <el-form-item label="科室ID">
                <el-input v-model.number="testData.departmentId" type="number" />
              </el-form-item>
              <el-form-item label="医生ID">
                <el-input v-model.number="testData.doctorId" type="number" />
              </el-form-item>
              <el-form-item label="患者姓名">
                <el-input v-model="testData.patientName" />
              </el-form-item>
              <el-form-item label="电话">
                <el-input v-model="testData.phone" />
              </el-form-item>
              <el-form-item label="身份证">
                <el-input v-model="testData.idCard" />
              </el-form-item>
              <el-form-item>
                <el-button @click="testCreateRegistration">创建挂号</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="test-section">
            <el-button @click="testGetRegistrations">获取挂号列表</el-button>
            <div v-if="registrations.length" class="result">
              <h5>挂号列表：</h5>
              <ul>
                <li v-for="reg in registrations" :key="reg.id">
                  {{ reg.id }} - {{ reg.patientName }} - {{ reg.departmentName }} - {{ reg.doctorName }} - {{ reg.status }}
                </li>
              </ul>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 响应结果 -->
      <div v-if="lastResponse" class="response-section">
        <h4>最后响应：</h4>
        <pre>{{ JSON.stringify(lastResponse, null, 2) }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { authApi } from '../api/auth';
import { departmentApi } from '../api/department';
import { doctorApi } from '../api/doctor';
import { registrationApi } from '../api/registration';
import type { Department, Doctor, Registration } from '../types/dto';

const activeTab = ref('auth');
const lastResponse = ref<any>(null);
const departments = ref<Department[]>([]);
const doctors = ref<Doctor[]>([]);
const registrations = ref<Registration[]>([]);

const testData = reactive({
  email: 'test@example.com',
  codeType: 'REGISTER',
  registerEmail: 'test@example.com',
  registerPassword: 'password123',
  registerCode: '',
  departmentId: 1,
  doctorId: 1,
  patientName: '张三',
  phone: '13800138000',
  idCard: '110101199001011234'
});

const testSendCode = async () => {
  try {
    const result = await authApi.sendCode({
      email: testData.email,
      type: testData.codeType
    });
    lastResponse.value = result;
    ElMessage.success('验证码发送成功');
  } catch (error: any) {
    lastResponse.value = error;
    ElMessage.error(error.message || '发送失败');
  }
};

const testRegister = async () => {
  try {
    const result = await authApi.register({
      email: testData.registerEmail,
      password: testData.registerPassword,
      verificationCode: testData.registerCode
    });
    lastResponse.value = result;
    ElMessage.success('注册成功');
  } catch (error: any) {
    lastResponse.value = error;
    ElMessage.error(error.message || '注册失败');
  }
};

const testGetDepartments = async () => {
  try {
    const result = await departmentApi.getList();
    departments.value = result;
    lastResponse.value = result;
    ElMessage.success('获取科室列表成功');
  } catch (error: any) {
    lastResponse.value = error;
    ElMessage.error(error.message || '获取失败');
  }
};

const testGetDoctors = async () => {
  try {
    const result = await doctorApi.getList();
    doctors.value = result;
    lastResponse.value = result;
    ElMessage.success('获取医生列表成功');
  } catch (error: any) {
    lastResponse.value = error;
    ElMessage.error(error.message || '获取失败');
  }
};

const testCreateRegistration = async () => {
  try {
    const result = await registrationApi.create({
      departmentId: testData.departmentId,
      doctorId: testData.doctorId,
      patientName: testData.patientName,
      phone: testData.phone,
      idCard: testData.idCard,
      registrationTime: new Date().toISOString()
    });
    lastResponse.value = result;
    ElMessage.success('创建挂号成功');
  } catch (error: any) {
    lastResponse.value = error;
    ElMessage.error(error.message || '创建失败');
  }
};

const testGetRegistrations = async () => {
  try {
    const result = await registrationApi.getList();
    registrations.value = result;
    lastResponse.value = result;
    ElMessage.success('获取挂号列表成功');
  } catch (error: any) {
    lastResponse.value = error;
    ElMessage.error(error.message || '获取失败');
  }
};
</script>

<style scoped>
.api-test {
  padding: 20px;
}

.test-section {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
}

.test-section h4 {
  margin-top: 0;
  color: #409EFF;
}

.result {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.result h5 {
  margin-top: 0;
}

.result ul {
  margin: 0;
  padding-left: 20px;
}

.response-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.response-section pre {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}
</style>