package cn.sdkd.ccse.jdbexes.service.impl;


import cn.sdkd.ccse.jdbexes.mapper.ExperimentMapper;
import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.commons.result.Tree;
import com.wangzhixuan.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
@Service
public class ExperimentStuServiceImpl extends ServiceImpl<ExperimentStuMapper, ExperimentStu> implements IExperimentStuService {
    @Autowired
    private ExperimentStuMapper experimentStuMapper;
    @Autowired
    private ExperimentMapper experimentMapper;

    public List<ExperimentStu> selectAll() {
        EntityWrapper<ExperimentStu> wrapper = new EntityWrapper<ExperimentStu>();
        wrapper.orderBy("expno");
        return experimentStuMapper.selectList(wrapper);
    }

    @Override
    public void insert(Long userid, String expnos) {
        String[] expnoArray = expnos.split(",");
        List<ExperimentStu> list = new ArrayList<ExperimentStu>();
        for (String expno : expnoArray) {
            ExperimentStu es = new ExperimentStu();
            es.setExpno(Long.parseLong(expno));
            es.setStuno(userid);
            es.setStatus(1);
            es.setTeststatus(-1);
            list.add(es);
        }
        insertBatch(list);
        refreshCache();
        experimentMapper.refreshCache();
    }

    @Override
    public boolean updateStatusDesc(Long stuno, Long expno, Integer teststatus, String testdesc) {
        return experimentStuMapper.updateStatusDesc(stuno, expno, teststatus, testdesc);
    }

    @Override
    public boolean updateSimStatus(Long stuno, Long expno, Integer simstatus, String simdesc) {
        return experimentStuMapper.updateSimStatus(stuno, expno, simstatus, simdesc);
    }

    @Override
    public void selectDataGridByUser(PageInfo pageInfo, Long userid) {
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageInfo.getNowpage(), pageInfo.getSize());
        page.setOrderByField(pageInfo.getSort());
        page.setAsc(pageInfo.getOrder().equalsIgnoreCase("asc"));

        List<Map<String, Object>> list = experimentStuMapper.selectDataGridByUser(page, userid);

        pageInfo.setRows(list);
        pageInfo.setTotal(page.getTotal());
    }

    @Override
    public void experimentStuByExpno(PageInfo pageInfo) {

        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageInfo.getNowpage(), pageInfo.getSize());
        page.setOrderByField(pageInfo.getSort());
        page.setAsc(pageInfo.getOrder().equalsIgnoreCase("asc"));

        List<Map<String, Object>> list = experimentStuMapper.experimentStuByExpno(page, pageInfo.getCondition());

        pageInfo.setRows(list);
        pageInfo.setTotal(page.getTotal());
    }

    @Override
    public void experimentFilesDataGridByUser(PageInfo pageInfo, Long userid, Long expstuno) {
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageInfo.getNowpage(), pageInfo.getSize());
        page.setOrderByField(pageInfo.getSort());
        page.setAsc(pageInfo.getOrder().equalsIgnoreCase("asc"));
        List<Map<String, Object>> list = experimentStuMapper.experimentFilesDataGridByUser(page, userid, expstuno);

        pageInfo.setRows(list);
        pageInfo.setTotal(page.getTotal());
    }

    @Override
    public ExperimentStu selectById(Long expstuno) {
        return experimentStuMapper.selectById(expstuno);
    }

    @Override
    public List<Organization> selectOrganizations() {
        return experimentStuMapper.selectOrganizations();
    }


    @Override
    public List<Tree> selectTree() {
        List<ExperimentStu> experimentList = selectAll();

        List<Tree> trees = new ArrayList<Tree>();
        if (experimentList != null) {
            for (ExperimentStu experimentStu : experimentList) {
                Tree tree = new Tree();
                tree.setId(experimentStu.getExpstuno());
                tree.setText(experimentStu.getExpname());
                trees.add(tree);
            }
        }
        return trees;
    }

    public boolean refreshCache() {
        return experimentStuMapper.refreshCache();
    }

    @Override
    public ExperimentStu selectByStunoExpno(Long stuno, Long expno) {
        return experimentStuMapper.selectByStunoExpno(stuno, expno);
    }

}
