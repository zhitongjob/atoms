package com.lovver.atoms.broadcast.redis;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsBroadCastBean;
import com.lovver.atoms.config.AtomsBroadCastConfigBean;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;

public class RedisPubSub extends JedisPubSub{
	private static Serializer serializer=AtomsContext.getSerializer();
	private Jedis jedis;//
	
	private AtomsBroadCastConfigBean broadcastConfig;
	public RedisPubSub(AtomsBroadCastBean broadcastBean){
		broadcastConfig=broadcastBean.getBroadcastConfig();
		String host=this.broadcastConfig.getHost();
		String port=broadcastConfig.getPort();
		if(StringUtils.isEmpty(port)){
			port="6379";
		}
		int iPort=Integer.parseInt(port);
		String timeout=this.broadcastConfig.getTimeout();
		if(StringUtils.isEmpty(timeout)) {
			jedis = new Jedis(host, iPort);
		}else{
			int iTimeout=Integer.parseInt(timeout);
			jedis = new Jedis(host, iPort,iTimeout);
		}
		String password=broadcastConfig.getPassword();
		if(!StringUtils.isEmpty(password)){
			jedis.auth(password);
		}
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
	
    @SuppressWarnings("rawtypes")
	public void onMessage(String channel, String message){
		if (message != null && message.length() <= 0) {
			System.out.println("Message is empty.");
			return;
		}
		
		try {
			Command cmd = JSON.parseObject(message, Command.class);
			if (cmd == null)
				return;
			String client_id=cmd.getClient_id();
			
			Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
			switch (cmd.getOperator()) {
			case Command.OPT_DELETE_KEY:
				for(int i=1;i<=mCacheProvider.size();i++){
					if(i==1&&AtomsContext.isMe(client_id)){
						continue;
					}else{
						Cache cache=AtomsContext.getCache(cmd.getRegion(), i,client_id);
						Object key=cmd.getKey();
						if(key instanceof List){
							cache.evict((List)key);
						}else{
							cache.evict(cmd.getKey()); 
						}
					}
				}
				break;
			case Command.OPT_CLEAR_KEY:
				for(int i=1;i<=mCacheProvider.size();i++){
					if(i==1&&AtomsContext.isMe(client_id)){
						continue;
					}else{
						Cache cache=AtomsContext.getCache(cmd.getRegion(), i,client_id);
						cache.clear();
					}
				}
				break;
			case Command.OPT_PUT_KEY:
				for(int i=1;i<=mCacheProvider.size();i++){
					if(i==1&&AtomsContext.isMe(client_id)){
						continue;
					}else{
						Cache cache=AtomsContext.getCache(cmd.getRegion(), i,client_id);
						cache.put(cmd.getKey(), serializer.deserialize(cmd.getValue())); 
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
