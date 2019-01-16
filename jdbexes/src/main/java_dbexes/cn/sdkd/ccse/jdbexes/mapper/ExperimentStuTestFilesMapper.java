package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentStuTestFilesMapper extends BaseMapper<ExperimentStuTestFiles> {

    @Select("select experiment_stu_test_files_no, experiment_stu_test_no, experiment_files_stu_no, fileno, stuno " +
            " from experiment_stu_test_files where experiment_stu_test_no = #{experiment_stu_test_no}")
    List<ExperimentStuTestFiles> selectListByTestno(@Param("experiment_stu_test_no") Long experiment_stu_test_no);

    boolean refreshCache();
}
