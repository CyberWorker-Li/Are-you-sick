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
            <el-button type="primary" @click="openCreateDoctorDialog">添加医生</el-button>
          </div>
          <el-table :data="doctors" v-loading="loadingDoctors" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column label="科室" min-width="160">
              <template #default="scope">
                {{ scope.row.departmentName || '未分配' }}
              </template>
            </el-table-column>
            <el-table-column prop="title" label="职称" width="140" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button size="small" @click="editDoctor(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteDoctor(scope.row)">删除</el-button>
                <el-button size="small" type="info" @click="viewSchedules(scope.row)">排班</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 科室管理 -->
      <el-tab-pane label="科室管理" name="departments">
        <div class="section">
          <div class="section-header">
            <h2>科室列表</h2>
            <el-button type="primary" @click="openCreateDepartmentDialog">添加科室</el-button>
          </div>
          <el-table :data="departments" v-loading="loadingDepartments" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="科室名称" min-width="160" />
            <el-table-column prop="description" label="职能内容" min-width="220" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="创建时间" min-width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="医生数量" width="140">
              <template #default="scope">
                {{ getDepartmentDoctors(scope.row.id).length }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240">
              <template #default="scope">
                <el-button size="small" type="primary" text @click="viewDepartmentDetail(scope.row)">查看医生</el-button>
                <el-button size="small" @click="editDepartment(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteDepartment(scope.row)">删除</el-button>
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
          <el-table :data="schedules" v-loading="loadingSchedules" style="width: 100%">
            <el-table-column prop="doctorName" label="医生" width="140" />
            <el-table-column prop="dayOfWeek" label="星期" width="100" />
            <el-table-column prop="startTime" label="开始时间" width="120" />
            <el-table-column prop="endTime" label="结束时间" width="120" />
            <el-table-column prop="maxPatients" label="最大人数" width="120" />
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
          <el-table :data="adjustmentRequests" v-loading="loadingAdjustments" style="width: 100%">
            <el-table-column prop="doctorName" label="医生" width="140" />
            <el-table-column prop="dayOfWeek" label="星期" width="100" />
            <el-table-column prop="startTime" label="开始时间" width="120" />
            <el-table-column prop="endTime" label="结束时间" width="120" />
            <el-table-column prop="reason" label="申请原因" min-width="220" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="120">
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

      <!-- 统计看板 -->
      <el-tab-pane label="统计看板" name="stats">
        <div class="section stats-wrapper" v-loading="loadingStats">
          <div class="stats-toolbar">
            <h2>业务统计概览</h2>
            <div class="stats-controls">
              <el-date-picker
                v-model="statsDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                unlink-panels
                :disabled-date="disableFutureDate"
              />
              <el-button type="primary" @click="refreshStats()">刷新</el-button>
              <el-button @click="resetStatsRange">近7天</el-button>
            </div>
          </div>

          <div class="overview-grid">
            <el-card class="overview-card">
              <div class="metric-title">总患者数</div>
              <div class="metric-value">{{ formatNumber(overview.totalPatients) }}</div>
            </el-card>
            <el-card class="overview-card">
              <div class="metric-title">总医生数</div>
              <div class="metric-value">{{ formatNumber(overview.totalDoctors) }}</div>
            </el-card>
            <el-card class="overview-card">
              <div class="metric-title">科室数量</div>
              <div class="metric-value">{{ formatNumber(overview.totalDepartments) }}</div>
            </el-card>
            <el-card class="overview-card">
              <div class="metric-title">今日预约</div>
              <div class="metric-value">{{ formatNumber(overview.todaysAppointments) }}</div>
            </el-card>
            <el-card class="overview-card">
              <div class="metric-title">本周预约</div>
              <div class="metric-value">{{ formatNumber(overview.weekAppointments) }}</div>
            </el-card>
            <el-card class="overview-card">
              <div class="metric-title">待确认预约</div>
              <div class="metric-value">{{ formatNumber(overview.pendingAppointments) }}</div>
            </el-card>
          </div>

          <el-card class="stats-card">
            <template #header>
              <div class="card-header">
                <span>科室预约统计</span>
                <span class="card-subtitle">统计区间内按科室汇总</span>
              </div>
            </template>
            <el-table :data="departmentStats" size="small" stripe empty-text="暂无数据">
              <el-table-column prop="departmentName" label="科室" min-width="180" />
              <el-table-column prop="doctorCount" label="医生数" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.doctorCount) }}
                </template>
              </el-table-column>
              <el-table-column prop="appointmentCount" label="预约数" width="140">
                <template #default="scope">
                  {{ formatNumber(scope.row.appointmentCount) }}
                </template>
              </el-table-column>
              <el-table-column prop="description" label="职能内容" min-width="220" show-overflow-tooltip />
            </el-table>
          </el-card>

          <el-card class="stats-card">
            <template #header>
              <div class="card-header">
                <span>每日预约走势</span>
                <span class="card-subtitle">按日期统计预约状态</span>
              </div>
            </template>
            <el-table :data="dailyAppointmentStats" size="small" stripe empty-text="暂无数据">
              <el-table-column prop="date" label="日期" min-width="140" />
              <el-table-column prop="total" label="总预约" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.total) }}
                </template>
              </el-table-column>
              <el-table-column prop="pending" label="待确认" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.pending) }}
                </template>
              </el-table-column>
              <el-table-column prop="confirmed" label="已确认" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.confirmed) }}
                </template>
              </el-table-column>
              <el-table-column prop="completed" label="已完成" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.completed) }}
                </template>
              </el-table-column>
              <el-table-column prop="cancelled" label="已取消" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.cancelled) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card class="stats-card">
            <template #header>
              <div class="card-header">
                <span>医生工作量</span>
                <span class="card-subtitle">统计区间内医生接诊情况</span>
              </div>
            </template>
            <el-table :data="doctorWorkload" size="small" stripe empty-text="暂无数据">
              <el-table-column prop="doctorName" label="医生" min-width="160" />
              <el-table-column label="科室" min-width="140">
                <template #default="scope">
                  {{ scope.row.departmentName || '未分配' }}
                </template>
              </el-table-column>
              <el-table-column prop="totalAppointments" label="总预约" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.totalAppointments) }}
                </template>
              </el-table-column>
              <el-table-column prop="completedAppointments" label="已完成" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.completedAppointments) }}
                </template>
              </el-table-column>
              <el-table-column prop="pendingAppointments" label="待处理" width="120">
                <template #default="scope">
                  {{ formatNumber(scope.row.pendingAppointments) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
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
        <el-form-item label="科室" required>
          <el-select
            v-model="doctorForm.departmentId"
            placeholder="请选择科室"
            filterable
            :disabled="departments.length === 0"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
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

    <!-- 创建/编辑科室对话框 -->
    <el-dialog
      v-model="showDepartmentDialog"
      :title="editingDepartment ? '编辑科室' : '添加科室'"
      width="520px"
      :close-on-click-modal="false"
      @close="closeDepartmentDialog"
    >
      <el-form :model="departmentForm" label-width="90px">
        <el-form-item label="科室名称" required>
          <el-input v-model="departmentForm.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="职能内容">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="3"
            placeholder="简要介绍科室特色或服务"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDepartmentDialog = false">取消</el-button>
        <el-button type="primary" :loading="departmentSubmitting" @click="saveDepartment">
          {{ departmentSubmitting ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showDepartmentDetail"
      :title="currentDepartment ? `科室详情 - ${currentDepartment.name}` : '科室详情'"
      width="600px"
    >
      <div v-if="currentDepartment">
        <p class="department-brief">
          {{ currentDepartment.description || '暂无科室描述' }}
        </p>
        <el-alert
          v-if="selectedDepartmentDoctors.length === 0"
          title="该科室暂无医生"
          type="info"
          show-icon
          :closable="false"
          class="mb-3"
        />
        <el-table
          v-else
          :data="selectedDepartmentDoctors"
          size="small"
          stripe
          empty-text="暂无医生"
        >
          <el-table-column prop="id" label="医生ID" width="100" />
          <el-table-column prop="name" label="姓名" min-width="140" />
          <el-table-column prop="title" label="职称" min-width="120" />
          <el-table-column prop="phone" label="联系电话" min-width="160" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showDepartmentDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import adminApi, { type CreateDoctorRequest, type CreateScheduleRequest } from '../api/admin';
import departmentApi, { type DepartmentRequest } from '../api/department';
import type {
  DoctorDTO,
  DepartmentDTO,
  AdminOverviewStatsDTO,
  AdminDepartmentStatDTO,
  AdminDailyAppointmentDTO,
  AdminDoctorWorkloadDTO
} from '../types/dto';

const router = useRouter();
const auth = useAuthStore();

const userEmail = ref(auth.userInfo?.name || '管理员');
const activeTab = ref('doctors');

const loadingDoctors = ref(false);
const loadingSchedules = ref(false);
const loadingAdjustments = ref(false);
const loadingDepartments = ref(false);
const loadingStats = ref(false);
const statsLoaded = ref(false);

const doctors = ref<DoctorDTO[]>([]);
const departments = ref<DepartmentDTO[]>([]);
const schedules = ref<any[]>([]);
const adjustmentRequests = ref<any[]>([]);

const overview = ref<AdminOverviewStatsDTO>({
  totalPatients: 0,
  totalDoctors: 0,
  totalDepartments: 0,
  todaysAppointments: 0,
  weekAppointments: 0,
  pendingAppointments: 0
});
const departmentStats = ref<AdminDepartmentStatDTO[]>([]);
const dailyAppointmentStats = ref<AdminDailyAppointmentDTO[]>([]);
const doctorWorkload = ref<AdminDoctorWorkloadDTO[]>([]);

const statsDateRange = ref<[Date, Date] | null>(null);

const showCreateDoctorDialog = ref(false);
const showCreateScheduleDialog = ref(false);
const showDepartmentDialog = ref(false);

const editingDoctor = ref<DoctorDTO | null>(null);
const editingSchedule = ref<any>(null);
const editingDepartment = ref<DepartmentDTO | null>(null);

interface DoctorFormModel {
  name: string;
  email: string;
  password: string;
  phone: string;
  departmentId: number | null;
  title: string;
}

const createEmptyDoctorForm = (): DoctorFormModel => ({
  name: '',
  email: '',
  password: '',
  phone: '',
  departmentId: null,
  title: ''
});

const doctorForm = ref<DoctorFormModel>(createEmptyDoctorForm());

const scheduleForm = ref<any>({
  doctorId: null,
  dayOfWeek: '',
  startTime: null,
  endTime: null,
  maxPatients: 10
});

const departmentForm = reactive<DepartmentRequest>({
  name: '',
  description: ''
});

const departmentSubmitting = ref(false);
const showDepartmentDetail = ref(false);
const currentDepartment = ref<DepartmentDTO | null>(null);

const toDateString = (date: Date) => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const formatNumber = (value?: number) => new Intl.NumberFormat('zh-CN').format(value ?? 0);

const formatDateTime = (value?: string | null) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
};

const disableFutureDate = (date: Date) => {
  const todayEnd = new Date();
  todayEnd.setHours(23, 59, 59, 999);
  return date.getTime() > todayEnd.getTime();
};

const ensureStatsRange = () => {
  if (statsDateRange.value && statsDateRange.value.length === 2) {
    return;
  }
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 6);
  statsDateRange.value = [start, end];
};

const getStatsParams = () => {
  if (!statsDateRange.value || statsDateRange.value.length !== 2) {
    return undefined;
  }
  const [start, end] = statsDateRange.value;
  if (!start || !end) {
    return undefined;
  }
  return {
    startDate: toDateString(start),
    endDate: toDateString(end)
  };
};

const loadDoctors = async (): Promise<DoctorDTO[]> => {
  if (loadingDoctors.value) return doctors.value;
  try {
    loadingDoctors.value = true;
    const response = await adminApi.getAllDoctors();
    if (response.code === 200) {
      doctors.value = response.data || [];
    }
  } catch (error) {
    ElMessage.error('加载医生列表失败');
  } finally {
    loadingDoctors.value = false;
  }
  return doctors.value;
};

const loadSchedules = async () => {
  if (loadingSchedules.value) return;
  try {
    loadingSchedules.value = true;
    if (!doctors.value.length) {
      await loadDoctors();
    }
    const allSchedules: any[] = [];
    for (const doctor of doctors.value) {
      try {
        const response = await adminApi.getDoctorSchedules(doctor.id);
        if (response.code === 200 && Array.isArray(response.data)) {
          const enriched = response.data.map((item: any) => ({
            ...item,
            doctorName: doctor.name
          }));
          allSchedules.push(...enriched);
        }
      } catch (e) {
        // 忽略单个医生加载失败
      }
    }
    schedules.value = allSchedules;
  } catch (error) {
    ElMessage.error('加载排班列表失败');
  } finally {
    loadingSchedules.value = false;
  }
};

const loadAdjustmentRequests = async () => {
  if (loadingAdjustments.value) return;
  try {
    loadingAdjustments.value = true;
    const response = await adminApi.getAllAdjustmentRequests();
    if (response.code === 200) {
      adjustmentRequests.value = response.data || [];
    }
  } catch (error) {
    ElMessage.error('加载排班调整申请失败');
  } finally {
    loadingAdjustments.value = false;
  }
};

const loadDepartments = async (): Promise<DepartmentDTO[]> => {
  if (loadingDepartments.value) return departments.value;
  try {
    loadingDepartments.value = true;
    const response = await departmentApi.getAll();
    if (response.code === 200) {
      departments.value = response.data || [];
    }
  } catch (error) {
    ElMessage.error('加载科室列表失败');
  } finally {
    loadingDepartments.value = false;
  }
  return departments.value;
};

const loadStatsData = async () => {
  ensureStatsRange();
  const params = getStatsParams();
  loadingStats.value = true;
  try {
    const [overviewResp, departmentResp, dailyResp, doctorResp] = await Promise.all([
      adminApi.getOverviewStats(),
      adminApi.getDepartmentStats(params),
      adminApi.getDailyAppointmentStats(params),
      adminApi.getDoctorWorkload(params)
    ]);

    if (overviewResp.code === 200 && overviewResp.data) {
      overview.value = overviewResp.data;
    }

    if (departmentResp.code === 200) {
      departmentStats.value = departmentResp.data || [];
    }

    if (dailyResp.code === 200) {
      dailyAppointmentStats.value = (dailyResp.data || []).map(item => ({
        ...item,
        date: item.date
      }));
    }

    if (doctorResp.code === 200) {
      doctorWorkload.value = doctorResp.data || [];
    }

    statsLoaded.value = true;
  } catch (error) {
    ElMessage.error('获取统计数据失败');
  } finally {
    loadingStats.value = false;
  }
};

const refreshStats = async () => {
  if (loadingStats.value) return;
  await loadStatsData();
};

const resetStatsRange = () => {
  statsLoaded.value = false;
  statsDateRange.value = null;
  ensureStatsRange();
  refreshStats();
};

const saveDoctor = async () => {
  try {
    if (!doctorForm.value.name.trim()) {
      ElMessage.warning('请填写医生姓名');
      return;
    }
    if (!doctorForm.value.departmentId) {
      ElMessage.warning('请选择科室');
      return;
    }
    if (!editingDoctor.value) {
      if (!doctorForm.value.email.trim()) {
        ElMessage.warning('请填写医生邮箱');
        return;
      }
      if (!doctorForm.value.password.trim()) {
        ElMessage.warning('请设置登录密码');
        return;
      }
    }

    const buildDoctorPayload = (isEdit: boolean): CreateDoctorRequest => {
      const payload: CreateDoctorRequest = {
        name: doctorForm.value.name.trim(),
        departmentId: doctorForm.value.departmentId!,
        phone: doctorForm.value.phone.trim() ? doctorForm.value.phone.trim() : undefined,
        title: doctorForm.value.title.trim() ? doctorForm.value.title.trim() : undefined
      };

      if (isEdit) {
        if (doctorForm.value.email.trim()) {
          payload.email = doctorForm.value.email.trim();
        }
        if (doctorForm.value.password.trim()) {
          payload.password = doctorForm.value.password.trim();
        }
      } else {
        payload.email = doctorForm.value.email.trim();
        payload.password = doctorForm.value.password.trim();
      }

      return payload;
    };

    if (editingDoctor.value) {
      await adminApi.updateDoctor(editingDoctor.value.id, buildDoctorPayload(true));
      ElMessage.success('更新医生成功');
    } else {
      await adminApi.createDoctor(buildDoctorPayload(false));
      ElMessage.success('创建医生成功');
    }
    showCreateDoctorDialog.value = false;
    resetDoctorForm();
    await loadDoctors();
    await loadSchedules();
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

const openCreateDepartmentDialog = () => {
  resetDepartmentForm();
  showDepartmentDialog.value = true;
};

const editDepartment = (department: DepartmentDTO) => {
  editingDepartment.value = department;
  departmentForm.name = department.name;
  departmentForm.description = department.description || '';
  showDepartmentDialog.value = true;
};

const closeDepartmentDialog = () => {
  showDepartmentDialog.value = false;
  departmentSubmitting.value = false;
};

const saveDepartment = async () => {
  if (departmentSubmitting.value) return;
  if (!departmentForm.name.trim()) {
    ElMessage.warning('请填写科室名称');
    return;
  }
  try {
    departmentSubmitting.value = true;
    if (editingDepartment.value) {
      await departmentApi.update(editingDepartment.value.id, {
        name: departmentForm.name.trim(),
        description: departmentForm.description?.trim()
      });
      ElMessage.success('更新科室成功');
    } else {
      await departmentApi.create({
        name: departmentForm.name.trim(),
        description: departmentForm.description?.trim()
      });
      ElMessage.success('创建科室成功');
    }
    closeDepartmentDialog();
    resetDepartmentForm();
    await loadDepartments();
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败');
  } finally {
    departmentSubmitting.value = false;
  }
};

const deleteDepartment = async (department: DepartmentDTO) => {
  try {
    await ElMessageBox.confirm(`确定删除科室「${department.name}」吗？`, '确认删除', {
      type: 'warning'
    });
    await departmentApi.remove(department.id);
    ElMessage.success('删除科室成功');
    await loadDepartments();
  } catch (e) {
    // 用户取消
  }
};

const resetDoctorForm = () => {
  doctorForm.value = createEmptyDoctorForm();
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

const resetDepartmentForm = () => {
  departmentForm.name = '';
  departmentForm.description = '';
  editingDepartment.value = null;
};

const departmentDoctorMap = computed(() => {
  const map: Record<number, DoctorDTO[]> = {};
  doctors.value.forEach(doctor => {
    if (!doctor.departmentId) {
      return;
    }
    if (!map[doctor.departmentId]) {
      map[doctor.departmentId] = [];
    }
    map[doctor.departmentId].push(doctor);
  });
  return map;
});

const getDepartmentDoctors = (departmentId: number) => {
  return departmentDoctorMap.value[departmentId] || [];
};

const selectedDepartmentDoctors = computed(() => {
  if (!currentDepartment.value) {
    return [] as DoctorDTO[];
  }
  return getDepartmentDoctors(currentDepartment.value.id);
});

const openCreateDoctorDialog = async () => {
  resetDoctorForm();
  if (!departments.value.length) {
    await loadDepartments();
  }
  showCreateDoctorDialog.value = true;
};

const editDoctor = async (doctor: DoctorDTO) => {
  editingDoctor.value = doctor;
  if (!departments.value.length) {
    await loadDepartments();
  }
  doctorForm.value = {
    name: doctor.name,
    email: '',
    password: '',
    phone: doctor.phone || '',
    departmentId: doctor.departmentId ?? null,
    title: doctor.title || ''
  };
  showCreateDoctorDialog.value = true;
};

const viewDepartmentDetail = (department: DepartmentDTO) => {
  currentDepartment.value = department;
  showDepartmentDetail.value = true;
};

const deleteDoctor = async (doctor: DoctorDTO) => {
  try {
    await ElMessageBox.confirm('确定要删除该医生吗？', '确认删除', {
      type: 'warning'
    });
    await adminApi.deleteDoctor(doctor.id);
    ElMessage.success('删除成功');
    await loadDoctors();
    await loadSchedules();
  } catch (e) {
    // 用户取消
  }
};

const viewSchedules = (doctor: DoctorDTO) => {
  activeTab.value = 'schedules';
  // 可拓展筛选逻辑
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

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  };
  return map[status] || 'info';
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    APPROVED: '已批准',
    REJECTED: '已拒绝'
  };
  return map[status] || status;
};

const handleLogout = () => {
  auth.logout();
  router.push('/login');
  ElMessage.success('已退出登录');
};

watch(activeTab, (tab) => {
  if (tab === 'departments') {
    loadDepartments();
  }
  if (tab === 'adjustments') {
    loadAdjustmentRequests();
  }
  if (tab === 'stats' && !statsLoaded.value) {
    refreshStats();
  }
});

onMounted(async () => {
  ensureStatsRange();
  await loadDoctors();
  await loadSchedules();
  await Promise.all([loadAdjustmentRequests(), loadDepartments()]);
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

