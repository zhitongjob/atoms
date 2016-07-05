基于j2cache的理念，重新设计开发的一套分布式缓存。支持2级并不限于2级的多级缓存系统。

github地址：[atoms](https://github.com/zhitongjob/atoms)

配置文件
```
<?xml version="1.0" encoding="UTF-8"?>
<atoms>
	<broadcast type="redis" >
		<broadcastConfig host="192.168.1.53" port="6379"/>
	</broadcast>
	<serializer type="fst"/>
	<cache level="1" type="ehcache" delete_atom="true" ><!-- expiredOperator="update" waitTime="100" --><!-- expiredOperator: update,delete  当为update时waitTime有效-->
		<cacheConfig configFile="ehcache.xml"/>
		<cacheTTL>
			<ttl name="hello" value="1000"/><!-- name:regionName value:失效时间 单位（秒） -->
		</cacheTTL>
	</cache>
	<cache level="2" type="redis">
		<cacheConfig host="192.168.1.53" port="6379" timeout="2000" database="13" namespace="atoms" maxTotal="-1" maxIdle="2000"
		 maxWaitMillis="100" minEvictableIdleTimeMillis="864000000" minIdle="1000" numTestsPerEvictionRun="10" lifo="false"
		 softMinEvictableIdleTimeMillis="10" testOnBorrow="true" testOnReturn="false" testWhileIdle="false" timeBetweenEvictionRunsMillis="300000"
		 blockWhenExhausted="true" password=""/>
		<cacheTTL>
			<ttl name="hello" value="3000"/><!-- name:regionName value:失效时间 单位（秒） -->
		</cacheTTL>
	</cache>
	<cache level="3" type="redis">
		<cacheConfig host="192.168.1.22" port="6379" timeout="2000" database="13" namespace="atoms" maxTotal="-1" maxIdle="2000"
		 maxWaitMillis="100" minEvictableIdleTimeMillis="864000000" minIdle="1000" numTestsPerEvictionRun="10" lifo="false"
		 softMinEvictableIdleTimeMillis="10" testOnBorrow="true" testOnReturn="false" testWhileIdle="false" timeBetweenEvictionRunsMillis="300000"
		 blockWhenExhausted="true" password=""/>
		 <cacheTTL>
			<ttl name="hello" value="5000"/><!-- name:regionName value:失效时间 单位（秒） -->
		</cacheTTL>
	</cache>
</atoms>
```

使用代码：
``` java
CacheChannel cc=CacheChannel.getInstance();
cc.set("jobell", "hello", "nihaoya");
cc.evict("jobell", "hello");
while(true){
	Object value=cc.get("jobell", "hello");
	if(value==null){
		System.out.println("==============="+value);
	}else{
		System.out.println("==============="+value);
	}
}
```
