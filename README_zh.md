# EventFlow

[English](./README.md)

**EventFlow** 是一个面向作品集与简历展示的、偏生产化的**全栈活动报名与候补平台**。

它不只是一个普通 CRUD 网站，而是围绕真实业务流程设计：**容量限制、重复报名拦截、候补自动转正、角色权限控制、中英文国际化、缓存、异步通知、容器化部署与 CI 自动化**。整个项目的目标，是把它做成一个可以直接写进软件工程 / 全栈 / 后端岗位简历的完整项目。

## 项目预览

| 用户侧产品体验 | 管理后台体验 |
| --- | --- |
| ![Product screenshot placeholder](./docs/screenshots/product-placeholder.svg) | ![Admin screenshot placeholder](./docs/screenshots/admin-placeholder.svg) |

> 之后可直接替换为真实截图：
> - `docs/screenshots/product-home.png`
> - `docs/screenshots/admin-dashboard.png`

## 产品能力概览

EventFlow 覆盖了三个角色和完整的活动报名生命周期：

- **Guest（游客）**：浏览首页推荐活动、查看活动列表、搜索筛选、查看活动详情
- **User（普通用户）**：注册登录、报名活动、活动满员时进入候补、取消报名、查看我的报名和站内通知
- **Admin（管理员）**：创建活动草稿、发布/关闭/取消活动、查看报名和候补名单、查看平台统计数据

核心业务流围绕真实产品约束展开：

- 活动有最大容量
- 活动有报名截止时间
- 不允许重复有效报名
- 满员后自动进入候补队列
- 取消报名后按顺序自动候补转正
- 关键操作通过事务与约束控制一致性

## 功能亮点

### 用户侧产品功能
- 首页推荐活动与现代卡片式活动列表
- 活动搜索、分类筛选、城市筛选、时间筛选、排序
- 活动详情页展示时间、地点、标签、剩余名额、报名状态与操作按钮
- 注册、登录、个人资料、语言偏好等账号流程
- “我的报名”页面统一查看已报名、候补中、已取消记录
- 完整中英文国际化，语言切换可持久化

### 平台业务与系统能力
- 活动生命周期状态：`DRAFT`、`PUBLISHED`、`CLOSED`、`CANCELLED`
- 基于容量控制的报名逻辑，满员自动进入候补
- 重复有效报名拦截
- 取消报名后按候补顺位自动转正
- 报名成功、加入候补、候补转正、活动取消等站内通知链路
- 使用 Redis 缓存推荐活动与高频活动查询

### 管理后台能力
- Dashboard 统计卡片，展示活动、用户、报名、候补等指标
- 活动创建、编辑、发布、关闭、取消流程
- 单活动维度查看报名名单与候补名单
- 用户列表与参与概况查看

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | React 18、TypeScript、Vite、Tailwind CSS、React Router、Zustand、Axios、react-i18next |
| 后端 | Java 21、Spring Boot 3、Spring Security、JWT、Spring Data JPA、Bean Validation、Flyway、Springdoc OpenAPI |
| 数据与消息 | PostgreSQL、Redis、RabbitMQ |
| 测试 | JUnit 5、Mockito、Spring Security Test、Vitest、React Testing Library |
| 工程化 | Docker、Docker Compose、Nginx、GitHub Actions |

## 为什么这个项目适合写进简历

这个项目的定位不是课程作业，而是一个有明确工程深度的全栈项目。

- **全栈覆盖完整**：公共浏览、登录态用户流程、管理员后台、国际化前端全部具备
- **业务规则真实**：容量限制、截止时间、候补排队、转正通知、角色权限都贴近真实业务
- **后端设计有深度**：报名与取消链路考虑事务、一致性、并发与幂等性问题
- **工程交付完整**：Docker Compose、数据库迁移、种子数据、Swagger API 文档、CI 流程、代表性测试

## 演示账号

| 角色 | 邮箱 | 密码 |
| --- | --- | --- |
| 管理员 | `admin@eventflow.local` | `Admin123!` |
| 用户 | `alice@eventflow.local` | `User123!` |
| 用户 | `bob@eventflow.local` | `User123!` |

## 交付能力

- 提供 REST API 与 Swagger UI 文档入口
- 通过 Docker Compose 编排前端、后端、PostgreSQL、Redis、RabbitMQ、Nginx
- 提供种子数据，便于直接演示完整产品流程
- GitHub Actions 覆盖后端测试与打包、前端 lint / test / build

## 快速启动

### 使用 Docker 启动完整项目

```bash
docker compose up --build
```

### 主要访问入口

- 前端：`http://localhost:4173`
- 后端 API：`http://localhost:8080/api/v1`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- RabbitMQ 管理台：`http://localhost:15672`

## 简历描述参考

**EventFlow** 可以作为一个标准的全栈项目写入简历，重点体现“真实业务流程 + 工程化能力 + 后端一致性设计 + 前端产品体验”。

适合重点强调的方向：

- 设计并实现带候补自动转正的活动报名系统，覆盖容量控制、状态流转与通知流程
- 构建支持角色权限控制和中英文国际化的 React 前端与管理员后台
- 集成 Redis、RabbitMQ、Docker Compose 与 GitHub Actions，形成完整工程化交付链路

## 文档语言

- English: `README.md`
- 中文：`README_zh.md`
