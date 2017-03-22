package com.lovver.atoms.broadcast.redis;

import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.config.AtomsBroadCastBean;
import org.apache.commons.lang.StringUtils;

@SPI("redis")
public class RedisBroadCast implements BroadCast{
	
	private static String channel_prefix="atoms_channel";
	private String channel = null;
	
//	private RedisPubSub redisSub;
	private RedisPubSub redisPubSub;
	
	private Thread thread_subscribe;

	public void init(AtomsBroadCastBean broadcastBean){
		redisPubSub=new RedisPubSub(broadcastBean);
//		redisPub=new RedisPubSub(broadcastBean);
		if(StringUtils.isNotEmpty(broadcastBean.getChannel())) {
			channel = channel_prefix+"_"+broadcastBean.getChannel();
		}else{
			channel = channel_prefix;
		}
		thread_subscribe = new Thread(new Runnable() {
			@Override
			public void run() {
				redisPubSub.sub(redisPubSub, channel);
			}
		});
		thread_subscribe.start();
	}

	public void broadcast(byte[] message){
		redisPubSub.pub(channel, message);
	}
}
