# 掠星 DynamicTP

基于 Spring Boot 的动态线程池基础组件，支持通过 Nacos / Apollo 配置中心热更新线程池参数，并提供监控、告警与开发态 Dashboard。

## 特性

- **动态调参**：运行时调整核心线程数、最大线程数、队列容量、拒绝策略等，无需重启
- **配置中心集成**：支持 Nacos（Spring Cloud Alibaba）与 Apollo
- **Web 容器线程池**：可动态管理 Tomcat 等 Web 容器线程池
- **监控指标**：基于 Micrometer 暴露指标，对接 Prometheus + Grafana
- **告警通知**：队列/活跃线程超阈值时，支持钉钉机器人通知
- **开发控制台**：`dashboard-dev` 便于本地查看与调试（**不建议用于生产**）

## 技术栈

| 项 | 版本 / 说明 |
|----|-------------|
| Java | 17 |
| Spring Boot | 3.0.7 |
| Spring Cloud | 2022.0.3 |
| Spring Cloud Alibaba | 2022.0.0.0-RC2 |
| 构建 | Maven |

## 仓库结构

```
DynamicTP/                  # 核心工程（Maven 多模块）
├── core/                   # 线程池核心：执行器、监控、告警、配置解析
├── spring-base/            # Spring 扫描、启用开关、Banner 等
├── starter/                # 配置中心与 Web 适配 Starter
│   ├── nacos-cloud-spring-boot-starter
│   ├── apollo-spring-boot-starter
│   ├── common-spring-boot-starter
│   ├── adapter/web-spring-boot-starter
│   └── dashboard-dev-spring-boot-starter
├── example/                # 示例工程（Nacos / Apollo）
└── dashboard-dev/          # 开发态控制台后端

monitoring/                 # Nacos + Prometheus + Grafana（Docker Compose）
DynamicTP-nginx-1.3.0.1/    # 本地 Nginx / 前端静态资源（可选）
```

## 工作原理（简述）

1. 应用启动时根据本地配置创建动态线程池
2. 从配置中心拉取 YAML，覆盖/刷新线程池参数
3. `NacosCloudRefresherHandler`（或 Apollo 对应刷新器）监听配置变更，按差异热更新，而非整表覆盖
4. `ThreadPoolMonitor` 持续采集指标写入 Micrometer，供 Prometheus 拉取、Grafana 展示
5. `ThreadPoolAlarmChecker` 定时检查阈值，超限则发送告警

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
-（可选）Docker：用于启动 monitoring 中间件

### 编译

```bash
cd DynamicTP
./mvnw clean install -DskipTests
```

Windows：

```powershell
cd DynamicTP
.\mvnw.cmd clean install -DskipTests
```

### 启动监控中间件（可选）

```powershell
cd monitoring
docker compose up -d
```

| 服务 | 地址 |
|------|------|
| Nacos | http://127.0.0.1:8848/nacos （账号/密码：`nacos` / `nacos`） |
| Prometheus | http://127.0.0.1:9090 |
| Grafana | http://127.0.0.1:3000/d/luexing-dynamictp |

更多说明见 [`monitoring/README.md`](monitoring/README.md)。

### 运行 Nacos 示例

1. 在 Nacos 中创建配置（可参考 `DynamicTP/example/nacos-cloud-example/src/main/resources/nacos-config.yaml`）
2. 启动示例：

```powershell
cd DynamicTP\example\nacos-cloud-example
..\..\mvnw.cmd spring-boot:run
```

或在 IDE 中运行 `NacosCloudExampleApplication`。

### 配置示例

```yaml
dynamictp:
  nacos:
    data-id: dynamictp-nacos-cloud-example.yaml
    group: DEFAULT_GROUP
  config-file-type: yaml
  executors:
    - thread-pool-id: luexing-1
      core-pool-size: 12
      maximum-pool-size: 24
      keep-alive-time: 60
      work-queue: ResizableCapacityLinkedBlockingQueue
      queue-capacity: 10000
      rejected-handler: CallerRunsPolicy
      alarm:
        enable: true
        queue-threshold: 80
        active-threshold: 80
```

## 接入依赖（示意）

Nacos Cloud：

```xml
<dependency>
  <groupId>com.luexing.dynamictp</groupId>
  <artifactId>dynamictp-nacos-cloud-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Apollo：

```xml
<dependency>
  <groupId>com.luexing.dynamictp</groupId>
  <artifactId>dynamictp-apollo-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Web 容器线程池适配：

```xml
<dependency>
  <groupId>com.luexing.dynamictp</groupId>
  <artifactId>dynamictp-web-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

> 当前版本为 `0.0.1-SNAPSHOT`，需先本地 `mvn install` 后再在业务项目中引用。

## 注意事项

- `dashboard-dev` 仅用于开发调试，**请勿用于生产环境**
- 推送/告警前请配置真实的钉钉 Webhook，勿将密钥提交到公开仓库
- `monitoring` 下 Nacos / Grafana 运行时数据已通过 `.gitignore` 忽略，无需提交

## License

本项目仅供学习与交流使用。
