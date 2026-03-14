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


class GRU(nn.Module):
    def __init__(self, input_size, hidden_size, output_size, num_layers):
        super(GRU, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers
        self.gru = nn.GRU(input_size, hidden_size, num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size, output_size)

    def forward(self, x):
        h0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size)
        out, _ = self.gru(x, h0.detach())
        out = self.fc(out[:, -1, :])
        return out


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
    local_add = oss_client.download_file(file_add_url, Config.TEMP_DIR)
    local_global = oss_client.download_file(file_global_url, Config.TEMP_DIR)

    try:
        # 特征列定义（保持原样）
        head_x = ['mean_inDegree', 'std_inDegree', 'mean_outDegree', 'std_outDegree',
                  'mean_inEgodensity', 'std_inEgodensity', 'mean_outEgodensity', 'std_outEgodensity',
                  'mean_inSPA', 'std_inSPA', 'mean_outSPA', 'std_outSPA',
                  'mean_inExtdegree', 'std_inExtdegree', 'mean_outExtdegree', 'std_outExtdegree',
                  'mean_inAccdegree', 'std_inAccdegree', 'mean_outAccdegree', 'std_outAccdegree',
                  'mean_inNodemass', 'std_inNodemass', 'mean_outNodemass', 'std_outNodemass',
                  'mean_inCoredjaccard1', 'std_inCoredjaccard1', 'mean_outCoredjaccard1', 'std_outCoredjaccard1',
                  'mean_inCoredcosine1', 'std_inCoredcosine1', 'mean_outCoredcosine1', 'std_outCoredcosine1',
                  'mean_inCoredpearson1', 'std_inCoredpearson1', 'mean_outCoredpearson1', 'std_outCoredpearson1',
                  'mean_inLCC', 'std_inLCC', 'mean_outLCC', 'std_outLCC',
                  'mean_inConductance', 'std_inConductance', 'mean_outConductance', 'std_outConductance']

        head_add = ['maintain', 'maintain_value', 'dissolve', 'dissolve_value',
                    'merge', 'merge_value', 'split', 'split_value', 'grow',
                    'grow_value', 'shrink', 'shrink_value']

        head_global = ['diameter_difference', 'average_path_length_difference', 'density_difference']

        # 读取数据（保持原样，改为本地路径）
        df_x = pd.read_csv(local_x, usecols=head_x)
        df_add = pd.read_csv(local_add, usecols=head_add)
        df_add_global = pd.read_csv(local_global, usecols=head_global)
        df_y = pd.read_csv(local_global, usecols=['v_difference'])  # 注意：y在global文件里

        # 计算y_max（全局变量）
        y_max = df_y.max().item()
        df_y_1 = df_y.div(y_max)

        # 拼接特征
        df_data = pd.concat([df_x, df_add, df_add_global, df_y_1], axis=1)
        all_data = df_data.values

        # 滑动窗口
        X, Y = [], []
        for i in range(len(all_data) - size - 1):
            X.append(all_data[i:i + size, :])
            Y.append(all_data[i + size, [len(head_x) + len(head_add) + len(head_global)]])

        return np.array(X), np.array(Y), y_max

    finally:
        # 清理临时数据文件
        for f in [local_x, local_add, local_global]:
            if os.path.exists(f):
                os.remove(f)


# ========== 保持原样：result_to_01 ==========
def result_to_01(test_list, pre_list, n):
    correct_0 = wrong_0 = correct_1 = wrong_1 = 0

    for test, pre in zip(test_list, pre_list):
        pre_flag = 1 if pre > n else 0
        test_flag = 1 if test > n else 0

        if pre_flag == test_flag:
            if test_flag == 1:
                correct_1 += 1
            else:
                correct_0 += 1
        else:
            if test_flag == 1:
                wrong_1 += 1
            else:
                wrong_0 += 1
    event_num = correct_1 + wrong_1
    event_predict_num = correct_1 + wrong_0
    recall = correct_1 / (correct_1 + wrong_1) if (correct_1 + wrong_1) != 0 else -1
    precision = correct_1 / (correct_1 + wrong_0) if (correct_1 + wrong_0) != 0 else -1
    accuracy = (correct_1 + correct_0) / (correct_1 + correct_0 + wrong_1 + wrong_0) if (
               correct_1 + correct_0 + wrong_1 + wrong_0) != 0 else 0

    return event_num, event_predict_num, recall, precision, accuracy


