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

    /*查询未计算相似度的提交*/
    @Select(" select est1.* " +
            " from experiment_stu_test est1 join ( " +
            " select max(est.experiment_stu_test_no) experiment_stu_test_no " +
            " from experiment_stu_test est join  " +
            " (select expno, stuno from experiment_stu es where simstatus < 0 and teststatus > 0) es  " +
            " on est.expno = es.expno and est.stuno = es.stuno " +
            " group by est.expno, est.stuno " +
            " ) est2 on est1.experiment_stu_test_no = est2.experiment_stu_test_no")
    List<ExperimentStuTest> selectListUnCompare();

    List<Map<String, Object>> selectDataGridByUser(Pagination page, @Param("stuno") Long stuno);

    List<Map<String, Object>> selectDataGridByExpno(Pagination page, Map<String, Object> params);

    boolean refreshCache();
}
