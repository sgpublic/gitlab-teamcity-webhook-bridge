# gitlab-teamcity-webhook-bridge

这是一个用于简化 TeamCity WebHook 配置的工具，仅适合私有搭建，项目未作安全逻辑，请勿部署在公网且暴露端口。

## 食用方法

配置参数如下表：

| 参数             | 命令行                  | 环境变量                | 含义                              | 可选 | 默认值/示例值                      |
|----------------|----------------------|---------------------|---------------------------------|----|------------------------------|
| 端口             | --port=xxx           | GTWD_PORT           | 监听端口                            | 是  | 8080                         |  
| Host           | --host=xxx           | GTWD_HOST           | 监听 Host                         | 是  | 0.0.0.0                      |  
| TeamCity 主机    | --teamcity-host=xxx  | GTWD_TEAMCITY_HOST  | TeamCity 主机地址，必须以 `/` 结尾        | 否  | http://teamcity.example.org/ |  
| TeamCity 用户名   | --teamcity-user=xxx  | GTWD_TEAMCITY_USER  | 用于触发 TeamCity WebHook 的用户名      | 否  |                              |  
| TeamCity Token | --teamcity-token=xxx | GTWD_TEAMCITY_TOKEN | 用于触发 TeamCity WebHook 的用户 Token | 否  |                              |  

启动后将监听 `http://<host>:<port>/{buildConfId}`，例如：

假设你有一个 TeamCity 的构建配置，它的 `Build configuration ID` 为 `Test_Build`，则你可以在 GitLab 的 WebHook 中将链接设置为 `http://<host>:<port>/Test_Build`

docker-compose 启动方式：

```yaml
version: '3.6'
services:
  web:
    image: gitlab/gitlab-ce:16.6.1-ce.0
    restart: always
    ports:
      - '8080:80'
      - '2222:22'
    volumes:
      - /var/log/gitlab:/var/log/gitlab
      - /var/opt/gitlab:/var/opt/gitlab
      - /etc/gitlab:/etc/gitlab
    shm_size: '256m'
  webhookbridge:
    image: mhmzx/gitlab-teamcity-webhook-bridge:1.0.0-jvm
    environment:
      - GTWB_PORT=8095
      - GTWB_TEAMCITY_HOST=http://teamcity.example.org/
      - GTWB_TEAMCITY_USER=testuser
      - GTWB_TEAMCITY_TOKEN=eyJDe...
    restart: always
```