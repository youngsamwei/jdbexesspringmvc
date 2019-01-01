package cn.sdkd.ccse.jdbexes.service.impl;


import cn.sdkd.ccse.jdbexes.mapper.ExperimentMapper;
import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.wangzhixuan.commons.result.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        Page<Experiment> page = new Page<Experiment>(pageInfo.getNowpage(), pageInfo.getSize());

        EntityWrapper<Experiment> wrapper = new EntityWrapper<Experiment>();
        wrapper.orderBy(pageInfo.getSort(), pageInfo.getOrder().equalsIgnoreCase("ASC"));
        selectPage(page, wrapper);

        pageInfo.setRows(page.getRecords());
        pageInfo.setTotal(page.getTotal());
    }

    public boolean refreshCache(){
       return experimentMapper.refreshCache();
    };
}
