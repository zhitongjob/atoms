package com.lovver.atoms.broadcast.redis;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsBroadCastBean;
import com.lovver.atoms.config.AtomsBroadCastConfigBean;
import com.lovver.atoms.config.AtomsCacheBean;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;

public class RedisPubSub extends JedisPubSub{
	private static Serializer serializer=AtomsContext.getSerializer();
	private Jedis jedis;//
	
	private AtomsBroadCastConfigBean broadcastConfig;
	private int level;
	
	public RedisPubSub(AtomsBroadCastBean broadcastBean,int level){
		broadcastConfig=broadcastBean.getBroadcastConfig();
		this.level=level;
		String host=this.broadcastConfig.getHost();
		String port=broadcastConfig.getPort();
		if(StringUtils.isEmpty(port)){
			port="6379";
		}
		int iPort=Integer.parseInt(port);
		jedis = new Jedis(host,iPort);
		String password=broadcastConfig.getPassword();
		if(!StringUtils.isEmpty(password)){
			jedis.auth(password);
		}
		System.out.println("RedisPubSub "+ level+"===="+this);
	}
	
	public void pub(String channel,String message){
		jedis.publish(channel, message);
	}
	
	
	public void sub(JedisPubSub listener,String channel){
		jedis.subscribe(listener, channel);
	}
	
	public void close(String channel){
		jedis.close();
	}
	
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
	
	private static boolean getDeleteAtom(){
		String delete_atom;
		AtomsCacheBean cacheBean=AtomsContext.getAtomsCacheBean(1);
		delete_atom=cacheBean.getDelete_atom();
		if(!StringUtils.isEmpty(delete_atom)){
			delete_atom=delete_atom.toLowerCase();
		}
		if("true".equals(delete_atom)){
			return true;
		}else{
			return false;
		}
	}
	
	 public void onMessage(String channel, String message){
		 System.out.println("onMessage "+ level+"===="+this);
		if (message != null && message.length() <= 0) {
//			log.warn("Message is empty.");
			System.out.println("Message is empty.");
			return;
		}
		
		try {
			Command cmd = JSON.parseObject(message, Command.class);
//
			if (cmd == null)
				return;
			
			int targetCache=this.level+1;
			
//			Cache cache=AtomsContext.getCache(cmd.getRegion(), targetCache);
			BroadCast broadCast=null;
			boolean delete_atom=getDeleteAtom();
			System.out.println("delete_atom====="+delete_atom);
			switch (cmd.getOperator()) {
			case Command.OPT_DELETE_KEY:
				if(true==delete_atom){
					Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
					for(int i=1;i<=mCacheProvider.size();i++){
						
						Cache cache=AtomsContext.getCache(cmd.getRegion(), i);
						System.out.println("delete cache====="+AtomsContext.getCacheProvider().get(""+i).name());
						cache.evict(cmd.getKey()); 
					}
				}else{
//					cache.evict(cmd.getKey()); 
					broadCast=AtomsContext.getBroadCast().get(targetCache+"");
					if(broadCast!=null){
						Command bcmd=new Command(Command.OPT_DELETE_KEY,cmd.getRegion(),cmd.getKey());
						broadCast.broadcast(JSON.toJSONString(bcmd));
					}
				}
				break;
			case Command.OPT_CLEAR_KEY:
				     
				if(true==delete_atom){
					Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
					for(int i=1;i<=mCacheProvider.size();i++){
						
						Cache cache=AtomsContext.getCache(cmd.getRegion(), i);
						cache.clear();
					}
				}else{
					broadCast=AtomsContext.getBroadCast().get(targetCache+"");
					if(broadCast!=null){
						Command bcmd=new Command(Command.OPT_CLEAR_KEY,cmd.getRegion());
						broadCast.broadcast(JSON.toJSONString(bcmd));
					}
				}
				break;
			case Command.OPT_PUT_KEY:
				if(true==delete_atom){
					Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
					for(int i=targetCache;i<=mCacheProvider.size();i++){
						
						Cache cache=AtomsContext.getCache(cmd.getRegion(), i);
						cache.put(cmd.getKey(), serializer.deserialize(cmd.getValue())); 
					}
				}else{
					broadCast=AtomsContext.getBroadCast().get(targetCache+"");
					if(broadCast!=null){
						Command bcmd=new Command(Command.OPT_PUT_KEY,cmd.getRegion(),cmd.getKey(),cmd.getValue());
						broadCast.broadcast(JSON.toJSONString(bcmd));
					}
				}
				break;
			default:
//				log.warn("Unknown message type = " + cmd.getOperator());
			}
		} catch (Exception e) {
			e.printStackTrace();
//			log.error("Unable to handle received msg", e);
		}
		
	}
}
