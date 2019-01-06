package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.wangzhixuan.commons.result.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentStuMapper extends BaseMapper<ExperimentStu> {

    List<Map<String, Object>> selectDataGridByUser(PageInfo pageInfo, @Param("stuno")Long stuno);

    List<Map<String, Object>> experimentFilesDataGridByUser(PageInfo pageInfo, @Param("stuno")Long stuno,
                                                            @Param("expstuno") Long expstuno);

    @Select("select expstuno,expno,'' as expname, stuno,'' as stuname, selectedtime,`status` FROM experiment_stu e WHERE e.expstuno = #{expstuno}")
    ExperimentStu selectById(@Param("expstuno") Long expstuno);

    boolean updateStatusDesc(@Param("stuno")Long stuno,@Param("expno") Long expno, @Param("teststatus") Integer teststatus, @Param("testdesc") String testdesc);

    boolean refreshCache();
}
