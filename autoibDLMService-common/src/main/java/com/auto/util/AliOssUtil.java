package com.auto.util;

// const downloadSample = async() => {
// try {
//   const response = await request({
//     url: '/network/downloadSample',
//     method: 'GET',
//     responseType: 'blob'  // 关键：指定响应类型为二进制流
//   })

//   // 创建下载链接
//   const blob = new Blob([response.data], { type: 'text/csv' })
//   const link = document.createElement('a')
//   link.href = URL.createObjectURL(blob)
//   link.download = 'sample.csv'  // 下载文件名
//   document.body.appendChild(link)
//   link.click()
//   document.body.removeChild(link)
//   URL.revokeObjectURL(link.href)

// } catch (error) {
//   console.error('下载失败:', error)
//   alert('下载失败')
// }
// };
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.OSSObject;
import com.aliyuncs.exceptions.ClientException;
import com.auto.properties.AliOssProperties;
import com.auto.vo.NetworkResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
@Component
public class AliOssUtil {

    @Autowired
    private AliOssProperties aliyunOSSProperties;
    public String upload(byte[] content, String originalFilename, String parentPath) throws Exception {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        // 填写Object完整路径，例如202406/1.png。Object完整路径中不能包含Bucket名称。
        //获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFileName = "";
        //生成一个新的不重复的文件名
        if(originalFilename.contains("_x") || originalFilename.contains("_global")){
            newFileName = UUID.randomUUID() + originalFilename;
        }
        else{
            newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = parentPath + "/" + dir + "/" + newFileName;

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }
        // 返回云服务器中存储文件的访问路径
        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

    // 根据完整URL删除文件
    public void deleteByUrl(String fileUrl) throws Exception {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // 从URL提取objectName
        String objectName = fileUrl.substring(fileUrl.indexOf("/", 8) + 1);

        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.deleteObject(bucketName, objectName);
        } finally {
            ossClient.shutdown();
        }
    }
    public void download(HttpServletResponse response) throws IOException {
        String fileUrl = aliyunOSSProperties.getFileUrl();
        log.info("开始下载文件：{}", fileUrl);

        // 参数校验
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 从环境变量获取凭证
        EnvironmentVariableCredentialsProvider credentialsProvider;
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            log.error("OSS凭证获取失败", e);
            throw new RuntimeException("服务器配置错误", e);
        }

        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        // 提取纯文件名（用于下载时显示）
        String displayName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        if (!displayName.toLowerCase().endsWith(".csv")) {
            displayName += ".csv";
        }

        // 创建OSSClient（try-with-resources 或手动关闭）
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 1. 设置响应头
            response.setContentType("text/csv;charset=UTF-8");
            // 处理中文文件名（RFC 5987 标准）
            String encodedFileName = URLEncoder.encode(displayName, "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + displayName + "\"; filename*=UTF-8''" + encodedFileName);

            // 2. 从OSS获取文件流（fileUrl 就是 OSS 中的 Object Key）
            OSSObject ossObject = ossClient.getObject(bucketName, fileUrl);

