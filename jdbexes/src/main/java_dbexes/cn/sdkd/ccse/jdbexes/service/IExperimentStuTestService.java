package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import com.baomidou.mybatisplus.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by sam on 2019/1/17.
 */
public interface IExperimentStuTestService extends IService<ExperimentStuTest> {
    ExperimentStuTest selectLatestByUser(@Param("stuno") Long stuno);

    List<ExperimentStuTest> selectListByUser(@Param("stuno") Long stuno);

    /*查询未计算相似度的提交*/
    List<ExperimentStuTest> selectListUnCompare();

    ExperimentStuTest findLatestByUserExperiment(Long stuno, Long expno);

    List<ExperimentStuTest> findLatestByExpno(Long expno);

}
