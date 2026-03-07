# test_predict.py
import requests
import json

url = "http://localhost:5000/predict"

payload = {
    "file_x_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/data/17_x.csv",
    "file_add_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/data/17_add.csv",
    "file_global_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/data/17_global.csv",
    "threshold": 1000,
    "window_size": 7
}

try:
    response = requests.post(url, json=payload, timeout=60)
    print(f"状态码: {response.status_code}")
    print(f"响应内容:\n{json.dumps(response.json(), indent=2, ensure_ascii=False)}")
except Exception as e:
    print(f"请求失败: {e}")