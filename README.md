# takeout-srv
SpringBoot外卖后端，实现缓存优化、事务幂等、订单状态机与SQL性能调优
## 技术栈
 SpringBoot、MyBatis-Plus、MySQL、Redis、JWT、AOP、JMeter
 ## 核心功能
 1. JWT登录鉴权，AOP自定义注解记录操作日志
 2. 商品查询使用Cache-Aside旁路缓存，降低数据库压力
 3. 订单创建：Spring事务保证主表、明细表数据一致性，实现幂等防止重复下单
 4. 订单状态机，约束订单合法流转
 5. SQL性能优化，通过Explain分析慢SQL，建立联合索引；JMeter压测优化接口响应时间，720ms降至110ms
 ## 运行环境
 JDK8 + MySQL8 + Redis
