import pandas as pd
import numpy as np


def clean_weibo_data(weibo_file, comment_file, sub_comment_file, repost_file):
    """
    清洗四张微博数据表，合并为统一交互表
    """
    results = []

    # ========== 1. 处理原始微博表 ==========
    print("处理原始微博表...")
    df_weibo = pd.read_csv(weibo_file)

    for _, row in df_weibo.iterrows():
        results.append({
            'interaction': 'original',
            'interaction_id': None,  # 原始微博无父级
            'weibo': row.get('weibo_text'),  # 根据实际列名调整
            'weibo_id': row.get('weibo_id'),
            'user_id': row.get('user_id'),
            'user_name': row.get('user_name'),
            'create_time': row.get('create_time')
        })

    # ========== 2. 处理评论表 ==========
    print("处理评论表...")
    df_comment = pd.read_csv(comment_file)

    for _, row in df_comment.iterrows():
        results.append({
            'interaction': 'comment',
            'interaction_id': row.get('weibo_id'),  # 关联原微博
            'weibo': row.get('comment_text'),
            'weibo_id': row.get('comment_id'),
            'user_id': row.get('user_id'),
            'user_name': row.get('user_name'),
            'create_time': row.get('create_time')
        })

    # ========== 3. 处理子评论表（评论的评论） ==========
    print("处理子评论表...")
    df_sub = pd.read_csv(sub_comment_file)

    for _, row in df_sub.iterrows():
        results.append({
            'interaction': 'comment',  # 子评论也是comment类型
            'interaction_id': row.get('comment_id'),  # 关联父评论
            'weibo': row.get('child_comment_text'),
            'weibo_id': row.get('child_comment_id'),
            'user_id': row.get('user_id'),
            'user_name': row.get('user_name'),
            'create_time': row.get('create_time')
        })

    # ========== 4. 处理转发表 ==========
    print("处理转发表...")
    df_repost = pd.read_csv(repost_file)

    for _, row in df_repost.iterrows():
        results.append({
            'interaction': 'repost',
            'interaction_id': row.get('weibo_id'),  # 关联原微博
            'weibo': row.get('repost_text'),  # 转发附言
            'weibo_id': row.get('repost_id'),
            'user_id': row.get('user_id'),
            'user_name': row.get('user_name'),
            'create_time': row.get('create_time')
        })

    # ========== 5. 合并并清洗 ==========
    print("合并数据...")
    df_result = pd.DataFrame(results)

    # 数据清洗
    df_result['interaction_id'] = df_result['interaction_id'].replace('', np.nan)
    df_result['weibo'] = df_result['weibo'].astype(str).str.strip()
    df_result = df_result.drop_duplicates(subset=['weibo_id'])

    # 时间格式统一
    df_result['create_time'] = pd.to_datetime(df_result['create_time'], errors='coerce')

    print(f"清洗完成：共 {len(df_result)} 条记录")
    print(f"类型分布：\n{df_result['interaction'].value_counts()}")

    return df_result


# ========== 使用示例 ==========
if __name__ == "__main__":
    # 根据实际文件路径修改
    df_cleaned = clean_weibo_data(
        weibo_file='./data/\'175_weibo_info\'.csv',
        comment_file='./data/\'175_comment_info\'.csv',
        sub_comment_file='./data/\'175_child_comment_info\'.csv',
        repost_file='./data/\'175_repost_info\'.csv'
    )

    # 保存结果
    df_cleaned.to_csv('./data/interaction.csv', index=False, encoding='utf-8')
    print("已保存到 interaction.csv")