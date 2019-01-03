package cn.sdkd.ccse.jdbexes.controller;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import com.wangzhixuan.commons.base.BaseController;
import com.wangzhixuan.commons.result.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


@Controller
@RequestMapping("/dbexperiment_stu")
public class ExperimentStuController extends BaseController {

    @Autowired
    private IExperimentStuService experimentStuService;

    @Autowired
    private IExperimentService experimentService;

    /**
     * 实验管理页
     *
     * @return
     */
    @GetMapping("/manager")
    public String manager() {
        return "jdbexes/experiment/experiment_stu";
    }

    /**
     * 添加实验页
     *
     * @return
     */
    @GetMapping("/addPage")
    public String addPage() {
        return "jdbexes/experiment/experiment_stuAdd";
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
        ExperimentStu experimentStu = experimentStuService.selectById(id);
        model.addAttribute("experimentStu", experimentStu);
        return "jdbexes/experiment/experiment_stuEdit";
    }

    @RequestMapping("/submitFilePage")
    public String submitFilePage(Model model, Long expstuno) {
//        ExperimentStu experimentStu = experimentStuService.selectById(expstuno);
        model.addAttribute("expstuno", expstuno);
        return "jdbexes/experiment/experiment_stuSubmit";
    }

    /**
     * 添加实验选择
     *
     * @param expnos
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public Object add(String expnos) {
        experimentStuService.insert(getUserId(), expnos);
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
        experimentStuService.deleteById(id);
        experimentService.refreshCache();
        return renderSuccess("删除成功！");
    }

    /**
     * 更新
     *
     * @param experimentStu
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    public Object edit(@Valid ExperimentStu experimentStu) {
        experimentStuService.updateById(experimentStu);
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
        experimentStuService.selectDataGridByUser(pageInfo, getUserId());
        return pageInfo;
    }

    @RequestMapping("/experimentFilesDataGrid")
    @ResponseBody
    public Object experimentFilesDataGrid(Integer page, Integer rows, String sort, String order, Long expstuno) {
        PageInfo pageInfo = new PageInfo(page, rows, sort, order);
        experimentStuService.experimentFilesDataGridByUser(pageInfo, getUserId(), expstuno);
        return pageInfo;
    }

    @PostMapping("/uploadFile")
    @ResponseBody
    public Object uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info(file.getOriginalFilename());

        try {
            // 默认以utf-8形式
            String encode = "utf-8";
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), encode));

            String str = "";
            while ((str = reader.readLine()) != null) {
                logger.info(str);
            }
            return renderSuccess("上传成功！");
        } catch (UnsupportedEncodingException e1) {
            logger.error(e1);
        } catch (IOException e) {
            logger.error(e);
        }

        return renderError("上传失败！");
    }

    @PostMapping("/check")
    @ResponseBody
    public Object check() {
        return renderSuccess("测试通过！");
    }

    @PostMapping(value = "/tree")
    @ResponseBody
    public Object tree() {
        return experimentStuService.selectTree();
    }

}
