## 工具箱  
这是一个工具箱

### 待添加功能：
#### 数据库模块  
- 数据库导出模板添加 classicSingle 版。在数据库较大的情况下，多库详细版word编辑保存容易崩溃
- 使用数据库连接池，简化查询代码，独立数据库配置文件，只提供部分简化配置（用户名，密码，数据库，jdbcDriver等）
- 主外键分析，为每个表设值主键。当主键不是联合主键时，检索其他表是否有字段以本主键作为外键
- 相同字段名时字段长度不同数据分析（移除常用字段名称，如`id`, `type`等）
