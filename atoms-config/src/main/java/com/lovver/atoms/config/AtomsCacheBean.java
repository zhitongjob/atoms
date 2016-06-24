package com.lovver.atoms.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("cache")
public class AtomsCacheBean {
	@XStreamAsAttribute
	private String level;
	@XStreamAsAttribute
	private String type;
	@XStreamAsAttribute
	private String expiredOperator;
	@XStreamAsAttribute
	private String waitTime;
	
	private AtomsCacheConfigBean cacheConfig;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AtomsCacheConfigBean getCacheConfig() {
		return cacheConfig;
	}

	public void setCacheConfig(AtomsCacheConfigBean cacheConfig) {
		this.cacheConfig = cacheConfig;
	}

	public String getExpiredOperator() {
		return expiredOperator;
	}

	public void setExpiredOperator(String expiredOperator) {
		this.expiredOperator = expiredOperator;
	}

	public String getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(String waitTime) {
		this.waitTime = waitTime;
	}
}
