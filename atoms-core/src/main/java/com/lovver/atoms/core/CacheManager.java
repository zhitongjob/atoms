package com.lovver.atoms.core;

import java.util.List;
import java.util.Map;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheEventListenerFactory;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.context.AtomsContext;

public class CacheManager {
	private static Map<String,CacheProvider> cacheProviders=AtomsContext.getCacheProvider();
	
	public static Cache getCache(int level, String regionName, boolean autoCreate){
		CacheProvider cacheProvider=cacheProviders.get(level+"");
		
		if(level==cacheProviders.size()){
			//最后一级缓存不需要添加监听器
			return cacheProvider.buildCache(regionName, autoCreate, null);
		}
		CacheEventListener listener = null;
		listener = CacheEventListenerFactory.getCacheEventListener(cacheProvider.name(),level+"");
		return cacheProvider.buildCache(regionName, autoCreate, listener);
	}
	
	
	public static void shutdown(int level){
		CacheProvider cacheProvider=cacheProviders.get(level+"");
		cacheProvider.stop();
	}
	
	
	
	
	/**
	 * 获取缓存中的数据
	 * @param level Cache Level: L1 and L2
	 * @param name Cache region name
	 * @param key Cache key
	 * @return Cache object
	 */
	public final static Object get(int level, String name, Object key){
		//System.out.println("GET1 => " + name+":"+key);
		if(name!=null && key != null) {
            Cache cache = getCache(level, name, false);
            if (cache != null)
                return cache.get(key);
        }
		return null;
	}
	
	/**
	 * 获取缓存中的数据
	 * @param level Cache Level -&gt; L1 and L2
	 * @param resultClass Cache object class
	 * @param name Cache region name
	 * @param key Cache key
	 * @return Cache object
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T get(int level, Class<T> resultClass, String name, Object key){
		//System.out.println("GET2 => " + name+":"+key);
		if(name!=null && key != null) {
            Cache cache =getCache(level, name, false);
            if (cache != null)
                return (T)cache.get(key);
        }
		return null;
	}
	
	/**
	 * 写入缓存
	 * @param level Cache Level: L1 and L2
	 * @param name Cache region name
	 * @param key Cache key
	 * @param value Cache value
	 */
	public final static void set(int level, String name, Object key, Object value){
		//System.out.println("SET => " + name+":"+key+"="+value);
		if(name!=null && key != null && value!=null) {
            Cache cache =getCache(level, name, true);
            if (cache != null)
                cache.put(key,value);
        }
	}
	
	/**
	 * 清除缓存中的某个数据
	 * @param level Cache Level: L1 and L2
	 * @param name Cache region name
	 * @param key Cache key
	 */
	public final static void evict(int level, String name, Object key){
		//batchEvict(level, name, java.util.Arrays.asList(key));
		if(name!=null && key != null) {
            Cache cache =getCache(level, name, false);
            if (cache != null)
                cache.evict(key);
        }
	}
	
	/**
	 * 批量删除缓存中的一些数据
	 * @param level Cache Level： L1 and L2
	 * @param name Cache region name
	 * @param keys Cache keys
	 */
	@SuppressWarnings("rawtypes")
	public final static void batchEvict(int level, String name, List keys) {
		if(name!=null && keys != null && keys.size() > 0) {
            Cache cache =getCache(level, name, false);
            if (cache != null)
                cache.evict(keys);
        }
	}

	/**
	 * Clear the cache
	 * @param level Cache level
	 * @param name cache region name
	 */
	public final static void clear(int level, String name) throws CacheException {
        Cache cache =getCache(level, name, false);
        if(cache != null)
        	cache.clear();
	}
	
	/**
	 * list cache keys
	 * @param level Cache level
	 * @param name cache region name
	 * @return Key List
	 */
	@SuppressWarnings("rawtypes")
	public final static List keys(int level, String name) throws CacheException {
        Cache cache =getCache(level, name, false);
		return (cache!=null)?cache.keys():null;
	}
}
