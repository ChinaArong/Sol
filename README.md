## Zeus功能介绍

> 可能是最好的分布式服务调用框架

### 1.服务治理

- 自动发现和注册服务
- 服务动态扩容，服务宕机动态感知
- 服务隔离，不同粒度：单服务隔离，应用实例隔离
- 服务调用服务化，通过http直联，自动负载均衡
- 服务调用封装，自动生成线程池调用，提供了熔断和降级

### 2.动态配置

- 提供了动态配置的读取和写入
- HystrixCommand动态配置，实时修改，实时生效

### 3.服务调用全链路监控

- 无侵入的全链路监控
- 带有traceId的关键日志，方便定位与查找问题

### 4.服务的转发代理

- 通过一个注解就能生成一个代理的透传接口

### 5.直观方便的zeus访问监控

## links
- [使用手册](http://192.168.1.23:8098/common-service/zeus/blob/master/%E4%BD%BF%E7%94%A8%E6%89%8B%E5%86%8C.md)
- [Change log](http://192.168.1.23:8098/common-service/zeus/blob/master/CHANGELOG)
