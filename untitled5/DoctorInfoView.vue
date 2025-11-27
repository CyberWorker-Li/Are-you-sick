<template>
  <div class="doctor-info-page">
    <div class="header">
      <div class="header-left">
        <el-button type="primary" @click="goBack" class="back-btn">
          <i class="el-icon-arrow-left"></i>
          返回挂号页面
        </el-button>
        <h1>医生信息查询</h1>
      </div>
      <div class="user-info">
        <span>欢迎，{{ userEmail }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <div class="search-section">
      <el-card class="search-card">
        <el-form :model="searchForm" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="科室">
                <el-select
                  v-model="searchForm.department"
                  placeholder="选择科室"
                  clearable
                  @change="handleDepartmentChange"
                >
                  <el-option
                    v-for="dept in departments"
                    :key="dept.value"
                    :label="dept.label"
                    :value="dept.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="医生姓名">
                <el-input
                  v-model="searchForm.doctorName"
                  placeholder="输入医生姓名"
                  clearable
                  @input="handleSearch"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="职称">
                <el-select
                  v-model="searchForm.title"
                  placeholder="选择职称"
                  clearable
                  @change="handleSearch"
                >
                  <el-option label="主任医师" value="主任医师" />
                  <el-option label="副主任医师" value="副主任医师" />
                  <el-option label="主治医师" value="主治医师" />
                  <el-option label="住院医师" value="住院医师" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <i class="el-icon-search"></i>
              查询
            </el-button>
            <el-button @click="handleReset">
              <i class="el-icon-refresh"></i>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div class="doctors-section">
      <el-card>
        <div slot="header" class="table-header">
          <span>医生列表</span>
          <span class="total-count">共 {{ filteredDoctors.length }} 位医生</span>
        </div>

        <el-table
          :data="filteredDoctors"
          v-loading="loading"
          empty-text="暂无医生数据"
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="department" label="科室" width="120" />
          <el-table-column prop="title" label="职称" width="120" />
          <el-table-column prop="specialty" label="专业特长" min-width="200">
            <template #default="scope">
              <span v-if="scope.row.specialty">{{ scope.row.specialty }}</span>
              <span v-else style="color: #c0c4cc;">暂无信息</span>
            </template>
          </el-table-column>
          <el-table-column prop="introduction" label="医生介绍" min-width="300">
            <template #default="scope">
              <el-tooltip
                :content="scope.row.introduction"
                placement="top"
                v-if="scope.row.introduction"
              >
                <span class="intro-text">
                  {{ scope.row.introduction.length > 50 ? scope.row.introduction.substring(0, 50) + '...' : scope.row.introduction }}
                </span>
              </el-tooltip>
              <span v-else style="color: #c0c4cc;">暂无介绍</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="viewDoctorDetail(scope.row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 医生详情对话框 -->
    <el-dialog
      title="医生详细信息"
      v-model="detailDialogVisible"
      width="600px"
      :before-close="handleCloseDialog"
    >
      <div v-if="selectedDoctor" class="doctor-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ selectedDoctor.name }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ selectedDoctor.department }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ selectedDoctor.title || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ selectedDoctor.id }}</el-descriptions-item>
          <el-descriptions-item label="专业特长" :span="2">
            {{ selectedDoctor.specialty || '暂无信息' }}
          </el-descriptions-item>
          <el-descriptions-item label="医生介绍" :span="2">
            {{ selectedDoctor.introduction || '暂无介绍' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="goToRegistration(selectedDoctor)"
          v-if="selectedDoctor"
        >
          立即挂号
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import doctorApi from '../api/doctor';
import type { DoctorDTO } from '../types/dto';

const router = useRouter();
const auth = useAuthStore();

const userEmail = ref(auth.userInfo?.name || '用户');

// 搜索表单
const searchForm = ref({
  department: '',
  doctorName: '',
  title: ''
});

const departments = ref([
  { value: '内科', label: '内科' },
  { value: '外科', label: '外科' },
  { value: '儿科', label: '儿科' },
  { value: '妇产科', label: '妇产科' },
  { value: '眼科', label: '眼科' }
]);

// 严格按五个科室分类的专业特长
const departmentSpecialties: Record<string, string[]> = {
  '内科': [
    '心血管疾病诊断与治疗',
    '高血压病管理',
    '糖尿病诊治',
    '呼吸系统疾病',
    '消化系统疾病',
    '肾脏疾病治疗',
    '风湿免疫疾病'
  ],
  '外科': [
    '普外科手术',
    '骨科创伤治疗',
    '神经外科手术',
    '泌尿外科疾病',
    '胸外科手术',
    '乳腺外科疾病',
    '甲状腺疾病手术'
  ],
  '儿科': [
    '儿科常见病诊治',
    '儿童呼吸道疾病',
    '儿童生长发育',
    '新生儿疾病',
    '儿童营养指导',
    '儿童预防保健',
    '儿科急症处理'
  ],
  '妇产科': [
    '妇科疾病诊治',
    '产科孕产管理',
    '妇科内分泌',
    '不孕不育治疗',
    '妇科微创手术',
    '高危妊娠管理',
    '妇女保健指导'
  ],
  '眼科': [
    '眼科疾病诊断',
    '白内障手术',
    '青光眼治疗',
    '眼底病诊治',
    '屈光不正矫正',
    '眼表疾病治疗',
    '眼科常规手术'
  ]
};

// 医生介绍模板
const introductionTemplates = [
  '从事{department}工作{years}年，擅长{specialty}。',
  '{department}{title}，从业{years}年，精通{specialty}。',
  '在{department}领域工作{years}年，专注于{specialty}。',
  '{department}医生，拥有{years}年经验，擅长{specialty}。',
];

// 生成随机年份（5-20年）
const getRandomYears = () => Math.floor(Math.random() * 15) + 5;

// 获取科室相关特长
const getDepartmentSpecialty = (department: string) => {
  const specialties = departmentSpecialties[department];
  if (!specialties) {
    return '专科疾病诊治';
  }
  return specialties[Math.floor(Math.random() * specialties.length)];
};

// 生成随机介绍
const generateIntroduction = (doctor: DoctorDTO & { specialty: string }) => {
  const template = introductionTemplates[Math.floor(Math.random() * introductionTemplates.length)];
  const years = getRandomYears();

  return template
    .replace('{department}', doctor.department)
    .replace('{title}', doctor.title || '')
    .replace('{years}', years.toString())
    .replace('{specialty}', doctor.specialty);
};


// 医生数据
const allDoctors = ref<DoctorDTO[]>([]);
const filteredDoctors = ref<DoctorDTO[]>([]);
const loading = ref(false);
const detailDialogVisible = ref(false);
const selectedDoctor = ref<DoctorDTO | null>(null);

// 计算属性：过滤医生
const computedFilteredDoctors = computed(() => {
  let result = allDoctors.value;

  if (searchForm.value.department) {
    result = result.filter(doctor =>
      doctor.department === searchForm.value.department
    );
  }

  if (searchForm.value.doctorName) {
    result = result.filter(doctor =>
      doctor.name.includes(searchForm.value.doctorName)
    );
  }

  if (searchForm.value.title) {
    result = result.filter(doctor =>
      doctor.title === searchForm.value.title
    );
  }

  return result;
});

// 方法
const loadAllDoctors = async () => {
  try {
    loading.value = true;
    const response = await doctorApi.getAllDoctors();

    if (response.code === 200 && response.data) {
      // 为每个医生生成随机数据
      allDoctors.value = response.data.map(doctor => {
        const specialty = getDepartmentSpecialty(doctor.department);

        return {
          ...doctor,
          specialty: specialty,
          introduction: generateIntroduction({...doctor, specialty})
        };
      });

      filteredDoctors.value = allDoctors.value;
      ElMessage.success(`成功加载 ${allDoctors.value.length} 位医生信息`);
    } else {
      ElMessage.error('加载医生列表失败: ' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('加载医生列表异常:', error);
    ElMessage.error('加载医生列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  filteredDoctors.value = computedFilteredDoctors.value;
  ElMessage.success(`找到 ${filteredDoctors.value.length} 位医生`);
};

const handleReset = () => {
  searchForm.value = {
    department: '',
    doctorName: '',
    title: ''
  };
  filteredDoctors.value = allDoctors.value;
};

const handleDepartmentChange = () => {
  handleSearch();
};

const viewDoctorDetail = (doctor: DoctorDTO) => {
  selectedDoctor.value = doctor;
  detailDialogVisible.value = true;
};

const handleCloseDialog = () => {
  detailDialogVisible.value = false;
  selectedDoctor.value = null;
};

const goToRegistration = (doctor: DoctorDTO) => {
  // 跳转回挂号页面，并携带医生信息
  router.push({
    path: '/patient',
    query: {
      selectedDoctorId: doctor.id.toString(),
      selectedDepartment: doctor.department
    }
  });
};

const goBack = () => {
  router.push('/patient');
};

const handleLogout = () => {
  auth.logout();
  router.push('/login');
  ElMessage.success('已退出登录');
};

// 初始化加载数据
onMounted(() => {
  loadAllDoctors();
});
</script>

<style scoped>
.doctor-info-page {
  padding: 20px;
  max-width: 1200px;
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

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header h1 {
  color: #303133;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #606266;
}

.back-btn {
  background: linear-gradient(135deg, #909399, #a6a9ad);
  border: none;
  border-radius: 6px;
  font-weight: 500;
}

.back-btn:hover {
  background: linear-gradient(135deg, #a6a9ad, #909399);
}

.search-section {
  margin-bottom: 30px;
}

.search-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.doctors-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.total-count {
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
}

.intro-text {
  cursor: pointer;
  color: #606266;
}

.intro-text:hover {
  color: #409eff;
}

.doctor-detail {
  line-height: 1.6;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  background-color: #f5f7fa;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background-color: #f5f7fa;
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}

@media (max-width: 768px) {
  .doctor-info-page {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .header-left {
    flex-direction: column;
    gap: 10px;
  }
}
</style>