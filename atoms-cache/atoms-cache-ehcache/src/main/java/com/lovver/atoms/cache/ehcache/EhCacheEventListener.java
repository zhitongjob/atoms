package com.lovver.atoms.cache.ehcache;


import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.BroadCast;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.annotation.SPI;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


@SPI("ehcache")
public class EhCacheEventListener implements CacheEventListener {
    private final static Logger log = LoggerFactory.getLogger(EhCacheEventListener.class);
    private Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();

    @Override
    public void notifyElementExpired(String region, Object key) {
        log.debug("notifyElementExpired["+region+"]["+key+"]");
        for(int i=2;i<=mCacheProvider.size();i++) {
            Cache cache = AtomsContext.getCache(region, i);
            cache.evict(key);
        }
        if (null != broadCast) {
            Command cmd = new Command(Command.OPT_DELETE_KEY, region, key);
            broadCast.broadcast(cmd.toBuffers());
        }
    }

    @Override
    public void notifyElementRemoved(String region, Object key)
            throws CacheException {
        log.debug("notifyElementRemoved["+region+"]["+key+"]");
        if (null != broadCast) {
            Command cmd = new Command(Command.OPT_DELETE_KEY, region, key);
            broadCast.broadcast(cmd.toBuffers());
        }
    }

    @Override
    public void notifyElementEvicted(String region, Object key) {
        log.debug("notifyElementEvicted["+region+"]["+key+"]");
        if (null != broadCast) {
            Command cmd = new Command(Command.OPT_DELETE_KEY, region, key);
            broadCast.broadcast(cmd.toBuffers());
        }
    }

    @Override
    public void notifyRemoveAll(String region) {
        log.debug("notifyRemoveAll["+region+"]");
        if (null != broadCast) {
            Command cmd = new Command(Command.OPT_DELETE_KEY, region, "");
            broadCast.broadcast(cmd.toBuffers());
        }
    }

    @Override
    public void notifyElementPut(String region, Object key, Object value)
            throws CacheException {
        log.debug("notifyElementPut["+region+"]["+key+"]");
        if (null != broadCast) {
            Command cmd = new Command(Command.OPT_PUT_KEY, region, key,value);
            broadCast.broadcast(cmd.toBuffers());
        }
    }

    @Override
    public void notifyElementPut(String region, Object key, Object value, int expiretime) throws CacheException {
        log.debug("notifyElementPut["+region+"]["+key+"]");
        if (null != broadCast) {
            Command cmd = new Command(Command.OPT_PUT_KEY, region, key,value,expiretime);
            broadCast.broadcast(cmd.toBuffers());
        }
    }

    @Override
    public void notifyElementUpdated(String region, Object key, Object value)
            throws CacheException {
        log.debug("notifyElementUpdated["+region+"]");
        if (null != broadCast) {
            Command cmd = new Command(Command.OPT_PUT_KEY, region, key,value);
            broadCast.broadcast(cmd.toBuffers());
        }
    }

    private int level;

    @Override
    public void init(int level) {
        this.level = level;
        broadCast = AtomsContext.getBroadCast();
    }

    private static BroadCast broadCast;

}
