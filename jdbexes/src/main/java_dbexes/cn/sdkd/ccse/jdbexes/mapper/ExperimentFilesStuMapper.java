package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import com.baomidou.mybatisplus.mapper.BaseMapper;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentFilesStuMapper extends BaseMapper<ExperimentFilesStu> {

    boolean refreshCache();
}
