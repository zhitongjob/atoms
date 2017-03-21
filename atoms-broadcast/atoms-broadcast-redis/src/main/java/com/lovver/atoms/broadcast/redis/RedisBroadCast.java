package com.lovver.atoms.broadcast.redis;

import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.config.AtomsBroadCastBean;

@SPI("redis")
public class RedisBroadCast implements BroadCast{
	
	private static String channel_prefix="atoms_channel";
	private String channel = null;
	
	private RedisPubSub redisSub;
	private RedisPubSub redisPub;
	
	private Thread thread_subscribe;

	public void init(AtomsBroadCastBean broadcastBean){
		redisSub=new RedisPubSub(broadcastBean); 
		redisPub=new RedisPubSub(broadcastBean); 
		channel =  channel_prefix;
		thread_subscribe = new Thread(new Runnable() {
			@Override
			public void run() {
				redisSub.sub(redisSub, channel);
			}
		});
		thread_subscribe.start();
	}

	public void broadcast(byte[] message){
		redisPub.pub(channel, message);
	}
}
