import torch
import glob
import pandas as pd
import numpy as np
import torch.nn as nn
import matplotlib.pyplot as plt

num_seed = 1234
torch.manual_seed(num_seed)
y_max = 0


def split_windows(size):
    """
    划分时间片
    :param size: 窗口大小，目前设置的7
    :param community_discovery: 选择哪个社区发现算法，传入‘lpa_grow’或者’cd_grow‘
    :return:
    """
    """
    file_path_x存放的是网络的微观特征
    file_path_y存放的是相邻时间片之间的节点增量的变化
    """
    file_paths_x = glob.glob('data/to_NN_all/all_data/17_x.csv')
    """
    根据不同的社区选择算法选择不同的社区中观特征
    """
    file_paths_add = glob.glob('data/to_NN/cd_no_normalize/17_add.csv')  # cd_no_normalize
    # 读取网络的全局特征文件
    file_paths_add_global = glob.glob('data/to_NN/globle_par_csv/17_global.csv')  # global
    # 特征集合X和标签集合Y
    X = []
    Y = []
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
    # 用于中观特征的选择 注意这里是根据已选择的社区发现算法所选定的特征列 如果要是社区发现算法改变了的话需要改变 这里
    head_add = ['maintain', 'maintain_value', 'dissolve', 'dissolve_value',
                'merge', 'merge_value', 'split', 'split_value', 'grow',
                'grow_value', 'shrink', 'shrink_value']
    # 用于全局特征的选择
    head_gloabl = ['diameter_difference',
                   'average_path_length_difference', 'density_difference']
    for file_x, file_add, file_add_global in zip(file_paths_x, file_paths_add,
                                                 file_paths_add_global):
        # 选择微观特征

        df_x = pd.read_csv(file_x, usecols=head_x)
        df_add = pd.read_csv(file_add, usecols=head_add)
        df_add_global = pd.read_csv(file_add_global, usecols=head_gloabl)
        df_y = pd.read_csv(file_add_global, usecols=['v_difference'])
        global y_max
        y_max = df_y.max().item()
        df_y_1 = df_y.div(y_max)
        # 做归一化的时候用所有数据都去除那个最大的数
        # 将所有的特征都拼接起来
        df_data = pd.concat([df_x, df_add], axis=1)
        df_data = pd.concat([df_data, df_add_global], axis=1)
        df_data = pd.concat([df_data, df_y_1], axis=1)  # 加全局的结果
        all_data = df_data.values  # 用x和y 共同做x

        # X作为数据，Y作为标签
        # 滑动窗口，步长为1，构造窗口化数据，i的取值可能是为了避免出现边界问题
        for i in range(len(all_data) - size - 1):
            X.append(all_data[i:i + size, :])
            Y.append(all_data[i + size, [len(head_x) + len(head_add) + len(head_gloabl)]])
    return np.array(X), np.array(Y)


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


# 定义GRU模型
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


# 获取不同基准线下的各项评价指标
def result_to_01(test_list, pre_list, n):
    new_test = []
    new_pre = []
    correct_0 = 0
    wrong_0 = 0
    correct_1 = 0
    wrong_1 = 0
    for test, pre in zip(test_list, pre_list):
        pre_flag = 0
        test_flag = 0
        if test > n:
            test_flag = 1
        if pre > n:
            pre_flag = 1
        if pre_flag == test_flag:
            if test_flag == 1:
                # 正确预测出是突发事件
                correct_1 += 1
            else:
                # 正确预测出不是突发事件
                correct_0 += 1
        else:
            if test_flag == 1:
                # 没有正确预测出是突发事件
                wrong_1 += 1
            else:
                # 没有预测出不是突发事件
                wrong_0 += 1
        new_test.append(test_flag)
        new_pre.append(pre_flag)
    correct = correct_1 + correct_0
    wrong = wrong_1 + wrong_0
    if correct_1 + wrong_1 ==0:
        recall = -1
    else:
        recall = correct_1 / (correct_1 + wrong_1)
    if correct_1 + wrong_0 == 0:
        precision = -1
    else:
        precision = correct_1 / (correct_1 + wrong_0)
    accuracy = correct / (correct + wrong)
    return recall, precision, accuracy


def test_model(pre_Y, test_label, threhold):
    pre_Y = pre_Y
    test_Y = test_label
    plt.plot(test_Y, label='Ground truth')
    plt.plot(pre_Y, label='Prediction')
    plt.ylim(-270, y_max + 500)
    plt.title('auto-ibDLM')
    # 加标准线
    plt.ylabel('yt')
    plt.legend()
    plt.savefig('data/test_node_attention_Wlinear1.png')
    plt.savefig('data/test_node_attention_Wlinear1.pdf')
    plt.show()
    plt.cla()
    # 算准确率
    return result_to_01(test_Y, pre_Y, threhold)


def community_evolution_prediction_based_emergcy_prediction_regression(threhold):
    # 准备数据
    all_X, all_Y = split_windows(size=7)
    all_X = torch.from_numpy(all_X).float()
    all_Y = torch.from_numpy(all_Y).float()

    feature_selector = FeatureSelector(input_dim=59, output_dim=10)
    feature_selector.load_state_dict(torch.load('data/feature_selector_attention_Wlinear1.pth'))
    model = GRU(input_size=11, hidden_size=64, output_size=1, num_layers=8)
    model.load_state_dict(torch.load('data/GRU_model_attention_Wlinear1.pth'))
    criterion = nn.MSELoss()

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

    test_labels = all_Y * y_max

    y_pred = preds.flatten()
    test_Y = test_labels.flatten().tolist()
    recall, precision, accuracy = test_model(y_pred, test_Y, threhold)
    return round(loss.item(), 5), recall, precision, accuracy


loss, recall, precision, accuracy = community_evolution_prediction_based_emergcy_prediction_regression(1000)

