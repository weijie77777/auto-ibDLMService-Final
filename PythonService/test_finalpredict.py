# test_predict.py
import requests
import json

url = "http://localhost:5000/finalPredict"

payload = {
    "file_x_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/data/17_x.csv",
    # "file_add_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/data/17_add.csv",
    "file_global_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/data/17_global.csv",
    "feature_selector_url": "c132781945d140f7ae80c12958a55592_train_feature_selector.pth",
    "model_url": "cb5f2bb413ca46538475d3bbb5d4b9c2_train_model.path",
    "threshold": 1000,
    "window_size": 7
}

try:
    response = requests.post(url, json=payload, timeout=60)
    print(f"状态码: {response.status_code}")
    print(f"响应内容:\n{json.dumps(response.json(), indent=2, ensure_ascii=False)}")
except Exception as e:
    print(f"请求失败: {e}")