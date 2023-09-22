/*
 Navicat Premium Data Transfer

 Source Server         : mysql8
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3308
 Source Schema         : bbs

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 22/09/2023 17:02:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bbs_message
-- ----------------------------
DROP TABLE IF EXISTS `bbs_message`;
CREATE TABLE `bbs_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NULL DEFAULT NULL,
  `topic_id` int(11) NULL DEFAULT NULL,
  `status` tinyint(2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bbs_message
-- ----------------------------
INSERT INTO `bbs_message` VALUES (1, 1, 59, 1);
INSERT INTO `bbs_message` VALUES (2, 1, 73, 1);
INSERT INTO `bbs_message` VALUES (3, 97, 71, 1);
INSERT INTO `bbs_message` VALUES (4, 95, 78, 1);
INSERT INTO `bbs_message` VALUES (5, 99, 79, 1);
INSERT INTO `bbs_message` VALUES (6, 95, 75, 0);

-- ----------------------------
-- Table structure for bbs_module
-- ----------------------------
DROP TABLE IF EXISTS `bbs_module`;
CREATE TABLE `bbs_module`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `detail` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `turn` tinyint(2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bbs_module
-- ----------------------------
INSERT INTO `bbs_module` VALUES (1, '公告', '', 1);
INSERT INTO `bbs_module` VALUES (2, '讨论区', NULL, 2);

-- ----------------------------
-- Table structure for bbs_post
-- ----------------------------
DROP TABLE IF EXISTS `bbs_post`;
CREATE TABLE `bbs_post`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `has_reply` bit(1) NOT NULL DEFAULT b'0',
  `update_time` timestamp NULL DEFAULT NULL,
  `pros` int(11) NULL DEFAULT 0,
  `cons` int(11) NULL DEFAULT 0,
  `is_accept` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `topicID_P`(`topic_id` ASC) USING BTREE,
  INDEX `userID_P`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 269 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bbs_post
-- ----------------------------
INSERT INTO `bbs_post` VALUES (269, 78, 95, '<p>秦韵——陕西方言文化推广平台陕西方言推广论坛提供了用户友好的账户管理,帖子和评论互动,高效的搜索和标签系统,多媒体支持,私信通知机制,方言学习资源,以及坚实的社区规则和技术支持,旨在鼓励用户学习,分享和交流陕西方言,为方言的传承和推广提供强有力的在线支持。<br></p>', '2023-09-22 12:58:16', b'0', NULL, 1, 0, 0);
INSERT INTO `bbs_post` VALUES (270, 79, 95, '<p>陕西方言是中国方言中的一支，主要分布在陕西省及周边地区。它具有悠久的历史和独特的特点，是陕西地域文化的重要组成部分。陕西方言在音韵、词汇和语法上与普通话有着明显的差异。在音韵方面，陕西方言以浊音和重音为特点，发音较为浑厚有力。词汇方面，陕西方言保留了许多古代汉语的词汇，使其在语言表达上更具古朴韵味。此外，陕西方言还有丰富的方言词语和俚语，独特而生动地反映了当地人民的生活和文化。陕西方言也是中国文化的重要代表之一。陕西作为中国历史文化名城，方言中融入了大量的历史典故、传统文化和民间故事。方言歌曲、方言戏曲等艺术形式，以其独特的韵味和表达方式吸引着广大观众。<br></p>', '2023-09-22 16:45:33', b'0', NULL, 0, 0, 0);
INSERT INTO `bbs_post` VALUES (271, 79, 95, '<p>对的</p>', '2023-09-22 16:45:42', b'0', NULL, 0, 0, 0);

-- ----------------------------
-- Table structure for bbs_reply
-- ----------------------------
DROP TABLE IF EXISTS `bbs_reply`;
CREATE TABLE `bbs_reply`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic_id` int(11) NOT NULL DEFAULT 1,
  `post_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `content` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `topicID_R`(`topic_id` ASC) USING BTREE,
  INDEX `postID_R`(`post_id` ASC) USING BTREE,
  INDEX `userID_R`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bbs_reply
-- ----------------------------

-- ----------------------------
-- Table structure for bbs_topic
-- ----------------------------
DROP TABLE IF EXISTS `bbs_topic`;
CREATE TABLE `bbs_topic`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `module_id` int(11) NOT NULL,
  `post_count` int(11) NOT NULL DEFAULT 1,
  `reply_count` int(11) NOT NULL DEFAULT 0,
  `pv` int(11) NOT NULL DEFAULT 0,
  `content` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `emotion` tinyint(2) NULL DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_nice` bit(1) NOT NULL DEFAULT b'0',
  `is_up` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `moduleID_T`(`module_id` ASC) USING BTREE,
  INDEX `userID_T`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 78 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bbs_topic
-- ----------------------------
INSERT INTO `bbs_topic` VALUES (78, 95, 1, 1, 0, 3, 'QinYun论坛介绍', NULL, '2023-09-22 12:58:16', b'0', b'0');
INSERT INTO `bbs_topic` VALUES (79, 95, 1, 2, 0, 2, '弘扬陕西方言文化', NULL, '2023-09-22 16:45:33', b'0', b'0');

-- ----------------------------
-- Table structure for bbs_user
-- ----------------------------
DROP TABLE IF EXISTS `bbs_user`;
CREATE TABLE `bbs_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `score` int(11) NULL DEFAULT 0 COMMENT '积分',
  `level` int(11) NULL DEFAULT 1 COMMENT '积分换算成等级，新生，老生，班主任，教导主任，校长',
  `balance` int(11) NULL DEFAULT 0 COMMENT '积分余额',
  `corp` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bbs_user
-- ----------------------------
INSERT INTO `bbs_user` VALUES (1, 'xxx', 'delete', 'xxx', 54, 1, 54, NULL);
INSERT INTO `bbs_user` VALUES (4, '李家智', 'e10adc3949ba59abbe56e057f20f883e', NULL, 140, 2, 0, NULL);
INSERT INTO `bbs_user` VALUES (5, '赵晴文', 'e10adc3949ba59abbe56e057f20f883e', 'zhaoqingwen@coamc.com', 1000, 5, 0, NULL);
INSERT INTO `bbs_user` VALUES (6, '石萌', 'e10adc3949ba59abbe56e057f20f883e', 'shimeng@coamc.com', 12, 1, 0, NULL);
INSERT INTO `bbs_user` VALUES (95, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'xxxx@coamc.com', 278, 3, 278, NULL);
INSERT INTO `bbs_user` VALUES (96, 'lijiazhi', '202cb962ac59075b964b07152d234b70', '123@123.com', 0, 1, NULL, 'it');
INSERT INTO `bbs_user` VALUES (97, 'hank', 'e10adc3949ba59abbe56e057f20f883e', 'hank@163.com', 22, 1, 22, 'dfdsf');
INSERT INTO `bbs_user` VALUES (98, 'test1', 'e10adc3949ba59abbe56e057f20f883e', '123@123.com', 0, 0, 0, '11');
INSERT INTO `bbs_user` VALUES (99, 'test11', 'f696282aa4cd4f614aa995190cf442fe', 'test1@163.com', 29, 1, 29, '天天公司');
INSERT INTO `bbs_user` VALUES (100, 'adb', 'e10adc3949ba59abbe56e057f20f883e', 'xxx@126.com', 29, 1, 29, 'cc');

SET FOREIGN_KEY_CHECKS = 1;
