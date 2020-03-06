package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import com.baomidou.mybatisplus.service.IService;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.commons.result.Tree;
import com.wangzhixuan.model.Organization;

import java.util.List;

/**
 * Examination 表数据服务层接口
 */
public interface IExperimentStuService extends IService<ExperimentStu> {

    void selectDataGridByUser(PageInfo pageInfo, Long userid);

    void experimentStuByExpno(PageInfo pageInfo);

    void experimentFilesDataGridByUser(PageInfo pageInfo, Long userid, Long expstuno);

    ExperimentStu selectById(Long expstuno);

    /*查询学生所属的班级*/
    List<Organization> selectOrganizations();

    List<ExperimentStu> selectAll();

    void insert(Long userid, String expnos);

    boolean updateStatusDesc(Long stuno, Long expno, Integer teststatus, String testdesc);

    boolean updateSimStatus(Long stuno, Long expno, Integer simstatus, String simdesc);

    boolean updateCheckLog(Long stuno, Long expno, String content);

    String getCheckLog(Long stuno, Long expno);

    List<Tree> selectTree();

    /**
     * 更新缓存
     *
     * @return
     */
    boolean refreshCache();
}