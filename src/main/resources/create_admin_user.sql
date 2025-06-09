-- 导入管理员账号脚本
-- 此脚本用于手动添加系统管理员账号
-- 使用方法：在MySQL命令行中执行此脚本

-- 首先创建角色（如果不存在）
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_SYSTEM_ADMIN', '系统管理员');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_AUDIT_ADMIN', '审计管理员');

-- 插入管理员账号
-- 密码采用SM3加密，实际环境中请替换为系统加密的密码
-- 这里提供一个示例密码，实际使用时会被替换
INSERT INTO users (username, password, name, email, phone, enabled, account_non_locked, login_attempts, 
                  first_login, last_password_change_time, created_at, updated_at)
VALUES ('admin', 
        'Admin@123456', 
        '系统管理员', 
        'admin@example.com', 
        '13800000000', 
        true, 
        true, 
        0, 
        false, 
        NOW(), 
        NOW(), 
        NOW());

-- 关联用户和系统管理员角色
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_SYSTEM_ADMIN';
