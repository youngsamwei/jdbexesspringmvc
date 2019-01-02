package cn.sdkd.ccse.jdbexes.controller;

import cn.sdkd.ccse.jdbexes.model.Experiment;
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
import java.util.List;


@Controller
@RequestMapping("/dbexp")
public class DBExpController extends BaseController {

    @Autowired
    private IExperimentService experimentService;

    /**
     * 实验管理页
     *
     * @return
     */
    @GetMapping("/manager")
    public String manager() {
        return "jdbexes_admin/experiment/experiment";
    }

    /**
     * 添加实验页
     *
     * @return
     */
    @GetMapping("/addPage")
    public String addPage() {
        return "jdbexes_admin/experiment/experimentAdd";
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
        Experiment experiment = experimentService.selectById(id);
        model.addAttribute("experiment", experiment);
        return "jdbexes_admin/experiment/experimentEdit";
    }

    /**
     * 添加实验
     *
     * @param experiment
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public Object add(@Valid Experiment experiment) {
        experimentService.insert(experiment);
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
        experimentService.deleteById(id);
        return renderSuccess("删除成功！");
    }

    /**
     * 更新
     *
     * @param experiment
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    public Object edit(@Valid Experiment experiment) {
        experimentService.updateById(experiment);
        return renderSuccess("编辑成功！");
    }

    /**
     * 实验列表
     *
     * @param page
     * @param rows
     * @param sort
     * @param order
     * @return
     */
    @PostMapping("/dataGrid")
    @ResponseBody
    public Object dataGrid(Integer page, Integer rows, String sort, String order) {
        PageInfo pageInfo = new PageInfo(page, rows, sort, order);
        experimentService.selectDataGrid(pageInfo);
        return pageInfo;
    }

    @PostMapping(value = "/tree")
    @ResponseBody
    public Object tree() {
        return experimentService.selectTree();
    }

}
