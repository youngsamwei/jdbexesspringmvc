package cn.sdkd.ccse.jdbexes.mapper.test;

import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestFilesMapper;
import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by sam on 2019/1/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-config.xml"})
public class ExperimentStuTestMapperTest {
    private static Logger logger = LoggerFactory.getLogger(ExperimentStuTestMapperTest.class);
    @Autowired
    ExperimentStuTestMapper experimentStuTestMapper;
    @Autowired
    ExperimentStuTestFilesMapper experimentStuTestFilesMapper;

    @Test
    public void test(){

    }
    @Test
    public void insertExperimentStuTest() {
        ExperimentStuTest est = new ExperimentStuTest();
        est.setExpno(1);
        est.setSimdesc("相似度过高");
        est.setSimstatus(1);
        est.setStuno(130);
        est.setTestdesc("测试通过");
        est.setTeststatus(1);
        est.setTesttime(Timestamp.valueOf("2018-11-8 13:34:00"));
        Integer r = experimentStuTestMapper.insert(est);
        assertEquals(1, r.longValue());

        ExperimentStuTest est1 = experimentStuTestMapper.selectLatestByUser(130L);
        assertNotNull(est1);
        assertEquals(est1.getExpno().longValue(), 1);
        assertEquals(est1.getTestdesc(), "测试通过");
        assertEquals(est1.getSimdesc(), "相似度过高");

        Integer r1 = experimentStuTestMapper.deleteById(est1.getExperiment_stu_test_no());
        assertEquals(1, r1.longValue());
    }
    @Test
    public void selectListByUser() {
        for (int i = 0; i < 10; i++) {
            ExperimentStuTest est = new ExperimentStuTest();
            est.setExpno(i);
            est.setSimdesc("相似度过高");
            est.setSimstatus(1);
            est.setStuno(130);
            est.setTestdesc("测试通过");
            est.setTeststatus(1);
            est.setTesttime(Timestamp.valueOf("2018-11-8 13:34:00"));
            Integer r = experimentStuTestMapper.insert(est);
        }

        List<ExperimentStuTest> lse = experimentStuTestMapper.selectListByUser(130L);
        assertEquals(lse.size(), 10);
        for (ExperimentStuTest est : lse){
            assertEquals(est.getSimdesc(), "相似度过高");
            assertEquals(est.getTestdesc(), "测试通过");
            experimentStuTestMapper.deleteById(est.getExperiment_stu_test_no());
        }
        lse = experimentStuTestMapper.selectListByUser(130L);
        assertEquals(lse.size(), 0);
    }

    @Test
    public void insertExperimentStuTestFiles(){
        ExperimentStuTestFiles estf0 = new ExperimentStuTestFiles(10, 1, 1,1);

        Integer  r = experimentStuTestFilesMapper.insert(estf0);
        assertEquals(r.longValue(), 1);

        List<ExperimentStuTestFiles> lsestfs = experimentStuTestFilesMapper.selectListByTestno(10L);
        assertEquals(lsestfs.size(), 1);

        ExperimentStuTestFiles estf1 = new ExperimentStuTestFiles(10, 2, 2,1);
        Integer  r1 = experimentStuTestFilesMapper.insert(estf1);
        assertEquals(r1.longValue(), 1);
        ExperimentStuTestFiles estf2 = new ExperimentStuTestFiles(10, 3, 3,1);
        Integer  r2 = experimentStuTestFilesMapper.insert(estf2);
        assertEquals(r2.longValue(), 1);

        List<ExperimentStuTestFiles> lsestfs1 = experimentStuTestFilesMapper.selectListByTestno(10L);
        assertEquals(lsestfs1.size(), 3);

        for (ExperimentStuTestFiles estf : lsestfs1){
           r = experimentStuTestFilesMapper.deleteById(estf.getExperiment_stu_test_files_no());
           assertEquals(r.longValue(), 1);
        }

        List<ExperimentStuTestFiles> lsestfs2 = experimentStuTestFilesMapper.selectListByTestno(10L);
        assertEquals(lsestfs2.size(), 0);


    }
}
