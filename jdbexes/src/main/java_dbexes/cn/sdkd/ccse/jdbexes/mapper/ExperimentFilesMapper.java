package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentFiles;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentFilesMapper extends BaseMapper<ExperimentFiles> {

    List<Map<String, Object>> selectExperimentFilesPage(Pagination page, Map<String, Object> params);

    boolean refreshCache();
}
