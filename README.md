# 基于Spring Cloud的电影推荐微服务设计文档
**组员：袁佳豪 刘小捷 吴桐 吕晓强**
#一、	简介

##1.1	编写说明
此文档为本小组在大规模分布式系统课程期末项目设计说明文档，旨在描述本项目设计中希望解决的问题，以及具体的设计方案。

##1.2	项目说明
随着互联网的应用与迅速普及，信息过载问题越加突出。在网络视频服务中，随着影片资源的迅速增长，常常导致用户在网站中“信息迷航”，而个性化影片推荐服务是解决这类问题的有效方法，得到了绝大多数视频网站的应用与研究。本项目参考开源项目，使用Spring Cloud设计并实现了一个多后端的电影推荐微服务应用。微服务应用中，针对于不同的模块需求，每个服务有着自己的数据库系统，服务与服务之间通过消息队列等方式进行交互与协同。本项目中，会同时使用到关系型数据库MySQL和图形数据库Neo4j。
#二、	架构设计
##2.1	需求描述
在视频网站中，会根据用户观看电影的历史记录和用户画像进行相应的影片推荐，因此。我们的设计中，主要的对象是用户和电影，主要的行为是推荐服务。

针对用户，我们主要有以下功能：

- 用户登录、注册、基本信息录入

针对推荐服务，主要有实现以下功能算法：

- 基于用户的历史记录，推荐可能感兴趣的电影
- 针对用户所看电影，进行相似电影推荐

出于项目展示需求，我们设计并实现了一个前端页面，其主要功能是：

- 用户登录，信息修改
- 热门电影推荐
- 用户历史观影查看
- 用户相关电影推荐
- 类似电影查看

