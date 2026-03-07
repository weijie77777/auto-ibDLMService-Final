# 图片资源迁移说明

## 操作步骤

1. 将 `backend/imgs/` 目录下的所有图片文件复制到 `src/assets/images/` 目录

2. 可以使用以下命令（在项目根目录执行）：

**Windows PowerShell:**
```powershell
Copy-Item -Path "backend\imgs\*" -Destination "src\assets\images\" -Recurse
```

**或者手动复制以下文件：**
- a.jpg
- bg.jpg
- btn_close@2x.png
- developer1.jpg
- developer2.jpg
- developer3.jpg
- img_1.png 到 img_11.png
- img.png
- local.png
- logo.png
- logo1.png
- loss.png
- lun1.jpg 到 lun6.jpg
- p1.png 到 p6.png
- search.png
- simple-network.gif

## 注意事项

- 确保所有图片文件都正确复制
- 图片路径在 Vue 组件中已使用 `@/assets/images/` 别名引用
- 如果图片路径有问题，检查 vite.config.js 中的别名配置

