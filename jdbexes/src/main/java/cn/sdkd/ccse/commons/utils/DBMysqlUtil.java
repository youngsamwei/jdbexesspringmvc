package cn.sdkd.ccse.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Created by sam on 2019/1/7.
 */


public class DBMysqlUtil {
    private Connection conn = null;
    private PreparedStatement ps = null;
    private ResultSet rs = null;
    private String dbDriver = null;
    private String dbConnectionURL = null;
    private String dbUsername = null;
    private String dbPassword = null;
    private Properties props;
    private Logger logger = LoggerFactory.getLogger(DBMysqlUtil.class);

    public DBMysqlUtil() {
        Resource resource = new ClassPathResource("/config/application.properties");

        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
            dbDriver = "com.mysql.jdbc.Driver";
            dbConnectionURL = props.getProperty("db.master.url");
            dbUsername = props.getProperty("db.master.user");
            dbPassword = props.getProperty("db.master.password");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public DBMysqlUtil(String dbDriver, String dbConnectionURL, String dbUsername, String dbPassword) {
        this.dbDriver = dbDriver;
        this.dbConnectionURL = dbConnectionURL;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    /**
     * 功能：获取数据库连接
     */
    private Connection getConnection() {
//        System.out.println("连接地址：" + dbConnectionURL);
//        System.out.println("用户名：" + dbUsername);
//        System.out.println("密码：" + dbPassword);
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName(dbDriver);
                conn = DriverManager.getConnection(dbConnectionURL, dbUsername,
                        dbPassword);
            }
//            logger.info("数据库连接成功");
        } catch (Exception e) {
            logger.error("Error: DbUtil.getConnection() 获得数据库链接失败.\r\n链接类型:"
                    + dbDriver + "\r\n链接URL:" + dbConnectionURL + "\r\n链接用户:"
                    + dbUsername + "\r\n链接密码:" + dbPassword, e);
        }
        return conn;
    }

    /**
     * 功能：执行查询语句
     */
    public ResultSet select(String sql) {
//        logger.info("Exec select sql: " + sql);
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
        } catch (SQLException e) {
            logger.error("查询数据异常:" + e.getMessage());
        }
        return rs;

    }

    /**
     * 功能：执行查询语句，获取记录数
     */
    public int getRecordCount(String sql) {
//        logger.info("Exec getRecordCount sql: " + sql);
        int counter = 0;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery(sql);
            while (rs.next()) {
                counter++;
            }
        } catch (SQLException e) {
            logger.error("执行DbUtil.getRecordCount()方法发生异常，异常信息：", e);
        } finally {
            close();
        }
        System.out.println("counter总数：" + counter);
        return counter;
    }

    /*更新文件内容*/
    public int executeUpdate(String sql, Reader reader){
//        logger.info("Exec update sql:" + sql);
        int num = 0;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setCharacterStream(1, reader);
            num = ps.executeUpdate();
        } catch (SQLException sqle) {
            logger.error("insert/update/delete  data Exception: " +
                    sqle.getMessage());
        } finally {
            close();
        }
//        System.out.println("影响条数：" + num);
        return num;
    }
    /**
     * 功能:针对单条记录执行更新操作(新增、修改、删除)
     */
    public int executeupdate(String sql) throws Exception {
//        logger.info("Exec update sql:" + sql);
        int num = 0;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            num = ps.executeUpdate();
        } catch (SQLException sqle) {
            logger.error("insert/update/delete  data Exception: " +
                    sqle.getMessage());
        } finally {
            close();
        }
//        System.out.println("影响条数：" + num);
        return num;
    }

    /**
     * 功能:批量执行SQL(update或delete)
     *
     * @param sqlList sql语句集合
     */
    public int executeBatch(List<String> sqlList) {
        int result = 0;
        for (String sql : sqlList) {
            try {
                result += executeupdate(sql);
            } catch (Exception e) {
                logger.error("查询异常：" + e.getMessage());
            }
        }
//        System.out.println("executeBatch Result:" + result);
        return result;
    }

    /**
     * 功能:关闭数据库的连接
     */
    public void close() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
//            if (conn != null) {
//                conn.close();
//            }
//            logger.info("关闭数据库连接成功");
        } catch (Exception e) {
            logger.error("执行DbUtil.close()方法发生异常，异常信息：", e);
        }
    }

    public void closeConn(){
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
//            logger.info("关闭数据库连接成功");
        } catch (Exception e) {
            logger.error("执行DbUtil.close()方法发生异常，异常信息：", e);
        }
    }
}
