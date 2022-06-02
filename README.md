# AppServer

## 介绍

AppServer 是什么？

例举对于服务器端开发者常见的两个业务有用户注册、用户登录，那么当您选择使用环信服务后，您需要为自己的用户创建环信用户与环信关联并且获取 token，才可以让用户在App端使用环信服务，那么您如何让自己的用户与环信关联并获取 token 呢？

AppServer 正是为开发者演示了在用户注册、用户登录时，如何为用户创建环信用户与环信关联以及为用户获取 token 的参考。

* 快速了解AppServer。
<img width="1093" alt="register" src="https://user-images.githubusercontent.com/15087647/171544887-39f2a52e-0c3d-400c-b764-79977c73054a.png">
---

<img width="871" alt="login" src="https://user-images.githubusercontent.com/15087647/171544896-080971fd-81aa-42ad-bc30-35e9bf7cb78e.png">
---

![DC4314DA-FDF0-408D-B9C3-B666775CC246](https://user-images.githubusercontent.com/15087647/128315691-b69ad985-0642-4041-9361-af119c1480f3.png)
---

![40A2C15E-3F4E-4141-BD76-F4CC3DF91276](https://user-images.githubusercontent.com/15087647/128316482-bcc2b30c-6dbd-4712-a9a1-b6da1d98036d.png)
---

## 功能

- AppServer 在用户注册时通过 Server SDK 为用户创建环信用户与环信关联，同时会为用户生成一个 agoraUid，是为了使用[声网服务](https://docs.agora.io/cn/Voice/landing-page)准备的。
- AppServer 在用户登录后，通过 Server SDK 利用声网 appId、声网 appCert、环信用户 uuid 为用户生成 token。
- AppServer 通过数据库存储用户信息，用户信息包括用户账号、用户密码、环信用户名、环信用户 uuid、agoraUid。

## 技术选择

* [Spring Boot](https://spring.io/projects/spring-boot)

## 主要组件

* [Server SDK](https://docs-im.easemob.com/ccim/rest/javaserversdk#java_server_sdk)
* MySQL

## 准备

在获取声网 token 之前，需要准备环信 AppKey、声网 AppId（AppId）、声网 APP证书（AppCert）

* 获取环信 AppKey：
  - 1.如果您有环信管理后台账号并创建过应用，请先登录环信管理后台，点击[这里](https://console.easemob.com/user/login)，然后到"应用列表" -> 点击"查看"即可获取到appkey。
  - 2.如果您没有环信管理后台账号，请先注册账号，点击[这里](https://console.easemob.com/user/register)，注册成功后请登录，然后点击"添加应用"，添加成功后点击"查看"即可获取到appkey。

* 获取 AppId、AppCert：
  - 1.如果您有声网 Console 后台账号并创建过项目，请先登录声网 Console  后台，点击[这里](https://sso.agora.io/cn/login/)，然后到"项目列表" -> 找到自己的项目点击"编辑"图标后，即可看到 App ID、APP 证书。
  - 2.如果您没有声网Console后台账号，请先注册账号，点击[这里](https://sso.agora.io/cn/v4/signup)，注册成功后按照步骤1操作。

* 您需要自己来实现用户登录时的认证、授权由您自己来完成。

## 配置

配置文件中需要的参数来源于"准备"中获取到的环信 appkey、声网 AppId（AppId）、声网 APP证书（AppCert）。

* 服务配置文件参考：[application.properties](./agora-app-server/src/main/resources/application.properties)

  ```
      server.port=8086

      logging.level.com.easemob.im.http=debug
      logging.level.com.easemob.im.shaded.io.netty=ERROR
      
      ## 环信console 获取自己的appkey
      application.appkey=xxx
      
      ## 声网console获取appid
      application.agoraAppId=xxx
      ## 声网console获取appcert
      application.agoraCert=xxx
      ## 声网token过期时间(自已定义，不能超过1天)
      agora.token.expire.period.seconds=86400
      
      ## data source
      spring.datasource.driver-class-name=com.mysql.jdbc.Driver
      spring.datasource.url=jdbc:mysql://127.0.0.1:3306/app_server?useSSL=false&useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true
      spring.datasource.username=root
      spring.datasource.password=123456
      spring.datasource.hikari.maximum-pool-size=50
      spring.datasource.hikari.minimum-idle=20
  
      ## jpa
      spring.jpa.show_sql=false
      spring.jpa.properties.hibernate.format_sql=true
      spring.jpa.properties.hibernate.generate_statistics=false
      spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL57Dialect
      spring.jpa.hibernate.ddl-auto=validate
      
  ```

* 对 Server SDK 的配置请参考 AppServer 中的 "ApplicationConfig"

## 使用

上述的配置文件准备准备好，到 AgoraServerApplication 类中启动服务即可使用。

## API

### 用户注册。

在您的应用中为用户注册一个账号，此api 示例中使用的是用户名、密码的形式，当然您可以使用手机号等其他形式为用户注册一个账号。

**Path:** `http://localhost:8080/app/user/register`

**HTTP Method:** `POST`

**Request Headers:** 

| 参数         | 说明             |
| ------------ | ---------------- |
| Content-Type | application/json |

**Request Body示例:** 
{"userAccount":"jack", "userPassword":"123"}

**Request Body参数说明:** 

| 参数         | 类型   | 说明     |
| ------------ | ------ | -------- |
| userAccount  | String | 用户账号 |
| userPassword | String | 用户密码 |


**请求示例:**

```
curl -X POST -H 'Content-Type: application/json' 'http://localhost:8080/app/user/register' -d '{"userAccount": "jack","userPassword":"123"}'
```

**Response Parameters:**

| 参数            | 类型    | 说明       |
| --------------- | ------- | ---------- |
| code            | String  | 结果状态码 |
| easemobUserName | String  | 环信用户名 |
| agoraUid        | Integer | 声网uid    |

**返回示例:**

```json
{
    "code": "RES_OK",
    "easemobUserName": "em1792190072",
    "agoraUid": "1792190072"
}
```

---

### 用户登录。

用户在您应用上登录并获取一个token，利用此token在app端使用环信服务。

**Path:** `http://localhost:8080/app/user/login/{userAccount}`

需要在请求时对应填写{userAccount}，需要登录的用户账号。

**HTTP Method:** `POST`

**请求示例:**

```
curl -X POST 'http://localhost:8080/app/user/login/jack'
```

**Response Parameters:**

| 参数            | 类型    | 说明            |
| --------------- | ------- | --------------- |
| code            | String  | 结果状态码      |
| accessToken     | String  | token           |
| expireTimestamp | Long    | token的过期时间 |
| easemobUserName | String  | 环信用户名      |
| agoraUid        | Integer | 声网uid         |

**返回示例:**

```json
{
    "code": "RES_OK",
    "accessToken": "xxx",
    "expireTimestamp": 1628245967857,
    "easemobUserName": "em1792190072",
    "agoraUid": "1792190072"
}
```