            // 3. 流式传输（try-with-resources 自动关闭）
            try (InputStream inputStream = ossObject.getObjectContent();
                 OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[8192];  // 8KB 缓冲区更高效
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                outputStream.flush();
                log.info("文件下载完成：{}，共 {} 字节", fileUrl, totalBytes);
            }

        } catch (OSSException e) {
            log.error("OSS文件下载失败：{}", fileUrl, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("文件不存在或下载失败");

        } catch (IOException e) {
            log.error("文件流传输失败：{}", fileUrl, e);
            throw e;

        } finally {
            // 4. 必须关闭 OSSClient
            ossClient.shutdown();
        }
    }

    // 这里先重复一下下载逻辑，因为最开始考虑得不是那么好
    public void downloadEdgeList(HttpServletResponse response, String fileUrl) throws IOException {
        // String fileUrl = aliyunOSSProperties.getFileUrl();
        log.info("开始下载文件：{}", fileUrl);

        // 参数校验
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 从环境变量获取凭证
        EnvironmentVariableCredentialsProvider credentialsProvider;
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            log.error("OSS凭证获取失败", e);
            throw new RuntimeException("服务器配置错误", e);
        }

        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        // 提取纯文件名（用于下载时显示）
        String displayName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        if (!displayName.toLowerCase().endsWith(".csv")) {
            displayName += ".csv";
        }

        // 创建OSSClient（try-with-resources 或手动关闭）
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 1. 设置响应头
            response.setContentType("text/csv;charset=UTF-8");
            // 处理中文文件名（RFC 5987 标准）
            String encodedFileName = URLEncoder.encode(displayName, "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + displayName + "\"; filename*=UTF-8''" + encodedFileName);

            // 2. 从OSS获取文件流（fileUrl 就是 OSS 中的 Object Key）
            OSSObject ossObject = ossClient.getObject(bucketName, fileUrl);

            // 3. 流式传输（try-with-resources 自动关闭）
            try (InputStream inputStream = ossObject.getObjectContent();
                 OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[8192];  // 8KB 缓冲区更高效
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                outputStream.flush();
                log.info("文件下载完成：{}，共 {} 字节", fileUrl, totalBytes);
            }

        } catch (OSSException e) {
            log.error("OSS文件下载失败：{}", fileUrl, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("文件不存在或下载失败");

        } catch (IOException e) {
            log.error("文件流传输失败：{}", fileUrl, e);
            throw e;

        } finally {
            // 4. 必须关闭 OSSClient
            ossClient.shutdown();
        }
    }


    /**
     * 从 OSS CSV 文件读取边数据（跳过表头）
     *
     * @param fileUrl OSS 文件完整 URL
     * @return EdgeDTO 列表
     */
    public List<NetworkResponse.EdgeDTO> readEdgesFromCsv(String fileUrl) {

        log.info("开始下载文件：{}", fileUrl);

        // 参数校验
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 从环境变量获取凭证
        EnvironmentVariableCredentialsProvider credentialsProvider;
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        } catch (com.aliyuncs.exceptions.ClientException e) {
            log.error("OSS凭证获取失败", e);
            throw new RuntimeException("服务器配置错误", e);
        }

        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        // 创建OSSClient（try-with-resources 或手动关闭）
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        // 提取 ObjectKey
        int index = fileUrl.indexOf('/', 8); // 从第8位开始找（跳过 https://）

        // 提取 key（第三个 / 之后的内容）
        String objectKey = fileUrl.substring(index + 1);
        List<NetworkResponse.EdgeDTO> edges = new ArrayList<>();

        // 从 OSS 下载文件流
        OSSObject ossObject = ossClient.getObject(bucketName, objectKey);

        try (InputStream inputStream = ossObject.getObjectContent();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] line;
            boolean isFirstLine = true;

            while ((line = csvReader.readNext()) != null) {
                // 跳过第一行（表头）
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // 解析每一行数据
                NetworkResponse.EdgeDTO edge = parseEdge(line);
                if (edge != null) {
                    edges.add(edge);
                }
            }

        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("读取 CSV 文件失败: " + fileUrl, e);
        }finally {
            // 必须关闭 OSSClient！
            ossClient.shutdown();
        }

        return edges;
    }

    /**
     * 解析 CSV 行数据为 EdgeDTO
     */
    private NetworkResponse.EdgeDTO parseEdge(String[] line) {
        // 数据校验
        if (line.length < 3) {
            System.err.println("跳过无效行，列数不足: " + String.join(",", line));
            return null;
        }

        try {
            NetworkResponse.EdgeDTO edge = new NetworkResponse.EdgeDTO();

            // 解析 source（第1列）
            edge.setSource(Integer.parseInt(line[0].trim()));

            // 解析 target（第2列）
            edge.setTarget(Integer.parseInt(line[1].trim()));

            // 解析 type(第3列)
            edge.setType(line[2].trim());

            // 解析 timestamp(第4列)
            edge.setTimestamp(line[3].trim());

            // 解析 timestamp_ms（第5列）
            edge.setTimestampMs(Long.parseLong(line[4].trim()));

            return edge;

        } catch (NumberFormatException e) {
            System.err.println("解析数字失败: " + String.join(",", line) + ", 错误: " + e.getMessage());
            return null;
        }
    }
}
