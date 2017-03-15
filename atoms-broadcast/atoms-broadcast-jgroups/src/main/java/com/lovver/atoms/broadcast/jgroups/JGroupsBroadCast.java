package com.lovver.atoms.broadcast.jgroups;

import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.config.AtomsBroadCastBean;

@SPI("jgroups")
public class JGroupsBroadCast  implements BroadCast {
	
	private static String channel_prefix="atoms_channel";
	private String channel = null;

	private JGroupsPubSub jGroupsPubSub;

	public void init(AtomsBroadCastBean broadcastBean){
		jGroupsPubSub =new JGroupsPubSub(broadcastBean);
		channel =  channel_prefix;
		try {
			jGroupsPubSub.channel.connect(this.channel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcast(String message){
		jGroupsPubSub.pub(channel, message);
	}

}
