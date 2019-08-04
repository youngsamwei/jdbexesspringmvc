package cn.sdkd.ccse.jdbexes.controller;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import cn.sdkd.ccse.jdbexes.service.*;
import com.wangzhixuan.commons.base.BaseController;
import com.wangzhixuan.commons.result.PageInfo;
import com.wangzhixuan.model.vo.UserVo;
import com.wangzhixuan.service.IUserService;
import jplag.ExitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


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
    private IUserService userService;

    /**
     * 实验管理页
     *
     * @return
     */
    @GetMapping("/manager")
    public String manager() {
        return "jdbexes/experiment/experiment_stu";
    }

    @GetMapping("/manager4Teacher")
    public String manager4Teacher(){
        return "jdbexes_admin/experiment_stu/experiment_stu";
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
        ExperimentStu experimentStu = experimentStuService.selectById(expstuno);
        model.addAttribute("experimentStu", experimentStu);
        return "jdbexes/experiment/experiment_stuSubmit";
    }

    @RequestMapping("/openTestLogPage")
    public Object openTestLogPage(Model model, @RequestParam("expstuno") Long expstuno) {
        String logText = "";
        ExperimentStu es = experimentStuService.selectById(expstuno);
        UserVo u = userService.selectVoById(es.getStuno());
        String logFile = checkMissionService.getLogRootDir() + "/" + u.getLoginName() + "_" + u.getName() + "/" + es.getExpno();
        if (es.getTeststatus() == 2) {
            logFile += "/build.log";
        } else if ((es.getTeststatus() == 3) || (es.getTeststatus() == 4) || (es.getTeststatus() == 5)) {
            logFile += "/testcases.log";
        } else {
            logFile += "/notExists.log";
        }

        if ((!new File(logFile).exists())) {
            logText = "不存在日志.";
        } else {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = "";
                while ((line = br.readLine()) != null) {
                    logText += line + "\n";
                }
                if (br != null) {
                    br.close();
                }
            } catch (FileNotFoundException e) {
                logger.error(e);
            } catch (IOException e) {
                logger.error(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
            }
        }
        model.addAttribute("logText", logText);
        return "jdbexes/experiment/experiment_stuOpenTestLog";
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

    @PostMapping("/experimentStuByExpno")
    @ResponseBody
    public Object experimentStuByExpno(Integer page, Integer rows, String sort, String order, Long expno){
        PageInfo pageInfo = new PageInfo(page, rows, sort, order);
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("expno", expno);
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
            String encode = "utf-8";
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), encode));

            ExperimentFilesStu efs = new ExperimentFilesStu();
            efs.setExpstuno(expstuno);
            efs.setFileno(fileno);

            String content = "";
            String str = "";
            while ((str = reader.readLine()) != null) {
                content = content + str + "\n";
            }
            efs.setFile_content(content);

            experimentFilesStuService.insert(efs);
            experimentStuService.refreshCache();
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

        logger.info("开始批量测试代码.");
        String[] expnoArray = expstunos.split(",");
        for(String expstuno : expnoArray) {
            checkMissionService.submitJob(Long.parseLong(expstuno));
        }

        return renderSuccess("开始批量测试");
    }

    @PostMapping(value = "/tree")
    @ResponseBody
    public Object tree() {
        return experimentStuService.selectTree();
    }

}
