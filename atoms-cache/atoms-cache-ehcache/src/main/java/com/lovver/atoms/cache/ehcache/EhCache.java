package com.lovver.atoms.cache.ehcache;

import java.util.List;
import java.util.Map;

import com.lovver.atoms.config.AtomsCacheTTLConfigBean;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.context.AtomsContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EHCache
 */
public class EhCache implements Cache,CacheEventListener{

	private final static Logger log = LoggerFactory.getLogger(EhCache.class);
	private net.sf.ehcache.Cache cache;
	private com.lovver.atoms.cache.CacheEventListener listener;
	private AtomsCacheTTLConfigBean ttlConfigBean=null;
	private int level;
	private boolean boardset=false;

	/**
	 * Creates a new Hibernate pluggable cache based on a cache name.
	 *
	 * @param cache The underlying EhCache instance to use.
	 * @param listener cache listener
	 */
	public EhCache(net.sf.ehcache.Cache cache, com.lovver.atoms.cache.CacheEventListener listener,int level) {
		this.cache = cache;
		this.cache.getCacheEventNotificationService().registerListener(this);
		this.listener = listener;
		this.level=level;
		if(AtomsContext.getTTLConfig(this.level)!=null) {
			this.ttlConfigBean = AtomsContext.getTTLConfig(this.level).get(cache.getName());
			if(ttlConfigBean!=null&&StringUtils.isNotEmpty(ttlConfigBean.getBroadset())) {
                this.boardset =Boolean.parseBoolean(ttlConfigBean.getBroadset());
            }
		}
	}

	@SuppressWarnings("rawtypes")
	public List keys() throws CacheException {
		return this.cache.getKeys();
	}

	/**
	 * Gets a value of an element which matches the given key.
	 *
	 * @param key the key of the element to return.
	 * @return The value placed into the cache with an earlier put, or null if not found or expired
	 * @throws CacheException cache exception
	 */
	public Object get(Object key) throws CacheException {
		try {
			if ( key == null ) 
				return null;
			else {
                Element element = cache.get( key );
				if ( element != null )
					return element.getObjectValue();				
			}
			return null;
		}
		catch (net.sf.ehcache.CacheException e) {
			throw new CacheException( e );
		}
	}

	/**
	 * Puts an object into the cache.
	 *
	 * @param key   a key
	 * @param value a value
	 * @throws CacheException if the {@link CacheManager}
	 *                        is shutdown or another {@link Exception} occurs.
	 */
	public void update(Object key, Object value) throws CacheException {
		put( key, value );
//		if(listener != null){
//			listener.notifyElementUpdated(cache.getName(), key,value);
//		}
	}
	
	
//	public void expireUpdate(Object key, Object value) throws CacheException{
//		try {
//			Element element = new Element( key, value );
//			cache.put( element );
//		}
//		catch (IllegalArgumentException e) {
//			throw new CacheException( e );
//		}
//		catch (IllegalStateException e) {
//			throw new CacheException( e );
//		}
//		catch (net.sf.ehcache.CacheException e) {
//			throw new CacheException( e );
//		}
//	}

	/**
	 * Puts an object into the cache.
	 *
	 * @param key   a key
	 * @param value a value
	 * @throws CacheException if the {@link CacheManager}
	 *                        is shutdown or another {@link Exception} occurs.
	 */
	public void put(Object key, Object value) throws CacheException {
        put(key,value,true);
	}

    @Override
    public void put(Object key, Object value, boolean broadFlg) throws CacheException {
        try {
            Element element = new Element( key, value );
            cache.put( element );
        }
        catch (IllegalArgumentException e) {
            throw new CacheException( e );
        }
        catch (IllegalStateException e) {
            throw new CacheException( e );
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException( e );
        }

        if(listener != null&&boardset&&broadFlg){
            listener.notifyElementPut(cache.getName(), key,value);
        }
    }

    @Override
	public void put(Object key, Object value, Integer expiretime) throws CacheException {
		put(key,value,expiretime,true);
	}

    @Override
    public void put(Object key, Object value, Integer expiretime, boolean broadFlg) throws CacheException {
        try {
            Element element = new Element( key, value,false,expiretime,expiretime );
            cache.put( element );
        }
        catch (IllegalArgumentException e) {
            throw new CacheException( e );
        }
        catch (IllegalStateException e) {
            throw new CacheException( e );
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException( e );
        }

        if(listener != null&&boardset&&broadFlg){
            listener.notifyElementPut(cache.getName(), key,value,expiretime);
        }
    }

    /**
	 * Removes the element which matches the key
	 * If no element matches, nothing is removed and no Exception is thrown.
	 *
	 * @param key the key of the element to remove
	 * @throws CacheException cache exception
	 */
	@Override
	public void evict(Object key) throws CacheException {
		evict(key,true);
	}

	public void evict(Object key,boolean broadFlg) throws CacheException{
		try {
			cache.remove( key );
		}
		catch (IllegalStateException e) {
			throw new CacheException( e );
		}
		catch (net.sf.ehcache.CacheException e) {
			throw new CacheException( e );
		}
        if(listener != null&&broadFlg){
            listener.notifyElementRemoved(cache.getName(), key);
        }

	}

	/* (non-Javadoc)
	 * @see net.oschina.j2cache.Cache#batchRemove(java.util.List)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void evict(List keys) throws CacheException {
		evict(keys,true);
	}

	public void evict(List keys,boolean broadFlg) throws CacheException{
		cache.removeAll(keys);
        if(listener != null&&broadFlg){
            listener.notifyElementRemoved(cache.getName(), keys);
		}
	}

	/**
	 * Remove all elements in the cache, but leave the cache
	 * in a useable state.
	 *
	 * @throws CacheException cache exception
	 */
	public void clear() throws CacheException {
		clear(true);
	}

	public void clear(boolean broadFlg) throws CacheException {
		try {
			cache.removeAll();
		}
		catch (IllegalStateException e) {
			throw new CacheException( e );
		}
		catch (net.sf.ehcache.CacheException e) {
			throw new CacheException( e );
		}
        if(listener != null&&broadFlg){
			listener.notifyRemoveAll(cache.getName());
		}
	}

	/**
	 * Remove the cache and make it unuseable.
	 *
	 * @throws CacheException  cache exception
	 */
	public void destroy() throws CacheException {
		try {
			cache.getCacheManager().removeCache( cache.getName() );
		}
		catch (IllegalStateException e) {
			throw new CacheException( e );
		}
		catch (net.sf.ehcache.CacheException e) {
			throw new CacheException( e );
		}
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void notifyElementRemoved(Ehcache cache, Element element) throws net.sf.ehcache.CacheException {

	}

	@Override
	public void notifyElementPut(Ehcache cache, Element element) throws net.sf.ehcache.CacheException {

	}

	@Override
	public void notifyElementUpdated(Ehcache cache, Element element) throws net.sf.ehcache.CacheException {

	}

	@Override
	public void notifyElementEvicted(Ehcache cache, Element element) {
//		if(listener != null){
//			listener.notifyElementEvicted(cache.getName(), element.getObjectKey());
//		}
	}

	@Override
	public void notifyRemoveAll(Ehcache cache) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public void notifyElementExpired(Ehcache cache, Element elem) {
		if(listener != null){
			listener.notifyElementExpired(cache.getName(), elem.getObjectKey());
		}
	}
}