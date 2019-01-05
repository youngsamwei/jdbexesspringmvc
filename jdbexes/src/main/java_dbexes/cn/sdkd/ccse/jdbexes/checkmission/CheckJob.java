package cn.sdkd.ccse.jdbexes.checkmission;

import cn.sdkd.ccse.commons.utils.FileUtils;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
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

    private IExperimentFilesStuService experimentFilesStuService;

    private List<ExperimentFilesStuVO> experimentFilesStuVOList;

    public CheckJob(Long stuno, Long expno, IExperimentFilesStuService experimentFilesStuService, String srcDir, String projectDir, String originalProjectRootDir) {
        this.stuno = stuno;
        this.expno = expno;
        this.experimentFilesStuService = experimentFilesStuService;
        experimentFilesStuVOList = experimentFilesStuService.selectFilesLatest(this.stuno, this.expno);

        this.testTarget = experimentFilesStuVOList.get(0).getTesttarget();

        this.srcDir = srcDir;
        this.projectDir = projectDir;
        this.originalProjectRootDir = originalProjectRootDir;
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

        /*第一步：复制项目文件到学生个人文件夹*/
        try {
            FileUtils.copyDir(originalProjectRootDir, projectDir);
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }

        /*第二步：复制作业文件到学生个人文件夹*/
        for (ExperimentFilesStuVO efsv : experimentFilesStuVOList) {
            try {
                FileUtils.copyFile(this.srcDir + "/" + efsv.getSrcfilename(), this.projectDir + "/" + efsv.getDstfilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Runtime runtime = Runtime.getRuntime();

        try {

            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(runtime.exec(this.projectDir + "/cmd/refresh.bat" ).getInputStream(), "utf8"));
            while ((line = br.readLine()) != null) {
                logger.debug(line);
            }
            br = new BufferedReader(new InputStreamReader(runtime.exec(this.projectDir + "/cmd/clean.bat").getInputStream(), "utf8"));
            while ((line = br.readLine()) != null) {
                logger.debug(line);
            }
            br = new BufferedReader(new InputStreamReader(runtime.exec(this.projectDir + "/cmd/build.bat " + this.testTarget).getInputStream(), "utf8"));
            while ((line = br.readLine()) != null) {
                logger.debug(line);
            }
            br = new BufferedReader(new InputStreamReader(runtime.exec( this.projectDir + "/cmd/run_test.bat " +  this.testTarget).getInputStream(), "utf8"));
            while ((line = br.readLine()) != null) {
                logger.debug(line);
            }

        } catch (IOException e) {
           logger.debug(e.getMessage());
        }
        /*第三步：clean*/



        /*第四步：编译*/

        /*第五步：执行*/
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
