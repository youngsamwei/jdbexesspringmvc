package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.model.Organization;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentStuMapper extends BaseMapper<ExperimentStu> {

    List<Map<String, Object>> selectDataGridByUser(Pagination page, @Param("stuno")Long stuno);

    List<Map<String, Object>> experimentStuByExpno(Pagination page, Map<String, Object> params);

    List<Map<String, Object>> experimentFilesDataGridByUser(Pagination page, @Param("stuno")Long stuno,
                                                            @Param("expstuno") Long expstuno);

    @Select("select expstuno,expno,'' as expname, stuno,'' as stuname, selectedtime,`status` , teststatus FROM experiment_stu e WHERE e.expstuno = #{expstuno}")
    ExperimentStu selectById(@Param("expstuno") Long expstuno);

    @Select("select expstuno,expno,'' as expname, stuno,'' as stuname, selectedtime,`status` , teststatus FROM experiment_stu e WHERE e.stuno = #{stuno} AND e.expno = #{expno}")
    ExperimentStu selectByStunoExpno(@Param("stuno") Long stuno, @Param("expno") Long expno);

    @Select("select * from organization where id in (select organization_id from user join experiment_stu es on `user`.id = es.stuno)")
    List<Organization> selectOrganizations();

    boolean updateStatusDesc(@Param("stuno")Long stuno,@Param("expno") Long expno, @Param("teststatus") Integer teststatus, @Param("testdesc") String testdesc);

    boolean updateSimStatus(@Param("stuno")Long stuno, @Param("expno")Long expno, @Param("simstatus")Integer simstatus, @Param("simdesc")String simdesc);

    boolean refreshCache();
}
