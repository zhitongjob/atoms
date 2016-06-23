package com.lovver.atoms.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.broadcast.BroadCastFactory;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheEventListenerFactory;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.cache.CacheProviderFactory;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsBean;
import com.lovver.atoms.config.AtomsCacheBean;
import com.lovver.atoms.config.AtomsConfig;
import com.lovver.atoms.serializer.Serializer;
import com.lovver.atoms.serializer.SerializerFactory;

public class AtomsContext {
	private static AtomsBean atomBean = AtomsConfig.getAtomsConfig();
	private static Serializer serializer = null;
//	private static BroadCast broadCast = null;
	
	private static ConcurrentHashMap<String,CacheProvider> cacheProvider=new ConcurrentHashMap<String,CacheProvider>();
	private static ConcurrentHashMap<String,AtomsCacheBean> cacheConfig=new ConcurrentHashMap<String,AtomsCacheBean>();
	private static ConcurrentHashMap<String,BroadCast> broadCasts=new ConcurrentHashMap<String,BroadCast>();

	static {
		try {
			serializer=SerializerFactory.getSerializer(atomBean.getSerializer());
			List<AtomsCacheBean> lstCache= atomBean.getCache();
			for(AtomsCacheBean cacheBean:lstCache){
				if(StringUtils.isEmpty(cacheBean.getLevel())){
					throw new RuntimeException("cache level must give!");
				}
				int level=Integer.parseInt(cacheBean.getLevel());
				cacheProvider.put(cacheBean.getLevel(), CacheProviderFactory.getCacheProvider(cacheBean,level));
				if(level<lstCache.size()){//最后一级缓存不需要广播
					BroadCast broadCast=BroadCastFactory.getBroadCast(atomBean.getBroadcast(),level);
					broadCasts.put(cacheBean.getLevel(), broadCast);
				}
				cacheConfig.put(cacheBean.getLevel(), cacheBean);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static AtomsCacheBean getAtomsCacheBean(int level){
		return cacheConfig.get(level+"");
	}
	
	public static Map<String,CacheProvider> getCacheProvider(){
		return cacheProvider;
	}
	
	public static Serializer getSerializer(){
		return serializer;
	}
	
	public static Map<String,BroadCast> getBroadCast(){
		return broadCasts;
	}
	
	public static Cache getCache(String region,int level){
		CacheProvider cacheProvider=AtomsContext.getCacheProvider().get(level+"");
		CacheEventListener listener=CacheEventListenerFactory.getCacheEventListener(cacheProvider.name(), level+"");
		Cache cache=cacheProvider.buildCache(region, true, listener);
		return cache;
	}
	/**
	 * 
	 * @param region
	 * @param level
	 * @return Object[]{cache,listener}
	 */
	public static Object[] getCacheAndListener(String region,int level){
		CacheProvider cacheProvider=AtomsContext.getCacheProvider().get(level+"");
		CacheEventListener listener=CacheEventListenerFactory.getCacheEventListener(cacheProvider.name(), level+"");
		Cache cache=cacheProvider.buildCache(region, true, listener);
		return new Object[]{cache,listener};
	}

}
