<template>
  <div class="admin-dashboard">
    <header class="dashboard-header">
      <h1>管理员系统</h1>
      <div class="header-right">
        <el-icon class="user-icon">
          <UserFilled />
        </el-icon>
        <span class="user-name">{{ userGreeting }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <section class="dashboard-hub">
      <div class="hub-surface">
        <el-tabs v-model="activeTab" class="hub-tabs" type="border-card">
          <button
            v-for="metric in metricCards"
            :key="metric.key"
            class="metric-card"
            :class="{
              'is-active': metric.key === activeMetric
            }"
            type="button"
            @click="selectMetric(metric.key)"
          >
            <div class="metric-icon" :class="metric.accent">
              <el-icon>
                <component :is="metric.icon" />
              </el-icon>
            </div>
            <div class="metric-details">
              <span class="metric-label">{{ metric.label }}</span>
              <span class="metric-value">{{ formatNumber(metric.value) }}</span>
              <span v-if="metric.sublabel" class="metric-sublabel">{{ metric.sublabel }}</span>
            </div>
            <el-tooltip effect="dark" placement="top" :content="metric.description">
              <span class="metric-info">
                <el-icon><InfoFilled /></el-icon>
              </span>
            </el-tooltip>
          </button>

          <!-- 医生信息管理 -->
          <el-tab-pane name="doctors">
            <template #label>
              <div class="tab-label-with-metric">
                <el-icon class="tab-icon"><UserFilled /></el-icon>
                <div class="tab-text">
                  <span class="tab-title">医生信息管理</span>
                  <span class="tab-metric">{{ doctors.length }}位医生</span>
                </div>
              </div>
            </template>
            <div class="hub-toolbar">
              <div class="toolbar-filters">
                <el-select
                  v-model="doctorDepartmentFilter"
                  placeholder="按科室筛选"
                  clearable
                  size="large"
                  class="filter-select"
                >
                  <el-option label="全部科室" :value="null" />
                  <el-option
                    v-for="dept in departments"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                  />
                </el-select>
                <el-input
                  v-model="doctorKeyword"
                  placeholder="搜索医生姓名 / 手机"
                  size="large"
                  clearable
                  class="filter-input"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
              <div class="toolbar-actions">
                <el-button type="primary" size="large" @click="openCreateDoctorDialog">添加医生</el-button>
              </div>
            </div>
            <div class="section">
              <div class="section-header">
                <div class="section-title">
                  <h2>医生列表</h2>
                  <p class="section-subtitle">快速检索医生账号，支持按科室筛选编辑管理</p>
                </div>
                <div class="section-meta">
                  <span>共 {{ formatNumber(filteredDoctors.length) }} 位医生</span>
                </div>
              </div>
              <el-table :data="pagedDoctors" v-loading="loadingDoctors" style="width: 100%">
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
              <div class="table-footer">
                <el-pagination
                  background
                  layout="total, prev, pager, next"
                  :page-size="doctorPageSize"
                  :total="filteredDoctors.length"
                  :current-page="doctorPage"
                  @current-change="handleDoctorPageChange"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 科室管理 -->
          <el-tab-pane name="departments">
            <template #label>
              <div class="tab-label-with-metric">
                <el-icon class="tab-icon"><OfficeBuilding /></el-icon>
                <div class="tab-text">
                  <span class="tab-title">科室管理</span>
                  <span class="tab-metric">{{ departments.length }}个科室</span>
                </div>
              </div>
            </template>
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
          <el-tab-pane name="schedules">
            <template #label>
              <div class="tab-label-with-metric">
                <el-icon class="tab-icon"><Calendar /></el-icon>
                <div class="tab-text">
                  <span class="tab-title">排班管理</span>
                  <span class="tab-metric">{{ schedules.length }}条排班</span>
                </div>
              </div>
            </template>
            <div class="section">
              <div class="section-header">
                <h2>排班管理</h2>
                <el-button type="primary" @click="showCreateScheduleDialog = true">添加排班</el-button>
              </div>
              <el-table :data="schedules" v-loading="loadingSchedules" style="width: 100%">
                <el-table-column prop="doctorName" label="医生" width="140" />
                <el-table-column prop="dayOfWeek" label="星期" width="160" class-name="weekday-col" />
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
          <el-tab-pane name="adjustments">
            <template #label>
              <div class="tab-label-with-metric">
                <el-icon class="tab-icon"><TrendCharts /></el-icon>
                <div class="tab-text">
                  <span class="tab-title">排班调整申请</span>
                  <span class="tab-metric">{{ adjustmentRequests.filter(r => r.status === 'PENDING').length }}待审批</span>
                </div>
              </div>
            </template>
            <div class="section">
              <h2>排班调整申请</h2>
              <el-table :data="adjustmentRequests" v-loading="loadingAdjustments" style="width: 100%">
                <el-table-column prop="doctorName" label="医生" width="140" />
                <el-table-column prop="dayOfWeek" label="星期" width="160" class-name="weekday-col" />
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
                    <el-button v-if="scope.row.status === 'PENDING'" size="small" type="success" @click="approveRequest(scope.row)">批准</el-button>
                    <el-button v-if="scope.row.status === 'PENDING'" size="small" type="danger" @click="rejectRequest(scope.row)">拒绝</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 统计看板 -->
          <el-tab-pane name="stats">
            <template #label>
              <div class="tab-label-with-metric">
                <el-icon class="tab-icon"><TrendCharts /></el-icon>
                <div class="tab-text">
                  <span class="tab-title">统计看板</span>
                  <span class="tab-metric">数据分析</span>
                </div>
              </div>
            </template>
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
                    size="large"
                    :disabled-date="disableFutureDate"
                  />
                  <el-button type="primary" size="large" @click="refreshStats()">刷新</el-button>
                  <el-button size="large" @click="resetStatsRange">近7天</el-button>
                </div>
              </div>

              <div class="overview-grid">
                <el-card class="overview-card" shadow="hover">
                  <div class="metric-title">总患者数</div>
                  <div class="metric-value">{{ formatNumber(overview.totalPatients) }}</div>
                </el-card>
                <el-card class="overview-card" shadow="hover">
                  <div class="metric-title">总医生数</div>
                  <div class="metric-value">{{ formatNumber(overview.totalDoctors) }}</div>
                </el-card>
                <el-card class="overview-card" shadow="hover">
                  <div class="metric-title">科室数量</div>
                  <div class="metric-value">{{ formatNumber(overview.totalDepartments) }}</div>
                </el-card>
                <el-card class="overview-card" shadow="hover">
                  <div class="metric-title">今日预约</div>
                  <div class="metric-value">{{ formatNumber(overview.todaysAppointments) }}</div>
                </el-card>
                <el-card class="overview-card" shadow="hover">
                  <div class="metric-title">本周预约</div>
                  <div class="metric-value">{{ formatNumber(overview.weekAppointments) }}</div>
                </el-card>
                <el-card class="overview-card" shadow="hover">
                  <div class="metric-title">待确认预约</div>
                  <div class="metric-value">{{ formatNumber(overview.pendingAppointments) }}</div>
                </el-card>
              </div>

              <el-card class="stats-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>科室预约统计</span>
                    <span class="card-subtitle">统计区间内按科室汇总</span>
                  </div>
                </template>
                <el-table :data="departmentStats" stripe empty-text="暂无数据">
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

              <el-card class="stats-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>每日预约情况</span>
                    <span class="card-subtitle">按日期统计预约状态</span>
                  </div>
                </template>
                <el-table :data="dailyAppointmentStats" stripe empty-text="暂无数据">
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

              <el-card class="stats-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>医生工作量</span>
                    <span class="card-subtitle">统计区间内医生接诊情况</span>
                  </div>
                </template>
                <el-table :data="doctorWorkload" stripe empty-text="暂无数据">
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
      </div>
    </section>

    <el-dialog
      v-model="showDepartmentDialog"
      :title="editingDepartment ? '编辑科室' : '添加科室'"
      width="560px"
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
            placeholder="请输入科室职能内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDepartmentDialog">取消</el-button>
        <el-button type="primary" :loading="departmentSubmitting" @click="saveDepartment">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDepartmentDetail" title="科室医生" width="760px">
      <div style="margin-bottom: 12px; font-weight: 600; color: #303133;">
        {{ currentDepartment?.name || '科室' }}
      </div>
      <el-table :data="selectedDepartmentDoctors" style="width: 100%" empty-text="暂无医生">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="140" />
        <el-table-column prop="title" label="职称" width="140" />
        <el-table-column prop="phone" label="手机号" width="160" />
      </el-table>
      <template #footer>
        <el-button @click="showDepartmentDetail = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showCreateDoctorDialog"
      :title="editingDoctor ? '编辑医生' : '添加医生'"
      width="640px"
    >
      <el-form :model="doctorForm" label-width="90px">
        <el-form-item label="姓名" required>
          <el-input v-model="doctorForm.name" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="科室" required>
          <el-select v-model="doctorForm.departmentId" placeholder="请选择科室" style="width: 100%">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="doctorForm.title" placeholder="如：主任医师" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="doctorForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-if="!editingDoctor" label="邮箱" required>
          <el-input v-model="doctorForm.email" placeholder="用于登录" />
        </el-form-item>
        <el-form-item v-if="!editingDoctor" label="密码" required>
          <el-input v-model="doctorForm.password" show-password placeholder="用于登录" />
        </el-form-item>
        <el-form-item v-if="editingDoctor" label="邮箱">
          <el-input v-model="doctorForm.email" placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item v-if="editingDoctor" label="密码">
          <el-input v-model="doctorForm.password" show-password placeholder="留空则不修改" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDoctorDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDoctor">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showCreateScheduleDialog"
      :title="editingSchedule ? '编辑排班' : '添加排班'"
      width="560px"
    >
      <el-form :model="scheduleForm" label-width="90px">
        <el-form-item label="医生" required>
          <el-select v-model="scheduleForm.doctorId" placeholder="请选择医生" style="width: 100%">
            <el-option
              v-for="doc in doctors"
              :key="doc.id"
              :label="doc.name"
              :value="doc.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="星期" required>
          <el-select v-model="scheduleForm.dayOfWeek" placeholder="请选择星期" style="width: 100%">
            <el-option label="周一" value="MONDAY" />
            <el-option label="周二" value="TUESDAY" />
            <el-option label="周三" value="WEDNESDAY" />
            <el-option label="周四" value="THURSDAY" />
            <el-option label="周五" value="FRIDAY" />
            <el-option label="周六" value="SATURDAY" />
            <el-option label="周日" value="SUNDAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-picker
            v-model="scheduleForm.startTime"
            placeholder="开始时间"
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-picker
            v-model="scheduleForm.endTime"
            placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="最大人数" required>
          <el-input-number v-model="scheduleForm.maxPatients" :min="1" :max="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import adminApi, { type CreateDoctorRequest, type CreateScheduleRequest, type UpdateDoctorRequest } from '../api/admin';
