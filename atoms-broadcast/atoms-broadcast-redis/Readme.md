现象 :早上后台的订阅线程无故退出，导致统计和监控失效长达5个小时左右
日志:
2015-04-13 05:00:00.256 ERROR [Message SubScribe Monitor][SubScribeManager.java:127] - 订阅线程无故退出
com.lingyu.common.core.ServiceException: redis.clients.jedis.exceptions.JedisConnectionException: Unexpected end of stream.
        at com.lingyu.common.db.Redis.subscribe(Redis.java:1439) ~[Redis.class:?]
        at com.lingyu.common.db.SubScribeManager.run(SubScribeManager.java:125) ~[SubScribeManager.class:?]
        at java.lang.Thread.run(Thread.java:745) [?:1.7.0_65]
Caused by: redis.clients.jedis.exceptions.JedisConnectionException: Unexpected end of stream.
        at redis.clients.util.RedisInputStream.ensureFill(RedisInputStream.java:198) ~[jedis-2.6.2.jar:?]
        at redis.clients.util.RedisInputStream.read(RedisInputStream.java:180) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Protocol.processBulkReply(Protocol.java:158) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Protocol.process(Protocol.java:132) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Protocol.processMultiBulkReply(Protocol.java:183) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Protocol.process(Protocol.java:134) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Protocol.read(Protocol.java:192) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Connection.readProtocolWithCheckingBroken(Connection.java:282) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Connection.getRawObjectMultiBulkReply(Connection.java:227) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.JedisPubSub.process(JedisPubSub.java:108) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.JedisPubSub.proceed(JedisPubSub.java:102) ~[jedis-2.6.2.jar:?]
        at redis.clients.jedis.Jedis.subscribe(Jedis.java:2496) ~[jedis-2.6.2.jar:?]
        at com.lingyu.common.db.Redis.subscribe(Redis.java:1435) ~[Redis.class:?]
        ... 2 more

被try{}catch(Exception e){} 的居然还会退出，很疑惑~~
查到了这篇文章：
https://github.com/xetorthio/jedis/issues/932
client-output-buffer-limit was the cause. redis-server closed the connections, leading to the exceptions.
client-output-buffer-limit
客户端buffer控制。在客户端与server进行的交互中,每个连接都会与一个buffer关联,此buffer用来队列化等待被client接受的响应信息。如果client不能及时的消费响应信息,那么buffer将会被不断积压而给server带来内存压力.如果buffer中积压的数据达到阀值,将会导致连接被关闭,buffer被移除。


buffer控制类型包括:normal -> 普通连接；slave ->与slave之间的连接；pubsub ->pub/sub类型连接，此类型的连接，往往会产生此种问题;因为pub端会密集的发布消息,但是sub端可能消费不足.
指令格式:client-output-buffer-limit <class> <hard> <soft> <seconds>",其中hard表示buffer最大值,一旦达到阀值将立即关闭连接;
soft表示"容忍值",它和seconds配合,如果buffer值超过soft且持续时间达到了seconds,也将立即关闭连接,如果超过了soft但是在seconds之后，buffer数据小于了soft,连接将会被保留.
其中hard和soft都设置为0,则表示禁用buffer控制.通常hard值大于soft.
生产线上调整参数(内存和配置同步修改):
127.0.0.1:6380> CONFIG GET client-output-buffer-limit
client-output-buffer-limit
normal 0 0 0 slave 268435456 67108864 60 pubsub 33554432 8388608 60

127.0.0.1:6380> config set client-output-buffer-limit 'normal 0 0 0 slave 268435456 67108864 60 pubsub 0 0 0'


redis.conf
client-output-buffer-limit pubsub 0 0 0

=============================================================
6470672人


平均下来每个人，887字节 内存占用，608 AOF 磁盘占用 344 RDB磁盘占用


文件cache需要额外占用一半的内存占用，所以大概就有10几G的占用
=============================================================
慢查询获取
slowlog get
127.0.0.1:6379> slowlog get 
 1) 1) (integer) 27
    2) (integer) 1417531320
    3) (integer) 24623
    4) 1) "info"
其中，各项指标表示：

A unique progressive identifier for every slow log entry. slowlog的流水号

The unix timestamp at which the logged command was processed. unix时间戳

The amount of time needed for its execution, in microseconds 平均耗时（注意，microseconds翻译成微秒，而不是毫秒）.

The array composing the arguments of the command.


slowlog len 获取总共的slow log数量
slowlog get number 根据数量获取slowlog
=======================================
commandstats 部分记录了各种不同类型的命令的执行统计信息，比如命令执行的次数、命令耗费的 CPU 时间(单位毫秒)、执行每个命令耗费的平均 CPU 时间(单位毫秒)等等。对于每种类型的命令，这个部分都会添加一行以下格式的信息：

