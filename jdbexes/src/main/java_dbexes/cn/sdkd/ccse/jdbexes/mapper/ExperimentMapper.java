package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jsqles.model.Examination;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentMapper extends BaseMapper<Experiment> {

    boolean refreshCache();
}
