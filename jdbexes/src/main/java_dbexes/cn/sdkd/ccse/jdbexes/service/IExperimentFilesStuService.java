package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 *
 * Resource 表数据服务层接口
 *
 */
public interface IExperimentFilesStuService extends IService<ExperimentFilesStu> {

    List<ExperimentFilesStuVO> selectFilesLatest(Long stuno, Long expno);
    /**
     * 更新缓存
     * @return
     */
    boolean refreshCache();
}