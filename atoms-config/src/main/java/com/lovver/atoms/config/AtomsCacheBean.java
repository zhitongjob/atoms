package com.lovver.atoms.config;

import com.lovver.atoms.common.utils.StringUtils;
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
	@XStreamAsAttribute
	private String delete_atom="true";
	
	private AtomsCacheConfigBean cacheConfig;
	
	private AtomsCacheTTLBean cacheTTL;

	public AtomsCacheTTLBean getCacheTTL() {
		return cacheTTL;
	}

	public void setCacheTTL(AtomsCacheTTLBean cacheTTL) {
		this.cacheTTL = cacheTTL;
	}

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

	public String getDelete_atom() {
		if(StringUtils.isEmpty(delete_atom)){
			delete_atom="true";
		}
		return delete_atom;
	}

	public void setDelete_atom(String delete_atom) {
		this.delete_atom = delete_atom;
	}
	
}
