package com.lovver.atoms.cache;

import com.lovver.atoms.common.annotation.Extension;
import com.lovver.atoms.common.exception.CacheException;

/**
 * 侦听缓存中的某个记录超时 
 * @author jobell
 */
@Extension("spi")
public interface CacheEventListener {
	
	public void init(int level);

	/**
	 * 当缓存中的某个对象超时被清除的时候触发
	 * @param region: Cache region name
	 * @param key: cache key
	 */
	public void notifyElementExpired(String region, Object key) ;
	
	public void notifyElementRemoved(String region, Object key) throws CacheException;
	
	public void notifyElementPut(String region, Object key,Object value) throws CacheException;
	public void notifyElementPut(String region, Object key,Object value,int expiretime) throws CacheException;

	public void notifyElementUpdated(String region, Object key,Object value) throws CacheException;
	 
	public void notifyElementEvicted(String region, Object key);
	 
	public void notifyRemoveAll(String region);

}
