package cn.sdkd.ccse.commons.utils;

import org.slf4j.Logger;

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

    /*删除指定文件夹下所有文件和文件夹*/
    public static void delFiles(String sourcePath)  {
        File file = new File(sourcePath);
        if (!file.exists()){
            return;
        }
        if (file.isFile()){
            file.delete();
            return;
        }
        File[] filePath = file.listFiles();

        for (int i = 0; i < filePath.length; i++) {
            if (filePath[i].isDirectory()) {
                removeDir(filePath[i].getAbsolutePath());
            }else{
                filePath[i].delete();
            }
        }
    }

    /*删除文件夹以及内容，包括子文件夹和文件*/
    public static void removeDir(String dir){
        File file = new File(dir);
        if (file.exists()){
            if(file.isDirectory()){
                for (File f : file.listFiles()) {
                    if (f.isDirectory()) {
                        removeDir(f.getAbsolutePath());
                    }else{
                        f.delete();
                    }
                }
            }
            file.delete();
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
        Long start = System.currentTimeMillis();
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
                lastLine = line;
            }
            try{
                process.exitValue();
                break;
            }catch (IllegalThreadStateException e){
            }
                        /*超时强制退出*/
            if(System.currentTimeMillis() - start > 3000){
                break;
            }
        }

        brErr.close();
        brStd.close();
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

    public static void execCmdOutput(String cmd, final Logger logger, String encode) throws IOException {
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

    public static int execCmdOutputVerify(String cmd, String successFlag, String failedFlag, final Logger logger, String encode) throws IOException {
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
