package com.lovver.atoms.config;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("cacheTTL")
public class AtomsCacheTTLBean {
	
	@XStreamImplicit(itemFieldName = "ttl")  
	private List<AtomsCacheTTLConfigBean> lstTTL;

	public List<AtomsCacheTTLConfigBean> getLstTTL() {
		return lstTTL;
	}

	public void setLstTTL(List<AtomsCacheTTLConfigBean> lstTTL) {
		this.lstTTL = lstTTL;
	}
}
