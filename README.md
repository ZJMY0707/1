# 📚 学生个人知识库与日程标注系统

Spring Boot 3 + JavaFX 混合架构课程设计项目。

## 快速启动

### 1. 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 初始化数据库
```bash
# 修改 server/src/main/resources/application.yml 中的数据库连接信息
# 然后执行:
bash tscripts/init-db.sh root MiYu0707 # 你的MySQL密码
```

### 3. 构建项目
```bash
bash tscripts/build.sh
```

### 4. 运行
```bash
# 终端1: 启动后端
bash tscripts/start-server.sh

# 终端2: 启动客户端
bash tscripts/start-client.sh
```

### 5. 测试账号
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| 20210001 | 123456 | 学生 |
| 20210002 | 123456 | 学生 |

## 项目结构
```
student-knowledge-system/
├── pom.xml              # Maven父工程
├── sql/                 # 数据库脚本
│   ├── schema.sql       # 建表(6张表)
│   └── data.sql         # 示例数据
├── server/              # Spring Boot后端 (端口8088)
│   └── src/main/java/studentknowledge/
│       ├── config/      # 安全配置、跨域配置
│       ├── controller/  # 7个REST控制器
│       ├── service/     # 6个业务服务
│       ├── repository/  # 5个JPA仓库
│       ├── model/       # 5个实体 + 3个枚举
│       ├── dto/         # 请求/响应DTO
│       ├── security/    # JWT工具 + 认证过滤器
│       └── exception/   # 全局异常处理
├── client/              # JavaFX客户端
│   └── src/main/java/studentknowledge/
│       ├── client/      # 启动器
│       ├── service/     # HTTP客户端 + 会话管理
│       ├── model/       # 客户端模型
│       └── view/        # 9个UI视图
├── tdocs/               # 设计文档
└── tscripts/            # 构建运行脚本
```
