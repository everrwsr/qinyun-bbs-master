#QinYun-BBS

## 简介
秦韵——陕西方言文化推广平台
陕西方言推广论坛提供了用户友好的账户管理,帖子和评论互动,高效的搜索和标签系统,多媒体支持,私信通知机制,方言学习资源,以及坚实的社区规则和技术支持,旨在鼓励用户学习,分享和交流陕西方言,为方言的传承和推广提供强有力的在线支持。


## 技术

- Spring Boot2.1.7，Elastic Search , Caffeine Cache Java8


## 数据库设计
论坛消息 论坛模块 论坛帖子 论坛回复 论坛话题 论坛用户


## 安装

* lombok

* jdk8,maven3,mysql

* git clone 

* create database bbs; install mysql from intall-mysql.sql

* import as maven project

* run BbsMain

* access  http://127.0.0.1:8080/bbs/bbs/index/1.html,login as admin/123456 or register new user

* maven install  生成的war包部署到服务器上

## 切换搜索引擎

默认自带luncense，以管理员登录后， 点击初始化引擎。 

如果需要使用ES作为帖子搜索引擎

* 安装elastic search 作为全文搜索：下载 https://www.elastic.co/cn/downloads/elasticsearch (截止当前测试ES版本7.2.0 通过),进入bin目录，调用elasticsearch 启动

* 安装elastic search 分词：进入bin目录，运行 ./elasticsearch-plugin install  analysis-smartcn。然后重新启动elasticsearch










