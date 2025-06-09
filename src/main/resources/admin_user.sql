-- 首先确保角色表中有必要的角色
-- 如果没有角色，先插入角色数据
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_SYSTEM_ADMIN', '系统管理员');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_GRADUATE_ADMIN', '研究生院管理员');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_AUDIT_ADMIN', '审计管理员');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_SCHOOL_LEADER', '学校领导');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_GRADUATE_LEADER', '研究生院领导');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_COLLEGE_LEADER', '学院领导');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_COLLEGE_SECRETARY', '学院研究生秘书');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_TEACHER', '导师');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_STUDENT', '研究生');

-- 插入系统管理员账号
-- 注意：密码是一个示例加密值，实际应该是通过系统的SM3PasswordEncoder生成的
-- 假设密码是 Admin@123456
INSERT INTO users (username, password, name, email, phone, enabled, account_non_locked, login_attempts, 
                  first_login, last_password_change_time, created_at, updated_at)
VALUES ('admin', 
        '加密密码将被替换', -- 这里的密码将在应用运行后通过下面的Java代码替换
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

-- 获取插入的用户ID
SET @admin_user_id = LAST_INSERT_ID();

-- 获取系统管理员角色ID
SELECT id INTO @admin_role_id FROM roles WHERE name = 'ROLE_SYSTEM_ADMIN';

-- 关联用户和角色
INSERT INTO user_roles (user_id, role_id) VALUES (@admin_user_id, @admin_role_id);

-- 如果需要，可以为同一个账号添加多个角色
-- 例如同时赋予审计管理员权限
SELECT id INTO @audit_role_id FROM roles WHERE name = 'ROLE_AUDIT_ADMIN';
INSERT INTO user_roles (user_id, role_id) VALUES (@admin_user_id, @audit_role_id);
