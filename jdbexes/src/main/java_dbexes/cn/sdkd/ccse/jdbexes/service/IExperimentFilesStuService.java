package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import com.baomidou.mybatisplus.service.IService;

/**
 *
 * Resource 表数据服务层接口
 *
 */
public interface IExperimentFilesStuService extends IService<ExperimentFilesStu> {

    /**
     * 更新缓存
     * @return
     */
    boolean refreshCache();
}