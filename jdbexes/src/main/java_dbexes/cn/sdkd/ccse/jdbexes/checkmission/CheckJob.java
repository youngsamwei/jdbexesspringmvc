package cn.sdkd.ccse.jdbexes.checkmission;

import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 测试实验作业是否通过测试用例
 */
public class CheckJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CheckJob.class);

    DockerClient dockerClient;
    String image_name;

    private Long stuno; // 学生编号
    private Long expno; // 实验编号

    private String sno;
    private String sname;

    private IExperimentFilesStuService experimentFilesStuService;
    private IExperimentStuService experimentStuService;

    public CheckJob(String dockerHost, String image_name, Long stuno, Long expno, String sno, String sname,
                    IExperimentFilesStuService experimentFilesStuService, IExperimentStuService experimentStuService) {

        this.dockerClient = DockerClientBuilder.getInstance(dockerHost).build();
        this.image_name = image_name;

        this.stuno = stuno;
        this.expno = expno;
        this.sno = sno;
        this.sname = sname;

        this.experimentFilesStuService = experimentFilesStuService;
        this.experimentStuService = experimentStuService;

    }

    @Override
    public void run() {
        logger.info("Running test for (" + sno + ", " + sname + ").");

        List<ExperimentFilesStuVO> experimentFilesStuVOList = experimentFilesStuService.selectFilesLatest(this.stuno, this.expno);
        String testTarget = experimentFilesStuVOList.get(0).getTesttarget();

        // Create container
        CreateContainerResponse container = dockerClient
                .createContainerCmd(image_name)
                .withStdinOpen(true)
                .exec();

        // Start container
        dockerClient.startContainerCmd(container.getId()).exec();

        // Copy file into container
        try {
            for (ExperimentFilesStuVO efsv : experimentFilesStuVOList) {
                File file = generateTempFile(efsv.getSrcfilename(), efsv.getFile_content());
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
        int code_test = runTest(container, testTarget);
        switch (code_test) {
            case 0:
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 5, "通过");
                break;
            case 1:
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "结果错误");
                break;
            default:
                experimentStuService.updateStatusDesc(this.stuno, this.expno, 3, "运行时错误(" + code_test + ")");
        }

        // Stop and remove container
        dockerClient.killContainerCmd(container.getId()).exec();
        dockerClient.removeContainerCmd(container.getId()).exec();
    }


    /**
     * 将数据写入临时文件
     *
     * @param filename 文件名
     * @param contect  文件内容
     * @return 临时文件
     */
    private File generateTempFile(String filename, String contect) throws IOException {
        Path dir = Files.createTempDirectory("dongmendb");
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
        if (code != 0) {
            String log = out.toString() + "\n\nreturn code: " + code.toString();
            experimentStuService.updateCheckLog(stuno, expno, log);
        }

        return code;
    }

    /**
     * 执行测试
     *
     * @param container 容器
     * @param target    cmake 编译目标
     * @return 返回代码，-1 表示命令执行出错
     */
    private int runTest(CreateContainerResponse container, String target) {
        ExecCreateCmdResponse testExec = dockerClient.execCreateCmd(
                container.getId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd("/workspace/bin/exp_01_03_select_test")
                .exec();
        OutputStream out = new ByteArrayOutputStream();
        try {
            dockerClient.execStartCmd(testExec.getId()).withDetach(false)
                    .exec(new ExecStartResultCallback(out, out)).awaitCompletion();
        } catch (InterruptedException e) {
            logger.warn(e.toString());
            return -1;
        }

        InspectExecResponse response = dockerClient.inspectExecCmd(testExec.getId()).exec();
        Integer code = response.getExitCode();
        if (code != 0) {
            String log = out.toString() + "\n\nreturn code: " + code.toString();
            experimentStuService.updateCheckLog(stuno, expno, log);
        }

        return code;
    }
}
