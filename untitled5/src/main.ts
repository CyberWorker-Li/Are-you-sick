import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import {createPinia} from 'pinia'
import { useAuthStore } from './stores/auth'


const app = createApp(App)
const pinia = createPinia()


app.use(ElementPlus)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.use(pinia)
app.use(router)

const auth = useAuthStore(); // 注意：如果要在 main.ts 直接使用 store，需要在 pinia 注册之后
auth.init();


app.mount('#app')
