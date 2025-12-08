<p align="center">
  <img src="./assets/text-logo.png" width="600" alt="AutoReport">
</p>

<p align="center">AutoReport - 基于Multi-Agent的自动化数据分析系统</p>

## 项目介绍

**AutoReport** 是一个基于 LLM 与多 ReAct Agent 协作架构的自动化数据分析系统，能够智能理解分析需求、自主规划任务、调用工具执行数据查询与处理，并生成结构化洞察报告，主要用于自动化商业数据分析。


### Agent组成
该项目基于4个Agent协作完成数据分析任务：
1. ManagerAgent: 负责管理和调度任务
2. DatabaseAgent: 负责数据库的数据查询操作
3. AnalysisAgent: 负责执行具体的数据分析过程
4. ReportAgent: 负责根据数据分析结果生成报告

其中ManagerAgent相当于企业中的管理者，负责自动调度另外3个Agent完成对应的任务。


### 核心特征
1. 自主性（Autonomy）
2. 目标驱动（Goal-directed）
3. 工具使用（Tool Use）
4. 规划与反思（Planning & Reflection）
5. 多智能体协作（Multi-Agent）


## 安装部署

### 下载项目
1. 使用git项项目clone到本地
```bash
git clone https://github.com/zhushimmer/AutoReport.git
```


### 部署Agent服务API

1. 进入server目录
```bash
cd auto-report-server
```

2. 打包项目
```bash
mvn clean package
```

3. 启动
```bash
java -jar target/auto-report-server-0.0.1-SNAPSHOT.jar
```

### 启动Web UI

1. 进入UI目录
```bash
cd auto-report-ui
```

2. 安装依赖
```bash
pnpm install
```

3. 快速启动
```bash
pnpm dev
```


## 许可协议
本项目采用以下许可协议：[Apache 2.0 license](LICENSE)


## 联系
如果您有任何问题或功能需求，请联系我们。欢迎提交 PR。
 - https://github.com/zhushimmer/AutoReport/issues
