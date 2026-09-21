-- ============================================================
-- 餐饮外卖订单系统 数据库初始化脚本
-- 使用：mysql -uroot -proot < takeout_order.sql
-- ============================================================
DROP DATABASE IF EXISTS takeout_order;
CREATE DATABASE takeout_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE takeout_order;

-- 员工表（管理端）
CREATE TABLE employee (
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    name        VARCHAR(32)  NOT NULL COMMENT '姓名',
    username    VARCHAR(32)  NOT NULL COMMENT '用户名',
    password    CHAR(64)     NOT NULL COMMENT '密码(SHA-256)',
    phone       VARCHAR(20)  NULL COMMENT '手机号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) COMMENT '员工表';

-- 用户表（C端）
CREATE TABLE user (
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    username    VARCHAR(32)  NOT NULL COMMENT '用户名',
    password    CHAR(64)     NOT NULL COMMENT '密码(SHA-256)',
    phone       VARCHAR(20)  NULL COMMENT '手机号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) COMMENT '用户表';

-- 分类表
CREATE TABLE category (
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    name        VARCHAR(32)  NOT NULL COMMENT '分类名称',
    type        TINYINT      NOT NULL COMMENT '类型 1菜品分类 2套餐分类',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) COMMENT '分类表';

-- 菜品表
CREATE TABLE dish (
    id          BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    name        VARCHAR(64)   NOT NULL COMMENT '菜品名称',
    category_id BIGINT        NOT NULL COMMENT '分类id',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格',
    image       VARCHAR(200)  NULL COMMENT '图片',
    description VARCHAR(400)  NULL COMMENT '描述',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 0停售 1起售',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 联合索引：C端「按分类查起售菜品」走 (category_id, status)，
    -- 管理端分页「按名称模糊 + 按更新时间倒序」避免全表 filesort
    INDEX idx_category_status (category_id, status),
    INDEX idx_update_time (update_time)
) COMMENT '菜品表';

-- 菜品口味表
CREATE TABLE dish_flavor (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    dish_id BIGINT       NOT NULL COMMENT '菜品id',
    name    VARCHAR(32)  NOT NULL COMMENT '口味名称',
    value   VARCHAR(255) NULL COMMENT '口味取值(JSON数组字符串)',
    INDEX idx_dish_id (dish_id)
) COMMENT '菜品口味表';

-- 套餐表
CREATE TABLE setmeal (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)   NOT NULL COMMENT '套餐名称',
    category_id BIGINT        NOT NULL COMMENT '分类id',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格',
    image       VARCHAR(200)  NULL COMMENT '图片',
    description VARCHAR(400)  NULL COMMENT '描述',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 0停售 1起售',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_status (category_id, status),
    INDEX idx_update_time (update_time)
) COMMENT '套餐表';

-- 套餐菜品关联表
CREATE TABLE setmeal_dish (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    setmeal_id BIGINT        NOT NULL COMMENT '套餐id',
    dish_id    BIGINT        NOT NULL COMMENT '菜品id',
    name       VARCHAR(64)   NULL COMMENT '菜品名称冗余',
    price      DECIMAL(10,2) NULL COMMENT '菜品单价冗余',
    copies     INT           NOT NULL DEFAULT 1 COMMENT '份数',
    INDEX idx_setmeal_id (setmeal_id),
    INDEX idx_dish_id (dish_id)
) COMMENT '套餐菜品关联表';

-- 购物车表
CREATE TABLE shopping_cart (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL COMMENT '用户id',
    dish_id     BIGINT        NULL COMMENT '菜品id',
    setmeal_id  BIGINT        NULL COMMENT '套餐id',
    name        VARCHAR(64)   NOT NULL COMMENT '名称',
    image       VARCHAR(200)  NULL COMMENT '图片',
    dish_flavor VARCHAR(200)  NULL COMMENT '口味',
    amount      DECIMAL(10,2) NOT NULL COMMENT '单价',
    number      INT           NOT NULL DEFAULT 1 COMMENT '数量',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '购物车表';

-- 订单表
CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    number       VARCHAR(50)   NOT NULL COMMENT '订单号',
    user_id      BIGINT        NOT NULL COMMENT '用户id',
    consignee    VARCHAR(32)   NOT NULL COMMENT '收货人',
    phone        VARCHAR(20)   NOT NULL COMMENT '联系电话',
    address      VARCHAR(255)  NOT NULL COMMENT '收货地址',
    remark       VARCHAR(255)  NULL COMMENT '备注',
    amount       DECIMAL(10,2) NOT NULL COMMENT '实收金额',
    pay_status   TINYINT       NOT NULL DEFAULT 0 COMMENT '支付状态 0未支付 1已支付',
    pay_method   TINYINT       NULL COMMENT '支付方式 1微信 2支付宝',
    status       TINYINT       NOT NULL DEFAULT 1 COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
    order_time   DATETIME      NOT NULL COMMENT '下单时间',
    checkout_time DATETIME     NULL COMMENT '支付时间',
    cancel_time  DATETIME      NULL COMMENT '取消时间',
    cancel_reason VARCHAR(255) NULL COMMENT '取消原因',
    rejection_reason VARCHAR(255) NULL COMMENT '拒单原因',
    -- 联合索引1：C端历史订单查询 WHERE user_id = ? ORDER BY order_time DESC
    -- 联合索引2：管理端订单搜索 WHERE status = ? AND order_time BETWEEN ? AND ?
    UNIQUE KEY uk_number (number),
    INDEX idx_user_order_time (user_id, order_time),
    INDEX idx_status_order_time (status, order_time)
) COMMENT '订单表';

-- 订单明细表
CREATE TABLE order_detail (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT        NOT NULL COMMENT '订单id',
    dish_id    BIGINT        NULL COMMENT '菜品id',
    setmeal_id BIGINT        NULL COMMENT '套餐id',
    name       VARCHAR(64)   NOT NULL COMMENT '名称',
    image      VARCHAR(200)  NULL COMMENT '图片',
    amount     DECIMAL(10,2) NOT NULL COMMENT '单价',
    number     INT           NOT NULL DEFAULT 1 COMMENT '数量',
    INDEX idx_order_id (order_id)
) COMMENT '订单明细表';

-- 操作日志表（AOP 自动记录）
CREATE TABLE operation_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    oper_user   VARCHAR(32)  NULL COMMENT '操作人',
    oper_module VARCHAR(64)  NULL COMMENT '模块',
    oper_type   VARCHAR(32)  NULL COMMENT '操作类型',
    oper_method VARCHAR(200) NULL COMMENT '方法名',
    oper_params TEXT         NULL COMMENT '请求参数',
    oper_time   DATETIME     NOT NULL COMMENT '操作时间',
    cost_time   INT          NULL COMMENT '耗时(ms)'
) COMMENT '操作日志表';

