CREATE TABLE c_files (
     id INT IDENTITY(1,1) NOT NULL PRIMARY KEY, -- id
     upload_id VARCHAR(255) DEFAULT NULL,       -- 分片上传uploadId
     md5 VARCHAR(32) NOT NULL,                  -- 文件md5
     filename VARCHAR(255) DEFAULT NULL,        -- 文件名称
     upload_urls CLOB DEFAULT NULL,
     chunk_num INT NOT NULL,
     create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     update_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     UNIQUE(md5)
);