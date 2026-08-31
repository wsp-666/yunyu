/*
 Navicat Premium Data Transfer

 Source Server         : slh
 Source Server Type    : MySQL
 Source Server Version : 80400
 Source Host           : localhost:3306
 Source Schema         : yunyu

 Target Server Type    : MySQL
 Target Server Version : 80400
 File Encoding         : 65001

 Date: 27/05/2026 08:45:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商品编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `category` tinyint(1) NOT NULL COMMENT '品类（1竿/2轮/3线/4漂/5饵/6配件）',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `price` decimal(10, 2) NOT NULL COMMENT '售价',
  `points_price` int NULL DEFAULT 500 COMMENT '兑换所需积分',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `images` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `sales_count` int NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态（0下架/1上架）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上架时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_status`(`category` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_product
-- ----------------------------
INSERT INTO `t_product` VALUES (1, '光威 赤刃鲫 3.6米 鲫鱼竿', 1, '光威', 159.00, 200, 80, '/images/products/guangwei_295059a2.jpg', '碳素鲫鱼竿，37调性，自重78g，先径1.0mm/元径10.5mm，适合野钓鲫鱼，手感轻盈灵敏，新手入门首选', 2156, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (2, '光威 龙云五代 4.5米 综合竿', 1, '光威', 359.00, 200, 60, '/images/products/guangwei_64765e5c.jpg', '2025新款，28调性，5节设计，轻量化高碳布，自重120g，适合野钓鲫鲤草鳊，综合性能优秀', 1234, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (3, '光威 龙云五代 5.4米 综合竿', 1, '光威', 429.00, 200, 50, '/images/products/guangwei_fbe37370.jpg', '28调性，自重158g，大五节设计，腰力强劲，适合水库江河综合钓法', 892, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (4, '光威 龙云五代 6.3米 综合竿', 1, '光威', 519.00, 400, 40, '/images/products/guangwei_6cd08117.jpg', '28调性，自重208g，加强竿壁，适合库钓大物，抗拉强度高', 567, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (5, '光威 赤刃大物版 7.2米', 1, '光威', 299.00, 200, 35, '/images/products/guangwei_993f1611.jpg', '28偏19调性，加厚碳布，自重289g，专攻水库巨物，抗暴力操作', 432, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (6, '达亿瓦 一击 4.5米 台钓竿', 1, '达亿瓦', 899.00, 400, 30, '/images/products/abu_veritas_rod.jpg', '进口碳布，仿竹涂装，大五节设计，28调性，自重108g，腰力浑厚', 567, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (7, '达亿瓦 LEGALIS LT2500 纺车轮', 2, '达亿瓦', 350.00, 200, 40, '/images/products/abu_revox_voltiq.jpg', '5+1轴承，2500型，ZAION V机身轻量化，ATD刹车，淡海水通用，性价比极高', 1234, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (8, '光威 速龙 2000型 纺车轮', 2, '光威', 89.00, 100, 100, '/images/products/guangwei_6fd87e31.jpg', '4+1轴承，金属线杯，2000型，性价比爆表，适合新手入门练手', 4567, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (9, '光威 速龙 3000型 纺车轮', 2, '光威', 99.00, 100, 90, '/images/products/guangwei_d9a0f127.jpg', '4+1轴承，3000型，大线杯，适合水库江河，皮实耐用', 3456, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (10, '阿布 Garcia BMAX3 水滴轮', 2, '阿布', 420.00, 200, 35, '/images/products/abu_revosx.jpg', '7+1轴承，磁力刹车，轻量石墨机身，精准抛投，路亚入门神器', 1234, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (11, '海伯 战马5000 海钓纺车轮', 2, '海伯', 280.00, 200, 20, '/images/products/abu_beast200.jpg', '全金属机身，强力碳纤维刹车，5000型大物轮，海钓利器', 345, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (12, '达亿瓦 TATULA SV TW 103 水滴轮', 2, '达亿瓦', 1250.00, 800, 15, '/images/products/abu_revorocket.jpg', 'SV刹车系统，TWS线规，8+1轴承，零刹车干预远投，竞技级水滴轮', 234, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (13, '禧玛诺 SLX DC 150HG 水滴轮', 2, '禧玛诺', 1680.00, 800, 10, '/images/products/abu_revostx.jpg', 'DC电子刹车系统，I-DC5智能控制，HAGANE机身，炸线克星', 178, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (14, '光威 竞技抄网 2.1米 碳素竿+深网头', 6, '光威', 68.00, 100, 120, '/images/products/guangwei_13a27c41.jpg', '碳素抄网竿2.1m+40cm深网头，轻便结实不伤鱼，抄大鱼不费力', 2345, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (15, '汉鼎 双层竿包 1.25米 防水牛津布', 6, '汉鼎', 89.00, 100, 100, '/images/products/guangwei_1504b128.jpg', '防水耐磨牛津布，双层大容量可放4-6支竿，侧面支架袋，肩带舒适', 3456, 1, '2026-05-27 08:35:55');
INSERT INTO `t_product` VALUES (16, '光威 钓伞 2.2米 防风万向调节', 6, '光威', 128.00, 200, 80, '/images/products/guangwei_ecdccd77.jpg', '2.2m大伞面防泼水，万向调节铝合金伞骨，防风加固地插，防晒防雨', 1234, 1, '2026-05-27 08:35:55');

SET FOREIGN_KEY_CHECKS = 1;
