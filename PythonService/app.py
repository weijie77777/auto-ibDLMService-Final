# app.py
from flask import Flask, request, jsonify
from config import Config
from predictor import predict_from_oss
from pretrainer import pretrain_featureSelector
from trainer import train_model
from predictor1 import finalpredict_from_oss
from feature_analyze import feature_analyzer
import traceback
# 初始化配置
Config.init_dirs()

app = Flask(__name__)


@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'healthy',
        'service': 'community-evolution-predictor'
    })


@app.route('/predict', methods=['POST'])
def predict():
    """
    预测接口

    Request Body:
    {
        "file_x_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_x.csv",
        "file_add_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_add.csv",
        "file_global_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_global.csv",
        "threshold": 1000,
        "window_size": 7
    }

    Response:
    {
        "code": 200,
        "message": "success",
        "data": {
            "loss": 0.12345,
            'event_num': 2,
            'event_predict_num': 2,
            "recall": 0.85,
            "precision": 0.90,
            "accuracy": 0.88,
            "image_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/results/xxx_result.png"
        }
    }
    """
    try:
        # 获取JSON参数
        data = request.get_json()
        if not data:
            return jsonify({'code': 400, 'message': '请求体不能为空'}), 400

        # 提取参数
        file_x_url = data.get('file_x_url')
        file_add_url = data.get('file_add_url')
        file_global_url = data.get('file_global_url')
        threshold = data.get('threshold', 1000)
        window_size = data.get('window_size', 7)

        # 参数校验
        if not all([file_x_url, file_add_url, file_global_url]):
            return jsonify({
                'code': 400,
                'message': '缺少必要参数：file_x_url, file_add_url, file_global_url'
            }), 400

        # 执行预测
        result = predict_from_oss(
            file_x_url=file_x_url,
            file_add_url=file_add_url,
            file_global_url=file_global_url,
            threhold=threshold,
            size=window_size
        )

        return jsonify({
            'code': 200,
            'message': 'success',
            'data': result
        })

    except Exception as e:
        # 详细错误日志
        error_trace = traceback.format_exc()
        print(f"错误详情:\n{error_trace}")

        return jsonify({
            'code': 500,
            'message': f'预测失败: {str(e)}'
        }), 500
@app.route('/pretrain', methods=['POST'])
def pretrain():
    """
    预测接口

    Request Body:
    {
        "file_x_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_x.csv",
        "file_add_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_add.csv",
        "file_global_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_global.csv",
        "window_size": 7,
        "num_epochs": 1000
    }
    """
    try:
        # 获取JSON参数
        data = request.get_json()
        if not data:
            return jsonify({'code': 400, 'message': '请求体不能为空'}), 400

        # 提取参数
        file_x_url = data.get('file_x_url')
        file_add_url = data.get('file_add_url')
        file_global_url = data.get('file_global_url')
        epochs = data.get('epochs', 1000)
        window_size = data.get('window_size', 7)
        only_local = data.get('only_local', 1)

        # 参数校验
        if not all([file_x_url,  file_global_url]):
            return jsonify({
                'code': 400,
                'message': '缺少必要参数：file_x_url, file_global_url'
            }), 400

        # 执行预测
        feature_selector_url=pretrain_featureSelector(
            file_x_url=file_x_url,
            file_add_url=file_add_url,
            file_global_url=file_global_url,
            size=window_size,
            num_epochs=epochs,
            only_local=only_local
        )

        return jsonify({
            'code': 200,
            'message': 'success',
            'data': feature_selector_url
        })

    except Exception as e:
        # 详细错误日志
        error_trace = traceback.format_exc()
        print(f"错误详情:\n{error_trace}")

        return jsonify({
            'code': 500,
            'message': f'预训练失败: {str(e)}'
        }), 500

@app.route('/train', methods=['POST'])
def train():
    """
    预测接口

    Request Body:
    {
        "file_x_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_x.csv",
        "file_add_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_add.csv",
        "file_global_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_global.csv",
        "feature_selector_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/models/06551f488a174cf5b9a7853baa7ec0ae_pre_train_feature_selector_attention_W4.pth"
        "window_size": 7,
        "num_epochs": 1000
    }
    """
    try:
        # 获取JSON参数
        data = request.get_json()
        if not data:
            return jsonify({'code': 400, 'message': '请求体不能为空'}), 400

        # 提取参数
        file_x_url = data.get('file_x_url')
        file_add_url = data.get('file_add_url')
        file_global_url = data.get('file_global_url')
        feature_selector_url = data.get('feature_selector_url')
        epochs = data.get('epochs', 1000)
        window_size = data.get('window_size', 7)
        only_local = data.get('only_local', 1)

        # 参数校验
        if not all([file_x_url, feature_selector_url,  file_global_url]):
            return jsonify({
                'code': 400,
                'message': '缺少必要参数：file_x_url, feature_selector_url, file_global_url'
            }), 400

        # 执行预测
        all_model_url=train_model(
            file_x_url=file_x_url,
            file_add_url=file_add_url,
            file_global_url=file_global_url,
            feature_selector_url=feature_selector_url,
            size=window_size,
            num_epochs=epochs,
            only_local=only_local
        )

        return jsonify({
            'code': 200,
            'message': 'success',
            'data': all_model_url
        })

    except Exception as e:
        # 详细错误日志
        error_trace = traceback.format_exc()
        print(f"错误详情:\n{error_trace}")

        return jsonify({
            'code': 500,
            'message': f'训练失败: {str(e)}'
        }), 500

