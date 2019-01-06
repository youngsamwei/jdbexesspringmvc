package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import com.baomidou.mybatisplus.service.IService;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.commons.result.Tree;

import java.util.List;

/**
 * Examination 表数据服务层接口
 */
public interface IExperimentStuService extends IService<ExperimentStu> {

    void selectDataGridByUser(PageInfo pageInfo, Long userid);

    void experimentFilesDataGridByUser(PageInfo pageInfo, Long userid, Long expstuno);

    ExperimentStu selectById(Long expstuno);

    List<ExperimentStu> selectAll();

    void insert(Long userid, String expnos);

    boolean updateStatusDesc(Long stuno, Long expno, Integer teststatus, String testdesc);

    List<Tree> selectTree();

    /**
     * 更新缓存
     *
     * @return
     */
    boolean refreshCache();
}