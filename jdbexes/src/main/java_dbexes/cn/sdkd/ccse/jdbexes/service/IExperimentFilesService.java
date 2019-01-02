package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.ExperimentFiles;
import com.baomidou.mybatisplus.service.IService;
import com.wangzhixuan.commons.result.PageInfo;

import java.util.List;

/**
 *
 * Resource 表数据服务层接口
 *
 */
public interface IExperimentFilesService extends IService<ExperimentFiles> {

    List<ExperimentFiles> selectAll();
    void selectDataGrid(PageInfo pageInfo);

    /**
     * 更新缓存
     * @return
     */
    boolean refreshCache();
}