##2.2	总体架构设计
![架构图](http://i.imgur.com/GmaARA2.jpg)

#三、	功能描述
##3.1	用户服务
用户服务模块包括用户的登录功能、用户注册功能、用户密码修改功能、查看全部用户信息功能以及删除用户功能。

| 用户登录 |
| --- | --- |
| 请求方式 | POST |
| 路径 | /user/login |
| 描述 |	输入用户名和密码，查找该用户名和密码是否存在于数据库中 |

| 用户注册 |
| --- | --- |
| 请求方式 | POST |
| 路径 |	/user/register |
| 描述 |	将用户填写的注册信息添加到数据库中进行保存 |

| 用户密码修改 |
| --- | --- |
| 请求方式 |	GET |
| 路径 |	/user/updatePassword |
| 描述 |	输入用户名,更新的用户密码。根据用户的userid查询出用户，将新密码更新添加到数据库中|

| 获取全部用户信息 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /user/findAll |
| 描述 | 查询数据库中现存所有用户，并将用户信息查询输出 |

| 删除用户 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /user/delete |
| 描述 | 根据用户在数据库中的id查询到该用户，并将其从数据库中删除 |

在用户服务模块我们的采用H2数据库进行该部分微服务的数据存储。H2是常用的一种开源数据库，与HSQLBD类似，有很多优势：首先H2采用纯Java编写，因此不受平台限制；其次，H2只有一个jar包文件，直接通过使用pom.xml导入依赖即可使用，可以作为项目工程中的内嵌式数据库使用，不需要再安装独立的客户端和服务器端；另外，H2比HSQLDB的最大的优势就是h2提供了一个十分方便的web控制台用于操作和管理数据库内容。

H2数据库配置如下
- 添加依赖	pom.xml中引入如下配置使用HSQL
```
<!-- 内存数据库h2-->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```
<!-- 内存数据库h2-->

- 参数配置	Application.yml参数配置
```
spring:
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
  datasource:
    url: jdbc:h2:./.h4/testdb`
```
- 实体创建	创建一个User实体，包含id（主键）、name（姓名）、age（年龄）属性，通过ORM框架其会被映射到数据库表中，由于配置了hibernate.hbm2ddl.auto，在应用启动的时候框架会自动去数据库中创建对应的表
```
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String userid;
    private String password;
    private String age;
    private String gender;
}
```

##3.2	电影服务
电影服务中，使用MySQL数据库存储视频信息，这个功能暂时需要后台直接插入，对外只提供查询功能。

| 电影查询 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /movies/search/findByIdIn?ids
| 描述 | 根据电影Id查询电影所有信息,可以为1个或多个Id |

##3.3	评分服务
评分服务使用Neo4j数据库存储用户和电影之间的关系，我们侧重于推荐服务的实现而不是电影网站的管理，因此所有的数据也是离线后台导入，对外值提供功能。

| 查询已评分电影 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /products/search/findProductsByUser?id |
| 描述 | 查询用户看过并评过分的电影 |

| 查询用户电影评分 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /ratings/search/findByUserId?id|
| 描述 | 根据用户Id查询用户对电影的评分 |

##3.4	推荐服务
推荐服务直接使用Neo4j数据库存储每个针对用户需要推荐的具体电影。由于线上实时动态推荐要求较高，我们的推荐服务通过离线计算出模型，并将相应需要推荐的电影存入数据库中，以保证推荐的响应速度。

| 推荐电影 |
| --- | --- |
| 请求方式 | GET |
| 路径 | Recommendation-service 下/products/search/findProductsByUser?id |
| 描述 | 根据用户历史记录推荐用户可能喜欢的Top5个电影，使用带权重矩阵分解算法WRMF推荐算法|

| 相似电影推荐 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /getSimilarMovies?movieId |
| 描述 | 用户观看过的某电影推荐类似电影，计算各电影观看记录的Jaccard相似度，得出与该电影得分最相近的10个电影，返回movieId |

##3.5	交互界面
主要包括登录页面和用户页面，其中用户页面中包含最新电影推荐、用户观看电影历史记录、相关电影推荐三大部分。
如图1所示，为登录界面，测试用户名为4，密码为123456，另外为了方便系统演示，提供了demo account的登入链接。
 ![](http://i.imgur.com/58q3ctN.png)
图1
如图2所示，为主界面，由于我们暂时未解决新用户的推荐“冷启动”问题，因此暂时不提供注册功能
 ![](http://i.imgur.com/gNruSXn.jpg)
图2
如图3所示，是登陆的用户查看自己的电影观看记录
 ![](http://i.imgur.com/NTmiwKT.png)
图3
如图4所示，是用户根据自身过去看过的电影，推荐给用户与该电影相似的电影。
 ![](http://i.imgur.com/UMtJwVt.png)
图4
如图5所示，是根据用户整个的观看记录和电影评分，对用户进行电影推荐。
 ![](http://i.imgur.com/sfHfslk.png)
图5

观看电影记录中可以通过点击相似电影，查看与该电影相似的电影。相关电影推荐是通过用户的观看历史记录和对观看电影的评分综合推荐的用户可能喜欢的5部电影。由此提供的接口，主要有：

| 登录首页 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /login |
| 描述 | 用户登录页面 |

| 登录验证 | 
| --- | --- |
| 请求方式 | POST |
| 路径 | /login?valid |
| 描述 | 用户登录密码验证，通过调用user-microservie中的接口验证用户ID和密码 |

| 用户页面 |
| --- | --- |
| 请求方式 | POST |
| 路径 | /home |
| 描述 | 根据用户ID，显示用户页面，其中包括最新电影推荐、用户观看电影历史记录、相关电影推荐三大部分。通过调用user-microservice,movie-microservice,rating-microservice和recommendation-service中的相关接口，获取用户的相关数据。|

| 用户页面 |
| --- | --- |
| 请求方式 | GET |
| 路径 | /getmovies |
| 描述 | 根据已观看电影ID，通过调用recommendation-service中的接口，获取该电影的相似电影列表|
# 四、运行部署


- 安装Docker和Docker Compose。
- 运行微服务。从Github上将本项目下载，只需进入项目中的Docker文件夹，在命令行模式下通过运行 “docker-compose up”命令，即可运行微服务。

 