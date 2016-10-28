package com.lovver.atoms.core;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsCacheBean;
import com.lovver.atoms.config.AtomsConfig;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;

public class CacheChannel {
	private static AtomsCacheBean level1CacheBean=AtomsContext.getAtomsCacheBean(1) ;
	private static CacheChannel instance=new CacheChannel();
	private static BroadCast broadCast=AtomsContext.getBroadCast().get(1+"");
	private static Serializer serializer=AtomsContext.getSerializer();
	
	public static CacheChannel getInstance(){
		return instance;
	}
	
	
	private Object getNextLevelCache(String region,Object key){
		Object value=null;
		Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
		for(int i=2;i<=mCacheProvider.size();i++){
			
			Cache cache=AtomsContext.getCache(region, i);
			value=cache.get(key);
			if(value!=null){
				break;
			}
		}
		return value;
	}
	

	/**
	 * 获取缓存中的数据
	 * 
	 * @param region
	 *            : Cache Region name
	 * @param key
	 *            : Cache key
	 * @return cache object
	 */
	public Object get(String region, Object key) {
		
		if (region != null && key != null) {
			Cache tCache=CacheManager.getCache(1, region, true);
			Object value=tCache.get(key);
			if(value==null){
				String expiredOp=level1CacheBean.getExpiredOperator();
				String delete_atom=level1CacheBean.getDelete_atom();
				if("true".equals(delete_atom.toLowerCase())){//原子事务删除
					value=getNextLevelCache(region,key);
					if(value!=null){
						Cache cache=CacheManager.getCache(1,region,true);
						cache.put(key, value);
					}
					return value;
				}
				if("delete".equals(expiredOp.toLowerCase())){
					value=getNextLevelCache(region,key);
					if(value!=null){
						Cache cache=CacheManager.getCache(1,region,true);
						cache.put(key, value);
					}
					return value;
				}else{
					try {
						String waitTime=level1CacheBean.getWaitTime();
						if(StringUtils.isEmpty(waitTime)){
							waitTime="1000";
						}
						Thread.sleep(Integer.parseInt(waitTime)); 
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					value=getNextLevelCache(region,key);
				}
			}
			return value;
		}
		return null;
	}

	/**
	 * 写入缓存
	 * 
	 * @param region
	 *            : Cache Region name
	 * @param key
	 *            : Cache key
	 * @param value
	 *            : Cache value
	 */
	public void set(String region, Object key, Object value) {
		if (region != null && key != null) {
			if (value == null)
				evict(region, key);
			else {
				Cache cache=CacheManager.getCache(1,region,true);
				cache.put(key, value); 
			}
		}
		
		Command cmd;
		try {
			cmd = new Command(Command.OPT_PUT_KEY,region,key,serializer.serialize(value));
			broadCast.broadcast(JSON.toJSONString(cmd));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除缓存
	 * 
	 * @param region
	 *            : Cache Region name
	 * @param key
	 *            : Cache key
	 */
	public void evict(String region, Object key) {
		String delete_atom=level1CacheBean.getDelete_atom();
		if("true".equals(delete_atom.toLowerCase())){//原子事务删除
			Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
			for(int i=1;i<=mCacheProvider.size();i++){
				Cache cache=AtomsContext.getCache(region, i);
				cache.evict(key); 
			}
		}else{
			Cache cache=CacheManager.getCache(1,region,true);
			cache.evict(key); 
		}
		
		Command cmd= new Command(Command.OPT_DELETE_KEY,region,key);
		broadCast.broadcast(JSON.toJSONString(cmd));
		
	}

	/**
	 * 批量删除缓存
	 * 
	 * @param region
	 *            : Cache region name
	 * @param keys
	 *            : Cache key
	 */
	@SuppressWarnings({ "rawtypes" })
	public void batchEvict(String region, List keys) {
		String delete_atom=level1CacheBean.getDelete_atom();
		if("true".equals(delete_atom.toLowerCase())){//原子事务删除
			Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
			for(int i=1;i<=mCacheProvider.size();i++){
				Cache cache=AtomsContext.getCache(region, i);
				cache.evict(keys); 
			}
		}else{
			Cache cache=CacheManager.getCache(1,region,true);
			cache.evict(keys);
		}
		
		Command cmd= new Command(Command.OPT_DELETE_KEY,region,keys);
		broadCast.broadcast(JSON.toJSONString(cmd));
	}

	/**
	 * Clear the cache
	 * 
	 * @param region
	 *            : Cache region name
	 */
	public void clear(String region) throws CacheException {
		String delete_atom=level1CacheBean.getDelete_atom();
		if("true".equals(delete_atom.toLowerCase())){//原子事务删除
			Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
			for(int i=1;i<=mCacheProvider.size();i++){
				Cache cache=AtomsContext.getCache(region, i);
				cache.clear(); 
			}
		}else{
			Cache cache=CacheManager.getCache(1,region,true);
			cache.clear(); 
		}
		Command cmd=new Command(Command.OPT_CLEAR_KEY,region);
//		if(cmd.isSender()){
			broadCast.broadcast(JSON.toJSONString(cmd));
//		}
	}

	/**
	 * Get cache region keys
	 * 
	 * @param region
	 *            : Cache region name
	 * @return key list
	 */
	@SuppressWarnings("rawtypes")
	public List keys(String region) throws CacheException {
		List lstRet=null;
		String delete_atom=level1CacheBean.getDelete_atom();
		if("true".equals(delete_atom.toLowerCase())){//原子事务删除
			if(lstRet==null||lstRet.size()==0){
				Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
				for(int i=1;i<=mCacheProvider.size();i++){
					Cache nextCache=AtomsContext.getCache(region, i);
					lstRet=nextCache.keys();
					if(lstRet!=null&&lstRet.size()!=0){
						break;
					}
				}
			}
		}else{
			Cache cache=CacheManager.getCache(1,region,true);
			lstRet= cache.keys();
		}
		return lstRet;
	}

	

	/**
	 * 关闭到通道的连接
	 */
	public void close() {
		Collection<AtomsCacheBean> lstCacheBean=AtomsConfig.getAtomsConfig().getCache();
		for(AtomsCacheBean cp:lstCacheBean){
			CacheManager.shutdown(Integer.parseInt(cp.getLevel()));
		}
		
	}
}