import departmentApi, { type DepartmentRequest } from '../api/department';
import {
  UserFilled,
  Search,
  OfficeBuilding,
  Calendar,
  TrendCharts,
  InfoFilled
} from '@element-plus/icons-vue';
import type {
  DoctorDTO,
  DepartmentDTO,
  AdminOverviewStatsDTO,
  AdminDepartmentStatDTO,
  AdminDailyAppointmentDTO,
  AdminDoctorWorkloadDTO,
  ScheduleAdjustmentRequestDTO
} from '../types/dto';

const router = useRouter();
const auth = useAuthStore();

const userGreeting = computed(() => auth.greetingText);
const activeTab = ref('doctors');

const loadingDoctors = ref(false);
const loadingSchedules = ref(false);
const loadingAdjustments = ref(false);
const loadingDepartments = ref(false);
const loadingStats = ref(false);
const statsLoaded = ref(false);

const doctorDepartmentFilter = ref<number | null>(null);
const doctorKeyword = ref('');
const doctorPage = ref(1);
const doctorPageSize = 8;

const doctors = ref<DoctorDTO[]>([]);
const departments = ref<DepartmentDTO[]>([]);
const schedules = ref<any[]>([]);
const adjustmentRequests = ref<ScheduleAdjustmentRequestDTO[]>([]);

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

