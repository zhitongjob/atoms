package com.lovver.atoms.cache.redis;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.config.AtomsCacheBean;
import com.lovver.atoms.config.AtomsCacheConfigBean;
import com.lovver.atoms.context.AtomsContext;

/**
 * Redis 缓存实现
 * @author Winter Lau
 * @author wendal
 */
@SPI("redis")
public class RedisCacheProvider implements CacheProvider {
	
	private JedisPool pool;
	private AtomsCacheConfigBean cacheConfig;
	private String host;
	private int level;
	
	protected ConcurrentHashMap<String, RedisCache> caches = new ConcurrentHashMap<>();
	
	public String name() {
		return "redis";
	}
    
	// 这个实现有个问题,如果不使用RedisCacheProvider,但又使用RedisCacheChannel,这就NPE了
    public Jedis getResource() {
    	return pool.getResource();
    }

	@Override
	public Cache buildCache(String regionName, boolean autoCreate, CacheEventListener listener) throws CacheException {
		// 虽然这个实现在并发时有概率出现同一各regionName返回不同的实例
		// 但返回的实例一次性使用,所以加锁了并没有增加收益
		RedisCache cache = caches.get(regionName);
		if (cache == null) {
			synchronized (caches) {
				Map<String,String> mapTTL=AtomsContext.getTTLConfig(this.level);
				String ttlSeconds=mapTTL.get(regionName);
				cache = new RedisCache(regionName, pool,cacheConfig.getNamespace(),listener,host,ttlSeconds);
				caches.put(regionName, cache);
			}
		}
		return cache;
    }
	
	private String null2default(String value,String defalutValue){
		if(StringUtils.isEmpty(value)){
			return defalutValue;
		}else{
			return value;
		}
	}

	@Override
	public void start(AtomsCacheBean cacheBean) throws CacheException {
		JedisPoolConfig config = new JedisPoolConfig();
		cacheConfig=cacheBean.getCacheConfig();
		
		String host = null2default(cacheConfig.getHost(),"127.0.0.1");
		this.host=host;
		String password = cacheConfig.getPassword();
		if(StringUtils.isEmpty(password)){
			password=null;
		}
		String sPort=null2default(cacheConfig.getPort(),"6379");
		int port = Integer.parseInt(sPort); 
		
		String sTimeout=null2default(cacheConfig.getTimeout(),"2000");		
		int timeout = Integer.parseInt(sTimeout);//getProperty(props, "timeout", 2000);
		
		String sDatabase=null2default(cacheConfig.getDatabase(),"0");
		int database =Integer.parseInt(sDatabase);

		String sBlockWhenExhausted=null2default(cacheConfig.getBlockWhenExhausted(),"true");
		config.setBlockWhenExhausted(Boolean.parseBoolean(sBlockWhenExhausted)); 
		
		String sMaxIdle=null2default(cacheConfig.getMaxIdle(),"10");
		config.setMaxIdle(Integer.parseInt(sMaxIdle));
		
		String sMinIdle=null2default(cacheConfig.getMinIdle(),"5");
		config.setMinIdle(Integer.parseInt(sMinIdle));

		String sMaxTotal=null2default(cacheConfig.getMaxTotal(), "10000");
		config.setMaxTotal(Integer.parseInt(sMaxTotal));
		
		String sMaxWait=null2default(cacheConfig.getMaxWaitMillis(), "100");
		config.setMaxWaitMillis(Integer.parseInt(sMaxWait));
		
		String sTestWhileIdle=null2default(cacheConfig.getTestWhileIdle(), "false");
		config.setTestWhileIdle(Boolean.parseBoolean(sTestWhileIdle));
		
		String sTestOnBorrow=null2default(cacheConfig.getTestOnBorrow(), "true");
		config.setTestOnBorrow(Boolean.parseBoolean(sTestOnBorrow));
		
		String sTestOnReturn =null2default(cacheConfig.getTestOnReturn(), "false");
		config.setTestOnReturn(Boolean.parseBoolean(sTestOnReturn));
		
		String sNumTestsPerEvictionRun=null2default(cacheConfig.getNumTestsPerEvictionRun(), "10");
		config.setNumTestsPerEvictionRun(Integer.parseInt(sNumTestsPerEvictionRun));
		
		String sMinEvictableIdelTimeMillis=null2default(cacheConfig.getMinEvictableIdleTimeMillis(), "1000");
		config.setMinEvictableIdleTimeMillis(Integer.parseInt(sMinEvictableIdelTimeMillis));
		
		String sSoftMinEvictableIdleTimeMillis=null2default(cacheConfig.getSoftMinEvictableIdleTimeMillis(), "10");
		config.setSoftMinEvictableIdleTimeMillis(Integer.parseInt(sSoftMinEvictableIdleTimeMillis));
		
		String timeBetweenEvictionRunsMillis=null2default(cacheConfig.getTimeBetweenEvictionRunsMillis(), "10");
		config.setTimeBetweenEvictionRunsMillis(Integer.parseInt(timeBetweenEvictionRunsMillis));
		
		String lifo=null2default(cacheConfig.getLifo(), "false");
		config.setLifo(Boolean.parseBoolean(lifo));
		pool = new JedisPool(config, host, port, timeout, password, database);
		
		level=Integer.parseInt(cacheBean.getLevel());
	}

	@Override
	public void stop() {
		pool.destroy();
		caches.clear();
	}
}
