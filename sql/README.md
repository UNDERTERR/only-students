# 数据库初始化指南

## 步骤1: 启动Docker容器

> **注意**：docker-compose.yml 放在 `D:\Project\env\`

```bash
cd D:\Project\env
docker-compose up -d
```

等待30秒让服务启动完成。

## 步骤2: 创建数据库

```bash
# 使用物理机MySQL（密码：xiaojie666）
mysql -h localhost -P 3306 -u root -pxiaojie666 < sql/init/01-init-databases.sql
mysql -h localhost -P 3306 -u root -pxiaojie666 < sql/init/02-init-nacos.sql
```

或者使用MySQL客户端（如Navicat/DataGrip）手动执行这两个文件。

## 步骤3: 创建各服务表

```bash
# 用户服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_user < sql/user-service.sql

# 笔记服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_note < sql/note-service.sql

# 文件服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_file < sql/file-service.sql

# 订阅服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_subscription < sql/subscription-service.sql

# 支付服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_payment < sql/payment-service.sql

# 提现服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_withdrawal < sql/withdrawal-service.sql

# 评论服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_comment < sql/comment-service.sql

# 评分服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_rating < sql/rating-service.sql

# 消息服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_message < sql/message-service.sql

# 通知服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_notification < sql/notification-service.sql

# 举报服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_report < sql/report-service.sql

# 数据分析服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_analytics < sql/analytics-service.sql

# 后台管理服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_admin < sql/admin-service.sql

# 搜索服务
mysql -h localhost -P 3306 -u root -pxiaojie666 only_students_search < sql/search-service.sql
```

## 步骤4: 创建ES索引

```bash
# 创建笔记索引
curl -X PUT "localhost:9200/note_index" -H 'Content-Type: application/json' -d @es/note-index.json

# 创建用户索引
curl -X PUT "localhost:9200/user_index" -H 'Content-Type: application/json' -d @es/user-index.json

# 创建搜索建议索引
curl -X PUT "localhost:9200/search_suggestion_index" -H 'Content-Type: application/json' -d @es/suggestion-index.json
```

## 服务访问地址

| 服务 | 地址 | 账号/密码 |
|------|------|----------|
| Nacos | http://localhost:8848/nacos | nacos/nacos |
| MySQL | localhost:3306 | root/xiaojie666 (物理机) |
| Redis | localhost:6379 | xiaojie666 |
| RabbitMQ | http://localhost:15672 | admin/admin123 |
| Elasticsearch | http://localhost:9200 | - |
| Kibana | http://localhost:5601 | - |
| MinIO | http://localhost:9001 | minioadmin/minioadmin123 |

## 初始化完成检查

```bash
# 检查数据库（物理机MySQL）
mysql -h localhost -P 3306 -u root -pxiaojie666 -e "SHOW DATABASES LIKE 'only_students_%';"

# 检查ES索引
curl -X GET "localhost:9200/_cat/indices?v"

# 检查Redis连接
docker exec -it only-students-redis redis-cli -a xiaojie666 ping
```