type MetricKey = 'doctors' | 'departments' | 'schedules' | 'pending';

const statsDateRange = ref<[Date, Date] | null>(null);
const activeMetric = ref<MetricKey>('doctors');

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

const departmentDoctorMap = computed<Record<number, DoctorDTO[]>>(() => {
  return doctors.value.reduce((map, doctor) => {
    const deptId = doctor.departmentId;
    if (!deptId) {
      return map;
    }
    const bucket = map[deptId] ?? (map[deptId] = []);
    bucket.push(doctor);
    return map;
  }, {} as Record<number, DoctorDTO[]>);
});

const filteredDoctors = computed<DoctorDTO[]>(() => {
  const keyword = doctorKeyword.value.trim().toLowerCase();
  const departmentFilter = doctorDepartmentFilter.value;
  return doctors.value.filter((doctor) => {
    const matchesDepartment = departmentFilter == null || doctor.departmentId === departmentFilter;
    if (!keyword) {
      return matchesDepartment;
    }
    const target = `${doctor.name ?? ''}${doctor.phone ?? ''}`.toLowerCase();
    return matchesDepartment && target.includes(keyword);
  });
});

const pagedDoctors = computed<DoctorDTO[]>(() => {
  const start = (doctorPage.value - 1) * doctorPageSize;
  return filteredDoctors.value.slice(start, start + doctorPageSize);
});

