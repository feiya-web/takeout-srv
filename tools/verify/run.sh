#!/usr/bin/env bash
# M2 接口验收总入口 —— 一条命令跑完全部验收脚本。
#
# 用法：
#   bash tools/verify/run.sh              # 跑全部（应用需已在 :8080 运行）
#   bash tools/verify/run.sh --list      # 只看步骤清单
#   bash tools/verify/run.sh --only S0   # 只跑静态检查（不需要起服务）
#
# 连接参数可用环境变量覆盖，命名与 sql/benchmark/*.sh 一致：
#   MYSQL_BIN / MYSQL_HOST / MYSQL_PORT / MYSQL_USER / MYSQL_PASSWORD / MYSQL_DB
#   BASE_URL / DEMO_PASSWORD / ADMIN_USER / PYTHON
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && { pwd -W 2>/dev/null || pwd; })"
# ↑ 必须用 `pwd -W`：Git Bash 的 `pwd` 返回 /d/xxx，原生 Python 会把它当成
#   相对当前盘符的路径，解析成 D:\d\xxx 然后报 "No such file or directory"。


PYTHON="${PYTHON:-python}"
MYSQL_BIN="${MYSQL_BIN:-mysql}"                             # 默认取 PATH 中的 mysql 客户端
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PWD="${MYSQL_PASSWORD:-123456}"
MYSQL_DB="${MYSQL_DB:-takeout_order}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
DEMO_PASSWORD="${DEMO_PASSWORD:-123456}"
export MYSQL_BIN MYSQL_HOST MYSQL_PORT MYSQL_USER MYSQL_PASSWORD MYSQL_DB
export BASE_URL DEMO_PASSWORD PYTHON

exec "$PYTHON" "$HERE/m2_regression.py" "$@"