cmdstat_XXX:calls=XXX,usec=XXX,usecpercall=XXX
10.104.5.98:6379>info commandstats
# Commandstats
cmdstat_get:calls=180608685,usec=470928529,usec_per_call=2.61
cmdstat_set:calls=147550519,usec=562225572,usec_per_call=3.81
cmdstat_del:calls=177224,usec=1643815,usec_per_call=9.28
cmdstat_exists:calls=14130110,usec=31402378,usec_per_call=2.22
cmdstat_incr:calls=1017,usec=3261,usec_per_call=3.21
cmdstat_mget:calls=666034,usec=18069595,usec_per_call=27.13
cmdstat_lpush:calls=103077132,usec=181583996,usec_per_call=1.76
cmdstat_lrange:calls=38777511,usec=138617427,usec_per_call=3.57
cmdstat_ltrim:calls=2056,usec=7622,usec_per_call=3.71
cmdstat_lrem:calls=103075076,usec=579401111,usec_per_call=5.62
cmdstat_zadd:calls=15900133,usec=56515414,usec_per_call=3.55
cmdstat_zincrby:calls=11747959,usec=196212310,usec_per_call=16.70
cmdstat_zrem:calls=257783,usec=1053833,usec_per_call=4.09
cmdstat_zrange:calls=7141527,usec=41950470,usec_per_call=5.87
cmdstat_zrevrangebyscore:calls=10,usec=51489,usec_per_call=5148.90
cmdstat_zcount:calls=16104028,usec=112221789,usec_per_call=6.97
cmdstat_zrevrange:calls=27497771,usec=582807534,usec_per_call=21.19
cmdstat_zscore:calls=8663683,usec=44001575,usec_per_call=5.08
cmdstat_zrank:calls=3,usec=43,usec_per_call=14.33
cmdstat_zrevrank:calls=15906400,usec=68891802,usec_per_call=4.33
cmdstat_hset:calls=10236125,usec=37507245,usec_per_call=3.66
cmdstat_hget:calls=1618802100,usec=2755577270,usec_per_call=1.70
cmdstat_hmset:calls=369619411,usec=4843444966,usec_per_call=13.10
cmdstat_hmget:calls=56015,usec=344231,usec_per_call=6.15
cmdstat_hincrby:calls=170633471,usec=884820311,usec_per_call=5.19
cmdstat_hdel:calls=44233,usec=201881,usec_per_call=4.56
cmdstat_hlen:calls=21724,usec=39834,usec_per_call=1.83
cmdstat_hgetall:calls=311374011,usec=3269118749,usec_per_call=10.50
cmdstat_hexists:calls=70864759,usec=285319509,usec_per_call=4.03
cmdstat_incrby:calls=2942269,usec=42251052,usec_per_call=14.36
cmdstat_decrby:calls=2050,usec=3616,usec_per_call=1.76
cmdstat_rename:calls=6472,usec=33326,usec_per_call=5.15
cmdstat_keys:calls=3636,usec=1974535725,usec_per_call=543051.62
cmdstat_dbsize:calls=9,usec=15,usec_per_call=1.67
cmdstat_ping:calls=46747,usec=61691,usec_per_call=1.32
cmdstat_type:calls=1,usec=3,usec_per_call=3.00
cmdstat_psync:calls=1,usec=3164,usec_per_call=3164.00
cmdstat_replconf:calls=21643928,usec=25568830,usec_per_call=1.18
cmdstat_info:calls=4,usec=3669,usec_per_call=917.25
cmdstat_config:calls=2,usec=37,usec_per_call=18.50
cmdstat_subscribe:calls=45505,usec=476993,usec_per_call=10.48
cmdstat_publish:calls=34572782,usec=262298295,usec_per_call=7.59
cmdstat_client:calls=3,usec=47628,usec_per_call=15876.00
cmdstat_eval:calls=2050,usec=76432,usec_per_call=37.28
cmdstat_slowlog:calls=1,usec=30,usec_per_call=30.00

redis.2.8.23 版本部署时会有两个warning
[32555] 09 Nov 12:06:37.804 # WARNING you have Transparent Huge Pages (THP) support enabled in your kernel. This will create latency and memory usage issues with Redis. To fix this issue run the command 'echo never > /sys/kernel/mm/transparent_hugepage/enabled' as root, and add it to your /etc/rc.local in order to retain the setting after a reboot. Redis must be restarted after THP is disabled.
[32555] 09 Nov 12:06:37.804 # WARNING: The TCP backlog setting of 511 cannot be enforced because /proc/sys/net/core/somaxconn is set to the lower value of 128.
只要按照提示来就行:
echo never > /sys/kernel/mm/transparent_hugepage/enabled
echo 511 > /proc/sys/net/core/somaxconn
并加到 /etc/rc.local 
==============================================
Redis ”MISCONF Redis is configured to save RDB snapshots, but is currently not able to persist on disk”
1.没有配置 vm.overcommit_memory=1  
2.磁盘空间不足
3.rdb文件被删除，解决办法 
CONFIG SET dir /data/redis/