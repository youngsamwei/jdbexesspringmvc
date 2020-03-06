CREATE TABLE `experiment_stu_test_log`
(
    `experiment_stu_test_log_no` INT AUTO_INCREMENT,
    `stuno`                      BIGINT(11)   NOT NULL,
    `expno`                      INT(11)      NOT NULL,
    `content`                    mediumtext NULL,
    CONSTRAINT `experiment_stu_test_log_pk`
        PRIMARY KEY (`experiment_stu_test_log_no`),
    CONSTRAINT `experiment_stu_test_log_fk_expno` FOREIGN KEY (`expno`) REFERENCES `experiment` (`expno`),
    CONSTRAINT `experiment_stu_test_log_fk_stuno` FOREIGN KEY (`stuno`) REFERENCES `user` (`id`),
    CONSTRAINT `experiment_stu_test_log_pk` UNIQUE KEY (`expno`, `stuno`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    comment '保存执行测试的结果';
