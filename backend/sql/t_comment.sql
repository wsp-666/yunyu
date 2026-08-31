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

 Date: 31/05/2026 13:29:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_comment
-- ----------------------------
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评论编号',
  `post_id` int NOT NULL COMMENT '帖子ID',
  `user_id` int NOT NULL COMMENT '评论用户ID',
  `parent_id` int NULL DEFAULT 0 COMMENT '父评论ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_comment
-- ----------------------------
INSERT INTO `t_comment` VALUES (1, 1, 2, 0, '太强了！千岛湖一直想去，看了你的帖子更想去了！', '2026-05-25 10:30:00');
INSERT INTO `t_comment` VALUES (2, 1, 4, 0, '银色亮片确实是翘嘴杀手，我也经常用', '2026-05-25 11:15:00');
INSERT INTO `t_comment` VALUES (3, 1, 3, 2, '亮片克重要根据水深选，深水用重克数', '2026-05-25 12:00:00');
INSERT INTO `t_comment` VALUES (4, 2, 1, 0, '好详细！大板鲫+蓝鲫确实经典搭配', '2026-05-24 15:00:00');
INSERT INTO `t_comment` VALUES (5, 2, 5, 0, '问下调四钓二具体的调漂步骤？', '2026-05-24 16:20:00');
INSERT INTO `t_comment` VALUES (6, 2, 3, 5, '先把漂座撸到铅皮座旁边，重铅找底，然后下拉浮漂超过子线长度，剪铅皮到露出四目，挂饵再找底到露出两目', '2026-05-24 17:00:00');
INSERT INTO `t_comment` VALUES (7, 3, 3, 0, '光威赤刃鲫我也在用，性价比确实高', '2026-05-23 09:30:00');
INSERT INTO `t_comment` VALUES (8, 3, 1, 0, '3.6米会不会太短了？野钓够用吗？', '2026-05-23 10:00:00');
INSERT INTO `t_comment` VALUES (9, 5, 4, 0, '黑坑抢鱼写得很好！补充一点：早上第一波鱼口过了以后，换细线小钩效果更好', '2026-05-22 14:00:00');
INSERT INTO `t_comment` VALUES (10, 5, 1, 0, '果酸+红薯膏，懂行！天热的时候再加点药酒', '2026-05-22 14:30:00');
INSERT INTO `t_comment` VALUES (11, 6, 2, 0, '冬天钓鱼就是找鱼，找不到鱼窝一天都白费', '2026-05-21 08:00:00');
INSERT INTO `t_comment` VALUES (12, 6, 5, 0, '红虫效果是好，但是容易闹小鱼怎么办？', '2026-05-21 09:00:00');
INSERT INTO `t_comment` VALUES (13, 6, 3, 12, '冬天小鱼基本不闹，如果闹的话换大漂重铅快速到底', '2026-05-21 09:30:00');
INSERT INTO `t_comment` VALUES (14, 7, 1, 0, '恭喜第三名！倒钓钓组确实稳，我也爱用', '2026-05-20 18:00:00');
INSERT INTO `t_comment` VALUES (15, 7, 3, 0, '三岔湖路亚基地去过，环境很好，就是有点远', '2026-05-20 19:00:00');
INSERT INTO `t_comment` VALUES (16, 8, 3, 0, '罗非用冷冻饵！淘宝搜\"罗非冷冻饵\"，鸡肝味的效果最好', '2026-05-19 20:00:00');
INSERT INTO `t_comment` VALUES (17, 8, 4, 0, '南沙那边11月-3月罗非难钓，4-10月好钓', '2026-05-19 20:30:00');
INSERT INTO `t_comment` VALUES (18, 8, 1, 0, '钓罗非要找浅水+有障碍物的地方，它们喜欢躲在那里', '2026-05-19 21:00:00');
INSERT INTO `t_comment` VALUES (19, 9, 1, 0, '新手最简单的：调平水钓两目，挂蚯蚓或者搓饵都行，万金油', '2026-05-18 12:00:00');
INSERT INTO `t_comment` VALUES (20, 9, 3, 0, '去b站搜\"调漂教程\"，看视频比看文字清楚多了', '2026-05-18 13:00:00');
INSERT INTO `t_comment` VALUES (21, 10, 2, 0, '三月密云水库鲫鱼就开始靠边了？下周我也去试试', '2026-05-17 10:00:00');
INSERT INTO `t_comment` VALUES (22, 11, 5, 0, '哈哈哈，带老婆钓鱼是好办法，让她也爱上钓鱼以后经费好批！', '2026-05-16 21:00:00');
INSERT INTO `t_comment` VALUES (23, 11, 3, 0, '七夕夜钓也太浪漫了吧！', '2026-05-16 22:00:00');
INSERT INTO `t_comment` VALUES (24, 12, 1, 0, '好文！跳底是路亚的基本功，练好了什么鱼都能路', '2026-05-15 15:00:00');
INSERT INTO `t_comment` VALUES (25, 13, 4, 0, '雷强打黑是上瘾！那个炸水的感觉没有别的钓法能比', '2026-05-14 16:00:00');
INSERT INTO `t_comment` VALUES (26, 14, 5, 0, '双十一多少入的？我参考一下', '2026-05-13 20:00:00');
INSERT INTO `t_comment` VALUES (27, 14, 2, 5, '双十一789入的，平时卖899', '2026-05-13 21:00:00');
INSERT INTO `t_comment` VALUES (28, 15, 3, 0, '滇池现在禁钓区扩大了，海埂公园那边不能钓了，要去呈贡那边才行', '2026-05-12 18:00:00');

SET FOREIGN_KEY_CHECKS = 1;
