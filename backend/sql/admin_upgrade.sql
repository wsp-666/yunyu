-- 管理员表升级：增加角色和创建时间
ALTER TABLE t_admin ADD COLUMN role TINYINT NOT NULL DEFAULT 0 COMMENT '角色(0小管理员/1最终管理员)';
ALTER TABLE t_admin ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 将现有默认管理员设为最终管理员
UPDATE t_admin SET role = 1 WHERE account = 'admin';
