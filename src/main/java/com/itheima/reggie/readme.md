数据库准备
```shell
mysql -uroot -p
show databases;
create database reggie character set utf8mb4;
use reggie;

source db_reggie.sql;
```

调试地址
http://localhost:8080/backend/page/login/login.html