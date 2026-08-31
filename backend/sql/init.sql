-- ============================================
-- AI钓鱼大师 数据库建表脚本
-- 数据库名：ai_fishing_master
-- 字符集：utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS yunyu
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE yunyu;

-- 1. 用户表
CREATE TABLE t_user (
                        id INT(11) NOT NULL AUTO_INCREMENT COMMENT '用户编号',
                        account VARCHAR(50) NOT NULL COMMENT '账户名',
                        password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
                        nickname VARCHAR(50) COMMENT '昵称',
                        phone VARCHAR(20) COMMENT '手机号',
                        avatar VARCHAR(255) COMMENT '头像URL',
                        member_level TINYINT(1) NOT NULL DEFAULT 0 COMMENT '会员等级（0普通/1低级/2高级）',
                        member_expire DATETIME COMMENT '会员到期时间',
                        points INT(11) NOT NULL DEFAULT 0 COMMENT '积分余额',
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        last_login_time DATETIME COMMENT '最后登录时间',
                        lock_state CHAR(1) NOT NULL DEFAULT '1' COMMENT '锁定状态（0锁定/1正常）',
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_account (account),
                        KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 管理员表
CREATE TABLE t_admin (
                         id INT(11) NOT NULL AUTO_INCREMENT COMMENT '管理员编号',
                         account VARCHAR(50) NOT NULL COMMENT '管理员账号',
                         password VARCHAR(255) NOT NULL COMMENT '管理员密码',
                         name VARCHAR(50) NOT NULL COMMENT '管理员姓名',
                         role TINYINT NOT NULL DEFAULT 0 COMMENT '角色(0小管理员/1最终管理员)',
                         create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 3. 钓点信息表
CREATE TABLE t_fishing_spot (
                                id INT(11) NOT NULL AUTO_INCREMENT COMMENT '钓点编号',
                                name VARCHAR(100) NOT NULL COMMENT '钓点名称',
                                longitude DECIMAL(10,7) NOT NULL COMMENT '经度',
                                latitude DECIMAL(10,7) NOT NULL COMMENT '纬度',
                                type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '类型（1野钓/2黑坑）',
                                fish_species VARCHAR(255) COMMENT '主要鱼种',
                                water_quality VARCHAR(50) COMMENT '水质描述',
                                depth_desc VARCHAR(50) COMMENT '水深描述',
                                fee_desc VARCHAR(255) COMMENT '收费描述',
                                env_desc TEXT COMMENT '环境描述',
                                navigation VARCHAR(255) COMMENT '导航指引',
                                images VARCHAR(500) COMMENT '图片URL',
                                status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '审核状态（0待审/1通过/2驳回）',
                                reject_reason VARCHAR(255) COMMENT '驳回原因',
                                submit_user_id INT(11) NOT NULL COMMENT '提交用户ID',
                                view_count INT(11) NOT NULL DEFAULT 0 COMMENT '浏览次数',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_time DATETIME COMMENT '更新时间',
                                PRIMARY KEY (id),
                                KEY idx_lng_lat (longitude, latitude),
                                KEY idx_submit_user (submit_user_id),
                                KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钓点信息表';

-- 4. 钓场表
CREATE TABLE t_fishing_venue (
                                 id INT(11) NOT NULL AUTO_INCREMENT COMMENT '钓场编号',
                                 name VARCHAR(100) NOT NULL COMMENT '钓场名称',
                                 owner_id INT(11) NOT NULL COMMENT '塘主用户ID',
                                 address VARCHAR(255) NOT NULL COMMENT '详细地址',
                                 longitude DECIMAL(10,7) NOT NULL COMMENT '经度',
                                 latitude DECIMAL(10,7) NOT NULL COMMENT '纬度',
                                 phone VARCHAR(20) COMMENT '联系电话',
                                 cover_image VARCHAR(255) COMMENT '封面图URL',
                                 seat_map VARCHAR(255) COMMENT '钓位布局图URL',
                                 rule_desc TEXT COMMENT '钓场规则描述',
                                 total_seats INT(3) NOT NULL DEFAULT 0 COMMENT '总钓位数',
                                 follower_count INT(11) NOT NULL DEFAULT 0 COMMENT '关注人数',
                                 status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态（0待审/1营业/2歇业）',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (id),
                                 KEY idx_owner (owner_id),
                                 KEY idx_lng_lat (longitude, latitude),
                                 KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钓场表';

-- 5. 放鱼/上鱼视频表
CREATE TABLE t_fish_video (
                              id INT(11) NOT NULL AUTO_INCREMENT COMMENT '视频编号',
                              venue_id INT(11) NOT NULL COMMENT '所属钓场ID',
                              type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '类型（1放鱼视频/2上鱼视频）',
                              video_url VARCHAR(255) NOT NULL COMMENT '视频存储URL',
                              cover_url VARCHAR(255) COMMENT '封面图URL',
                              fish_count INT(11) COMMENT '放鱼数量',
                              fish_species VARCHAR(100) COMMENT '鱼种',
                              fish_size_desc VARCHAR(50) COMMENT '个体大小描述',
                              fishing_time DATETIME COMMENT '正钓时间',
                              ticket_price DECIMAL(10,2) COMMENT '票价',
                              like_count INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
                              upload_user_id INT(11) NOT NULL COMMENT '上传者ID',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                              PRIMARY KEY (id),
                              KEY idx_venue_type_time (venue_id, type, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='放鱼/上鱼视频表';

-- 6. 场次表
CREATE TABLE t_session (
                           id INT(11) NOT NULL AUTO_INCREMENT COMMENT '场次编号',
                           venue_id INT(11) NOT NULL COMMENT '所属钓场ID',
                           name VARCHAR(100) NOT NULL COMMENT '场次名称',
                           ticket_price DECIMAL(10,2) NOT NULL COMMENT '票价',
                           total_seats INT(3) NOT NULL COMMENT '总钓位数',
                           remain_seats INT(3) NOT NULL COMMENT '剩余钓位数',
                           start_time DATETIME NOT NULL COMMENT '开始时间',
                           end_time DATETIME NOT NULL COMMENT '结束时间',
                           draw_time DATETIME COMMENT '抽位截止时间',
                           status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态（0未开始/1可报名/2报名中/3进行中/4已结束）',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           PRIMARY KEY (id),
                           KEY idx_venue_status (venue_id, status),
                           KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场次表';

-- 7. 订单表
CREATE TABLE t_order (
                         id INT(11) NOT NULL AUTO_INCREMENT COMMENT '订单编号',
                         order_no VARCHAR(32) NOT NULL COMMENT '订单号',
                         user_id INT(11) NOT NULL COMMENT '用户ID',
                         session_id INT(11) NOT NULL COMMENT '场次ID',
                         venue_id INT(11) NOT NULL COMMENT '钓场ID',
                         product_id INT(11) COMMENT '兑换商品ID',
                         seat_no INT(3) COMMENT '抽选钓位号',
                         amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
                         pay_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '支付状态（0未付/1已付/2退款）',
                         pay_time DATETIME COMMENT '支付时间',
                         start_time DATETIME COMMENT '开竿时间',
                         end_time DATETIME COMMENT '收竿时间',
                         catch_weight DECIMAL(8,2) COMMENT '钓获总重量（斤）',
                         return_amount DECIMAL(10,2) COMMENT '回鱼金额',
                         total_fee DECIMAL(10,2) COMMENT '实际结算费用',
                         create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_order_no (order_no),
                         KEY idx_user (user_id),
                         KEY idx_session (session_id),
                         KEY idx_venue (venue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 8. 社区帖子表
CREATE TABLE t_post (
                        id INT(11) NOT NULL AUTO_INCREMENT COMMENT '帖子编号',
                        user_id INT(11) NOT NULL COMMENT '发布用户ID',
                        type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '类型（1渔获/2装备/3钓法/4比赛/5求助）',
                        title VARCHAR(100) COMMENT '标题',
                        content TEXT NOT NULL COMMENT '正文内容',
                        images VARCHAR(500) COMMENT '图片URL',
                        video_url VARCHAR(255) COMMENT '视频URL',
                        spot_id INT(11) COMMENT '关联钓点ID',
                        venue_id INT(11) COMMENT '关联钓场ID',
                        topic_tag VARCHAR(50) COMMENT '话题标签',
                        weather_info VARCHAR(100) COMMENT '发布时天气信息',
                        like_count INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
                        comment_count INT(11) NOT NULL DEFAULT 0 COMMENT '评论数',
                        collect_count INT(11) NOT NULL DEFAULT 0 COMMENT '收藏数',
                        status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（0待审/1正常/2删除）',
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                        PRIMARY KEY (id),
                        KEY idx_type_status_time (type, status, create_time),
                        KEY idx_user (user_id),
                        KEY idx_topic_tag (topic_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- 9. 评论表
CREATE TABLE t_comment (
                           id INT(11) NOT NULL AUTO_INCREMENT COMMENT '评论编号',
                           post_id INT(11) NOT NULL COMMENT '帖子ID',
                           user_id INT(11) NOT NULL COMMENT '评论用户ID',
                           parent_id INT(11) DEFAULT 0 COMMENT '父评论ID',
                           content TEXT NOT NULL COMMENT '评论内容',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                           PRIMARY KEY (id),
                           KEY idx_post (post_id),
                           KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 10. 积分明细表
CREATE TABLE t_points_log (
                              id INT(11) NOT NULL AUTO_INCREMENT COMMENT '记录编号',
                              user_id INT(11) NOT NULL COMMENT '用户ID',
                              points INT(4) NOT NULL COMMENT '积分变动（正为获取，负为消费）',
                              type TINYINT(1) NOT NULL COMMENT '类型（1分享钓点/2帮助他人/3每日签到/4积分兑换/5系统奖励）',
                              ref_id INT(11) COMMENT '关联业务ID',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
                              PRIMARY KEY (id),
                              KEY idx_user_type_time (user_id, type, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分明细表';

-- 11. 商品表
CREATE TABLE t_product (
                           id INT(11) NOT NULL AUTO_INCREMENT COMMENT '商品编号',
                           name VARCHAR(100) NOT NULL COMMENT '商品名称',
                           category TINYINT(1) NOT NULL COMMENT '品类（1竿/2轮/3线/4漂/5饵/6配件）',
                           brand VARCHAR(50) COMMENT '品牌',
                           price DECIMAL(10,2) NOT NULL COMMENT '售价',
                           stock INT(11) NOT NULL DEFAULT 0 COMMENT '库存',
                           images VARCHAR(500) COMMENT '图片URL',
                           description TEXT COMMENT '商品描述',
                           sales_count INT(11) NOT NULL DEFAULT 0 COMMENT '销量',
                           status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（0下架/1上架）',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上架时间',
                           PRIMARY KEY (id),
                           KEY idx_category_status (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 12. 关注关系表
CREATE TABLE t_follow (
                          id INT(11) NOT NULL AUTO_INCREMENT COMMENT '记录编号',
                          user_id INT(11) NOT NULL COMMENT '关注用户ID',
                          target_type TINYINT(1) NOT NULL COMMENT '目标类型（1用户/2钓场）',
                          target_id INT(11) NOT NULL COMMENT '目标ID',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系表';

-- 13. 通知消息表
CREATE TABLE t_message (
                           id INT(11) NOT NULL AUTO_INCREMENT COMMENT '消息编号',
                           user_id INT(11) NOT NULL COMMENT '接收用户ID',
                           type TINYINT(1) NOT NULL COMMENT '类型（1放鱼通知/2评论通知/3点赞通知/4关注通知/5系统通知）',
                           title VARCHAR(100) NOT NULL COMMENT '消息标题',
                           content VARCHAR(255) NOT NULL COMMENT '消息内容',
                           ref_type VARCHAR(20) COMMENT '关联类型',
                           ref_id INT(11) COMMENT '关联ID',
                           is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0未读/1已读）',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                           PRIMARY KEY (id),
                           KEY idx_user_read_time (user_id, is_read, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

ALTER TABLE t_user ADD COLUMN role TINYINT NOT NULL DEFAULT 0 COMMENT '角色(0普通用户/1塘主)' AFTER id;

ALTER TABLE t_fishing_venue ADD COLUMN images VARCHAR(500) DEFAULT NULL COMMENT '更多图片URL(逗号分隔)';

ALTER TABLE t_admin ADD COLUMN role TINYINT NOT NULL DEFAULT 0;
ALTER TABLE t_admin ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
UPDATE t_admin SET role = 1 WHERE account = 'admin';

INSERT INTO t_admin (account, password, name, role) VALUES
    ('admin', '0192023a7bbd73250516f069df18b500', '超级管理员', 1);

-- 小管理员（普通管理员，无管理员管理权限）
-- 账号: admin2  密码: admin888
INSERT INTO t_admin (account, password, name, role) VALUES
    ('admin2', '7fef6171469e80d32c0559f88b377245', '小管理员', 0);