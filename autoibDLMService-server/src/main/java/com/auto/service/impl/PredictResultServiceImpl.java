package com.auto.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.auto.common.BaseContext;
import com.auto.common.CustomException;
import com.auto.constant.MessageConstant;
import com.auto.dto.PredictionResultDTO;
import com.auto.entity.PredictResult;
import com.auto.entity.User;
import com.auto.entity.UserImage;
import com.auto.mapper.PredictResultMapper;
import com.auto.service.PredictResultService;
import com.auto.service.UserService;
import com.auto.vo.PredictionResultVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PredictResultServiceImpl extends ServiceImpl<PredictResultMapper, PredictResult> implements PredictResultService {
    /**
     * 保存预测结果
     * @param predictionResultDTO
     */
    @Autowired
    private UserService userService;
    @Override
    @Transactional
    public void saveResult(PredictionResultDTO predictionResultDTO) {
        if(predictionResultDTO == null){
            throw new CustomException(MessageConstant.RESULT_NULL);
        }
        String username = getUsername();
        PredictResult predictResult = BeanUtil.copyProperties(predictionResultDTO, PredictResult.class);
        predictResult.setUsername(username);
        this.save(predictResult);
    }

    @Override
    public Page<PredictionResultVo> getByUsername(long current, long size) {
        String username = getUsername();
        QueryWrapper<PredictResult> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        wrapper.orderByDesc("create_time");
        Page<PredictResult> page = new Page<>(current, size);
        Page<PredictResult> predictResultPage = baseMapper.selectPage(page, wrapper);
        Page<PredictionResultVo> predictionResultVoPage = new Page<>();
        predictionResultVoPage.setCurrent(predictResultPage.getCurrent());
        predictionResultVoPage.setSize(predictResultPage.getSize());
        predictionResultVoPage.setTotal(predictResultPage.getTotal());
        // 转换实体列表到VO列表
        if (predictResultPage.getRecords() != null) {
            predictionResultVoPage.setRecords(predictResultPage.getRecords().stream()
                    .map(entity -> BeanUtil.copyProperties(entity, PredictionResultVo.class))
                    .collect(java.util.stream.Collectors.toList()));
        }
        return predictionResultVoPage;
    }

    @Transactional
    @Override
    public Page<PredictionResultVo> deleteByImagePath(String imagePath, long current, long size) {
        baseMapper.delete(new QueryWrapper<PredictResult>().eq("image_url", imagePath));
        return getByUsername(current, size);
    }

    private String getUsername(){
        Long currentId = BaseContext.getCurrentId();
        User user = userService.getById(currentId);
        return user.getUsername();
    }
}
