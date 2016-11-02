package com.lovver.atoms.cache;

import java.util.List;

import com.lovver.atoms.common.exception.CacheException;

/**
 * Implementors define a caching algorithm. All implementors
 * <b>must</b> be threadsafe.
 * @author liudong
 */
public interface Cache {

	/**
	 * Get an item from the cache, nontransactionally
	 * @param key cache key
	 * @return the cached object or null
	 */
	public Object get(Object key) throws CacheException;
	
	/**
	 * Add an item to the cache, nontransactionally, with
	 * failfast semantics
	 * @param key cache key
	 * @param value cache value
	 */
	public void put(Object key, Object value) throws CacheException;
	
	/**
	 * Add an item to the cache
	 * @param key cache key
	 * @param value cache value
	 */
	public void update(Object key, Object value) throws CacheException;
	
	/**
	 * 上一级失效更新，不做通知处理
	 * 
	 * @param key
	 * @param value
	 * @throws CacheException
	 */
	public void expireUpdate(Object key, Object value) throws CacheException;
	

	@SuppressWarnings("rawtypes")
	public List keys() throws CacheException ;
	
	/**
	 * @param key Cache key
	 * Remove an item from the cache
	 */
	public void evict(Object key) throws CacheException;
	
	/**
	 * Batch remove cache objects
	 * @param keys the cache keys to be evicted
	 */
	@SuppressWarnings("rawtypes")
	public void evict(List keys) throws CacheException;
	
	/**
	 * Clear the cache
	 */
	public void clear() throws CacheException;
	
	/**
	 * Clean up
	 */
	public void destroy() throws CacheException;
	
}
