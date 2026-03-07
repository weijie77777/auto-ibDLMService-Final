//package com.auto.controller;
//
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClientBuilder;
//
//import com.auto.entity.UserImage;
//import com.auto.service.UserImageService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import java.io.*;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@RestController
//public class TestController {
//    @Value("${aliyun.oss.endpoint}")
//    private String endpoint;
//
//    @Value("${aliyun.oss.accessKeyId}")
//    private String accessKeyId;
//
//    @Value("${aliyun.oss.accessKeySecret}")
//    private String accessKeySecret;
//
//    @Value("${aliyun.oss.bucketName}")
//    private String bucketName;
//
//    @Autowired
//    private UserImageService userImageService;
//
//    @GetMapping("/test")
//    public int runGcnModel(HttpServletRequest request) {
//        String pythonInterpreter = "D:\\Download\\anaconda\\envs\\test003\\python.exe";
//        String gcnModelPath = "D:\\pythonProject2\\GRU\\GRN1.py";
//        //String datasetPath = "D:\\pythonProject2\\GRU\\data";
//        String command = pythonInterpreter + " " + gcnModelPath + " " ;
//
//        try {
//            System.out.println("正在执行命令：" + command);
//            Process process = Runtime.getRuntime().exec(command);
//
//            // 读取进程的标准输出流
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            StringBuilder output = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//                System.out.println("Python输出：" + line);
//                // 在这里你可以根据需要处理输出，比如根据特定的输出内容做一些操作
//            }
//
//            // 等待进程结束
//            int exitVal = process.waitFor();
//            System.out.println("命令执行结束，退出值：" + exitVal);
//
//            if (exitVal == 0) {
//                //在模型运行结束之后，上传图片到阿里云服务器 并获取到一个url
//                // 指定固定的本地图片文件路径
//                String localFilePath = "D:\\SSM资料库\\reggie\\src\\main\\resources\\backend\\imgs\\local.png";
//
//                // 检查文件是否存在
//                File file = new File(localFilePath);
//                if (!file.exists()) {
//                    System.out.println("上传的文件不存在");
//                }
//
//                // 生成唯一标识符
//                String uuid = UUID.randomUUID().toString().replace("-", "");
//                // 获取文件名
//                String originalFilename = file.getName();
//                // 构建存储路径，这里可以根据需求自定义
//                String remoteFilePath = "images/" + uuid + "_" + originalFilename;
//
//                // 创建OSSClient对象
//                OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//                try {
//                    // 上传文件
//                    ossClient.putObject(bucketName, remoteFilePath, new FileInputStream(file));
//                    // 获取图片URL
//                    String imageUrl = "http://" + bucketName + "." + endpoint + "/" + remoteFilePath;
//                    System.out.println(imageUrl);
//                    //获得图片url之后，将其保存到数据库表中
//                    String username = (String) request.getSession().getAttribute("username");
//                    //获取当前时间
//                    LocalDateTime currentTime = LocalDateTime.now();
//                    UserImage userImage=new UserImage();
//                    userImage.setUsername(username);
//                    userImage.setImagePath(imageUrl);
//                    userImage.setCreateTime(currentTime);
//                    boolean result = userImageService.save(userImage);
//                    if (result) {
//                        System.out.println("用户图片添加成功");;
//                    } else {
//                        System.out.println("用户图片添加失败");;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    // 关闭OSSClient
//                    ossClient.shutdown();
//                }
//                return 1;
//            } else {
//                return 0;
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//}