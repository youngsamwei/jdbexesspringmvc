CREATE TABLE `experiment_stu_test_latest`
(
    `experiment_stu_test_latest_no` INT AUTO_INCREMENT,
    `stuno`                      BIGINT(11)   NOT NULL,
    `expno`                      INT(11)      NOT NULL,
    `experiment_stu_test_no`     INT(11) NULL,
    CONSTRAINT `experiment_stu_test_latest_pk` PRIMARY KEY (`experiment_stu_test_latest_no`),
    CONSTRAINT `experiment_stu_test_latest_fk_expno` FOREIGN KEY (`expno`) REFERENCES `experiment` (`expno`),
    CONSTRAINT `experiment_stu_test_latest_fk_stuno` FOREIGN KEY (`stuno`) REFERENCES `user` (`id`),
    CONSTRAINT `experiment_stu_test_latest_fk_experiment_stu_test_no` FOREIGN KEY (`experiment_stu_test_no`) REFERENCES `experiment_stu_test` (`experiment_stu_test_no`),
    CONSTRAINT `experiment_stu_test_latest_uk` UNIQUE KEY (`expno`, `stuno`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    comment ''保存最新测试的编号'';
