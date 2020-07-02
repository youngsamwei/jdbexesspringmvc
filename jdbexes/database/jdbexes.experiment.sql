IF EXISTS(
    SELECT table_name
    FROM INFORMATION_SCHEMA.TABLES
    WHERE `table_schema` = 'jdbexes' AND `table_name` LIKE 'experiment')

THEN
    ALTER TABLE jdbexes.experiment
    ADD docker_image VARCHAR(64) NULL COMMENT 'Docker 镜像' AFTER expname;
    ALTER TABLE experiment
    ADD memory_limit INT NULL COMMENT '内存限制（单位：MiB）' AFTER testtarget;
    ALTER TABLE experiment
    ADD timeout INT NULL COMMENT '超时（单位：秒）'  AFTER memory_limit;

ELSE
    CREATE TABLE experiment(
        expno           INT AUTO_INCREMENT  PRIMARY KEY,
        expname         VARCHAR(64)         NOT NULL,
        docker_image    VARCHAR(64)         NULL,
        testtarget      VARCHAR(64)         NULL,
        memory_limit    INT                 NULL,
        timeout         INT                 NULL,
        is_Open         TINYINT DEFAULT 1   NULL
    );

END IF;

UPDATE jdbexes.experiment
SET docker_image = 'dongmendb-test', memory_limit = 512, timeout = 30
WHERE docker_image IS NULL;