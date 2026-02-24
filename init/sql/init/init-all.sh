#!/bin/bash
# OnlyStudents 数据库一键初始化脚本
# 使用方式: ./init-database.sh
# 要求: MySQL 8.0+, 已配置root密码为 xiaojie666

set -e

echo "========================================"
echo "OnlyStudents 数据库初始化工具"
echo "========================================"
echo ""

# 数据库配置
DB_HOST="localhost"
DB_USER="root"
DB_PASS="xiaojie666"
MYSQL_CMD="mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS}"

echo "步骤1: 创建数据库..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} < init/01-init-databases.sql
echo "✓ 数据库创建完成"
echo ""

echo "步骤2: 创建User Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_user < ../user-service.sql
echo "✓ User Service表创建完成"
echo ""

echo "步骤3: 创建Note Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_note < ../note-service.sql
echo "✓ Note Service表创建完成"
echo ""

echo "步骤4: 创建File Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_file < ../file-service.sql
echo "✓ File Service表创建完成"
echo ""

echo "步骤5: 创建Subscription Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_subscription < ../subscription-service.sql
echo "✓ Subscription Service表创建完成"
echo ""

echo "步骤6: 创建Payment Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_payment < ../payment-service.sql
echo "✓ Payment Service表创建完成"
echo ""

echo "步骤7: 创建Comment Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_comment < ../comment-service.sql
echo "✓ Comment Service表创建完成"
echo ""

echo "步骤8: 创建Rating Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_rating < ../rating-service.sql
echo "✓ Rating Service表创建完成"
echo ""

echo "步骤9: 创建Message Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_message < ../message-service.sql
echo "✓ Message Service表创建完成"
echo ""

echo "步骤10: 创建Notification Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_notification < ../notification-service.sql
echo "✓ Notification Service表创建完成"
echo ""

echo "步骤11: 创建Report Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_report < ../report-service.sql
echo "✓ Report Service表创建完成"
echo ""

echo "步骤12: 创建Withdrawal Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_withdrawal < ../withdrawal-service.sql
echo "✓ Withdrawal Service表创建完成"
echo ""

echo "步骤13: 创建Analytics Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_analytics < ../analytics-service.sql
echo "✓ Analytics Service表创建完成"
echo ""

echo "步骤14: 创建Admin Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_admin < ../admin-service.sql
echo "✓ Admin Service表创建完成"
echo ""

echo "步骤15: 创建Search Service表..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} only_students_search < ../search-service.sql
echo "✓ Search Service表创建完成"
echo ""

echo "步骤16: 应用数据库修复..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} < ../fix-database.sql
echo "✓ 数据库修复完成"
echo ""

echo "步骤17: 初始化Nacos数据库..."
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} < init/02-init-nacos.sql
echo "✓ Nacos数据库初始化完成"
echo ""

echo "========================================"
echo "✅ 所有数据库初始化完成！"
echo "========================================"
echo ""
echo "数据库列表:"
mysql -h ${DB_HOST} -u ${DB_USER} -p${DB_PASS} -e "SHOW DATABASES LIKE 'only_students_%'"
echo ""
echo "注意:"
echo "1. 超级管理员账号: admin / admin123"
echo "2. 所有表已添加逻辑删除字段(deleted)"
echo "3. 补偿任务表已创建，用于处理收入分配失败"
echo "4. 示例数据已初始化"
