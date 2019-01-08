package cn.sdkd.ccse.commons.utils;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 2019/1/7.
 * <p>
 * 从蓝墨云班课的作业导出中导入。
 */
public class ImportFromLanMo {
    private String rootDir;
    private String descFileName;
    private Long expno;

    private DBMysqlUtil mysqlUtil;

    public static void main(String[] args) {

        String exp2RootDir = "F:\\云班课作业 2018\\计算机16-1，2，3-数据库系统-课程设计_实验2_实现u_第3次";

        Long exp3no = 6L;
        String exp3RootDir = "F:\\云班课作业 2018\\计算机16-1，2，3-数据库系统-课程设计_实验3_实现D_第3次";
        ImportFromLanMo iflm = new ImportFromLanMo(exp3no, exp3RootDir,
                "评分详情.xlsx");

        try {
            iflm.run();

            iflm.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImportFromLanMo(Long expno, String rootDir, String descFileName) {
        this.expno = expno;
        this.rootDir = rootDir;
        this.descFileName = descFileName;

        mysqlUtil = new DBMysqlUtil();
    }

    public void run() throws Exception {
        List<TaskSubmitDesc> listTaskDesc = resolveTaskDesc();

        List<ExperimentFile> expFiles = getExpFiles();

//        for(String s : expFiles){
//            System.out.println(s);
//        }
        for (TaskSubmitDesc tsd : listTaskDesc) {
            Long stuno = getStuno(tsd.sno);
            if (stuno < 0) {
                System.out.println("not in mysql: " + tsd.sname + ", " + tsd.sno);
                continue;
            }
            insertExperimentStu(stuno, this.expno);
            System.out.println(tsd.sno + "," + tsd.sname + "," + tsd.submittime);

            Long expstuno = getExpstuno(expno, stuno);
            if (expstuno < 0) {
                System.out.println("错误:" + tsd.sno + " ," + tsd.sname + " 没选实验" + expno);
                continue;
            }

            insertExperimentFilesStu(expFiles, expstuno, tsd);

        }
    }

    private void insertExperimentFilesStu(List<ExperimentFile> expFiles, Long expstuno, TaskSubmitDesc tsd) throws SQLException, FileNotFoundException {
        for (ExperimentFile ef : expFiles) {
            /*查找最合适的文件*/
            String fileName = lookForFileName(this.rootDir + "/" + tsd.sno + "_" + tsd.sname + "/", ef.srcfilename);
            if (fileName.length() <= 0) {
                System.out.println(tsd.sno + "_" + tsd.sname + " 不存在" + ef.srcfilename);
                continue;
            }
            /*先判断是否存在相同日期的文件*/
            ResultSet rs = mysqlUtil.select("select expfilestuno from experiment_files_stu where fileno = "
                    + ef.fileno + " and expstuno="
                    + expstuno + " and submittime='"
                    + tsd.submittime + "';");
            if (!rs.next()) {
                Reader reader = new BufferedReader(new FileReader(fileName));
                mysqlUtil.executeUpdate("insert into experiment_files_stu(fileno, expstuno, file_content, submittime) values( "
                        + ef.fileno + ", "
                        + expstuno + ", ?, '"
                        + tsd.submittime
                        + "')", reader);
            }
        }

    }

    private String lookForFileName(String dir, String fileName) {

        File fdir = new File(dir);
        if (fdir.exists()) {
            for (File f : fdir.listFiles()) {
                if (f.isFile()) {
                    if (f.getName().toLowerCase().endsWith(fileName.toLowerCase())) {
                        return f.getAbsolutePath();
                    }
                }
            }
        }
        return "";
    }

    private Long getExpstuno(Long expno, Long stuno) throws SQLException {
        ResultSet rs = mysqlUtil.select("select expstuno from experiment_stu where expno = " + expno + " and stuno =" + stuno);
        if (rs.next()) {
            return rs.getLong("expstuno");
        } else {
            return -1L;
        }
    }

    private Long getStuno(String sno) throws SQLException {
        ResultSet rs = mysqlUtil.select("select id from user where login_name = '" + sno + "'");

        if (rs.next()) {
            return rs.getLong("id");
        } else {
            return -1L;
        }
    }

    /*在学生实验表中增加*/
    public void insertExperimentStu(Long stuno, Long expno) throws Exception {
        ResultSet rs = mysqlUtil.select("select expstuno from experiment_stu where stuno = " + stuno + " and expno=" + expno);
        if (!rs.next()) {
            mysqlUtil.executeupdate("insert into experiment_stu(expno, stuno, selectedtime) values(" + expno + "," + stuno + ",'2018-09-01 00:00:00')");
        }
    }

    public void close() {
        mysqlUtil.close();
    }

    public List<ExperimentFile> getExpFiles() throws SQLException {
        List<ExperimentFile> files = new ArrayList<ExperimentFile>();

        ResultSet rs = mysqlUtil.select("select fileno,srcfilename from experiment_files where expno =" + this.expno);
        while (rs.next()) {
            files.add(new ExperimentFile(rs.getLong("fileno"), rs.getString("srcfilename")));
        }
        return files;
    }

    private List<TaskSubmitDesc> resolveTaskDesc() throws IOException, InvalidFormatException {
        List<TaskSubmitDesc> listTaskDesc = new ArrayList<TaskSubmitDesc>();
        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(new File(this.rootDir + "/" + this.descFileName)));

        Workbook workbook = WorkbookFactory.create(bis);
        int sheetCount = workbook.getNumberOfSheets(); // Sheet的数量
        if (sheetCount > 0) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows(); // 获取总行数
            if (rowCount > 2) {
                    /* 第一行作为标题行：包含学号，姓名，交作业时间等字段 */
                Row row = sheet.getRow(0);
                int snono = -1, snameno = -1, submittimeno = -1;
                int cellCount = row.getPhysicalNumberOfCells();
                for (int c = 0; c < cellCount; c++) {
                    Cell cell = row.getCell(c);
                    String cellValue = cell.getStringCellValue();
                    if (cellValue.equalsIgnoreCase("学号")) {
                        snono = c;
                    } else if (cellValue.equalsIgnoreCase("姓名")) {
                        snameno = c;
                    } else if (cellValue.equalsIgnoreCase("交作业时间")) {
                        submittimeno = c;
                    }
                }
                for (int r = 1; r < rowCount; r++) {
                    Row arow = sheet.getRow(r);
                    String sno = "", sname = "", submittime = "";
                    Cell snocell = arow.getCell(snono);
                    Cell snamecell = arow.getCell(snameno);
                    Cell submittimecell = arow.getCell(submittimeno);

                    sno = snocell.getStringCellValue();
                    sname = snamecell.getStringCellValue();
                    submittime = submittimecell.getStringCellValue();

                    if ((sno != null) && (sname != null)) {
                        TaskSubmitDesc tsd = new TaskSubmitDesc(sno, sname, submittime);
                        listTaskDesc.add(tsd);
                    }
                }
            }
        }
        return listTaskDesc;
    }
}

class TaskSubmitDesc {
    String sno;
    String sname;
    String submittime;

    public TaskSubmitDesc(String sno, String sname, String submittime) {
        this.sno = sno;
        this.sname = sname;
        this.submittime = submittime;
    }
}

class ExperimentFile {
    Long fileno;
    String srcfilename;

    public ExperimentFile(Long fileno, String srcfilename) {
        this.fileno = fileno;
        this.srcfilename = srcfilename;
    }
}