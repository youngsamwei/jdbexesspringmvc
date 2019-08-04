package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
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

    @Insert("insert into experiment_stu_test_files(experiment_stu_test_no, experiment_files_stu_no, fileno, stuno) " +
            " select #{experiment_stu_test_no}, expfilestuno, fileno, stuno " +
            " from experiment_stu es join experiment_files_stu_latest efsl on es.expstuno = efsl.expstuno " +
            " where es.stuno = #{stuno} and es.expno = #{expno}")
    boolean insertLatestTestFiles(@Param("experiment_stu_test_no") Long experiment_stu_test_no, @Param("stuno") Long stuno, @Param("expno")Long expno);

    boolean refreshCache();
}
