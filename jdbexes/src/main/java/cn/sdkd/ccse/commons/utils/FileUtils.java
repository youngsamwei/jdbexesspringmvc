package cn.sdkd.ccse.commons.utils;

import org.apache.ibatis.logging.Log;

import java.io.*;

/**
 * Created by sam on 2019/1/5.
 */
public class FileUtils {

    public static void copyDir(String sourcePath, String newPath) throws IOException {
        File file = new File(sourcePath);
        String[] filePath = file.list();

        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdir();
        }

        for (int i = 0; i < filePath.length; i++) {

            if ((new File(sourcePath + file.separator + filePath[i])).isDirectory()) {
                copyDir(sourcePath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
            }

            if (new File(sourcePath + file.separator + filePath[i]).isFile()) {
                copyFile(sourcePath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
            }

        }
    }

    public static void copyFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        File file = new File(newPath);
        FileInputStream in = new FileInputStream(oldFile);
        FileOutputStream out = new FileOutputStream(file);
        ;

        byte[] buffer = new byte[2097152];
        int readByte = 0;
        while ((readByte = in.read(buffer)) != -1) {
            out.write(buffer, 0, readByte);
        }

        in.close();
        out.close();
    }

    public static void execCmdOutput(String cmd, String filename, String encode) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        String line;
        Process process  = runtime.exec(cmd);
        BufferedReader brStd = new BufferedReader(new InputStreamReader(process.getInputStream(), encode));
        BufferedReader brErr = new BufferedReader(new InputStreamReader(process.getErrorStream(), encode));
        while(true){
            if(brErr.ready()){
                line = brErr.readLine();
                bw.write(line);
                bw.newLine();
            }
            if(brStd.ready()){
                line = brStd.readLine();
                bw.write(line);
                bw.newLine();
            }
            try{
                process.exitValue();
                break;
            }catch (IllegalThreadStateException e){
            }
        }

        brErr.close();
        brStd.close();
        bw.flush();
        bw.close();
    }

    public static int execCmdOutputVerify(String cmd, String successFlag, String failedFlag, String filename, String encode) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        String line, lastLine = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(runtime.exec(cmd).getInputStream(), encode));
        long start  = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            bw.write(line);
            bw.newLine();
            lastLine = line;

            /*超时强制退出*/
            if(System.currentTimeMillis() - start > 3000){
                break;
            }
        }
        br.close();
        bw.flush();
        bw.close();

        if (lastLine.contains(successFlag)) {
            return 0;
        } else if (lastLine.contains(failedFlag)) {
            return -1;
        } else {
            return -2;
        }
    }

    public static void execCmdOutput(String cmd, final Log logger, String encode) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String line;

        final Process process = runtime.exec(cmd);
        BufferedReader brStd = new BufferedReader(new InputStreamReader(process.getInputStream(), encode));
        while (true) {
            try {
                if (brStd.ready()) {
                    line = brStd.readLine();
                    logger.debug(line);
                }
                try {
                    int exitv = process.exitValue();
                    logger.debug("process exitValue : " + exitv);
                    break;
                } catch (IllegalThreadStateException e) {
                            /*进程未结束*/
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
                break;
            }
        }

        process.destroy();
        brStd.close();
        ;
    }

    public static int execCmdOutputVerify(String cmd, String successFlag, String failedFlag, final Log logger, String encode) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String line, lastLine = "";
        long start = System.currentTimeMillis();

        final Process process = runtime.exec(cmd);
        BufferedReader brStd = new BufferedReader(new InputStreamReader(process.getInputStream(), encode));
        /*暂时 没有使用brErr*/
        BufferedReader brErr = new BufferedReader(new InputStreamReader(process.getErrorStream(), encode));

        while (true) {
            try {
                if (brStd.ready()) {
                    line = brStd.readLine();
                    logger.debug(line);
                    lastLine = line;
                }

                try {
                    int exitv = process.exitValue();
                    logger.debug("process exitValue : " + exitv);
                    break;
                } catch (IllegalThreadStateException e) {
                            /*进程未结束*/
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
                break;
            }
        }

        process.destroy();
        brStd.close();

        if (lastLine.contains(successFlag)) {
            return 0;
        } else if (lastLine.contains(failedFlag)) {
            return -1;
        } else {
            return -2;
        }
    }
}
