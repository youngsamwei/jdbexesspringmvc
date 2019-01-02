package cn.sdkd.ccse.jdbexes.controller;


import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentFiles;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesService;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import com.wangzhixuan.commons.base.BaseController;
import com.wangzhixuan.commons.result.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/dbexpfiles")
public class ExperimentFilesController extends BaseController {

    @Autowired
    private IExperimentFilesService experimentFilesService;
    @Autowired
    private IExperimentService experimentService;
    /**
     * 权限管理页
     *
     * @return
     */
    @GetMapping("/manager")
    public String manager() {
        return "jdbexes_admin/experimentFiles/experimentFiles";
    }

    /**
     * 权限列表
     *
     * @param page
     * @param rows
     * @param sort
     * @param order
     * @return
     */
    @PostMapping("/dataGrid")
    @ResponseBody
    public Object dataGrid(ExperimentFiles file, Integer page, Integer rows, String sort, String order) {
        PageInfo pageInfo = new PageInfo(page, rows, sort, order);

        Map<String, Object> condition = new HashMap<String, Object>();

        if (file.getExpno()!= null) {
            condition.put("expno", file.getExpno());
        }
        pageInfo.setCondition(condition);
        experimentFilesService.selectDataGrid(pageInfo);
        return pageInfo;
    }

    /**
     * 添加权限页
     *
     * @return
     */
    @GetMapping("/addPage")
    public String addPage(Model model, Long id) {
        Experiment e = experimentService.selectById(id);
        model.addAttribute("experiment", e);
        return "jdbexes_admin/experimentFiles/experimentFilesAdd";
    }

    /**
     * 添加权限
     *
     * @param experimentFiles
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public Object add(@Valid ExperimentFiles experimentFiles) {
        experimentFilesService.insert(experimentFiles);
        return renderSuccess("添加成功！");
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @RequestMapping("/delete")
    @ResponseBody
    public Object delete(Long id) {
        experimentFilesService.deleteById(id);
        return renderSuccess("删除成功！");
    }

    /**
     * 编辑页
     *
     * @param model
     * @param id
     * @return
     */
    @RequestMapping("/editPage")
    public String editPage(Model model, Long id) {
        ExperimentFiles experimentFiles = experimentFilesService.selectById(id);
        model.addAttribute("experimentFiles", experimentFiles);
        return "jdbexes_admin/experimentFiles/experimentFilesEdit";
    }

    /**
     * 更新
     *
     * @param experimentFiles
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    public Object edit(@Valid ExperimentFiles experimentFiles) {

        experimentFilesService.updateById(experimentFiles);
        return renderSuccess("编辑成功！");
    }

}
