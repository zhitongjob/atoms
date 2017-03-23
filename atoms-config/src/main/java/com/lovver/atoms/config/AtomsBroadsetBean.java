package com.lovver.atoms.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Created by Administrator on 2017/3/23.
 */
@XStreamAlias("broadset")
public class AtomsBroadsetBean {
    @XStreamAsAttribute
    private String region;
    @XStreamAsAttribute
    private String key;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
