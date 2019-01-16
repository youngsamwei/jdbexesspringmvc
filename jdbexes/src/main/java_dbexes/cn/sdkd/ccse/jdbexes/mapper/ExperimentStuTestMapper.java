package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentStuTestMapper extends BaseMapper<ExperimentStuTest> {

    @Select("select experiment_stu_test_no, expno, stuno, testtime, testdesc, teststatus, simdesc, simstatus " +
            " from experiment_stu_test where stuno = #{stuno} order by testtime desc limit 0,1")
    ExperimentStuTest selectLatestByUser( @Param("stuno") Long stuno);

    @Select("select experiment_stu_test_no, expno, stuno, testtime, testdesc, teststatus, simdesc, simstatus " +
            " from experiment_stu_test where stuno = #{stuno} ")
    List<ExperimentStuTest> selectListByUser(@Param("stuno") Long stuno);

    List<Map<String, Object>> selectDataGridByUser(Pagination page, @Param("stuno") Long stuno);

    List<Map<String, Object>> selectDataGridByExpno(Pagination page, Map<String, Object> params);

    boolean refreshCache();
}