const averageDoctorsPerDepartment = computed(() => {
  if (!departments.value.length) {
    return 0;
  }
  return Math.round(doctors.value.length / departments.value.length);
});

const unassignedDoctorsCount = computed(() =>
  doctors.value.filter((doc) => !doc.departmentId).length
);

const hasPendingAdjustments = computed(() =>
  adjustmentRequests.value.some((request) => request.status === 'PENDING')
);

const metricCards = computed(() => {
  const pendingAdjustments = adjustmentRequests.value.filter((item) => item.status === 'PENDING').length;
  return [
    {
      key: 'doctors' as MetricKey,
      label: '医生数量',
      value: doctors.value.length,
      sublabel: '含在岗与新入职医生',
      description: '当前系统内可管理的医生账号数量',
      icon: UserFilled,
      accent: 'accent-indigo'
    },
    {
      key: 'departments' as MetricKey,
      label: '科室覆盖',
      value: departments.value.length,
      sublabel: '当前运营科室总数',
      description: '所有已上线且可分配医生的科室数',
      icon: OfficeBuilding,
      accent: 'accent-emerald'
    },
    {
      key: 'schedules' as MetricKey,
      label: '排班总览',
      value: schedules.value.length,
      sublabel: '本周排班次数',
      description: '医生排班总数，可快速检测是否存在空档',
      icon: Calendar,
      accent: 'accent-amber'
    },
    {
      key: 'pending' as MetricKey,
      label: '待审批',
      value: pendingAdjustments,
      sublabel: '排班调整待处理',
      description: '待管理员回复的排班调整请求数量',
      icon: TrendCharts,
      accent: 'accent-rose'
    }
  ];
});

watch([doctorDepartmentFilter, doctorKeyword], () => {
  doctorPage.value = 1;
});

watch(filteredDoctors, (list) => {
  const maxPage = Math.max(1, Math.ceil(list.length / doctorPageSize));
  if (doctorPage.value > maxPage) {
    doctorPage.value = maxPage;
  }
});

const handleDoctorPageChange = (page: number) => {
  doctorPage.value = page;
};

const selectMetric = (key: MetricKey) => {
  activeMetric.value = key;
  if (key === 'doctors') {
    activeTab.value = 'doctors';
  } else if (key === 'departments') {
    activeTab.value = 'departments';
  } else if (key === 'schedules') {
    activeTab.value = 'schedules';
  } else if (key === 'pending') {
    activeTab.value = 'adjustments';
  }
};

