package com.lovver.atoms.broadcast.zookeeper;

import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.config.AtomsBroadCastBean;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;

@SPI("zookeeper")
public class ZookeeperBroadCast implements BroadCast{
	
	private static String channel_prefix="atoms_channel";
	private String channel = null;
	
	private ZookeeperPubSub zkPubSub;

	public void init(AtomsBroadCastBean broadcastBean){
		if(StringUtils.isNotEmpty(broadcastBean.getChannel())) {
			channel = channel_prefix+"_"+broadcastBean.getChannel();
		}else{
			channel = channel_prefix;
		}
		zkPubSub=new ZookeeperPubSub(broadcastBean,this.channel);
	}

	public void broadcast(byte[] message){
		zkPubSub.pub(message);
	}
}
