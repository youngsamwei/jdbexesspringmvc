package cn.sdkd.ccse.jdbexes.service;

import org.apache.ibatis.annotations.Param;

import java.util.Map;
import java.util.Set;

/**
 * Created by sam on 2019/1/14.
 */
public interface INeo4jService {

    Object getStudents();
    Object getExperiments();

    Object getSimilarities(@Param("sim") float sim);
}
