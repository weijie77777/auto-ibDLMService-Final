package com.auto.constant;

public class RedisConstant {
    // 设置存储结果的key前缀
    public static final String USER_EDGELIST_RESULT = "edgeList_result";
    // 存放的节点层面的特征属性
    public static final String USER_FEATURES_X_RESULT = "features_x_result";
    // 存放的网络层面的特征属性
    public static final String USER_FEATURES_GLOBAL_RESULT = "features_global_result";
    // 存放当前任务的id
    public static final String USER_TASKID_RESULT = "task_id";
    // 存放当前用户设置的窗口
    public static final String USER_WINDOW_SIZE = "windowSize";
    // 存放用户的自训练模型
    public static final String USER_PRETRAIN_MODEL = "pretrain_feature_selector";

    public static final String USER_FEATURE_SELECTOR = "feature_selector";
    public static final String USER_MODEL = "gru";
    public static final String USER_LATENT_REPRESENTATION = "latent_representation";
    public static final String USER_ONLY_LOCAL = "only_local";

    // 存放example
    public static final String EXAMPLE_EDGELIST = "example_edgeList";
    public static final String EXAMPLE_FEATURES_X = "example_feature_x";
    public static final String EXAMPLE_FEATURES_GLOBAL = "example_feature_global";
    public static final String EXAMPLE_LATENT_REPRESENTATION = "example_latent_representation";
    public static final String EXAMPLE_FEATURE_SELECTOR = "example_feature_selector";
    public static final String EXAMPLE_MODEL = "example_gru";
}
