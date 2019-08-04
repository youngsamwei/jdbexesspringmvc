package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestFilesMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuTestFilesService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sam on 2019/1/17.
 */
@Service
public class ExperimentStuTestFilesServiceImpl extends ServiceImpl<ExperimentStuTestFilesMapper, ExperimentStuTestFiles> implements IExperimentStuTestFilesService {
    @Autowired
    private ExperimentStuTestFilesMapper experimentStuTestFilesMapper;

    @Override
    public List<ExperimentStuTestFiles> selectListByTestno(Long experiment_stu_test_no) {
        return experimentStuTestFilesMapper.selectListByTestno(experiment_stu_test_no);
    }

    @Override
    public boolean insertLatestTestFiles(Long experiment_stu_test_no, Long stuno, Long expno) {
        return experimentStuTestFilesMapper.insertLatestTestFiles(experiment_stu_test_no, stuno, expno);
    }
}
