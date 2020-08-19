/*
Navicat MySQL Data Transfer

Source Server         : 192.168.11.10
Source Server Version : 50730
Source Host           : 192.168.11.10:3306
Source Database       : test

Target Server Type    : MYSQL
Target Server Version : 50730
File Encoding         : 65001

Date: 2020-08-18 14:51:52
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `user_self`
-- ----------------------------
DROP TABLE IF EXISTS `user_self`;
CREATE TABLE `user_self` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(60) DEFAULT NULL,
  `enable` tinyint(4) DEFAULT '1',
  `roles` text COMMENT '用户角色，多个角色之间用都好隔开',
  PRIMARY KEY (`id`),
  KEY `username` (`username`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_self
-- ----------------------------
INSERT INTO `user_self` VALUES ('1', 'admin', '123', '1', 'ROLE_ADMIN,ROLE_USER');
INSERT INTO `user_self` VALUES ('2', 'user', '123', '1', 'ROLE_USER');
