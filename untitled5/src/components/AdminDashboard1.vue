<template>
  <div class="admin-dashboard">
    <div class="header">
      <h1>管理员系统</h1>
      <div class="user-info">
        <span>欢迎，{{ userEmail }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 医生信息管理 -->
      <el-tab-pane label="医生信息管理" name="doctors">
        <div class="section">
          <div class="section-header">
            <h2>医生列表</h2>
            <el-button type="primary" @click="showCreateDoctorDialog = true">添加医生</el-button>
          </div>
          <el-table :data="doctors" v-loading="loading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="department" label="科室" width="120" />
            <el-table-column prop="title" label="职称" width="120" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button size="small" @click="editDoctor(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteDoctor(scope.row)">删除</el-button>
                <el-button size="small" type="info" @click="viewSchedules(scope.row)">排班</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 排班管理 -->
      <el-tab-pane label="排班管理" name="schedules">
        <div class="section">
          <div class="section-header">
            <h2>排班管理</h2>
            <el-button type="primary" @click="showCreateScheduleDialog = true">添加排班</el-button>
          </div>
          <el-table :data="schedules" v-loading="loading" style="width: 100%">
            <el-table-column prop="doctorName" label="医生" width="120" />
            <el-table-column prop="dayOfWeek" label="星期" width="100" />
            <el-table-column prop="startTime" label="开始时间" width="120" />
            <el-table-column prop="endTime" label="结束时间" width="120" />
            <el-table-column prop="maxPatients" label="最大人数" width="100" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" @click="editSchedule(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteSchedule(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 排班调整申请 -->
      <el-tab-pane label="排班调整申请" name="adjustments">
        <div class="section">
          <h2>排班调整申请</h2>
          <el-table :data="adjustmentRequests" v-loading="loading" style="width: 100%">
            <el-table-column prop="doctorName" label="医生" width="120" />
            <el-table-column prop="dayOfWeek" label="星期" width="100" />
            <el-table-column prop="startTime" label="开始时间" width="120" />
            <el-table-column prop="endTime" label="结束时间" width="120" />
            <el-table-column prop="reason" label="申请原因" width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" v-if="adjustmentRequests.some(r => r.status === 'PENDING')">
              <template #default="scope">
                <el-button 
                  v-if="scope.row.status === 'PENDING'"
                  size="small" 
                  type="success" 
                  @click="approveRequest(scope.row)"
                >批准</el-button>
                <el-button 
                  v-if="scope.row.status === 'PENDING'"
                  size="small" 
                  type="danger" 
                  @click="rejectRequest(scope.row)"
                >拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑医生对话框 -->
    <el-dialog 
      v-model="showCreateDoctorDialog" 
      :title="editingDoctor ? '编辑医生' : '添加医生'"
      width="600px"
    >
      <el-form :model="doctorForm" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="doctorForm.name" />
        </el-form-item>
        <el-form-item label="邮箱" required>
          <el-input v-model="doctorForm.email" />
        </el-form-item>
        <el-form-item :label="editingDoctor ? '新密码' : '密码'" :required="!editingDoctor">
          <el-input v-model="doctorForm.password" type="password" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="doctorForm.phone" />
        </el-form-item>
        <el-form-item label="科室">
          <el-input v-model="doctorForm.department" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="doctorForm.title" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDoctorDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDoctor">保存</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑排班对话框 -->
    <el-dialog 
      v-model="showCreateScheduleDialog" 
      :title="editingSchedule ? '编辑排班' : '添加排班'"
      width="600px"
    >
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="医生" required>
          <el-select v-model="scheduleForm.doctorId" placeholder="请选择医生">
            <el-option
              v-for="doc in doctors"
              :key="doc.id"
              :label="doc.name"
              :value="doc.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="星期" required>
          <el-select v-model="scheduleForm.dayOfWeek" placeholder="请选择星期">
            <el-option label="星期一" value="MONDAY" />
            <el-option label="星期二" value="TUESDAY" />
            <el-option label="星期三" value="WEDNESDAY" />
            <el-option label="星期四" value="THURSDAY" />
            <el-option label="星期五" value="FRIDAY" />
            <el-option label="星期六" value="SATURDAY" />
            <el-option label="星期日" value="SUNDAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-picker 
            v-model="scheduleForm.startTime" 
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-picker 
            v-model="scheduleForm.endTime" 
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="scheduleForm.maxPatients" :min="1" :max="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import adminApi, { type CreateDoctorRequest, type CreateScheduleRequest } from '../api/admin';

const router = useRouter();
const auth = useAuthStore();

const userEmail = ref(auth.userInfo?.name || '管理员');
const activeTab = ref('doctors');
const loading = ref(false);

const doctors = ref<any[]>([]);
const schedules = ref<any[]>([]);
const adjustmentRequests = ref<any[]>([]);

const showCreateDoctorDialog = ref(false);
const showCreateScheduleDialog = ref(false);
const editingDoctor = ref<any>(null);
const editingSchedule = ref<any>(null);

const doctorForm = ref<CreateDoctorRequest>({
  name: '',
  email: '',
  password: '',
  phone: '',
  department: '',
  title: ''
});

const scheduleForm = ref<any>({
  doctorId: null,
  dayOfWeek: '',
  startTime: null,
  endTime: null,
  maxPatients: 10
});

const loadDoctors = async () => {
  try {
    loading.value = true;
    const response = await adminApi.getAllDoctors();
    if (response.code === 200) {
      doctors.value = response.data || [];
    }
  } catch (error) {
    ElMessage.error('加载医生列表失败');
  } finally {
    loading.value = false;
  }
};

const loadSchedules = async () => {
  try {
    loading.value = true;
    // 加载所有医生的排班
    const allSchedules: any[] = [];
    for (const doctor of doctors.value) {
      try {
        const response = await adminApi.getDoctorSchedules(doctor.id);
        if (response.code === 200 && response.data) {
          allSchedules.push(...response.data);
        }
      } catch (e) {
        // 忽略单个医生加载失败
      }
    }
    schedules.value = allSchedules;
  } catch (error) {
    ElMessage.error('加载排班列表失败');
  } finally {
    loading.value = false;
  }
};

const loadAdjustmentRequests = async () => {
  try {
    loading.value = true;
    const response = await adminApi.getAllAdjustmentRequests();
    if (response.code === 200) {
      adjustmentRequests.value = response.data || [];
    }
  } catch (error) {
    ElMessage.error('加载排班调整申请失败');
  } finally {
    loading.value = false;
  }
};

const saveDoctor = async () => {
  try {
    if (editingDoctor.value) {
      await adminApi.updateDoctor(editingDoctor.value.id, doctorForm.value);
      ElMessage.success('更新医生成功');
    } else {
      await adminApi.createDoctor(doctorForm.value);
      ElMessage.success('创建医生成功');
    }
    showCreateDoctorDialog.value = false;
    resetDoctorForm();
    await loadDoctors();
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败');
  }
};

const formatTimeValue = (value: any) => {
  if (!value) return '';
  if (typeof value === 'string') {
    return value.length > 5 ? value.slice(0, 5) : value;
  }
  if (value instanceof Date) {
    return value.toTimeString().slice(0, 5);
  }
  return String(value);
};

const saveSchedule = async () => {
  try {
    const request: CreateScheduleRequest = {
      doctorId: scheduleForm.value.doctorId,
      dayOfWeek: scheduleForm.value.dayOfWeek,
      startTime: formatTimeValue(scheduleForm.value.startTime),
      endTime: formatTimeValue(scheduleForm.value.endTime),
      maxPatients: scheduleForm.value.maxPatients
    };
    
    if (editingSchedule.value) {
      await adminApi.updateSchedule(editingSchedule.value.id, request);
      ElMessage.success('更新排班成功');
    } else {
      await adminApi.createSchedule(request);
      ElMessage.success('创建排班成功');
    }
    showCreateScheduleDialog.value = false;
    resetScheduleForm();
    await loadSchedules();
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败');
  }
};

const editDoctor = (doctor: any) => {
  editingDoctor.value = doctor;
  doctorForm.value = {
    name: doctor.name,
    email: '', // 需要从用户表获取
    password: '',
    phone: doctor.phone || '',
    department: doctor.department || '',
    title: doctor.title || ''
  };
  showCreateDoctorDialog.value = true;
};

const deleteDoctor = async (doctor: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该医生吗？', '确认删除', {
      type: 'warning'
    });
    await adminApi.deleteDoctor(doctor.id);
    ElMessage.success('删除成功');
    await loadDoctors();
  } catch (e) {
    // 用户取消
  }
};

