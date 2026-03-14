import torch
import pandas as pd
import numpy as np
import torch.nn as nn
import matplotlib

matplotlib.use('Agg')  # 无GUI环境必需
import matplotlib.pyplot as plt
import io
import os
from config import Config
from oss_utils import OSSClient

# 设置随机种子
num_seed = 1234
torch.manual_seed(num_seed)

# ========== 全局变量（Flask启动时加载） ==========
y_max = 0
oss_client = OSSClient()

def get_device():
    """自动检测最佳可用设备"""
    if torch.cuda.is_available():
        device = torch.device('cuda')
        print(f"[Device] 使用 GPU: {torch.cuda.get_device_name(0)}")
    else:
        device = torch.device('cpu')
        print(f"[Device] 使用 CPU")
    return device

# 全局设备（启动时检测）
DEVICE = get_device()
# ========== 模型定义（保持原样） ==========
class FeatureSelector(nn.Module):
    def __init__(self, input_dim, output_dim):
        super(FeatureSelector, self).__init__()
        self.fc = nn.Linear(input_dim, output_dim, bias=False)
        nn.init.xavier_uniform_(self.fc.weight)

    def forward(self, X):
        X_selected = self.fc(X)
        return X_selected

def Mse_loss1(X, Y):
    # 我们先将Y扩展至与X同维度
    Y = Y.expand(-1, -1, X.size(2))
    # 然后使用切片的方式进行张量的运算
    diff1 = Y[:, 1:] - X[:, 1:]
    diff2 = Y[:, 0:-1] - X[:, 0:-1]
    diff = diff1 - diff2
    # 计算平方差
    squared_diff = diff ** 2
    # 对平方差进行求和
    total_loss = squared_diff.sum()
    loss = total_loss / (X.size(0) * (X.size(1) - 1) * X.size(2))
    return loss

# ========== 改造：split_windows 支持OSS路径 ==========
def split_windows(size, file_x_url, file_add_url, file_global_url):
    """
    划分时间片（改造版：从OSS下载数据）
    :param size: 窗口大小
    :param file_x_url: OSS上的微观特征文件URL
    :param file_add_url: OSS上的中观特征文件URL
    :param file_global_url: OSS上的全局特征文件URL
    :return: X, Y, y_max_value
    """
    global y_max

    # 下载数据文件到本地临时目录
    local_x = oss_client.download_file(file_x_url, Config.TEMP_DIR)
    local_add = None
    if file_add_url is not None:
        local_add = oss_client.download_file(file_add_url, Config.TEMP_DIR)
    local_global = oss_client.download_file(file_global_url, Config.TEMP_DIR)

    try:
        # 特征列定义（保持原样）
        # head_x = ['mean_inDegree', 'std_inDegree', 'mean_outDegree', 'std_outDegree',
        #           'mean_inEgodensity', 'std_inEgodensity', 'mean_outEgodensity', 'std_outEgodensity',
        #           'mean_inSPA', 'std_inSPA', 'mean_outSPA', 'std_outSPA',
        #           'mean_inExtdegree', 'std_inExtdegree', 'mean_outExtdegree', 'std_outExtdegree',
        #           'mean_inAccdegree', 'std_inAccdegree', 'mean_outAccdegree', 'std_outAccdegree',
        #           'mean_inNodemass', 'std_inNodemass', 'mean_outNodemass', 'std_outNodemass',
        #           'mean_inCoredjaccard1', 'std_inCoredjaccard1', 'mean_outCoredjaccard1', 'std_outCoredjaccard1',
        #           'mean_inCoredcosine1', 'std_inCoredcosine1', 'mean_outCoredcosine1', 'std_outCoredcosine1',
        #           'mean_inCoredpearson1', 'std_inCoredpearson1', 'mean_outCoredpearson1', 'std_outCoredpearson1',
        #           'mean_inLCC', 'std_inLCC', 'mean_outLCC', 'std_outLCC',
        #           'mean_inConductance', 'std_inConductance', 'mean_outConductance', 'std_outConductance']

        head_x = ["mean_inD", "mean_outD", "mean_inEXTD", "mean_outEXTD", "mean_inACCD",
                        "mean_outACCD", "mean_inNM", "mean_outNM", "mean_inCE", "mean_outCE",
                        "mean_inDE", "mean_outDE", "mean_inLCC", "mean_outLCC", "mean_inCOREDC",
                        "mean_outCOREDC", "mean_inCOREDJ", "mean_outCOREDJ", "mean_inCOREDP", "mean_outCOREDP",
                        "mean_inCOREDPA", "mean_outCOREDPA", "std_inD", "std_outD", "std_inEXTD",
                        "std_outEXTD", "std_inACCD", "std_outACCD", "std_inNM", "std_outNM",
                        "std_inCE", "std_outCE", "std_inDE", "std_outDE", "std_inLCC",
                        "std_outLCC", "std_inCOREDC", "std_outCOREDC", "std_inCOREDJ", "std_outCOREDJ",
                        "std_inCOREDP", "std_outCOREDP", "std_inCOREDPA", "std_outCOREDPA"]

        head_add = ['maintain', 'maintain_value', 'dissolve', 'dissolve_value',
                    'merge', 'merge_value', 'split', 'split_value', 'grow',
                    'grow_value', 'shrink', 'shrink_value']

        # head_global = ['diameter_difference', 'average_path_length_difference', 'density_difference']
        #
        head_global = ["density", "diameter", "average_path_length"]

        # 读取数据（保持原样，改为本地路径）
        df_x = pd.read_csv(local_x, usecols=head_x)
        df_add = None
        if local_add is not None:
            df_add = pd.read_csv(local_add, usecols=head_add)
        # df_add = pd.read_csv(local_add, usecols=head_add)
        df_add_global = pd.read_csv(local_global, usecols=head_global)
        df_y = pd.read_csv(local_global, usecols=['node_count'])  # 注意：y在global文件里

        # 计算y_max（全局变量）
        y_max = df_y.max().item()
        df_y_1 = df_y.div(y_max)

        df_data = None
        # 拼接特征
        if df_add is not None:
            df_data = pd.concat([df_x, df_add, df_add_global, df_y_1], axis=1)
        else:
            df_data = pd.concat([df_x, df_add_global, df_y_1], axis=1)
        all_data = df_data.values

        # 滑动窗口
        X, Y1 = [], []
        for i in range(len(all_data) - size - 1):
            X.append(all_data[i:i + size, :])
            if df_add is not None:
                Y1.append(all_data[i:i + size, [len(head_x) + len(head_add) + len(head_global)]])
            else:
                Y1.append(all_data[i:i + size, [len(head_x)  + len(head_global)]])
        return np.array(X), np.array(Y1), y_max

    finally:
        # 清理临时数据文件
        if local_add is not None:
            for f in [local_x, local_add, local_global]:
                if os.path.exists(f):
                    os.remove(f)
        else:
            for f in [local_x, local_global]:
                if os.path.exists(f):
                    os.remove(f)


