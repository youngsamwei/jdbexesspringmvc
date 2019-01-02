package cn.sdkd.ccse.jdbexes.service.impl;


import cn.sdkd.ccse.jdbexes.mapper.ExperimentFilesMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentFiles;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.wangzhixuan.commons.result.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 2017/7/13.
 */
@Service
public class ExperimentFilesServiceImpl extends ServiceImpl<ExperimentFilesMapper, ExperimentFiles> implements IExperimentFilesService {
    @Autowired
    private ExperimentFilesMapper experimentFilesMapper;

    public List<ExperimentFiles> selectAll() {
        EntityWrapper<ExperimentFiles> wrapper = new EntityWrapper<ExperimentFiles>();
        wrapper.orderBy("fileno");
        return experimentFilesMapper.selectList(wrapper);
    }

    @Override
    public void selectDataGrid(PageInfo pageInfo) {
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageInfo.getNowpage(), pageInfo.getSize());
        page.setOrderByField(pageInfo.getSort());
        page.setAsc(pageInfo.getOrder().equalsIgnoreCase("asc"));
        List<Map<String, Object>> list = experimentFilesMapper.selectExperimentFilesPage(page, pageInfo.getCondition());
        pageInfo.setRows(list);
        pageInfo.setTotal(page.getTotal());
    }

    ;public boolean refreshCache(){
        return experimentFilesMapper.refreshCache();
    };
}
