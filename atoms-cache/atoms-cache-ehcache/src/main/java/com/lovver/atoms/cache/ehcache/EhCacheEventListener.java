package com.lovver.atoms.cache.ehcache;


import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsCacheBean;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;


@SPI("ehcache")
public class EhCacheEventListener implements CacheEventListener {
	private static Serializer serializer=AtomsContext.getSerializer();
	
	private static byte getExpiredOperator(){
		String expiredOperator;
		AtomsCacheBean cacheBean=AtomsContext.getAtomsCacheBean(1);
		expiredOperator=cacheBean.getExpiredOperator();
		if(!StringUtils.isEmpty(expiredOperator)){
			expiredOperator=expiredOperator.toLowerCase();
		}
		if("update".equals(expiredOperator)){
			return Command.EXPIRE_UPDATE;
		}else{
			return Command.EXPIRE_DELETE;
		}
	}
	@Override
	public void notifyElementExpired(String region, Object key) {
		System.out.println("notifyElementExpired");
		byte expiredOperator=getExpiredOperator();
		if(level==1){//判断是否是第一级缓存失效
			byte operator;
			if(expiredOperator==Command.EXPIRE_DELETE){
				operator=Command.OPT_DELETE_KEY;
				Command cmd=new Command(operator,expiredOperator,region,key);
				broadCast.broadcast(JSON.toJSONString(cmd));
			}else{
				operator=Command.OPT_PUT_KEY;
				Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
				Object value=null;
				int targetCache=level+1;
				for(int i=targetCache;i<=mCacheProvider.size();i++){
					Cache cache=AtomsContext.getCache(region, i);
					value=cache.get(key);
					if(value!=null){
						break;
					}
				}
				Cache cache=AtomsContext.getCache(region, 1);
				cache.put(key, value); 
			}
			
		}else{
			int targetCache=level+1;
			if(Command.EXPIRE_DELETE==expiredOperator){
//				Object[] listenerCache=AtomsContext.getCacheAndListener(region, targetCache);
//				CacheEventListener listener=(CacheEventListener) listenerCache[1];
				Cache cache=AtomsContext.getCache(region, targetCache);
				cache.evict(key); 
				if(null!=broadCast){
					Command cmd=new Command(Command.OPT_DELETE_KEY,expiredOperator,region,key);
					broadCast.broadcast(JSON.toJSONString(cmd));
				}
			}else{
				Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
				Object value=null;
				for(int i=targetCache;i<=mCacheProvider.size();i++){
					
					Cache cache=AtomsContext.getCache(region, i);
					value=cache.get(key);
					if(value!=null){
						break;
					}
				}
				Cache cache=AtomsContext.getCache(region, 1);
				cache.put(key, value); 
			}
		}
	}

	@Override
	public void notifyElementRemoved(String region, Object key)
			throws CacheException {
		System.out.println("notifyElementRemoved");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_DELETE_KEY,region,key);
			broadCast.broadcast(JSON.toJSONString(cmd));
		}
	}

	@Override
	public void notifyElementPut(String region, Object key, Object value)
			throws CacheException {
		try{
			System.out.println("notifyElementPut");
			if(null!=broadCast){
				Command cmd=new Command(Command.OPT_PUT_KEY,region,key,serializer.serialize(value));
				broadCast.broadcast(JSON.toJSONString(cmd));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void notifyElementUpdated(String region, Object key, Object value)
			throws CacheException {
		try{
			System.out.println("notifyElementUpdated");
			if(null!=broadCast){
				Command cmd=new Command(Command.OPT_PUT_KEY,region,key,serializer.serialize(value));
				broadCast.broadcast(JSON.toJSONString(cmd));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void notifyElementEvicted(String region, Object key, Object value) {
		System.out.println("notifyElementEvicted");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_DELETE_KEY,region,key);
			broadCast.broadcast(JSON.toJSONString(cmd));
		}
	}

	@Override
	public void notifyRemoveAll(String region) {
		System.out.println("notifyRemoveAll");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_CLEAR_KEY,region);
			broadCast.broadcast(JSON.toJSONString(cmd));
		}
	}

	@Override
	public void init(String level) {
		this.level=Integer.parseInt(level);
		broadCast=AtomsContext.getBroadCast().get(this.level+"");
	}
	private int level;
	private static BroadCast broadCast;

}
