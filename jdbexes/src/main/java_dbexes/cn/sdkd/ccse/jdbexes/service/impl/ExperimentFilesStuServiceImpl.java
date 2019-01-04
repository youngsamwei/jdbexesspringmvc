package cn.sdkd.ccse.jdbexes.service.impl;


import cn.sdkd.ccse.jdbexes.mapper.ExperimentFilesMapper;
import cn.sdkd.ccse.jdbexes.mapper.ExperimentFilesStuMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentFiles;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesService;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.wangzhixuan.commons.result.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
@Service
public class ExperimentFilesStuServiceImpl extends ServiceImpl<ExperimentFilesStuMapper, ExperimentFilesStu> implements IExperimentFilesStuService {
    @Autowired
    private ExperimentFilesStuMapper experimentFilesStuMapper;

   ;public boolean refreshCache(){
        return experimentFilesStuMapper.refreshCache();
    };
}
