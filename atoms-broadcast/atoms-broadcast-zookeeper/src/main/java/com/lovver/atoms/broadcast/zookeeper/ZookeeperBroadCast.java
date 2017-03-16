package com.lovver.atoms.broadcast.zookeeper;

import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.config.AtomsBroadCastBean;

@SPI("zookeeper")
public class ZookeeperBroadCast implements BroadCast{
	
	private static String channel_prefix="atoms_channel";
	private String channel = null;
	
	private ZookeeperPubSub zkSub;
	private ZookeeperPubSub zkPub;
	
	private Thread thread_subscribe;
	
//	private int level;
	
	public void init(AtomsBroadCastBean broadcastBean){
		zkSub=new ZookeeperPubSub(broadcastBean);
		zkPub=new ZookeeperPubSub(broadcastBean);
		channel =  channel_prefix;
	}

	public void broadcast(String message){
//		System.out.println(level+"---------------"+channel);
//		redisPub.pub(channel, message);
	}
	
//	public int getLevel(){
//		return this.level;
//	}
}
