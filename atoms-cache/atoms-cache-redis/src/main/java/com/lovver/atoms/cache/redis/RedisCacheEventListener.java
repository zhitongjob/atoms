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

	public void notifyElementExpired(String region, Object key) {
	}

	@Override
	public void notifyElementRemoved(String region, Object key)
			throws CacheException {
		System.out.println("notifyElementRemoved|self");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_DELETE_KEY,region,key);
			broadCast.broadcast(cmd.toBuffers());
		}
	}

	@Override
	public void notifyElementPut(String region, Object key, Object value)
			throws CacheException {
//		try{
//			System.out.println("notifyElementPut|["+region+"]["+key+"]------client_id="+client_id);
//			if(null!=broadCast){
//				Command cmd=new Command(Command.OPT_PUT_KEY,region,key,serializer.serialize(value),client_id);
//				broadCast.broadcast(JSON.toJSONString(cmd));
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
	}

	@Override
	public void notifyElementUpdated(String region, Object key, Object value)
			throws CacheException {
//		try{
//			System.out.println("notifyElementUpdated|["+region+"]["+key+"]------client_id="+client_id);
//			if(null!=broadCast){
//				Command cmd=new Command(Command.OPT_PUT_KEY,region,key,serializer.serialize(value),client_id);
//				broadCast.broadcast(JSON.toJSONString(cmd));
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
	}

	@Override
	public void notifyElementEvicted(String region, Object key) {
		System.out.println("notifyElementEvicted|["+region+"]["+key+"]------");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_DELETE_KEY,region,key);
			broadCast.broadcast(cmd.toBuffers());
		}
	}

	@Override
	public void notifyRemoveAll(String region) {
		System.out.println("notifyRemoveAll|["+region+"]------");
		if(null!=broadCast){
			Command cmd=new Command(Command.OPT_CLEAR_KEY,region,"");
			broadCast.broadcast(cmd.toBuffers());
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
