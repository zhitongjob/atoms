package com.lovver.atoms.spring.cache;


import com.lovver.atoms.config.AtomsSpringConfig;
import com.lovver.atoms.core.CacheChannel;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

public class SpringAtomsCache implements Cache{

    private  String name;

    public SpringAtomsCache(){}

    public SpringAtomsCache(String name){
        this.name=name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return channel;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object val=channel.get(this.name,key);
        if(val==null){
            return null;
        }
        return new SimpleValueWrapper(val);
    }

    @Override
    public void put(Object key, Object value) {
        channel.set(this.name,key,value);
    }

    @Override
    public void evict(Object key) {
        channel.evict(this.name,key);
    }

    @Override
    public void clear() {
        channel.clear(this.name);
    }

    private AtomsSpringConfig atomsSpringConfig;
    private static CacheChannel channel=null;

    public void setAtomsSpringConfig(AtomsSpringConfig atomsSpringConfig) {
        this.atomsSpringConfig = atomsSpringConfig;
        channel=CacheChannel.getInstance();
    }

    public AtomsSpringConfig getAtomsSpringConfig() {
        return atomsSpringConfig;
    }
}