# ========== 改造：test_model 返回图片URL而非保存本地 ==========
def test_model(pre_Y, test_label, threhold, global_y_max):
    """
    生成图表并上传OSS
    :return: recall, precision, accuracy, image_url
    """
    # 绘图（保持原样）
    plt.figure(figsize=(10, 6))
    plt.plot(test_label, label='Ground truth')
    plt.plot(pre_Y, label='Prediction')
    plt.ylim(-270, global_y_max + 500)
    plt.title('auto-ibDLM')
    plt.ylabel('yt')
    plt.legend()

    # 保存到临时文件
    temp_image = os.path.join(Config.TEMP_DIR, f'result_{os.urandom(4).hex()}.png')
    plt.savefig(temp_image, dpi=150, bbox_inches='tight')
    plt.close()

    try:
        # 上传OSS
        image_url = oss_client.upload_file(temp_image)

        # 计算指标
        event_num, event_predict_num, recall, precision, accuracy = result_to_01(test_label, pre_Y, threhold)

        return event_num, event_predict_num, recall, precision, accuracy, image_url

    finally:
        # 清理临时图片
        if os.path.exists(temp_image):
            os.remove(temp_image)


# ========== 改造：主预测函数，支持OSS路径 ==========
def predict_from_oss(file_x_url, file_add_url, file_global_url, threhold, size=7):
    """
    主预测函数（Flask调用入口）
    :param file_x_url: OSS上的微观特征文件URL
    :param file_add_url: OSS上的中观特征文件URL
    :param file_global_url: OSS上的全局特征文件URL
    :param threhold: 阈值
    :param size: 窗口大小
    :return: loss, recall, precision, accuracy, image_url
    """
    global y_max, DEVICE

    # 1. 加载数据（从OSS下载）
    all_X, all_Y, current_y_max = split_windows(size, file_x_url, file_add_url, file_global_url)
    y_max = current_y_max  # 更新全局y_max

    all_X = torch.from_numpy(all_X).float().to(DEVICE)
    all_Y = torch.from_numpy(all_Y).float().to(DEVICE)

    # 2. 加载模型（从OSS下载到本地，首次下载后缓存 以后调用不用再重复下载）
    feature_selector = FeatureSelector(input_dim=59, output_dim=10)
    fs_path = oss_client.download_model(Config.FEATURE_SELECTOR_MODEL, Config.MODEL_DIR)
    # feature_selector.load_state_dict(torch.load(fs_path, map_location='cpu'))
    # 优化后（自动选择）
    feature_selector.load_state_dict(torch.load(fs_path, map_location=DEVICE))
    feature_selector.to(DEVICE)  # 模型移到对应设备
    model = GRU(input_size=11, hidden_size=64, output_size=1, num_layers=8)
    gru_path = oss_client.download_model(Config.GRU_MODEL, Config.MODEL_DIR)
    model.load_state_dict(torch.load(gru_path, map_location=DEVICE))
    model.to(DEVICE)
    print(f"model loaded_path: {Config.MODEL_DIR}")
    criterion = nn.MSELoss()

    # 3. 预测（保持原样）
    with torch.no_grad():
        selected_features_test = []
        for i in range(all_X.size(0)):
            X_sample = all_X[i, :, :59]
            X_last = all_X[i, :, -1].unsqueeze(1)
            X_selected_i = feature_selector(X_sample)
            X_selected = torch.cat((X_selected_i, X_last), dim=1)
            selected_features_test.append(X_selected)

        selected_features_test = torch.stack(selected_features_test)
        preds = model(selected_features_test)
        loss = criterion(preds, all_Y)
        preds = preds.cpu().numpy()
        preds = preds * y_max

    # 4. 处理结果
    test_labels = all_Y * y_max
    y_pred = preds.flatten().tolist()
    test_Y = test_labels.flatten().tolist()

    # 5. 生成图表并上传OSS
    event_num, event_predict_num, recall, precision, accuracy, image_url = test_model(y_pred, test_Y, threhold, y_max)

    return {
        'loss': round(loss.item(), 5),
        'event_num': event_num,
        'event_predict_num': event_predict_num,
        'recall': recall,
        'precision': precision,
        'accuracy': accuracy,
        'image_url': image_url
    }
