package cn.sdkd.ccse.jdbexes.checkmission;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.wangzhixuan.model.vo.UserVo;
import com.wangzhixuan.service.IUserService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 测试实验作业是否通过测试用例
 */
public class CheckJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CheckJob.class);

    DockerClient dockerClient;

    private final Long stuno; // 学生编号
    private final Long expno; // 实验编号

    private final IUserService userService;
    private final IExperimentFilesStuService experimentFilesStuService;
    private final IExperimentStuService experimentStuService;
    private final IExperimentService experimentService;

    public CheckJob(String dockerHost, Long stuno, Long expno, IUserService userService,
                    IExperimentFilesStuService experimentFilesStuService, IExperimentStuService experimentStuService, IExperimentService experimentService) {

        this.dockerClient = DockerClientBuilder.getInstance(dockerHost).build();

        this.stuno = stuno;
        this.expno = expno;
        this.userService = userService;

        this.experimentFilesStuService = experimentFilesStuService;
        this.experimentStuService = experimentStuService;
        this.experimentService = experimentService;
    }

    @Override
    public void run() {

        UserVo u = userService.selectVoById(stuno);
        String sno = u.getLoginName();
        String sname = u.getName();
        logger.info("Running test for (" + sno + ", " + sname + ").");

        experimentStuService.updateStatusDesc(stuno, expno, -1, "测试中");

        List<ExperimentFilesStuVO> experimentFilesStuVOList = experimentFilesStuService.selectFilesLatest(this.stuno, this.expno);

        Experiment experiment = experimentService.selectById(this.expno);
        String image_name = experiment.getDocker_image();
        String testTarget = experiment.getTesttarget();
        long memory_limit = experiment.getMemory_limit() * 1024 * 1024;
        int timeout = experiment.getTimeout();

        // Create container
        CreateContainerResponse container = dockerClient
                .createContainerCmd(image_name)
                .withStdinOpen(true)
                .exec();

        dockerClient.updateContainerCmd(container.getId())
                .withMemory(memory_limit)
                .withMemorySwap(memory_limit)
                .exec();

        // Start container
        dockerClient.startContainerCmd(container.getId()).exec();

        // Copy file into container
        try {
            Path tempDir = Files.createTempDirectory("dongmendb");
            for (ExperimentFilesStuVO efsv : experimentFilesStuVOList) {
                File file = generateTempFile(tempDir, efsv.getSrcfilename(), efsv.getFile_content());
                String srcDir = file.getAbsolutePath();
                String distDir = FilenameUtils.separatorsToUnix(
                        FilenameUtils.concat(
                                "/workspace",
                                FilenameUtils.getPath(efsv.getDstfilename()))
                );
                copyFile(container, srcDir, distDir);
            }
        } catch (IOException e) {
            logger.error(e.toString());
        }

        // Build test
        int code_build = buildTest(container, testTarget);
        if (code_build != 0) {
            experimentStuService.updateStatusDesc(this.stuno, this.expno, 2, "编译时错误(" + code_build + ")");
            return;
        }

        // Run test
        int code_test = runTest(container, testTarget, timeout);
        switch (code_test) {
            case 0:
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 5, "通过");
                break;
            case 1:
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "结果错误");
                break;
            case 124:
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "运行超时");
                break;
            default:
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "运行时错误(" + code_test + ")");
        }

        // Stop and remove container
        dockerClient.killContainerCmd(container.getId()).exec();
        dockerClient.removeContainerCmd(container.getId()).exec();

        logger.info("Test for (" + sno + ", " + sname + ") Finished.");
    }


    /**
     * 将数据写入临时文件
     *
     * @param filename 文件名
     * @param contect  文件内容
     * @return 临时文件
     */
    private File generateTempFile(Path dir, String filename, String contect) throws IOException {
        String fullpath = FilenameUtils.concat(dir.toString(), filename);
        File file = new File(fullpath);

        try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            op.append(contect);
            op.flush();
        }

        return file;
    }

    /**
     * 将文件拷贝到容器中指定位置
     *
     * @param container 容器
     * @param from      源文件路径
     * @param to        目标文件路径
     */
    private void copyFile(CreateContainerResponse container, String from, String to) {
        // Copy temp file into container
        dockerClient.copyArchiveToContainerCmd(container.getId())
                .withHostResource(from)
                .withRemotePath(to)
                .withNoOverwriteDirNonDir(false)
                .exec();
    }

    /**
     * 构建测试可执行文件
     *
     * @param container 容器
     * @param target    cmake 编译目标
     * @return 返回代码，-1 表示命令执行出错
     */
    private int buildTest(CreateContainerResponse container, String target) {
        // Build test
        String[] buildCmd = new String[]{
                "bash",
                "-c",
                "cd /workspace/ && cmake --build /workspace/cmake-build-debug --target " + target
        };

        ExecCreateCmdResponse buildExec = dockerClient.execCreateCmd(
                container.getId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(buildCmd)
                .exec();
        OutputStream out = new ByteArrayOutputStream();
        try {
            dockerClient.execStartCmd(buildExec.getId()).withDetach(false)
                    .exec(new ExecStartResultCallback(out, out)).awaitCompletion();
        } catch (InterruptedException e) {
            logger.warn(e.toString());
            return -1;
        }

        InspectExecResponse response = dockerClient.inspectExecCmd(buildExec.getId()).exec();
        Integer code = response.getExitCode();

        experimentStuService.updateCheckLog(stuno, expno, out.toString());
        return code;
    }

    /**
     * 执行测试
     *
     * @param container 容器
     * @param target    cmake 编译目标
     * @return 返回代码，-1 表示命令执行出错
     */
    private int runTest(CreateContainerResponse container, String target, int timeout) {
        ExecCreateCmdResponse testExec = dockerClient.execCreateCmd(
                container.getId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd("/workspace/bin/" + target)
                .exec();
        OutputStream out = new ByteArrayOutputStream();
        try {
            dockerClient.execStartCmd(testExec.getId()).withDetach(false)
                    .exec(new ExecStartResultCallback(out, out)).awaitCompletion(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn(e.toString());
            return -1;
        }

        InspectExecResponse response = dockerClient.inspectExecCmd(testExec.getId()).exec();
        Integer code = response.getExitCode();

        if (code == null) {
            experimentStuService.updateCheckLog(stuno, expno, "");
            return 124;
        }

        experimentStuService.updateCheckLog(stuno, expno, out.toString());
        return code;
    }
}
