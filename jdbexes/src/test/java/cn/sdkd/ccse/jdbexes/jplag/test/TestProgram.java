package cn.sdkd.ccse.jdbexes.jplag.test;

import jplag.*;
import jplag.options.Options;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sam on 2019/1/7.
 */
public class TestProgram  {
    private ConcurrentHashMap<String, Submission> submissions;
    private jplag.options.Options options;
    private Program program;
    private GSTiling gsTiling;

    public TestProgram(Options options) throws ExitException {
        this.options = options;
        this.program = new Program(options);
        gsTiling = new GSTiling(this.program);
    }

    private void jdbCreateSubmissions() throws ExitException {
        submissions = new ConcurrentHashMap<String, Submission>();
        File f = new File(options.root_dir);

        File[] list = f.listFiles();
        for (int i = 0; i < list.length; i++) {

            Submission s = new Submission(list[i].getName(), list[i], true/*options.read_subdirs*/, this.program, this.program.get_language());
            s.parse();
            submissions.put(list[i].getName(), s);

        }
    }

    private void jdbCompare() throws ExitException {

        Submission s = submissions.get("201001060101_测试账号1");
        for (Map.Entry<String, Submission> entry : submissions.entrySet()){
            AllMatches match = this.gsTiling.compare(entry.getValue(), s);
            System.out.println("Comparing " + s.name + "-" + entry.getValue().name + ": " + match.percent());
        }
    }

    public void jdbRun() throws ExitException {
        jdbCreateSubmissions();
        jdbCompare();
    }
}