const viewSchedules = (doctor: any) => {
  activeTab.value = 'schedules';
  // 可以添加筛选逻辑
};

const editSchedule = (schedule: any) => {
  editingSchedule.value = schedule;
  scheduleForm.value = {
    doctorId: schedule.doctorId,
    dayOfWeek: schedule.dayOfWeek,
    startTime: schedule.startTime,
    endTime: schedule.endTime,
    maxPatients: schedule.maxPatients
  };
  showCreateScheduleDialog.value = true;
};

const deleteSchedule = async (schedule: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '确认删除', {
      type: 'warning'
    });
    await adminApi.deleteSchedule(schedule.id);
    ElMessage.success('删除成功');
    await loadSchedules();
  } catch (e) {
    // 用户取消
  }
};

const approveRequest = async (request: any) => {
  try {
    await adminApi.approveAdjustmentRequest(request.id);
    ElMessage.success('批准成功');
    await loadAdjustmentRequests();
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败');
  }
};

const rejectRequest = async (request: any) => {
  try {
    await adminApi.rejectAdjustmentRequest(request.id);
    ElMessage.success('拒绝成功');
    await loadAdjustmentRequests();
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败');
  }
};

const resetDoctorForm = () => {
  doctorForm.value = {
    name: '',
    email: '',
    password: '',
    phone: '',
    department: '',
    title: ''
  };
  editingDoctor.value = null;
};

const resetScheduleForm = () => {
  scheduleForm.value = {
    doctorId: null,
    dayOfWeek: '',
    startTime: null,
    endTime: null,
    maxPatients: 10
  };
  editingSchedule.value = null;
};

const getStatusType = (status: string) => {
  const map: { [key: string]: string } = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  };
  return map[status] || 'info';
};

const getStatusText = (status: string) => {
  const map: { [key: string]: string } = {
    'PENDING': '待处理',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝'
  };
  return map[status] || status;
};

const handleLogout = () => {
  auth.logout();
  router.push('/login');
  ElMessage.success('已退出登录');
};

onMounted(async () => {
  await loadDoctors();
  await loadSchedules();
  await loadAdjustmentRequests();
});
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.main-tabs {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>

