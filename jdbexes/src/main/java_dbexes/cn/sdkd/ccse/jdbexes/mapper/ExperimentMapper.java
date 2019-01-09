package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentMapper extends BaseMapper<Experiment> {

    List<Map<String, Object>> unSelectedDataGrid(Pagination page, @Param("stuno")Long stuno);

    @Select("select expno, expname, testtarget, is_open from experiment ")
    List<Map<String, Object>> selectDataGrid(Pagination page);

    boolean refreshCache();
}
