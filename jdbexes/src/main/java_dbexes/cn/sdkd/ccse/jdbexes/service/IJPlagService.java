package cn.sdkd.ccse.jdbexes.service;

/**
 * Created by sam on 2019/1/7.
 */
public interface IJPlagService {
    /*测试指定学生的指定实验与其他学生的指定实验的相似程度，返回最高的相似度*/
    double test(Long stuno, Long expno);
}
