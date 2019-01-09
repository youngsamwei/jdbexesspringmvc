package cn.sdkd.ccse.jdbexes.jplag.test;

import jplag.ExitException;
import jplag.options.CommandLineOptions;

/**
 * Created by sam on 2018/12/26.
 */
public class TestJPalg {


    static public void testJPlag(String[] paras){
        try {
            CommandLineOptions options = new CommandLineOptions(paras, null);
            TestProgram program = new TestProgram(options);

            System.out.println("initialize ok");
            program.jdbRun();
        }
        catch(ExitException ex) {
            System.out.println("Error: "+ex.getReport());
            System.exit(1);
        }
    }
    public static void main(String[] args) {

        String s = "F:\\云班课作业 2018\\计算机16-1，2，3-数据库系统-课程设计_实验2_实现u_第13次";

        s = "F:\\jdbexes_files\\submit_files\\";
        String[] paras = {
                "-l","c/c++",
                "-s",s,
                "-clustertype", "min"
        };

        testJPlag(paras);
        System.out.println("hello world");
    }
}
