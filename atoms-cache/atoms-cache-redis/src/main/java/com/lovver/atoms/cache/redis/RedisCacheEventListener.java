package com.lovver.atoms.cache.redis;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;

@SPI("redis")
public class RedisCacheEventListener implements CacheEventListener {

	private static Serializer serializer=AtomsContext.getSerializer();
	
	public void notifyElementExpired(String region, Object key,String client_id) {
//		System.out.println("notifyElementExpired");
//		if(level==1){//判断是否是第一级缓存失效
//			byte operator;
//			if(expiredOperator==Command.EXPIRE_DELETE){
//				operator=Command.OPT_DELETE_KEY;
//				Command cmd=new Command(operator,expiredOperator,region,key,Command.CLIENT_ID);
//				broadCast.broadcast(JSON.toJSONString(cmd));
//			}else{
//				operator=Command.OPT_PUT_KEY;
//				Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
//				Object value=null;
//				int targetCache=level+1;
//				for(int i=targetCache;i<=mCacheProvider.size();i++){
//					Cache cache=AtomsContext.getCache(region, i);
//					value=cache.get(key);
//					if(value!=null){
//						break;
//					}
//				}
//				Cache cache=AtomsContext.getCache(region, 1);
//				cache.put(key, value); 
//			}
//			
//		}else{
//			int targetCache=level+1;
//			if(Command.EXPIRE_DELETE==expiredOperator){
////				Object[] listenerCache=AtomsContext.getCacheAndListener(region, targetCache);
////				CacheEventListener listener=(CacheEventListener) listenerCache[1];
//				Cache cache=AtomsContext.getCache(region, targetCache);
//				cache.evict(key); 
//				if(null!=broadCast){
//					Command cmd=new Command(Command.OPT_DELETE_KEY,expiredOperator,region,key,Command.CLIENT_ID);
//					broadCast.broadcast(JSON.toJSONString(cmd));
//				}
//			}else{
//				Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
//				Object value=null;
//				for(int i=targetCache;i<=mCacheProvider.size();i++){
//					
//					Cache cache=AtomsContext.getCache(region, i);
//					value=cache.get(key);
//					if(value!=null){
//						break;
//					}
//				}
//				Cache cache=AtomsContext.getCache(region, 1);
//				cache.put(key, value); 
//			}
//		}
	}

	@Override
	public void notifyElementRemoved(String region, Object key,String client_id)
			throws CacheException {
		System.out.println("notifyElementRemoved|self"+AtomsContext.CLIENT_ID+"=="+client_id);
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_DELETE_KEY,region,key,client_id);
			broadCast.broadcast(JSON.toJSONString(cmd));
		}
	}

	@Override
	public void notifyElementPut(String region, Object key, Object value,String client_id)
			throws CacheException {
		try{
			System.out.println("notifyElementPut");
			if(null!=broadCast){
				Command cmd=new Command(Command.OPT_PUT_KEY,region,key,serializer.serialize(value),client_id);
				broadCast.broadcast(JSON.toJSONString(cmd));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void notifyElementUpdated(String region, Object key, Object value,String client_id)
			throws CacheException {
		try{
			System.out.println("notifyElementUpdated");
			if(null!=broadCast){
				Command cmd=new Command(Command.OPT_PUT_KEY,region,key,serializer.serialize(value),client_id);
				broadCast.broadcast(JSON.toJSONString(cmd));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void notifyElementEvicted(String region, Object key, Object value,String client_id) {
		System.out.println("notifyElementEvicted");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_DELETE_KEY,region,key,client_id);
			broadCast.broadcast(JSON.toJSONString(cmd));
		}
	}

	@Override
	public void notifyRemoveAll(String region,String client_id) {
		System.out.println("notifyRemoveAll");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_CLEAR_KEY,region,client_id);
			broadCast.broadcast(JSON.toJSONString(cmd));
		}
	}

	private int level;
	@Override
	public void init(int level) {
		this.level=level;
		broadCast=AtomsContext.getBroadCast();
	}
	private static BroadCast broadCast;
}
