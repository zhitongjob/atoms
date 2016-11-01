package com.lovver.atoms.context;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.lovver.atoms.config.AtomsCacheTTLBean;
import com.lovver.atoms.config.AtomsCacheTTLConfigBean;
import com.lovver.atoms.config.AtomsConfig;
import com.lovver.atoms.serializer.Serializer;
import com.lovver.atoms.serializer.SerializerFactory;

public class AtomsContext {
	private static AtomsBean atomBean = AtomsConfig.getAtomsConfig();
	private static Serializer serializer = null;
	private static BroadCast broadCast = null;
	
	
	//客户端身份标示
	public final static String CLIENT_ID = UUID.randomUUID().toString();
	
	private static ConcurrentHashMap<String,CacheProvider> cacheProvider=new ConcurrentHashMap<String,CacheProvider>();
	private static ConcurrentHashMap<String,AtomsCacheBean> cacheConfig=new ConcurrentHashMap<String,AtomsCacheBean>();
//	private static ConcurrentHashMap<String,BroadCast> broadCasts=new ConcurrentHashMap<String,BroadCast>();
	private static ConcurrentHashMap<String,ConcurrentHashMap<String,String>> cahcelevelTTLConfig=new ConcurrentHashMap<String,ConcurrentHashMap<String,String>>();

	static {
		try {
			serializer=SerializerFactory.getSerializer(atomBean.getSerializer());
			List<AtomsCacheBean> lstCache= atomBean.getCache();
			
			broadCast=BroadCastFactory.getBroadCast(atomBean.getBroadcast());
			
			ConcurrentHashMap<String,String> levelTTLConfig=null;
			for(AtomsCacheBean cacheBean:lstCache){
				levelTTLConfig=new ConcurrentHashMap<String,String>();
				
				if(StringUtils.isEmpty(cacheBean.getLevel())){
					throw new RuntimeException("cache level must give!");
				}
				int level=Integer.parseInt(cacheBean.getLevel());
				cacheProvider.put(cacheBean.getLevel(), CacheProviderFactory.getCacheProvider(cacheBean,level));
				
				cacheConfig.put(cacheBean.getLevel(), cacheBean);
				
				AtomsCacheTTLBean cacheTTLBean=cacheBean.getCacheTTL();
				if(null!=cacheTTLBean){
					List<AtomsCacheTTLConfigBean> lstTTLConfigBean=cacheTTLBean.getLstTTL();
					if(null!=lstTTLConfigBean&&lstTTLConfigBean.size()>0){						
						for(AtomsCacheTTLConfigBean ttl:lstTTLConfigBean){
							levelTTLConfig.put(ttl.getName(), ttl.getValue());
						}
					}
				}
				cahcelevelTTLConfig.put(cacheBean.getLevel(), levelTTLConfig);
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
	
	public static BroadCast getBroadCast(){
		return broadCast;
	}
	
	public static Cache getCache(String region,int level){
		CacheProvider cacheProvider=AtomsContext.getCacheProvider().get(level+"");
		CacheEventListener listener=null;
		if(level==1){//只有第一级缓存有监听器
			listener=CacheEventListenerFactory.getCacheEventListener(cacheProvider.name(),level);
		}
		Cache cache=cacheProvider.buildCache(region, true, listener,CLIENT_ID);
		return cache;
	}
	
	public static Cache getCache(String region,int level,String client_id){
		CacheProvider cacheProvider=AtomsContext.getCacheProvider().get(level+"");
		CacheEventListener listener=null;
		if(level==1){//只有第一级缓存有监听器
			listener=CacheEventListenerFactory.getCacheEventListener(cacheProvider.name(),level);
		}
		Cache cache=cacheProvider.buildCache(region, true, listener,client_id);
		return cache;
	}
	
	public static Map<String,String> getTTLConfig(int level){
		return cahcelevelTTLConfig.get(level+"");
	}
	/**
	 * 
	 * @param region
	 * @param level
	 * @return Object[]{cache,listener}
	 */
	public static Object[] getCacheAndListener(String region,int level){
		CacheProvider cacheProvider=AtomsContext.getCacheProvider().get(level+"");
		CacheEventListener listener=null;
		if(level==1){
			listener=CacheEventListenerFactory.getCacheEventListener(cacheProvider.name(),level);
		}
		Cache cache=cacheProvider.buildCache(region, true, listener,CLIENT_ID);
		return new Object[]{cache,listener};
	}
	
	public static boolean isMe(String client_id){
		if(client_id!=null&&client_id.equals(CLIENT_ID)){
			return true;
		}else{
			return false;
		}
	}
}
