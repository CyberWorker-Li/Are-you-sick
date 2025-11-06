<template>
  <div class="dashboard">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="250px">
        <div class="logo">
          <h2>医院挂号系统</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="overview">
            <el-icon><DataBoard /></el-icon>
            <span>概览</span>
          </el-menu-item>
          <el-menu-item index="registration">
            <el-icon><DocumentAdd /></el-icon>
            <span>预约挂号</span>
          </el-menu-item>
          <el-menu-item index="registrations">
            <el-icon><Document /></el-icon>
            <span>挂号记录</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="departments">
            <el-icon><OfficeBuilding /></el-icon>
            <span>科室管理</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="doctors">
            <el-icon><User /></el-icon>
            <span>医生管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-content">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item>{{ getBreadcrumbText() }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="user-info">
              <el-dropdown @command="handleUserCommand">
                <span class="user-name">
                  {{ userInfo?.name || '用户' }}
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                    <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main class="main-content">
          <!-- 概览页面 -->
          <div v-if="activeMenu === 'overview'" class="overview">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon color="#409EFF"><DocumentAdd /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.totalRegistrations }}</div>
                      <div class="stat-label">总挂号数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon color="#67C23A"><CircleCheck /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.completedRegistrations }}</div>
                      <div class="stat-label">已完成</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon color="#E6A23C"><Clock /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.pendingRegistrations }}</div>
                      <div class="stat-label">待就诊</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon color="#F56C6C"><CircleClose /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.cancelledRegistrations }}</div>
                      <div class="stat-label">已取消</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 最近挂号记录 -->
            <el-card class="recent-registrations" style="margin-top: 20px;">
              <template #header>
                <div class="card-header">
                  <span>最近挂号记录</span>
                  <el-button type="text" @click="activeMenu = 'registrations'">查看全部</el-button>
                </div>
              </template>
              <el-table :data="recentRegistrations" style="width: 100%">
                <el-table-column prop="patientName" label="患者姓名" width="100" />
                <el-table-column prop="departmentName" label="科室" width="120" />
                <el-table-column prop="doctorName" label="医生" width="100" />
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
              </el-table>
            </el-card>
          </div>

          <!-- 挂号表单 -->
          <RegistrationForm v-if="activeMenu === 'registration'" />

          <!-- 挂号记录列表 -->
          <RegistrationList v-if="activeMenu === 'registrations'" />

          <!-- 科室管理（管理员） -->
          <div v-if="activeMenu === 'departments' && isAdmin" class="departments">
            <h3>科室管理功能开发中...</h3>
          </div>

          <!-- 医生管理（管理员） -->
          <div v-if="activeMenu === 'doctors' && isAdmin" class="doctors">
            <h3>医生管理功能开发中...</h3>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  DataBoard,
  DocumentAdd,
  Document,
  OfficeBuilding,
  User,
  ArrowDown,
  CircleCheck,
  Clock,
  CircleClose
} from '@element-plus/icons-vue';
import { useAuthStore } from '../stores/auth';
import { registrationApi } from '../api/registration';
import RegistrationForm from './RegistrationForm.vue';
import RegistrationList from './RegistrationList.vue';
import type { Registration } from '../types/dto';

const router = useRouter();
const authStore = useAuthStore();

const activeMenu = ref('overview');
const recentRegistrations = ref<Registration[]>([]);

const stats = reactive({
  totalRegistrations: 0,
  completedRegistrations: 0,
  pendingRegistrations: 0,
  cancelledRegistrations: 0
});

const userInfo = computed(() => authStore.userInfo);
const isAdmin = computed(() => authStore.userInfo?.role === 'ADMIN');

// 获取面包屑文本
const getBreadcrumbText = () => {
  switch (activeMenu.value) {
    case 'overview': return '概览';
    case 'registration': return '预约挂号';
    case 'registrations': return '挂号记录';
    case 'departments': return '科室管理';
    case 'doctors': return '医生管理';
    default: return '概览';
  }
};

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  activeMenu.value = index;
};

// 处理用户下拉菜单
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中...');
      break;
    case 'logout':
      try {
        await ElMessageBox.confirm('确认退出登录吗？', '确认退出', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        });
        authStore.logout();
        router.push('/login');
      } catch {
        // 用户取消
      }
      break;
  }
};

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

// 加载统计数据
const loadStats = async () => {
  try {
    const registrations = await registrationApi.getList();
    
    stats.totalRegistrations = registrations.length;
    stats.completedRegistrations = registrations.filter(r => r.status === 'COMPLETED').length;
    stats.pendingRegistrations = registrations.filter(r => r.status === 'PENDING').length;
    stats.cancelledRegistrations = registrations.filter(r => r.status === 'CANCELLED').length;
    
    // 获取最近5条记录
    recentRegistrations.value = registrations
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 5);
  } catch (error) {
    ElMessage.error('加载统计数据失败');
  }
};

onMounted(() => {
  loadStats();
});
</script>

<style scoped>
.dashboard {
  height: 100vh;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #e6e6e6;
}

.logo h2 {
  margin: 0;
  color: #409EFF;
}

.sidebar-menu {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-name {
  cursor: pointer;
  color: #606266;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.recent-registrations .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>