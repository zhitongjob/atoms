package com.lovver.atoms.cache.ehcache;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.CacheManager;

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
	private ConcurrentHashMap<String, EhCache> _CacheManager = new ConcurrentHashMap<String, EhCache> () ;
	private int level;

	@Override
	public String name() {
		return "ehcache";
	}

    public EhCache buildCache(String regionName, boolean autoCreate, CacheEventListener listener) throws CacheException {
    	EhCache ehcache = _CacheManager.get(regionName);
    	if(ehcache == null && autoCreate){
		    try {
	            synchronized(_CacheManager){
	            	ehcache = _CacheManager.get(regionName);
	            	if(ehcache == null){
			            net.sf.ehcache.Cache cache = manager.getCache(regionName);
			            if (cache == null) {
			                log.warn("Could not find configuration [" + regionName + "]; using defaults.");
			                manager.addCache(regionName);
			                cache = manager.getCache(regionName);
			                Map<String,String> mapTTL=AtomsContext.getTTLConfig(this.level);
			                String ttlSeconds=mapTTL.get(regionName);
			                cache.getCacheConfiguration().setTimeToLiveSeconds(Long.parseLong(ttlSeconds)); 
			                log.debug("started EHCache region: " + regionName);                
			            }
			            ehcache = new EhCache(cache, listener);
			            _CacheManager.put(regionName, ehcache);
	            	}
	            }
		    }
	        catch (net.sf.ehcache.CacheException e) {
	            throw new CacheException(e);
	        }
    	}
        return ehcache;
    }

	/**
	 * Callback to perform any necessary initialization of the underlying cache implementation
	 * during SessionFactory construction.
	 *
	 * @param props current configuration settings.
	 */
	public void start(AtomsCacheBean cacheBean) throws CacheException {
		if (manager != null) {
            log.warn("Attempt to restart an already started EhCacheProvider.");
            return;
        }
		String configFile=cacheBean.getCacheConfig().getConfigFile();
		if(StringUtils.isNotEmpty(configFile)){
			try{
				manager = new CacheManager(configFile);
			}catch(Exception e){
				URL uriConfigFile=ClassUtils.getDefaultClassLoader().getResource(configFile);
				manager = new CacheManager(uriConfigFile);
			}
		}else{
			manager = CacheManager.getInstance();
		}
		this.level=Integer.parseInt(cacheBean.getLevel());
        _CacheManager = new ConcurrentHashMap<String, EhCache>();
	}

	/**
	 * Callback to perform any necessary cleanup of the underlying cache implementation.
	 */
	public void stop() {
		if (manager != null) {
            manager.shutdown();
            _CacheManager.clear();
            manager = null;
        }
	}

}
