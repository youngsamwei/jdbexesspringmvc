package cn.sdkd.ccse.jdbexes.service.impl;


import cn.sdkd.ccse.jdbexes.mapper.ExperimentMapper;
import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import cn.sdkd.ccse.jsqles.model.Examination;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.commons.result.Tree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
@Service
public class ExperimentServiceImpl extends ServiceImpl<ExperimentMapper, Experiment> implements IExperimentService {
    @Autowired
    private ExperimentMapper experimentMapper;

    public List<Experiment> selectAll() {
        EntityWrapper<Experiment> wrapper = new EntityWrapper<Experiment>();
        wrapper.orderBy("expno");
        return experimentMapper.selectList(wrapper);
    }

    @Override
    public void selectDataGrid(PageInfo pageInfo) {
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageInfo.getNowpage(), pageInfo.getSize());
        page.setOrderByField(pageInfo.getSort());
        page.setAsc(pageInfo.getOrder().equalsIgnoreCase("asc"));
        List<Map<String, Object>> list = experimentMapper.selectDataGrid(page);

        pageInfo.setRows(list);
        pageInfo.setTotal(page.getTotal());
    }

    @Override
    public void unSelectedDataGrid(PageInfo pageInfo, Long userid) {
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageInfo.getNowpage(), pageInfo.getSize());
        page.setOrderByField(pageInfo.getSort());
        page.setAsc(pageInfo.getOrder().equalsIgnoreCase("asc"));
        List<Map<String, Object>> list = experimentMapper.unSelectedDataGrid(page, userid);

        pageInfo.setRows(list);
        pageInfo.setTotal(page.getTotal());
    }

    @Override
    public List<Tree> selectTree() {
        List<Experiment> experimentList = selectAll();

        List<Tree> trees = new ArrayList<Tree>();
        if (experimentList != null) {
            for (Experiment experiment : experimentList) {
                Tree tree = new Tree();
                tree.setId(experiment.getExpno());
                tree.setText(experiment.getExpname());
                trees.add(tree);
            }
        }
        return trees;
    }

    public boolean refreshCache(){
       return experimentMapper.refreshCache();
    };
}
