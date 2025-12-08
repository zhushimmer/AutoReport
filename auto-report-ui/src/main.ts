import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import TDesign from 'tdesign-vue-next' 
import TDesignChat from '@tdesign-vue-next/chat' // 引入chat组件

import './assets/main.css'
import 'tdesign-vue-next/es/style/index.css' // 引入少量全局样式变量

const app = createApp(App)

app.use(router)
app.use(TDesign)
app.use(TDesignChat)
app.mount('#app')
