import os

# 获取当前文件所在目录（项目根目录）
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
class Config:
    # OSS配置（从环境变量读取，安全）
    OSS_ACCESS_KEY_ID = os.environ.get('OSS_ACCESS_KEY_ID', '')
    OSS_ACCESS_KEY_SECRET = os.environ.get('OSS_ACCESS_KEY_SECRET', '')
    OSS_ENDPOINT = 'https://oss-cn-chengdu.aliyuncs.com'
    OSS_BUCKET_NAME = 'java-ai-wei123'

    # OSS路径配置
    OSS_MODEL_DIR = 'models/'  # 模型存放目录
    OSS_RESULT_DIR = 'results/'  # 结果图存放目录

    # 本地临时目录
    TEMP_DIR = os.path.join(BASE_DIR, '/tmp/flask_predictor')
    MODEL_DIR = os.path.join(TEMP_DIR, 'models')

    # 模型文件名
    FEATURE_SELECTOR_MODEL = 'feature_selector_attention_Wlinear1.pth'
    GRU_MODEL = 'GRU_model_attention_Wlinear1.pth'

    @classmethod
    def init_dirs(cls):
        """初始化目录"""
        import os
        os.makedirs(cls.TEMP_DIR, exist_ok=True)
        os.makedirs(cls.MODEL_DIR, exist_ok=True)