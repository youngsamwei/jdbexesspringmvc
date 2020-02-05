package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuTestService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sam on 2019/1/17.
 */
@Service
public class ExperimentStuTestServiceImpl extends ServiceImpl<ExperimentStuTestMapper, ExperimentStuTest> implements IExperimentStuTestService {
    @Autowired
    private ExperimentStuTestMapper experimentStuTestMapper;

    @Override
    public ExperimentStuTest selectLatestByUser(Long stuno) {
        return experimentStuTestMapper.selectLatestByUser(stuno);
    }

    @Override
    public List<ExperimentStuTest> selectListByUser(Long stuno) {
        return experimentStuTestMapper.selectListByUser(stuno);
    }

    @Override
    public List<ExperimentStuTest> selectListUnCompare() {
        return experimentStuTestMapper.selectListUnCompare();
    }

    @Override
    public ExperimentStuTest findLatestByUserExperiment(Long stuno, Long expno) {
        return experimentStuTestMapper.selectLatestByUserExperiment(stuno, expno);
    }
}
