package cn.sdkd.ccse.jdbexes.checkmission;

import cn.sdkd.ccse.commons.utils.FileUtils;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.List;

/**
 * Created by sam on 2019/1/4.
 */
public class CheckJob implements Runnable {
    private static final Log logger = LogFactory.getLog(CheckJob.class);
    /*从0开始计数*/
    private int step;

    /* 学生编号*/
    private Long stuno;
    /*实验编号*/
    private Long expno;

    private String testTarget;

    /*学生实验提交文件根目录*/
    private String srcDir;
    /*学生实验项目根目录*/
    private String projectDir;

    private String originalProjectRootDir;

    private String logDir;

    private IExperimentFilesStuService experimentFilesStuService;
    private IExperimentStuService experimentStuService;

    private List<ExperimentFilesStuVO> experimentFilesStuVOList;

    public CheckJob(Long stuno, Long expno, IExperimentFilesStuService experimentFilesStuService, IExperimentStuService experimentStuService, String srcDir, String projectDir, String originalProjectRootDir, String logDir) {
        this.stuno = stuno;
        this.expno = expno;
        this.experimentFilesStuService = experimentFilesStuService;
        this.experimentStuService = experimentStuService;

        experimentFilesStuVOList = experimentFilesStuService.selectFilesLatest(this.stuno, this.expno);
        this.testTarget = experimentFilesStuVOList.get(0).getTesttarget();

        this.srcDir = srcDir;
        this.projectDir = projectDir;
        this.originalProjectRootDir = originalProjectRootDir;
        this.logDir = logDir;
        initDirs();
    }

    private void initDirs() {
        File fSrcDir = new File(this.srcDir);
        if (!fSrcDir.exists()) {
            fSrcDir.mkdirs();
        }
        File fProjectDir = new File(this.projectDir);
        if (!fProjectDir.exists()) {
            fProjectDir.mkdirs();
        }
        File fLogDir = new File(this.logDir);
        if (!fLogDir.exists()) {
            fLogDir.mkdirs();
        }
    }

    /*从数据库中读取学生提交的代码文件，产生到srcRootDir中指定实验的文件夹中
    * 如果存在则覆盖，可能其他进程会也在访问该文件
    * */
    public void step1GenerateFiles() {

        logger.debug("获得" + experimentFilesStuVOList.size() + "文件");
        for (ExperimentFilesStuVO efsv : experimentFilesStuVOList) {
            String fname = this.srcDir + efsv.getSrcfilename();
            OutputStreamWriter op = null;
            try {
                op = new OutputStreamWriter(new FileOutputStream(fname), "utf-8");
                op.append(efsv.getFile_content());
                op.flush();
                op.close();
            } catch (UnsupportedEncodingException e) {
                logger.debug(e.getMessage());
            } catch (FileNotFoundException e) {
                logger.debug(e.getMessage());
            } catch (IOException e) {
                logger.debug(e.getMessage());
            }

        }
    }

    /*读取需要比较的代码文件，先解析
    * 可能会与文件产生冲突
    * */
    public void step2SimilarityParser() {

    }

    /*解析完成后计算相似度*/
    public void step3SimilarityCheck() {

    }

    /*执行功能测试*/
    public void step4TestCases() {
        boolean passed = false;
        /*第一步：复制项目文件到学生个人文件夹*/
        try {
            FileUtils.copyDir(originalProjectRootDir, projectDir);
            passed = true;
        } catch (IOException e) {
            logger.error(e.getMessage());
            experimentStuService.updateStatusDesc(this.stuno, this.expno, 0, "复制文件发生错误");
        }
        if (passed) {
            passed = false;
            /*第二步：复制作业文件到学生个人文件夹*/
            experimentStuService.updateStatusDesc(this.stuno, this.expno, 0, "正在复制文件..");
            for (ExperimentFilesStuVO efsv : experimentFilesStuVOList) {
                try {
                    File srcf = new File(this.srcDir + "/" + efsv.getSrcfilename());
                    if (!srcf.exists()) {
                        experimentStuService.updateStatusDesc(this.stuno, this.expno, 1, "缺少文件:" + efsv.getSrcfilename());
                        passed = false;
                        break;
                    } else {
                        FileUtils.copyFile(this.srcDir + "/" + efsv.getSrcfilename(), this.projectDir + "/" + efsv.getDstfilename());
                        passed = true;
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    experimentStuService.updateStatusDesc(this.stuno, this.expno, 1, "复制文件时出错");
                    passed = false;
                    break;
                }
            }
        }
        if (passed) {
            passed = false;
            try {
                /*第三步：refresh*/
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 0, "正在刷新项目..");
                FileUtils.execCmdOutput(this.projectDir + "/cmd/refresh.bat", this.logDir + "refresh.log", "utf8");
                passed = true;
            } catch (IOException e) {
                logger.error(e.getMessage());
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 2, "刷新项目时出错");
            }
        }
        if (passed) {
            passed = false;
            try {
            /*第四步：clean*/
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 0, "正在清理项目..");
                FileUtils.execCmdOutput(this.projectDir + "/cmd/clean.bat", logger, "utf8");
                passed = true;
            } catch (IOException e) {
                logger.error(e.getMessage());
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 2, "清理项目时出错");
            }
        }
        if (passed) {
            passed = false;
            try {
            /*第五步：build*/
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 0, "正在编译项目..");
                FileUtils.execCmdOutput(this.projectDir + "/cmd/build.bat " + this.testTarget, this.logDir + "/build.log", "utf8");
                File t = new File(this.projectDir + "/bin/" + this.testTarget + ".exe");
                if (!t.exists()) {
                    experimentStuService.updateStatusDesc(this.stuno, this.expno, 2, "编译项目时出错");
                } else {
                    passed = true;
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 2, "编译项目时出错");
            }
        }
        if (passed) {
            passed = false;
            try {
            /*第六步：run_test*/
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 0, "正在执行测试..");
                int verify = FileUtils.execCmdOutputVerify(this.projectDir + "/cmd/run_test.bat " + this.testTarget,
                        "[  PASSED  ]",
                        "FAILED TEST", this.logDir + "/testcases.log", "utf8");
                if (verify == 0) {
                    passed = true;
                }else if (verify == -1) {
                    experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "执行测试未通过");
                }else{
                    experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "执行测试时出错");
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "执行测试时出错");
            }
        }

        if (passed) {
            experimentStuService.updateStatusDesc(this.stuno, this.expno, 5, "通过");
        }
        try {
            /*第七步：删除临时文件夹*/
            FileUtils.execCmdOutput(this.projectDir + "/cmd/deldir.bat " + this.projectDir, logger, "utf8");
        } catch (IOException e) {
            logger.error("最后删除项目文件:" + e.getMessage());
        }

    }

    @Override
    public void run() {
        step1GenerateFiles();
        step++;
        step2SimilarityParser();
        step++;
        step3SimilarityCheck();
        step++;
        step4TestCases();
    }
}
