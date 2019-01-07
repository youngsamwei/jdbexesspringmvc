package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.service.IJPlagService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by sam on 2019/1/7.
 */
@Service
public class JPlagServiceImpl implements IJPlagService {

    private String submitFilesRootDir;
    private Properties props = new Properties();

    public JPlagServiceImpl() {
        initProperties();
        this.submitFilesRootDir = props.getProperty("submitFilesRootDir");
    }

    @Override
    public double test(Long stuno, Long expno) {
        return 0;
    }

    private void initProperties() {
        // 读取配置文件
        Resource resource = new ClassPathResource("/config/checkmission.properties");

        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
