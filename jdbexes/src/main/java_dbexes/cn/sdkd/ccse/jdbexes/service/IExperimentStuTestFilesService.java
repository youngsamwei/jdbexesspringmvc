package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import com.baomidou.mybatisplus.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by sam on 2019/1/17.
 */
public interface IExperimentStuTestFilesService extends IService<ExperimentStuTestFiles> {

    /*根据测试编号查询测试代码文件*/
    List<ExperimentStuTestFiles> selectListByTestno(@Param("experiment_stu_test_no") Long experiment_stu_test_no);

    boolean insertLatestTestFiles(@Param("experiment_stu_test_no") Long experiment_stu_test_no, @Param("stuno") Long stuno, @Param("expno")Long expno);
}
