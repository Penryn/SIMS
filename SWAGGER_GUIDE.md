# Swagger API文档使用指南

## 概述

本项目已成功集成Swagger API文档生成工具，您可以通过Swagger UI界面查看和测试所有的API接口。

## 访问方式

启动应用后，在浏览器中访问以下地址：

```
http://localhost:8080/sims/api/swagger-ui/index.html
```

## 功能特性

### 1. 自动生成API文档
- 基于OpenAPI 3.0规范
- 自动扫描所有Controller类
- 生成详细的接口文档

### 2. 接口分组
- **认证管理**: 用户登录、登出、密码修改等认证相关接口
- **用户管理**: 用户信息管理相关接口
- **学生学籍管理**: 学生学籍信息管理相关接口
- **学院专业管理**: 学院和专业管理相关接口

### 3. JWT认证支持
- 支持Bearer Token认证
- 在Swagger UI中可以设置Authorization header
- 自动为需要认证的接口添加安全要求

## 使用步骤

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 访问Swagger UI
在浏览器中打开: `http://localhost:8080/sims/api/swagger-ui/index.html`

### 3. 认证设置
1. 首先调用登录接口获取JWT token
2. 点击页面右上角的"Authorize"按钮
3. 在弹出框中输入: `Bearer YOUR_JWT_TOKEN`
4. 点击"Authorize"确认

### 4. 测试接口
- 选择要测试的接口
- 点击"Try it out"按钮
- 填写必要的参数
- 点击"Execute"执行请求
- 查看响应结果

## 配置说明

### 1. Maven依赖
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.6.14</version>
</dependency>
```

### 2. Swagger配置
配置文件位置: `src/main/java/com/graduate/management/config/SwaggerConfig.java`

主要配置内容:
- API基本信息（标题、描述、版本等）
- JWT认证配置
- 安全要求设置

### 3. Spring Security配置
在SecurityConfig中添加了Swagger相关路径的访问权限:
```java
.antMatchers("/swagger-ui/**").permitAll()
.antMatchers("/swagger-ui.html").permitAll()
.antMatchers("/swagger-resources/**").permitAll()
.antMatchers("/v2/api-docs").permitAll()
.antMatchers("/v3/api-docs/**").permitAll()
.antMatchers("/webjars/**").permitAll()
```

## 注解说明

### Controller级别注解
- `@Tag`: 为Controller分组，设置分组名称和描述

### 方法级别注解
- `@Operation`: 描述具体的API操作
- `@Parameter`: 描述方法参数

### DTO级别注解
- `@Schema`: 描述数据模型和字段

## 示例代码

### Controller注解示例
```java
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Operation(summary = "用户登录", description = "用户通过用户名和密码登录系统")
    @PostMapping("/login")
    public ApiResponse<JwtResponse> login(@Parameter(description = "登录请求信息", required = true) @Valid @RequestBody LoginRequest loginRequest) {
        // 实现代码
    }
}
```

### DTO注解示例
```java
@Schema(description = "登录请求")
public class LoginRequest {
    
    @Schema(description = "用户名", required = true, example = "admin")
    private String username;
    
    @Schema(description = "密码", required = true, example = "123456")
    private String password;
}
```

## 注意事项

1. **版本兼容性**: 使用springdoc-openapi而不是springfox，因为springfox与Spring Boot 2.7+存在兼容性问题

2. **安全配置**: 确保Swagger相关路径在Spring Security中被正确配置为允许访问

3. **生产环境**: 在生产环境中建议禁用Swagger UI或限制访问权限

4. **文档维护**: 及时更新Controller和DTO中的注解，保持文档的准确性

## 故障排除

### 1. 无法访问Swagger UI
- 检查应用是否正常启动
- 确认端口号和上下文路径是否正确
- 检查Spring Security配置

### 2. 接口不显示
- 确认Controller类是否在正确的包路径下
- 检查@RestController注解是否存在
- 验证@RequestMapping注解是否正确

### 3. 认证失败
- 确认JWT token格式正确（需要Bearer前缀）
- 检查token是否过期
- 验证权限配置是否正确

通过以上配置和使用说明，您可以充分利用Swagger来管理和测试您的API接口。
