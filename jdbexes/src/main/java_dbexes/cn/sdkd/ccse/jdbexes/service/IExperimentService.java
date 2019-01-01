package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import com.baomidou.mybatisplus.service.IService;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.commons.result.Tree;

import java.util.List;

/**
 *
 * Examination 表数据服务层接口
 *
 */
public interface IExperimentService extends IService<Experiment> {

    void selectDataGrid(PageInfo pageInfo);

    List<Experiment> selectAll();

    /**
     * 更新缓存
     * @return
     */
    boolean refreshCache();
}