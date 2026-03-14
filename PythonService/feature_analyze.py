import time
import torch
import torch.nn as nn
import os
from config import Config
from oss_utils import OSSClient
num_seed = 1234
torch.manual_seed(num_seed)

oss_client = OSSClient()

class FeatureSelector(nn.Module):
    def __init__(self, input_dim, output_dim):
        super(FeatureSelector, self).__init__()
        #   定义全连接层，将输入特征维度映射到输出特征维度
        self.fc = nn.Linear(input_dim, output_dim, bias=False)
        # 初始化权重
        nn.init.xavier_uniform_(self.fc.weight)


    def forward(self, X):
        X_selected = self.fc(X)
        return X_selected


def feature_analyzer(feature_selector_url, only_local):
    local_feature_selector_url = oss_client.download_file(feature_selector_url, Config.TEMP_DIR)
    features_name = []
    input_dim = 47
    output_dim = 10
    weights_mean = torch.empty(47)
    weights_sum = torch.empty(47)
    if only_local==0:
        features_name = ["mean_inD", "mean_outD", "mean_inEXTD", "mean_outEXTD", "mean_inACCD",
                        "mean_outACCD", "mean_inNM", "mean_outNM", "mean_inCE", "mean_outCE",
                        "mean_inDE", "mean_outDE", "mean_inLCC", "mean_outLCC", "mean_inCOREDC",
                        "mean_outCOREDC", "mean_inCOREDJ", "mean_outCOREDJ", "mean_inCOREDP", "mean_outCOREDP",
                        "mean_inCOREDPA", "mean_outCOREDPA", "std_inD", "std_outD", "std_inEXTD",
                        "std_outEXTD", "std_inACCD", "std_outACCD", "std_inNM", "std_outNM",
                        "std_inCE", "std_outCE", "std_inDE", "std_outDE", "std_inLCC",
                        "std_outLCC", "std_inCOREDC", "std_outCOREDC", "std_inCOREDJ", "std_outCOREDJ",
                        "std_inCOREDP", "std_outCOREDP", "std_inCOREDPA", "std_outCOREDPA"]
        input_dim = 44
        weights_mean = torch.empty(44)
        weights_sum = torch.empty(44)
    else:
        features_name = ["mean_inD", "mean_outD", "mean_inEXTD", "mean_outEXTD", "mean_inACCD",
                        "mean_outACCD", "mean_inNM", "mean_outNM", "mean_inCE", "mean_outCE",
                        "mean_inDE", "mean_outDE", "mean_inLCC", "mean_outLCC", "mean_inCOREDC",
                        "mean_outCOREDC", "mean_inCOREDJ", "mean_outCOREDJ", "mean_inCOREDP", "mean_outCOREDP",
                        "mean_inCOREDPA", "mean_outCOREDPA", "std_inD", "std_outD", "std_inEXTD",
                        "std_outEXTD", "std_inACCD", "std_outACCD", "std_inNM", "std_outNM",
                        "std_inCE", "std_outCE", "std_inDE", "std_outDE", "std_inLCC",
                        "std_outLCC", "std_inCOREDC", "std_outCOREDC", "std_inCOREDJ", "std_outCOREDJ",
                        "std_inCOREDP", "std_outCOREDP", "std_inCOREDPA", "std_outCOREDPA", "density", "diameter", "average_path_length"]
    try:
        feature_selector = FeatureSelector(input_dim=input_dim, output_dim=output_dim)
        feature_selector.load_state_dict(torch.load(local_feature_selector_url))
        print("权重矩阵 {weight_matrix}:")
        data = feature_selector.fc.weight
        weight_matrix = data.detach().numpy()
        weight_matrix = torch.from_numpy(weight_matrix)

        for i in range(weight_matrix.size(1)):
            avg_abs = torch.mean(torch.abs(weight_matrix[:, i]))  # 关键改动
            weights_mean[i] = avg_abs

        for i in range(weight_matrix.size(1)):
            weights_sum[i] = torch.sum(weight_matrix[:, i] ** 2) / 10

        return {
            "weightsMean": weights_mean.numpy().tolist(),  # numpy数组 → 列表
            "weightsSum": weights_sum.numpy().tolist(),
            "featureNames": features_name
        }
    finally:
        # 定义所有需要删除的本地文件
        files_to_clean = [local_feature_selector_url]
        # 批量删除文件（兼容文件不存在的情况）
        for f in files_to_clean:
            if f and os.path.exists(f):  # 增加f非空判断，避免None报错
                try:
                    os.remove(f)
                    print(f"已删除临时文件：{f}")
                except Exception as e:
                    print(f"删除文件失败 {f}：{e}")





