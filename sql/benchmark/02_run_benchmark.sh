#!/usr/bin/env bash
# ============================================================
# SQL 基准测试脚本：对比「优化前（无联合索引）」与「优化后（联合索引）」的真实耗时
# 耗时数据取自 performance_schema.events_statements_summary_by_digest（只统计 SQL 本身，不含客户端开销）
# 前置：先执行 01_seed_bulk_data.sql 造数
# 用法：bash 02_run_benchmark.sh
# ============================================================
MYSQL_BIN="C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"
MYSQL_USER="root"
MYSQL_PWD="123456"
DB="takeout_order"
N=50   # 每个场景执行次数

exec_sql() {
  "$MYSQL_BIN" -u"$MYSQL_USER" -p"$MYSQL_PWD" --default-character-set=utf8mb4 "$DB" -e "$1" 2>/dev/null
}

# 清零统计表
exec_sql "TRUNCATE performance_schema.events_statements_summary_by_digest"

# 构造重复 SQL（一次连接内执行 N 次，避免客户端启动开销污染结果）
build() {
  local sql="$1" out="" i
  for ((i = 0; i < N; i++)); do out="$out $sql"; done
  echo "$out"
}

echo ">>> 场景1 浅分页：WHERE status=? AND order_time BETWEEN ? ORDER BY order_time DESC LIMIT 20"
exec_sql "$(build "SELECT * FROM orders IGNORE INDEX (idx_status_order_time) WHERE status=2 AND order_time>='2025-06-01' AND order_time<'2025-07-01' ORDER BY order_time DESC LIMIT 20;")" >/dev/null
exec_sql "$(build "SELECT * FROM orders WHERE status=2 AND order_time>='2025-06-01' AND order_time<'2025-07-01' ORDER BY order_time DESC LIMIT 20;")" >/dev/null

echo ">>> 场景2 深分页：WHERE status=? ORDER BY order_time DESC LIMIT 5000, 20"
exec_sql "$(build "SELECT * FROM orders IGNORE INDEX (idx_status_order_time) WHERE status=2 ORDER BY order_time DESC LIMIT 5000, 20;")" >/dev/null
exec_sql "$(build "SELECT * FROM orders WHERE status=2 ORDER BY order_time DESC LIMIT 5000, 20;")" >/dev/null

echo ">>> 场景3 用户历史订单：WHERE user_id=? ORDER BY order_time DESC LIMIT 20"
exec_sql "$(build "SELECT * FROM orders IGNORE INDEX (idx_user_order_time) WHERE user_id=500 ORDER BY order_time DESC LIMIT 20;")" >/dev/null
exec_sql "$(build "SELECT * FROM orders WHERE user_id=500 ORDER BY order_time DESC LIMIT 20;")" >/dev/null

echo ">>> 场景4 订单明细：WHERE order_id=?"
exec_sql "$(build "SELECT * FROM order_detail IGNORE INDEX (idx_order_id) WHERE order_id=50000;")" >/dev/null
exec_sql "$(build "SELECT * FROM order_detail WHERE order_id=50000;")" >/dev/null

echo ""
echo "==================== 实测结果（耗时单位 ms） ===================="
echo "说明：AVG_TIMER_WAIT 单位为皮秒，需除以 10^9 换算为毫秒"
exec_sql "SELECT
  LEFT(REPLACE(DIGEST_TEXT, '\n', ' '), 95) AS SQL样本,
  COUNT_STAR AS 次数,
  ROUND(AVG_TIMER_WAIT/1000000000, 2) AS 平均耗时ms,
  ROUND(MAX_TIMER_WAIT/1000000000, 2) AS 最大耗时ms,
  SUM_ROWS_EXAMINED DIV COUNT_STAR AS 平均扫描行数
FROM performance_schema.events_statements_summary_by_digest
WHERE (DIGEST_TEXT LIKE '%orders%' OR DIGEST_TEXT LIKE '%order_detail%')
  AND COUNT_STAR >= $N
ORDER BY 平均扫描行数 DESC, 平均耗时ms DESC"
