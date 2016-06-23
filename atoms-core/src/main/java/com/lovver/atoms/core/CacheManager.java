package com.lovver.atoms.core;

import java.util.Map;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheEventListenerFactory;
import com.lovver.atoms.cache.CacheProvider;
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
}
