#### 目标
> 相关名词：https://ruby-china.org/topics/26221

- 调优，在有限硬件资源的情况下，提供更大的吞吐率，并最终得到吞吐率与硬件，jvm参数的关系
- 调试事务，不同级别锁的影响程度，以及TPS
- 秒杀实践


#### 衍生问题思考
##### MySQL数据库
- 两百万数据的 select count 很慢，大约11s，如何优化 
    - 通过 `explain` 查看走的是否是索引，本项目查询后发现走的是索引，因此不是索引的问题
    - 开启 profiling
    - 先执行要分析的sql，如 `select count(1) from tbname`
    - 执行 `show profiles`，找到上条查询对应的 `Query_ID`
    - 执行 `SHOW PROFILE FOR QUERY ${上面的Query_ID}`，查看时间分布
    - 发现时间都在`send data`上，对此网上有种说法是，大数据字段，如text，很长长度的varchar等，mysql会将部分数据放在`溢出页`里面，
        因此还会走一遍磁盘扫描。但本项目直接查的`count(1)`，没有查询大字段，因此该说法不适合当前情况。继续分析发现，查询时，系统磁盘IO很猛，
        按理查的是索引，在内存里面，不会有这么多的磁盘IO，遂想到修改配置文件`my.cnf`，增大`innodb_buffer_pool_size`，
        增大的原则是`Data_length + Index_length 值的总和的110%`。修改后重启，查询降低到0.1s，问题解决。(注意第一次就有点慢，
        因为刚刚启动，索引数据还没有加载到内存，后续查询很快)
        - 继续验证上面提到的问题，`select count(detail)`，查询大字段确实比查主键慢，当然原因在于索引，没有涉及到`溢出页`的验证