@app.route('/finalPredict', methods=['POST'])
def finalpredict():
    """
    预测接口

    Request Body:
    {
        "file_x_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_x.csv",
        "file_add_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_add.csv",
        "file_global_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/data/17_global.csv",
        "feature_selector_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/models/c132781945d140f7ae80c12958a55592_train_feature_selector.pth",
        "model_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/models/cb5f2bb413ca46538475d3bbb5d4b9c2_train_model.path"
        "threshold": 1000,
        "window_size": 7,
        "timestep": 8
    }

    Response:
    {
        "code": 200,
        "message": "success",
        "data": {
            "loss": 0.12345,
            'event_num': 2,
            'event_predict_num': 2,
            "recall": 0.85,
            "precision": 0.90,
            "accuracy": 0.88,
            "image_url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/results/xxx_result.png"
        }
    }
    """
    try:
        # 获取JSON参数
        data = request.get_json()
        if not data:
            return jsonify({'code': 400, 'message': '请求体不能为空'}), 400

        # 提取参数
        file_x_url = data.get('file_x_url')
        file_add_url = data.get('file_add_url')
        file_global_url = data.get('file_global_url')
        feature_selector_url = data.get('feature_selector_url')
        model_url = data.get('model_url')
        threshold = data.get('threshold', 1000)
        window_size = data.get('window_size', 7)
        timestep = data.get('timestep')
        only_local =data.get('only_local', 1)
        # 参数校验
        if not all([file_x_url, feature_selector_url, model_url, file_global_url]):
            return jsonify({
                'code': 400,
                'message': '缺少必要参数：file_x_url, feature_selector_url, model_url, file_global_url'
            }), 400

        # 执行预测
        result = finalpredict_from_oss(
            file_x_url=file_x_url,
            file_add_url=file_add_url,
            file_global_url=file_global_url,
            feature_selector_url = feature_selector_url,
            model_url = model_url,
            threhold=threshold,
            timestep=timestep,
            size=window_size,
            only_local=only_local
        )

        return jsonify({
            'code': 200,
            'message': 'success',
            'data': result
        })

    except Exception as e:
        # 详细错误日志
        error_trace = traceback.format_exc()
        print(f"错误详情:\n{error_trace}")

        return jsonify({
            'code': 500,
            'message': f'预测失败: {str(e)}'
        }), 500


@app.route('/featureAnalyze', methods=['POST'])
def featureAnalyze():
    """
    预测接口

    Request Body:
    {,
        "feature_selector_url": "https://java-ai-wei123.oss-cn-chengdu.aliyuncs.com/models/c132781945d140f7ae80c12958a55592_train_feature_selector.pth",
        "only_local": 1
    }

    Response:
    {
        "code": 200,
        "message": "success",
        "data": {
            "weightsMean": [],  # numpy数组 → 列表
            "weightsSum": []
        }
    }
    """
    try:
        # 获取JSON参数
        data = request.get_json()
        if not data:
            return jsonify({'code': 400, 'message': '请求体不能为空'}), 400

        # 提取参数
        feature_selector_url = data.get('feature_selector_url')
        only_local = data.get('only_local', 1)
        # 参数校验
        if not all([feature_selector_url]):
            return jsonify({
                'code': 400,
                'message': '缺少必要参数：feature_selector_url'
            }), 400

        # 执行预测
        result = feature_analyzer(
            feature_selector_url = feature_selector_url,
            only_local = only_local
        )

        return jsonify({
            'code': 200,
            'message': 'success',
            'data': result
        })

    except Exception as e:
        # 详细错误日志
        error_trace = traceback.format_exc()
        print(f"错误详情:\n{error_trace}")

        return jsonify({
            'code': 500,
            'message': f'特征分析失败: {str(e)}'
        }), 500


if __name__ == '__main__':
    # 端口设置为0.0.0.0 能够监听本机 局域网 公网
    app.run(host='0.0.0.0', port=5000, debug=False)