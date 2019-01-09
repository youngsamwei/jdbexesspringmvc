package cn.sdkd.ccse.jdbexes.checkmission;

import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import cn.sdkd.ccse.jdbexes.service.IJPlagService;
import jplag.ExitException;
import jplag.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 检验实验作业的相似程度。
 * Created by sam on 2019/1/9.
 */
public class JPlagJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JPlagJob.class);
    /* 学生编号*/
    private Long stuno;
    /*实验编号*/
    private Long expno;

    private String sno;
    private String sname;
    private Submission submission;
    String submitFilesRootDir;
    IJPlagService jPlagService;


    public JPlagJob(Long stuno, Long expno, String sno, String sname, String submitFilesRootDir, IJPlagService jPlagService) {
        this.stuno = stuno;
        this.expno = expno;
        this.sno = sno;
        this.sname = sname;
        this.submitFilesRootDir = submitFilesRootDir;
        this.jPlagService = jPlagService;
    }

    @Override
    public void run() {
        try {
            parse();
            jPlagService.compare(this.submission);
        } catch (ExitException e) {
            logger.error(e.getMessage());
        }
    }

    /*解析当前作业*/
    private void parse() throws ExitException {
        File f = new File(this.submitFilesRootDir + "/"  + sno + "_" + sname + "/");
        this.submission = new Submission(sno + "_" + sname, f, true, jPlagService.getProgram(), jPlagService.getProgram().get_language());
        submission.parse();

        jPlagService.putSubmission(sno + "_" + sname, submission);
    }

}