watch(activeTab, (tab) => {
  if (tab === 'departments') {
    loadDepartments();
    activeMetric.value = 'departments';
  } else if (tab === 'adjustments') {
    loadAdjustmentRequests();
    activeMetric.value = 'pending';
  } else if (tab === 'stats') {
    activeMetric.value = 'pending';
    if (!statsLoaded.value) {
      refreshStats();
    }
  }

  if (tab === 'doctors') {
    activeMetric.value = 'doctors';
  } else if (tab === 'schedules') {
    activeMetric.value = 'schedules';
  }
});

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
      dailyAppointmentStats.value = (dailyResp.data || []).map((item: AdminDailyAppointmentDTO) => ({
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

    const buildDoctorPayload = (isEdit: boolean): CreateDoctorRequest | UpdateDoctorRequest => {
      const payload: any = {
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
      await adminApi.updateDoctor(editingDoctor.value.id, buildDoctorPayload(true) as UpdateDoctorRequest);
      ElMessage.success('更新医生成功');
    } else {
      await adminApi.createDoctor(buildDoctorPayload(false) as CreateDoctorRequest);
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

  const trimmedName = departmentForm.name.trim();

  if (!trimmedName) {
    ElMessage.warning('请填写科室名称');
    return;
  }

  if (!editingDepartment.value) {
    const normalizedName = trimmedName.toLowerCase();
    const hasDuplicate = departments.value.some(
      dept => dept.name.trim().toLowerCase() === normalizedName
    );
    if (hasDuplicate) {
      ElMessage.warning('科室已存在');
      return;
    }
  }
  try {
    departmentSubmitting.value = true;
    const payload = {
      name: trimmedName,
      description: departmentForm.description?.trim()
    };
    if (editingDepartment.value) {
      await departmentApi.update(editingDepartment.value.id, payload);
      ElMessage.success('更新科室成功');
    } else {
      await departmentApi.create(payload);
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
    startTime: formatTimeValue(schedule.startTime),
    endTime: formatTimeValue(schedule.endTime),
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
  // 逻辑已合并到上方 watch 覆盖
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
  background-color: #3675d3;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.dashboard-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right .user-icon {
  font-size: 20px;
  color: #409eff;
}

.header-right .user-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.dashboard-hub {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.hub-surface {
  position: relative;
}

.metric-card {
  display: none;
}

.hub-tabs {
  border: none;
}

.hub-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
}

.hub-tabs :deep(.el-tabs__nav) {
  border: none;
}

.hub-tabs :deep(.el-tabs__item) {
  padding: 16px 20px;
  height: auto;
  line-height: 1.4;
  border: none;
  border-right: 1px solid #e4e7ed;
}

.hub-tabs :deep(.el-tabs__item:last-child) {
  border-right: none;
}

.tab-label-with-metric {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tab-icon {
  font-size: 18px;
  color: #909399;
}

.hub-tabs :deep(.el-tabs__item.is-active) .tab-icon {
  color: #409eff;
}

.tab-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.tab-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.tab-metric {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.hub-tabs :deep(.el-tabs__item.is-active) .tab-title {
  color: #409eff;
  font-weight: 600;
}

.hub-tabs :deep(.el-tabs__item.is-active) .tab-metric {
  color: #66b1ff;
}

.hub-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.toolbar-filters {
  display: flex;
  gap: 12px;
  flex: 1;
}

.filter-select {
  width: 220px;
}

.filter-input {
  width: 320px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.toolbar-filters :deep(.el-input__wrapper),
.toolbar-actions :deep(.el-button) {
  min-height: 42px;
  font-size: 15px;
}

.toolbar-filters :deep(.el-input__inner),
.toolbar-filters :deep(.el-select__selected-item) {
  font-size: 15px;
}

.section {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title h2 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.section-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.section-meta {
  font-size: 14px;
  color: #606266;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.stats-wrapper {
  padding: 28px;
}

.stats-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
}

.stats-toolbar h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.stats-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stats-controls :deep(.el-date-editor) {
  min-width: 280px;
}

.stats-controls :deep(.el-input__wrapper) {
  font-size: 15px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}

.overview-card {
  text-align: center;
  padding: 18px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
}

.overview-card:hover {
  transform: translateY(-4px);
}

.metric-title {
  font-size: 15px;
  color: #909399;
  margin-bottom: 12px;
  font-weight: 500;
}

.metric-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

@media (min-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}

.stats-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
}

.stats-card:last-child {
  margin-bottom: 0;
}

.stats-card :deep(.el-card__header) {
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-bottom: 2px solid #e4e7ed;
}

.stats-card :deep(.el-card__body) {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header > span:first-child {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.card-subtitle {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

.stats-card :deep(.el-table) {
  font-size: 14px;
}

.stats-card :deep(.el-table th) {
  font-size: 14px;
  padding: 14px 0;
  font-weight: 600;
}

.stats-card :deep(.el-table td) {
  font-size: 14px;
  padding: 14px 0;
}

.stats-card :deep(.el-table__row:hover) {
  background-color: #f0f9ff;
}

.admin-dashboard :deep(.weekday-col .cell) {
  white-space: nowrap;
}
</style>
