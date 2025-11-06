<template>
  <div class="registration-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>挂号记录</span>
          <div class="header-actions">
            <el-button type="primary" @click="exportExcel" :loading="exportLoading">
              <el-icon><Download /></el-icon>
              导出Excel
            </el-button>
            <el-button @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filters" inline>
          <el-form-item label="科室">
            <el-select v-model="filters.departmentId" placeholder="全部科室" clearable @change="loadData">
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="医生">
            <el-select v-model="filters.doctorId" placeholder="全部医生" clearable @change="loadData">
              <el-option
                v-for="doctor in doctors"
                :key="doctor.id"
                :label="doctor.name"
                :value="doctor.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="全部状态" clearable @change="loadData">
              <el-option label="待就诊" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="filteredData"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="挂号ID" width="80" />
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
        <el-table-column prop="registrationTime" label="预约时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.registrationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              @click="updateStatus(row.id, 'COMPLETED')"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="warning"
              size="small"
              @click="updateStatus(row.id, 'CANCELLED')"
            >
              取消
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteRecord(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Download, Refresh } from '@element-plus/icons-vue';
import { registrationApi } from '../api/registration';
import { departmentApi } from '../api/department';
import { doctorApi } from '../api/doctor';
import type { Registration, Department, Doctor } from '../types/dto';

const loading = ref(false);
const exportLoading = ref(false);
const registrations = ref<Registration[]>([]);
const departments = ref<Department[]>([]);
const doctors = ref<Doctor[]>([]);

const currentPage = ref(1);
const pageSize = ref(20);

const filters = reactive({
  departmentId: null as number | null,
  doctorId: null as number | null,
  status: null as string | null
});

// 过滤后的数据
const filteredData = computed(() => {
  let data = registrations.value;
  
  if (filters.departmentId) {
    data = data.filter(item => {
      const dept = departments.value.find(d => d.name === item.departmentName);
      return dept?.id === filters.departmentId;
    });
  }
  
  if (filters.doctorId) {
    data = data.filter(item => {
      const doctor = doctors.value.find(d => d.name === item.doctorName);
      return doctor?.id === filters.doctorId;
    });
  }
  
  if (filters.status) {
    data = data.filter(item => item.status === filters.status);
  }
  
  return data;
});

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString('zh-CN');
};

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'COMPLETED': return 'success';
    case 'CANCELLED': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待就诊';
    case 'COMPLETED': return '已完成';
    case 'CANCELLED': return '已取消';
    default: return status;
  }
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    registrations.value = await registrationApi.getList();
  } catch (error) {
    ElMessage.error('加载挂号记录失败');
  } finally {
    loading.value = false;
  }
};

// 加载科室和医生数据
const loadDepartmentsAndDoctors = async () => {
  try {
    const [deptData, doctorData] = await Promise.all([
      departmentApi.getList(),
      doctorApi.getList()
    ]);
    departments.value = deptData;
    doctors.value = doctorData;
  } catch (error) {
    ElMessage.error('加载基础数据失败');
  }
};

// 更新状态
const updateStatus = async (id: number, status: string) => {
  try {
    await ElMessageBox.confirm(
      `确认将挂号记录状态更新为"${getStatusText(status)}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await registrationApi.updateStatus(id, status);
    ElMessage.success('状态更新成功');
    await loadData();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '状态更新失败');
    }
  }
};

// 删除记录
const deleteRecord = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确认删除这条挂号记录吗？此操作不可恢复！',
      '确认删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    );
    
    await registrationApi.delete(id);
    ElMessage.success('删除成功');
    await loadData();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

// 导出Excel
const exportExcel = async () => {
  exportLoading.value = true;
  try {
    const blob = await registrationApi.exportExcel();
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `挂号记录_${new Date().toISOString().slice(0, 10)}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('导出成功');
  } catch (error) {
    ElMessage.error('导出失败');
  } finally {
    exportLoading.value = false;
  }
};

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
};

onMounted(() => {
  loadData();
  loadDepartmentsAndDoctors();
});
</script>

<style scoped>
.registration-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>