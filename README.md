# 个性化智慧校园助手

本项目为“基于 Spring Boot + Vue 的个性化智慧校园助手的设计与实现”毕设代码。

## 项目结构

```text
bishe
├─ backend   Spring Boot 后端
├─ frontend  Vue 3 前端
├─ 开发日志.md
├─ 项目环境与配置文档.md
├─ 后端项目文档.md
└─ 前端项目文档.md
```

## 后端启动

1. 创建数据库并执行脚本：

```text
backend/src/main/resources/sql/schema.sql
backend/src/main/resources/sql/data.sql
```

2. 修改 MySQL 密码：

```text
backend/src/main/resources/application-dev.yml
```

3. 如需启用 AI 能力，补充配置：

```text
backend/src/main/resources/application-dev.yml
app.ai.enabled=true
app.ai.api-key=你的 DeepSeek API Key
```

4. 启动 Redis 和 Kafka。

5. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8085
```

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

## 默认账号

```text
学生账号：23050539414
教师账号：teacher01
管理员账号：admin
默认密码：123456
```

说明：默认密码在数据库中使用 BCrypt 密文存储，登录后建议修改密码。

## 已完成能力

- Spring Boot 3 后端工程
- Vue 3 前端工程
- MyBatis-Plus + MySQL
- JWT 登录认证
- Bean Validation 参数校验
- BCrypt 密码加密
- 统一返回结果
- 全局异常处理
- 登录拦截器
- 公告查询
- 场地查询
- 场地预约
- 预约审核和取消
- 活动报名
- 活动签到
- 数据看板
- Redis / Redisson 依赖接入
- Redisson 重复执行限制骨架
- Redisson 延迟队列骨架
- Kafka 生产者、消费者、DLQ 消费者骨架
- 数据库初始化脚本
- 场地预约时间段库存
- 预约并发锁
- 预约库存扣减与释放
- 按角色显示前端菜单
- 系统管理基础 CRUD
- AI 客服助手后端接口
- AI 内容审核员后端骨架
- 讨论区发帖/评论 AI 审核接入

## AI 功能说明

当前项目已新增两个 AI 能力：

1. `AI 客服助手`

- 独立后端接口：`POST /ai/chat`
- 可回答系统使用问题，如场地预约、活动报名、活动签到、公告通知、讨论交流
- 可根据当前登录用户上下文回答部分个人业务问题，例如最近一条场地预约状态

2. `AI 内容审核员`

- 独立 AI 模块，接入讨论区发帖和评论流程
- 支持 `PASS`、`FLAG`、`BLOCK` 三类审核结果
- 未配置 AI Key 时，系统仍可通过本地敏感词规则完成基础审核兜底

## 数据库升级说明

如果已经执行过旧版数据库脚本，不想重建数据库，需要额外执行：

```text
backend/src/main/resources/sql/upgrade-venue-slot.sql
backend/src/main/resources/sql/upgrade-activity-venue.sql
backend/src/main/resources/sql/upgrade-notification.sql
backend/src/main/resources/sql/upgrade-college-permission.sql
```

否则提交预约时会因为没有开放时间段库存而提示“该场地当前时间段未开放预约”。

## 验证结果

- 后端 `mvn -q -DskipTests compile` 已通过。
- 前端 `npm run build` 已通过。
