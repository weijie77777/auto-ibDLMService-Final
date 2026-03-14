# Interaction Network Frontend

Vue3 工程化项目，将原有的 backend 静态页面转换为 Vue3 组件化结构。

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口
│   │   ├── login.js
│   │   ├── register.js
│   │   ├── predict.js
│   │   ├── train.js
│   │   └── file.js
│   ├── assets/           # 静态资源
│   │   └── images/      # 图片资源（需要从 backend/imgs 复制）
│   ├── components/       # 公共组件
│   │   ├── NavBar.vue
│   │   └── Footer.vue
│   ├── router/          # 路由配置
│   │   └── index.js
│   ├── styles/          # 样式文件
│   │   ├── index.css
│   │   ├── home.css
│   │   ├── login.css
│   │   ├── register.css
│   │   └── about.css
│   ├── utils/           # 工具函数
│   │   ├── request.js
│   │   ├── common.js
│   │   └── auth.js
│   ├── views/           # 页面组件
│   │   ├── Home.vue
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── About.vue
│   │   ├── Introduce.vue
│   │   ├── Personnel.vue
│   │   ├── Predict.vue
│   │   ├── Train.vue
│   │   ├── TrainResult.vue
│   │   ├── TrainModelPredict.vue
│   │   ├── HistoricalRecord.vue
│   │   └── user/
│   │       ├── FileUpload.vue
│   │       ├── FileUpload1.vue
│   │       ├── FileUpload2.vue
│   │       └── FileList.vue
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## 安装和运行

1. 安装依赖：
```bash
npm install
```

2. 复制图片资源：
将 `backend/imgs/` 目录下的所有图片文件复制到 `src/assets/images/` 目录

3. 运行开发服务器：
```bash
npm run dev
```

4. 构建生产版本：
```bash
npm run build
```

## 注意事项

1. **图片资源迁移**：需要手动将 `backend/imgs/` 下的所有图片复制到 `src/assets/images/` 目录

2. **API 代理**：所有后端 API 请求已配置代理，确保后端服务运行在 `http://localhost:8080`

3. **路由守卫**：部分页面需要登录才能访问，已配置路由守卫

4. **Element Plus**：项目使用 Element Plus UI 库，已配置图标支持

## 待完成的工作

由于文件较多，以下组件需要根据原始 HTML 文件继续完善：

- `src/views/TrainResult.vue` - 训练结果页面
- `src/views/TrainModelPredict.vue` - 使用训练模型预测页面
- `src/views/HistoricalRecord.vue` - 历史记录页面
- `src/views/user/FileUpload1.vue` - 文件上传页面1
- `src/views/user/FileUpload2.vue` - 文件上传页面2
- `src/views/user/FileList.vue` - 文件列表页面

这些组件可以参考已创建的组件结构，按照相同的模式进行转换。

