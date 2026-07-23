# 掠星 DynamicTP · 监控与中间件配置

Nacos + Prometheus + Grafana 的配置文件统一放在本目录。

## 目录结构

```
monitoring/
├── docker-compose.yml              # 容器编排
├── prometheus/
│   └── prometheus.yml              # Prometheus 抓取配置
├── grafana/
│   ├── provisioning/               # Grafana 自动配置（需保留）
│   │   ├── datasources/prometheus.yml
│   │   └── dashboards/
│   │       ├── dashboards.yml
│   │       └── json/luexing-dynamictp.json
│   └── data/                       # 运行时数据（Git 忽略，可清空）
└── nacos/
    ├── data/                       # Nacos 业务数据（Git 忽略）
    └── logs/                       # Nacos 日志（Git 忽略，可清空）
```

## 启动

```powershell
cd e:\JAVAWORK\DynamicTP\monitoring
docker compose up -d
```

## 访问

| 服务 | 地址 |
|------|------|
| Nacos | http://127.0.0.1:8848/nacos （nacos / nacos） |
| Prometheus | http://127.0.0.1:9090 |
| Grafana | http://127.0.0.1:3000/d/luexing-dynamictp |

## 说明

- 需保留的配置：`docker-compose.yml`、`prometheus/`、`grafana/provisioning/`
- 可清理的运行时文件：`nacos/logs/`、`grafana/data/`、Nacos 的 `LOG.old.*`
- 面板源码备份：`DynamicTP/dashboard-dev/grafana/luexing-dynamictp-dashboard.json`
