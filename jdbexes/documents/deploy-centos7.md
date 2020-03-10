# 在 CentOS 7 上部署

## Setup Oracle Java 8

You can follow the link to install jre 8: [How To Install Java on CentOS and Fedora | DigitalOcean](https://www.digitalocean.com/community/tutorials/how-to-install-java-on-centos-and-fedora#install-oracle-java-8).

1. Download oracle jre .rpm file form [here](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html), login needed.
2. Copy .rpm file to server using scp command.
3. Run `yum localinstall jre-8u161-linux-x64.rpm`, enter y then RETURN to continue with the installation.
4. Check installation by `java -version`.

## Setup MySql 5.7

You can follow the link to install mysql 5.7: [How To Install MySQL on CentOS 7](https://www.digitalocean.com/community/tutorials/how-to-install-mysql-on-centos-7).

1. Install `mysql-server`
    ```shell script
    wget https://dev.mysql.com/get/mysql57-community-release-el7-9.noarch.rpm
    rpm -ivh mysql57-community-release-el7-9.noarch.rpm
    yum install mysql-server
    ```
2. Modify configuration `/etc/mysql/my.conf`.
3. Turn on MySQL Daemon
    ```shell script
       systemctl start mysqld
    ```
4. Test connection by `mysql -uroot -p`.

## Setup Docker CE

You can follow the link to install mysql 5.7: [How To Install and Use Docker on CentOS 7](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-centos-7).

1. Install Docker CE
    ```shell script
    yum check-update
    curl -fsSL https://get.docker.com/ | sh # docker installing script
    ```
2. Configure remote access: run `systemctl edit docker`, Add/Modify following lines:
    ```ini
    [Service]
    ExecStart=
    ExecStart=/usr/bin/dockerd -H fd:// -H tcp://127.0.0.1:2375
    ```
   then run following command to restart daemon:
   ```shell script
    systemctl daemon-reload
    systemctl restart docker.service
    ```
3. Run `netstat -lntp | grep dockerd` to check if `dockerd` is listening on the configured port.

## Build docker image for DonmenDB

```shell script
git clone https://github.com/youngsamwei/DongmenDB.git
docker build -t "dongmendb-test" ./DongmenDB
```

## Setup Maven

1. Install Maven
    ```shell script
    yum install maven
    ```
2. Install libraries to local maven repository
    ```shell script
    cd jdbexes
    chmod +x scripts/install-libs.sh
    scripts/install-libs.sh
    ```

## Modify project profiles

Modify configurations located in `src/main/conf`.

## Run Project
```shell script
mvn tomcat7:run --batch-mode >/var/log/jdbexes/out.log 2>/var/log/jdbexes/err.log
```