-- ============================================================
-- 种子数据（密码均为 123456 的 SHA-256）
-- ============================================================
INSERT INTO employee (name, username, password, phone, status) VALUES
('管理员', 'admin', SHA2('123456', 256), '13800000001', 1);

INSERT INTO user (username, password, phone, status) VALUES
('zhangsan', SHA2('123456', 256), '13900000001', 1),
('lisi',     SHA2('123456', 256), '13900000002', 1);

INSERT INTO category (name, type, sort) VALUES
('凉菜', 1, 1), ('热菜', 1, 2), ('主食', 1, 3), ('汤羹', 1, 4),
('甜品', 1, 5), ('超值套餐', 2, 6);

INSERT INTO dish (name, category_id, price, description, status) VALUES
('拍黄瓜',     1, 8.00,  '清爽开胃', 1),
('凉拌木耳',   1, 12.00, '酸辣爽口', 1),
('宫保鸡丁',   2, 32.00, '经典川菜', 1),
('鱼香肉丝',   2, 28.00, '家常下饭', 1),
('红烧牛肉',   2, 48.00, '慢火炖制', 1),
('扬州炒饭',   3, 18.00, '粒粒分明', 1),
('牛肉拉面',   3, 22.00, '手工现拉', 1),
('西红柿蛋汤', 4, 10.00, '酸甜暖胃', 1),
('酸辣汤',     4, 12.00, '开胃解腻', 1),
('芒果布丁',   5, 15.00, '当日现做', 1),
('老坛酸菜鱼', 2, 58.00, '鲈鱼现杀', 0);

INSERT INTO dish_flavor (dish_id, name, value) VALUES
(3, '辣度', '["不辣","微辣","中辣","重辣"]'),
(4, '辣度', '["不辣","微辣","中辣"]'),
(6, '份量', '["单人份","双人份"]'),
(7, '份量', '["小份","大份"]'),
(11, '辣度', '["微辣","中辣","重辣"]');

INSERT INTO setmeal (name, category_id, price, description, status) VALUES
('双人超值套餐A', 6, 68.00, '两荤一汤一主食', 1),
('单人工作餐套餐', 6, 29.90, '一荤一主食', 1);

INSERT INTO setmeal_dish (setmeal_id, dish_id, name, price, copies) VALUES
(1, 3, '宫保鸡丁', 32.00, 1),
(1, 5, '红烧牛肉', 48.00, 1),
(1, 8, '西红柿蛋汤', 10.00, 1),
(1, 6, '扬州炒饭', 18.00, 2),
(2, 4, '鱼香肉丝', 28.00, 1),
(2, 6, '扬州炒饭', 18.00, 1);
