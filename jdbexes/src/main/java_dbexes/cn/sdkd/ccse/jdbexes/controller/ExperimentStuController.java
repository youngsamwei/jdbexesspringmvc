package cn.sdkd.ccse.jdbexes.controller;

import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.service.*;
import cn.sdkd.ccse.jdbexes.service.impl.jplag.Configuration;
import com.wangzhixuan.commons.base.BaseController;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/dbexperiment_stu")
public class ExperimentStuController extends BaseController {

    @Autowired
    private IExperimentStuService experimentStuService;

    @Autowired
    private IExperimentService experimentService;

    @Autowired
    private IExperimentFilesStuService experimentFilesStuService;

    @Autowired
    private ICheckMissionService checkMissionService;

    @Autowired
    private IJPlagService jPlagService;

    @Autowired
    private IExperimentStuTestService experimentStuTestService;

    @Autowired
    private INeo4jService neo4jService;

    /**
     * 实验管理页
     */
    @GetMapping("/manager")
    public String manager() {
        return "jdbexes/experiment/experiment_stu";
    }

    @RequestMapping("/manager4Teacher")
    public String manager4Teacher(Model model) {
        List<Organization> organizations = experimentStuService.selectOrganizations();
        model.addAttribute("organizations", organizations);
        return "jdbexes_admin/experiment_stu/experiment_stu";
    }

    /**
     * 添加实验页
     */
    @GetMapping("/addPage")
    public String addPage() {
        return "jdbexes/experiment/experiment_stuAdd";
    }

    @RequestMapping("/submitFilePage")
    public String submitFilePage(Model model, Long expstuno) {
        ExperimentStu experimentStu = experimentStuService.selectById(expstuno);
        model.addAttribute("experimentStu", experimentStu);
        return "jdbexes/experiment/experiment_stuSubmit";
    }

    @RequestMapping("/openTestLogPage")
    public Object openTestLogPage(Model model, @RequestParam("expstuno") Long expstuno) {
        ExperimentStu es = experimentStuService.selectById(expstuno);
        String logText = experimentStuService.getCheckLog(es.getStuno(), es.getExpno());
        model.addAttribute("logText", logText);
        return "jdbexes/experiment/experiment_stuOpenTestLog";
    }

    @RequestMapping("/openSimCheckResultPage")
    public Object openSimCheckResultPage(Model model, @RequestParam("expstuno") Long expstuno) {
        ExperimentStu experimentStu = experimentStuService.selectById(expstuno);
        ExperimentStuTest experimentStuTest = experimentStuTestService.findLatestByUserExperiment(experimentStu.getStuno(), experimentStu.getExpno());
        if (experimentStuTest == null) {
            model.addAttribute("logText", "未查询到相应 ExperimentStuTest");
            return "jdbexes/experiment/experiment_stuOpenTestLog";
        }
        long experiment_stu_test_no = experimentStuTest.getExperiment_stu_test_no();
        Assignment assignment = neo4jService.findAssignmentByExperimentStuTestNo(experiment_stu_test_no);
        logger.debug("assignment id: " + experiment_stu_test_no);
        if (assignment == null) {
            model.addAttribute("logText", "未查询到相应 Assignment");
            return "jdbexes/experiment/experiment_stuOpenTestLog";
        }

        logger.debug("node id: " + assignment.getId());

        List<Student> students = neo4jService.findStudentBySimValueAssignmentid(Configuration.SIM_THRESHOLD, assignment.getId());

        StringBuilder simResult = new StringBuilder();
        for (Student student : students) {
            simResult.append(student.getName());
            simResult.append('\n');
        }

        model.addAttribute("logText", simResult.toString());
        return "jdbexes/experiment/experiment_stuOpenTestLog";
    }

    /**
     * 添加实验选择
     */
    @PostMapping("/add")
    @ResponseBody
    public Object add(String expnos) {
        experimentStuService.insert(getUserId(), expnos);
        return renderSuccess("添加成功！");
    }

    /**
     * 删除
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
     */
    @RequestMapping("/edit")
    @ResponseBody
    public Object edit(@Valid ExperimentStu experimentStu) {
        experimentStuService.updateById(experimentStu);
        return renderSuccess("编辑成功！");
    }

    /**
     * 实验列表
     */
    @PostMapping("/dataGrid")
    @ResponseBody
    public Object dataGrid(Integer page, Integer rows, String sort, String order) {
        PageInfo pageInfo = new PageInfo(page, rows, sort, order);
        experimentStuService.selectDataGridByUser(pageInfo, getUserId());
        return pageInfo;
    }

    @PostMapping("/experimentStuByExpno")
    @ResponseBody
    public Object experimentStuByExpno(Integer page, Integer rows, String sort, String order, Long expno, @RequestParam("organization_id") Long organization_id) {
        PageInfo pageInfo = new PageInfo(page, rows, sort, order);
        Map<String, Object> condition = new HashMap<>();
        condition.put("expno", expno);
        condition.put("organization_id", organization_id);
        pageInfo.setCondition(condition);
        experimentStuService.experimentStuByExpno(pageInfo);
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
    public Object uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("expstuno") Integer expstuno,
                             @RequestParam("fileno") Integer fileno) {

        try {
            // 默认以utf-8形式
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            ExperimentFilesStu efs = new ExperimentFilesStu();
            efs.setExpstuno(expstuno);
            efs.setFileno(fileno);

            StringBuilder content = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                content.append(line).append('\n');
            }
            efs.setFile_content(content.toString());

            experimentFilesStuService.insert(efs);
            experimentStuService.refreshCache();
            return renderSuccess("上传成功！");
        } catch (IOException e1) {
            logger.error(e1);
        }

        return renderError("上传失败！");
    }

    /**/
    @PostMapping("/check")
    @ResponseBody
    public Object check(Long expno) {

        logger.info("开始测试代码.");
        checkMissionService.submitJob(getUserId(), expno);

        experimentStuService.updateSimStatus(getUserId(), expno, -1, "正在计算相似度");
        jPlagService.submitJob(getUserId(), expno);

        return renderSuccess("开始测试");
    }

    @PostMapping("/checkBatch")
    @ResponseBody
    public Object checkBatch(String expstunos) {
        if (expstunos.isEmpty()) {
            return renderSuccess("没有合适的参数");
        }

        String[] expsutnoArray = expstunos.split(",");

        logger.info("开始批量测试代码：" + Arrays.toString(expsutnoArray));

        List<Long> expstunoList = Arrays.stream(expsutnoArray)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 重新测试代码
        for (Long expstuno : expstunoList) {
            logger.debug("测试代码 expstuno=" + expstuno);
            checkMissionService.submitJob(expstuno);
        }

        // 刷新计算相似度
        List<ExperimentStu> experimentStuList = expstunoList.stream()
                .map(x -> experimentStuService.selectById(x))
                .collect(Collectors.toList());
        for (ExperimentStu es : experimentStuList) {
            logger.debug("测试相似度 stuno=" + es.getStuno() + ", expno=" + es.getExpno());
            experimentStuService.updateSimStatus(es.getStuno(), es.getExpno(), -1, "重新计算相似度");
        }
        for (ExperimentStu es : experimentStuList) {
            jPlagService.refreshSimStatus(es.getStuno(), es.getExpno());
        }

        return renderSuccess("开始批量测试");
    }


    @PostMapping(value = "/tree")
    @ResponseBody
    public Object tree() {
        return experimentStuService.selectTree();
    }

}
