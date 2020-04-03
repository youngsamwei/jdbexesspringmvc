package cn.sdkd.ccse.jdbexes.mapper;

import  cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by Sam on 2017/7/13.
 */
public interface ExperimentFilesStuMapper extends BaseMapper<ExperimentFilesStu> {
    @Select("select es.expno, es.expstuno, es.stuno, ef.fileno, ef.srcfilename, ef.dstfilename, ef.objfilename, efsl.file_content, efsl.submittime, e.docker_image, e.testtarget, e.memory_limit, e.timeout" +
            " from experiment_stu es join experiment_files ef on es.expno = ef.expno " +
            " join experiment_files_stu_latest efsl on efsl.expstuno = es.expstuno and efsl.fileno = ef.fileno " +
            " join experiment e on es.expno=e.expno " +
            " where es.expno=#{expno} and es.stuno = #{stuno}")
    List<ExperimentFilesStuVO> selectFilesLatest(@Param("stuno") Long stuno, @Param("expno") Long expno);

    boolean refreshCache();
}
