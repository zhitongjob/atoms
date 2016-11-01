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
	
//	private int level;
	
	public void init(AtomsBroadCastBean broadcastBean){
		redisSub=new RedisPubSub(broadcastBean); 
		redisPub=new RedisPubSub(broadcastBean); 
		channel =  channel_prefix;
//		this.level=level;
		thread_subscribe = new Thread(new Runnable() {
			@Override
			public void run() {
				redisSub.sub(redisSub, channel);
			}
		});
		thread_subscribe.start();
	}

	public void broadcast(String message){
//		System.out.println(level+"---------------"+channel);
		redisPub.pub(channel, message);
	}
	
//	public int getLevel(){
//		return this.level;
//	}
}