# ========== 改造：主预测函数，支持OSS路径 ==========
def pretrain_featureSelector(file_x_url, file_add_url, file_global_url, num_epochs, size=7):
    local_pretrain_path = 'data/pre_train_feature_selector.pth'
    try:
        global y_max, DEVICE

        # 1. 加载数据（从OSS下载）
        all_X, all_Y, current_y_max = split_windows(size, file_x_url, file_add_url, file_global_url)
        y_max = current_y_max  # 更新全局y_max

        all_X = torch.from_numpy(all_X).float().to(DEVICE)
        all_Y = torch.from_numpy(all_Y).float().to(DEVICE)
        learning_rate = 0.001
        feature_selector = FeatureSelector(all_X.size(2) - 1, output_dim=10)
        feature_selector.to(DEVICE)  # 模型移到对应设备
        optimizer = torch.optim.Adam(feature_selector.parameters(), lr=learning_rate)
        best_loss = float('inf')
        for epoch in range(num_epochs):
            # 将模型设置为训练模式 开始进行训练
            feature_selector.train()
            # 梯度清零 在每一次训练时将梯度清0
            optimizer.zero_grad()
            # 定义一个特征选择列表 用于接收模型的输出
            selected_features = []
            # 循环遍历训练数据
            for i in range(all_X.size(0)):
                X_sample = all_X[i, :, :-1]
                X_last = all_X[i, :, -1].unsqueeze(1)
                X_selected_i = feature_selector(X_sample)
                X_selected = torch.cat((X_selected_i, X_last), dim=1)
                selected_features.append(X_selected)
            # 将特征选择结果拼接起来
            selected_features = torch.stack(selected_features)
            # 使用 train_Y1 作为目标，进行训练
            loss = Mse_loss1(selected_features, all_Y)
            loss.backward()
            optimizer.step()
            if loss < best_loss:
                best_loss = loss
                # TODO 这里还要考虑线程多并发的情况
                torch.save(feature_selector.state_dict(), local_pretrain_path)
            if (epoch + 1) % 10 == 0:
                print(f'Epoch [{epoch + 1}/{num_epochs}], Loss: {loss.item():.5f}')

        feature_selector_url = oss_client.upload_file(local_pretrain_path, Config.OSS_MODEL_DIR)
        return feature_selector_url
    finally:
        for f in [local_pretrain_path]:
            if os.path.exists(f):
                os.remove(f)