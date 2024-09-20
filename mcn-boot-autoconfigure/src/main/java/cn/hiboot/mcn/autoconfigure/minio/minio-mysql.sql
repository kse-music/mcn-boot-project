CREATE TABLE IF NOT EXISTS c_files (
   id int NOT NULL AUTO_INCREMENT COMMENT 'id',
   upload_id varchar(255) DEFAULT NULL COMMENT '分片上传uploadId',
   md5 varchar(32) NOT NULL COMMENT '文件md5',
   filename varchar(255) DEFAULT NULL COMMENT '文件名称',
   upload_urls longtext  DEFAULT NULL,
   chunk_num   int(11)   NOT NULL,
   create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   update_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   UNIQUE KEY md5 (md5),
   PRIMARY KEY (id) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;