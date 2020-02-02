package cn.sdkd.ccse.jdbexes.mapper;

import cn.sdkd.ccse.jdbexes.model.ExperimentStuSim;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ExperimentStuSimMapper extends BaseMapper<ExperimentStuSim> {
    @Select("SELECT expstuno, result " +
            "FROM experiment_stu_sim " +
            "WHERE expstuno=#{expstuno}")
    ExperimentStuSim selectById(@Param("expstuno") Long expstuno);

    @Insert("INSERT INTO experiment_stu_sim(expstuno, result) " +
            "VALUES(#{expstuno}, #{result}) " +
            "ON DUPLICATE KEY UPDATE result = #{result}")
    Integer updateResult(@Param("expstuno") Long expstuno, @Param("result") String result);
}
