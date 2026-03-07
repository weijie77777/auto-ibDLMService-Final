import oss2
import os
import uuid
from urllib.parse import urlparse
from config import Config


class OSSClient:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._init_client()
        return cls._instance

    def _init_client(self):
        """初始化OSS客户端"""
        auth = oss2.Auth(Config.OSS_ACCESS_KEY_ID, Config.OSS_ACCESS_KEY_SECRET)
        self.bucket = oss2.Bucket(auth, Config.OSS_ENDPOINT, Config.OSS_BUCKET_NAME)

    def download_file(self, oss_url, local_dir):
        """
        从OSS下载文件
        :param oss_url: OSS完整URL或Object Key
        :param local_dir: 本地保存目录
        :return: 本地文件路径
        """
        # 解析URL获取Object Key
        if oss_url.startswith('http'):
            parsed = urlparse(oss_url)
            # 路径格式: /bucket-name/object-key 或 /object-key
            # 去掉第一个下划线
            path = parsed.path.lstrip('/')
            if path.startswith(Config.OSS_BUCKET_NAME):
                object_key = path[len(Config.OSS_BUCKET_NAME) + 1:]
            else:
                object_key = path
        else:
            object_key = oss_url

        # 生成本地文件名 使用uuid避免文件被覆盖
        file_name = f"{uuid.uuid4().hex}_{os.path.basename(object_key)}"
        local_path = os.path.join(local_dir, file_name)
        # 下载
        self.bucket.get_object_to_file(object_key, local_path)
        return local_path

    def upload_file(self, local_path, oss_dir=None):
        """
        上传文件到OSS
        :param local_path: 本地文件路径
        :param oss_dir: OSS目标目录，默认使用Config.OSS_RESULT_DIR
        :return: 文件访问URL
        """
        if oss_dir is None:
            oss_dir = Config.OSS_RESULT_DIR

        file_name = os.path.basename(local_path)
        object_key = f"{oss_dir}{uuid.uuid4().hex}_{file_name}"

        self.bucket.put_object_from_file(object_key, local_path)

        # 生成URL
        url = f"https://{Config.OSS_BUCKET_NAME}.{Config.OSS_ENDPOINT.replace('https://', '')}/{object_key}"
        return url

    def download_model(self, model_name, local_dir):
        """下载模型文件（如果不存在）"""
        local_path = os.path.join(local_dir, model_name)
        if os.path.exists(local_path):
            return local_path

        oss_key = f"{Config.OSS_MODEL_DIR}{model_name}"
        self.bucket.get_object_to_file(oss_key, local_path)
        return local_path