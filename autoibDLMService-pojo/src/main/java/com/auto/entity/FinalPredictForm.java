package com.auto.entity;

import lombok.Data;
import javax.validation.constraints.NotNull; // 替换为 javax 校验注解（关键！）

@Data
public class FinalPredictForm {
    // 允许为空，无注解
    public Integer timestep;

    // 必传，使用 javax.validation.constraints.NotNull
    @NotNull(message = "threshold不能为空")
    public Integer threshold;
}