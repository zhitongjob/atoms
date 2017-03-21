package com.lovver.atoms.cache.redis;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;

/**
 * Redis 缓存基于Hashs实现
 * @author wendal
 */
public class RedisCache implements Cache {

	private final static Logger log = LoggerFactory.getLogger(RedisCache.class);
	private static Serializer serializer=AtomsContext.getSerializer();
	// 记录region
	protected byte[] region2;
	protected String region;
	protected JedisPool pool;
	private String srcRegion;
	
	private String namespace;
	private CacheEventListener listener;
	private String host;
	private Integer ttlSeconds;
	

	public RedisCache(String region, JedisPool pool,String namespace,CacheEventListener listener,String host,String ttlSeconds) {
		if (region == null || region.isEmpty())
			region = "_"; // 缺省region
		this.srcRegion=region;
		this.namespace=namespace;
		this.region = getRegionName(region);
		this.listener=listener;
		this.pool = pool;
//		this.region = region;
		this.region2 = this.region.getBytes();
		this.host=host;
		if(StringUtils.isEmpty(ttlSeconds)){
			this.ttlSeconds=null;
		}else{
			this.ttlSeconds=Integer.parseInt(ttlSeconds);
		}

	}

	/**
	 * 在region里增加一个可选的层级,作为命名空间,使结构更加清晰
	 * 同时满足小型应用,多个J2Cache共享一个redis database的场景
	 * @param region
	 * @return
     */
	private String getRegionName(String region) {
		if(namespace != null && !namespace.isEmpty()) {
			region = namespace + ":" + region;
		}
		return region;
	}
	
	protected byte[] getKeyName(Object key) {
		if(key instanceof Number)
			return ("I:" + key).getBytes();
		else if(key instanceof String || key instanceof StringBuilder || key instanceof StringBuffer)
			return ("S:" + key).getBytes();
		return ("O:" + key).getBytes();
	}

	public Object get(Object key) throws CacheException {
		if (null == key)
			return null;
		Object obj = null;
		try (Jedis cache = pool.getResource()) {
			byte[] b = cache.hget(region2, getKeyName(key));
			if(b != null)
				obj = serializer.deserialize(b);
		} catch (Exception e) {
			log.error("Error occured when get data from redis2 cache", e);
			if(e instanceof IOException || e instanceof NullPointerException)
				evict(key);
		}
		return obj;
	}

	public void put(Object key, Object value) throws CacheException {
		System.out.println(this.host+" ==================put reids");
		if (key == null){
			return;
		}
		if (value == null){
			evict(key);
		}else {
			try (Jedis cache = pool.getResource()) {
				cache.hset(region2, getKeyName(key), serializer.serialize(value));
				if(ttlSeconds!=null){
					cache.expire(region2, ttlSeconds);
				}
				if(listener!=null){
					listener.notifyElementPut(this.srcRegion, key, value);
				}
			} catch (Exception e) {
				throw new CacheException(e);
			}
		}
	}

	@Override
	public void put(Object key, Object value, Integer expiretime) throws CacheException {
		this.put(key,value);
	}

	public void update(Object key, Object value) throws CacheException {
		put(key, value);
		if(listener!=null){
			listener.notifyElementPut(this.srcRegion, key, value);
		}
	}
	
	
	public void expireUpdate(Object key, Object value) throws CacheException{
		System.out.println(this.host+" ==================expireUpdate reids");
		if (key == null){
			return;
		}
		if (value == null){
			evict(key);
		}else {
			try (Jedis cache = pool.getResource()) {
				cache.hset(region2, getKeyName(key), serializer.serialize(value));
				if(ttlSeconds!=null){
					cache.expire(region2, ttlSeconds);
				}
				if(listener!=null){
					listener.notifyElementPut(this.srcRegion, key, value);
				}
			} catch (Exception e) {
				throw new CacheException(e);
			}
		}
	}

	public void evict(Object key) throws CacheException {
		if (key == null)
			return;
		try (Jedis cache = pool.getResource()) {
			cache.hdel(region2, getKeyName(key));
			if(listener!=null){
				listener.notifyElementRemoved(this.srcRegion, key);
			}
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void evict(List keys) throws CacheException {
		if(keys == null || keys.size() == 0)
			return ;
		try (Jedis cache = pool.getResource()) {
			int size = keys.size();
			byte[][] okeys = new byte[size][];
			for(int i=0; i<size; i++){
				okeys[i] = getKeyName(keys.get(i));
			}
			cache.hdel(region2, okeys);
			if(listener!=null){
				for(Object key:keys){
					listener.notifyElementRemoved(this.srcRegion, key);
				}
			}
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public List<String> keys() throws CacheException {
		try (Jedis cache = pool.getResource()) {
			return new ArrayList<String>(cache.hkeys(region));
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public void clear() throws CacheException {
		try (Jedis cache = pool.getResource()) {
			cache.del(region2);
			if(listener!=null){
				listener.notifyRemoveAll(this.srcRegion);
			} 
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	public void destroy() throws CacheException {
		this.clear();
		if(listener!=null){
			listener.notifyRemoveAll(this.srcRegion);
		} 
	}
}
