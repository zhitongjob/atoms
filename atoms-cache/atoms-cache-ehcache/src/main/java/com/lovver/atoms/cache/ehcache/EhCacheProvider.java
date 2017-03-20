package com.lovver.atoms.cache.ehcache;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lovver.atoms.cache.Cache;
import net.sf.ehcache.CacheManager;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.common.utils.ClassUtils;
import com.lovver.atoms.config.AtomsCacheBean;
import com.lovver.atoms.context.AtomsContext;

/**
 * EhCache Provider plugin
 * 
 * @author jobell
 */
@SPI("ehcache")
public class EhCacheProvider implements CacheProvider {

	private final static Logger log = LoggerFactory.getLogger(EhCacheProvider.class);

	private CacheManager manager;
//	private ConcurrentHashMap<String, EhCache> _CacheManager = new ConcurrentHashMap<String, EhCache> () ;
	private int level;

	@Override
	public String name() {
		return "ehcache";
	}



    public EhCache buildCache(String regionName, boolean autoCreate,final CacheEventListener listener,String client_id) throws CacheException {
    	EhCache ehcache = null;
//    	EhCache ehcache = _CacheManager.get(regionName);
//    	if(ehcache == null && autoCreate){
//		    try {
//	            synchronized(_CacheManager){
//	            	ehcache = _CacheManager.get(regionName);
//	            	if(ehcache == null){
			            net.sf.ehcache.Cache cache = manager.getCache(regionName);
			            if (cache == null) {
			            	log.warn("Could not find configuration [" + regionName + "]; using defaults.");
			            	 
			            	Map<String,String> mapTTL=AtomsContext.getTTLConfig(this.level);
			            	String ttlSeconds=mapTTL.get(regionName);
			            	if(StringUtils.isNotEmpty(ttlSeconds)){
			            		cache=new net.sf.ehcache.Cache(regionName,1000,false,false,Long.parseLong(ttlSeconds),Long.parseLong(ttlSeconds));
			            		manager.addCache(cache);
			            	}else{
				                manager.addCache(regionName);
				                cache = manager.getCache(regionName);
			            	}
			               
			            	if(StringUtils.isNotEmpty(ttlSeconds)){
			            		cache.getCacheConfiguration().setTimeToLiveSeconds(Long.parseLong(ttlSeconds)); 
			            	}
			                log.debug("started EHCache region: " + regionName);

							cache.getCacheEventNotificationService().registerListener(new net.sf.ehcache.event.CacheEventListener(){

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
								}
								@Override
								public void notifyRemoveAll(Ehcache cache) {
								}
								@Override
								public void dispose() {
								}

								private Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();

								@Override
								public void notifyElementExpired(Ehcache cache, Element elem) {
									System.out.println("EhCache-notifyElementExpired[name]="+cache.getName()+"[key]="+elem.getObjectKey());
									for(int i=(level+1);i<=mCacheProvider.size();i++){
										Cache lCache=AtomsContext.getCache(cache.getName(), i);
										Object key=elem.getObjectKey();
										if(key instanceof List){
											lCache.evict((List)key);
										}else{
											lCache.evict(key);
										}
									}
									if(listener != null){
										listener.notifyElementExpired(cache.getName(), elem.getObjectKey(),AtomsContext.CLIENT_ID);
									}
								}
							});
//							this.cache.getCacheEventNotificationService().registerListener(this);
			            }
			            ehcache = new EhCache(cache, listener,client_id);
//			            _CacheManager.put(regionName, ehcache);
//	            	}
//	            }
//		    }
//	        catch (net.sf.ehcache.CacheException e) {
//	            throw new CacheException(e);
//	        }
//    	}
        return ehcache;
    }

	/**
	 * Callback to perform any necessary initialization of the underlying cache implementation
	 * during SessionFactory construction.
	 *
	 * @param  cacheBean current configuration settings.
	 */
	public void start(AtomsCacheBean cacheBean) throws CacheException {
		if (manager != null) {
            log.warn("Attempt to restart an already started EhCacheProvider.");
            return;
        }
		String configFile=cacheBean.getCacheConfig().getConfigFile();
		if(StringUtils.isNotEmpty(configFile)){
			try{
				manager=CacheManager.create(configFile);
				//manager = new CacheManager(configFile);
			}catch(Exception e){
				URL uriConfigFile=ClassUtils.getDefaultClassLoader().getResource(configFile);
//				manager = new CacheManager(uriConfigFile);
				manager=CacheManager.create(uriConfigFile);
			}
		}else{
			manager = CacheManager.getInstance();
		}
		this.level=Integer.parseInt(cacheBean.getLevel());
//        _CacheManager = new ConcurrentHashMap<String, EhCache>();
	}

	/**
	 * Callback to perform any necessary cleanup of the underlying cache implementation.
	 */
	public void stop() {
		if (manager != null) {
            manager.shutdown();
//            _CacheManager.clear();
            manager = null;
        }
	}

}
