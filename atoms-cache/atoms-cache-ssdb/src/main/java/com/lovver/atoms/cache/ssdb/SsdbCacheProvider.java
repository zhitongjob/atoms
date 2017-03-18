package com.lovver.atoms.cache.ssdb;


import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.lovver.ssdbj.pool.SSDBDataSource;
import org.apache.commons.lang.StringUtils;


import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.config.AtomsCacheBean;
import com.lovver.atoms.config.AtomsCacheConfigBean;
import com.lovver.atoms.context.AtomsContext;

/**
 * Ssdb 缓存实现
 * @author Winter Lau
 * @author wendal
 */
@SPI("ssdb")
public class SsdbCacheProvider implements CacheProvider {
	
	private SSDBDataSource ssdbDs;
	private AtomsCacheConfigBean cacheConfig;
	private String host;
	private int level;
	
	protected ConcurrentHashMap<String, SsdbCache> caches = new ConcurrentHashMap<>();
	
	public String name() {
		return "ssdb";
	}
    
	// 这个实现有个问题,如果不使用RedisCacheProvider,但又使用RedisCacheChannel,这就NPE了
//    public Jedis getResource() {
//    	return pool.getResource();
//    }

	@Override
	public Cache buildCache(String regionName, boolean autoCreate, CacheEventListener listener,String client_id) throws CacheException {
		// 虽然这个实现在并发时有概率出现同一各regionName返回不同的实例
		// 但返回的实例一次性使用,所以加锁了并没有增加收益
		SsdbCache cache = caches.get(regionName);
		if (cache == null) {
			synchronized (caches) {
				Map<String,String> mapTTL=AtomsContext.getTTLConfig(this.level);
				String ttlSeconds=mapTTL.get(regionName);
				cache = new SsdbCache(regionName, ssdbDs,cacheConfig.getNamespace(),listener,host,ttlSeconds,client_id);
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

		Properties info = new Properties();

		info.setProperty("loginTimeout", "300");
		info.setProperty("tcpKeepAlive", "true");
		info.setProperty("protocolName", "ssdb");
		info.setProperty("protocolVersion", "1.0");


		//JedisPoolConfig config = new JedisPoolConfig();
		cacheConfig=cacheBean.getCacheConfig();
		
		String host = null2default(cacheConfig.getHost(),"127.0.0.1");
		this.host=host;
		String password = cacheConfig.getPassword();
		if(!StringUtils.isEmpty(password)){
            info.setProperty("password", password);
        }

		String sPort=null2default(cacheConfig.getPort(),"8888");
		int port = Integer.parseInt(sPort); 
		
		String sTimeout=null2default(cacheConfig.getTimeout(),"2000");
		if(StringUtils.isNotEmpty(sTimeout)) {
			info.setProperty("loginTimeout", sTimeout);
		}
		String sBlockWhenExhausted=null2default(cacheConfig.getBlockWhenExhausted(),"true");
		if(StringUtils.isNotEmpty(sBlockWhenExhausted)) {
            info.setProperty("blockWhenExhausted", sBlockWhenExhausted);
        }
		String sMaxIdle=null2default(cacheConfig.getMaxIdle(),"10");
		if(StringUtils.isNotEmpty(sMaxIdle)) {
            info.setProperty("maxIdle", sMaxIdle);
        }
		String sMinIdle=null2default(cacheConfig.getMinIdle(),"5");
		if(StringUtils.isNotEmpty(sMinIdle)) {
            info.setProperty("minIdle", sMinIdle);
        }
		String sMaxTotal=null2default(cacheConfig.getMaxTotal(), "10000");
		if(StringUtils.isNotEmpty(sMaxTotal)) {
            info.setProperty("maxTotal", sMaxTotal);
        }
		String sMaxWait=null2default(cacheConfig.getMaxWaitMillis(), "100");
		if(StringUtils.isNotEmpty(sMaxWait)) {
            info.setProperty("maxWaitMillis", sMaxWait);
        }
		String sTestWhileIdle=null2default(cacheConfig.getTestWhileIdle(), "false");
		if(StringUtils.isNotEmpty(sTestWhileIdle)) {
            info.setProperty("testWhileIdle", sTestWhileIdle);
        }
		String sTestOnBorrow=null2default(cacheConfig.getTestOnBorrow(), "true");
		if(StringUtils.isNotEmpty(sTestOnBorrow)) {
            info.setProperty("testOnBorrow", sTestOnBorrow);
        }
		String sTestOnReturn =null2default(cacheConfig.getTestOnReturn(), "false");
		if(StringUtils.isNotEmpty(sTestOnReturn)) {
            info.setProperty("testOnReturn", sTestOnReturn);
        }
		String sNumTestsPerEvictionRun=null2default(cacheConfig.getNumTestsPerEvictionRun(), "10");
		if(StringUtils.isNotEmpty(sNumTestsPerEvictionRun)) {
            info.setProperty("numTestsPerEvictionRun", sNumTestsPerEvictionRun);
        }
		String sMinEvictableIdelTimeMillis=null2default(cacheConfig.getMinEvictableIdleTimeMillis(), "1000");
		if(StringUtils.isNotEmpty(sMinEvictableIdelTimeMillis)) {
            info.setProperty("minEvictableIdleTimeMillis", sMinEvictableIdelTimeMillis);
        }
		String sSoftMinEvictableIdleTimeMillis=null2default(cacheConfig.getSoftMinEvictableIdleTimeMillis(), "10");
		if(StringUtils.isNotEmpty(sSoftMinEvictableIdleTimeMillis)) {
            info.setProperty("softMinEvictableIdleTimeMillis", sSoftMinEvictableIdleTimeMillis);
        }
		String timeBetweenEvictionRunsMillis=null2default(cacheConfig.getTimeBetweenEvictionRunsMillis(), "10");
		if(StringUtils.isNotEmpty(timeBetweenEvictionRunsMillis)) {
            info.setProperty("timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
        }
		String lifo=null2default(cacheConfig.getLifo(), "false");
		if(StringUtils.isNotEmpty(lifo)) {
            info.setProperty("lifo", lifo);
        }

		level=Integer.parseInt(cacheBean.getLevel());

		ssdbDs = new SSDBDataSource(host,port,null,info);
	}

	@Override
	public void stop() {
		ssdbDs.distory();
		caches.clear();
	}
}
