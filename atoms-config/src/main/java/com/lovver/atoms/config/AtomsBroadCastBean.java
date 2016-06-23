package com.lovver.atoms.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("broadcast")
public class AtomsBroadCastBean {
	
	@XStreamAsAttribute
	private String type;
	
	private AtomsBroadCastConfigBean broadcastConfig;
//	
//	@XStreamAsAttribute
//	private String host;
//	
//	@XStreamAsAttribute
//	private String port;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AtomsBroadCastConfigBean getBroadcastConfig() {
		return broadcastConfig;
	}

	public void setBroadcastConfig(AtomsBroadCastConfigBean broadcastConfig) {
		this.broadcastConfig = broadcastConfig;
	}
	
	

//	public String getHost() {
//		return host;
//	}
//
//	public void setHost(String host) {
//		this.host = host;
//	}
//
//	public String getPort() {
//		return port;
//	}
//
//	public void setPort(String port) {
//		this.port = port;
//	}

}
