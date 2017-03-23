package com.lovver.atoms.config;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class AtomsCacheTTLConfigBean {
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String value;
	@XStreamAsAttribute
	private String broadset;//set时是否广播

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getBroadset() {
		return broadset;
	}

	public void setBroadset(String broadset) {
		this.broadset = broadset;
	}
}
