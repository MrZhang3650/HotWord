-- 创建预处理日志表
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `preprocess_log`;
CREATE TABLE `preprocess_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据类型（hotword/commodity）',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `total_count` int NULL DEFAULT NULL COMMENT '总处理记录数',
  `clean_count` int NULL DEFAULT NULL COMMENT '清洗记录数',
  `outlier_count` int NULL DEFAULT NULL COMMENT '剔除异常数',
  `vector_count` int NULL DEFAULT NULL COMMENT '向量化完成数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理状态（success/failed）',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '日志详细信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_data_type`(`data_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预处理日志表' ROW_FORMAT = Dynamic;

-- 创建商品聚类结果表
DROP TABLE IF EXISTS `commodity_cluster`;
CREATE TABLE `commodity_cluster`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cluster_id` int NOT NULL COMMENT '聚类分组ID（1,2,3...）',
  `item_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品唯一ID',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品标题',
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联热词',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `sales` int NULL DEFAULT 0 COMMENT '销量',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ebay' COMMENT '来源平台',
  `cluster_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '聚类时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cluster_id`(`cluster_id` ASC) USING BTREE,
  INDEX `idx_item_id`(`item_id` ASC) USING BTREE,
  INDEX `idx_keyword`(`keyword` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品聚类结果表' ROW_FORMAT = Dynamic;

-- 创建热词聚类结果表
DROP TABLE IF EXISTS `hotword_cluster`;
CREATE TABLE `hotword_cluster`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hotword_id` bigint NOT NULL COMMENT '热词ID，关联hotword表',
  `cluster_id` int NOT NULL COMMENT '簇编号',
  `distance_to_center` double NULL DEFAULT NULL COMMENT '到簇中心的距离',
  `algorithm_params` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '聚类参数（如k值、迭代次数）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '聚类时间',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'hotword',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_hotword_id`(`hotword_id` ASC) USING BTREE,
  INDEX `idx_cluster_id`(`cluster_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 175 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '热词聚类结果表' ROW_FORMAT = Dynamic;

-- 创建聚类中心信息表
DROP TABLE IF EXISTS `cluster_info`;
CREATE TABLE `cluster_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cluster_id` int NOT NULL COMMENT '聚类类别编号（如 0、1、2）',
  `data_type` varchar(20) NOT NULL DEFAULT 'hotword' COMMENT '数据类型：hotword-热词 / commodity-商品',
  `center` text NOT NULL COMMENT '聚类中心向量（TF-IDF/Word2Vec生成）',
  `size` int NOT NULL DEFAULT 0 COMMENT '该类别包含的数据条数',
  `label` varchar(50) DEFAULT NULL COMMENT '聚类类别标签（如：服饰、电子、美妆）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '聚类生成时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_cluster_data_type` (`cluster_id`,`data_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聚类中心信息表';

-- 创建关联规则表
DROP TABLE IF EXISTS `relation_rule`;
CREATE TABLE `relation_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `antecedent` varchar(255) NOT NULL COMMENT '前项（A -> B 中的A）',
  `consequent` varchar(255) NOT NULL COMMENT '后项（A -> B 中的B）',
  `support` decimal(10,4) NOT NULL COMMENT '支持度',
  `confidence` decimal(10,4) NOT NULL COMMENT '置信度',
  `lift` decimal(10,4) DEFAULT NULL COMMENT '提升度',
  `data_type` varchar(20) DEFAULT 'hotword' COMMENT '数据类型 hotword/commodity',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关联规则表';

SET FOREIGN_KEY_CHECKS = 1;
