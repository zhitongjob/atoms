package com.lovver.atoms.broadcast.redis;

import java.util.List;
import java.util.concurrent.*;

import com.lovver.atoms.broadcast.redis.support.SafeEncoder;
import com.lovver.atoms.common.exception.CacheException;
import redis.clients.jedis.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsBroadCastBean;
import com.lovver.atoms.config.AtomsBroadCastConfigBean;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;
import sun.rmi.runtime.Log;

import java.util.concurrent.Executors;

public class RedisPubSub extends BinaryJedisPubSub {
    private final static Logger log = LoggerFactory.getLogger(RedisPubSub.class);

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private CopyOnWriteArraySet<String> broadsetConfig=AtomsContext.getBroadsetConfig();
    private static JedisPool pool;
    private AtomsBroadCastConfigBean broadcastConfig;
    private AtomsBroadCastBean broadcastBean;
    private boolean isUsePool = false;

    public RedisPubSub(final AtomsBroadCastBean broadcastBean) {

        this.broadcastBean = broadcastBean;
        this.broadcastConfig = broadcastBean.getBroadcastConfig();
        this.isUsePool = Boolean.valueOf(null2default(broadcastConfig.getUsePool(), "true"));
        if (isUsePool) {
            if (this.pool == null) {
                this.pool = buildPool(broadcastConfig);
            }
        }
//        jedis = getJedis();
    }


    private Jedis getJedisWithoutPool() {
        String host = this.broadcastConfig.getHost();
        String port = broadcastConfig.getPort();
        Jedis jedis;//

        if (StringUtils.isEmpty(port)) {
            port = "6379";
        }
        int iPort = Integer.parseInt(port);
        String timeout = this.broadcastConfig.getTimeout();
        if (StringUtils.isEmpty(timeout)) {
            jedis = new Jedis(host, iPort);
        } else {
            int iTimeout = Integer.parseInt(timeout);
            jedis = new Jedis(host, iPort, iTimeout);
        }
        String password = broadcastConfig.getPassword();
        if (!StringUtils.isEmpty(password)) {
            jedis.auth(password);
        }
        return jedis;
    }

    private Jedis getJedis() {
        if (this.broadcastBean == null) {
            this.broadcastBean = AtomsContext.getAtomsBroadCastBean();
        }
        if (this.broadcastConfig == null) {
            this.broadcastConfig = this.broadcastBean.getBroadcastConfig();
        }

        if (this.isUsePool) {
            return this.pool.getResource();
        } else {
            return this.getJedisWithoutPool();
        }
    }

    public void pub(String channel, byte[] message) {
        Jedis jedisPub = null;
        try {
            jedisPub = getJedis();
            log.debug("pub message" + message.toString());
            jedisPub.publish(SafeEncoder.encode(channel), message);
        } catch (Exception ex) {
            log.error("pub message error.", ex);
        } finally {
            jedisPub.close();
        }
    }


    public void sub(BinaryJedisPubSub listener, String channel) {
        log.debug("subscribe channel["+channel+"]");
        Jedis jedisPub = null;
        try {
            jedisPub = getJedis();
            jedisPub.subscribe(listener, SafeEncoder.encode(channel));
        } catch (Exception ex) {
            log.error("subscribe has error.", ex);
        } finally {
            jedisPub.close();
        }
    }

    public void close() {
        if (this.isUsePool) {
            this.pool.destroy();
        }
    }

    @SuppressWarnings("rawtypes")
    public void onMessage(byte[] channel, byte[] message) {
        if (message != null && message.length <= 0) {
            log.warn("Message is empty.");
            return;
        }

        try {
            Command cmd = Command.parse(message);

            if (cmd == null || cmd.isLocalCommand())
                return;

            Cache cache = AtomsContext.getCache(cmd.getRegion(), 1);
            switch (cmd.getOperator()) {
                case Command.OPT_DELETE_KEY:
                    log.debug("on Message[delete]["+cmd.getRegion()+"]["+cmd.getKey()+"]");
                    Object key = cmd.getKey();
                    if (key instanceof List) {
                        cache.evict((List) key,false);
                    } else {
                        cache.evict(cmd.getKey(),false);
                    }
                    break;
                case Command.OPT_CLEAR_KEY:
                    log.debug("on Message[clear]["+cmd.getRegion()+"]["+cmd.getKey()+"]");
                    cache.clear(false);
                    break;
                case Command.OPT_PUT_KEY:
                    log.debug("on Message[put]["+cmd.getRegion()+"]["+cmd.getKey()+"]");
                    if(broadsetConfig==null||(broadsetConfig!=null&&broadsetConfig.contains(cmd.getBroadsetKey()))){
                        if(cmd.getExpiretime()>0){
                            cache.put(cmd.getKey(), cmd.getVal(),cmd.getExpiretime(), false);
                        }else {
                            cache.put(cmd.getKey(), cmd.getVal(), false);
                        }
                    }
                    break;
                default:
                    log.warn("Unknown message type = " + cmd.getOperator());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unable to handle received msg", e);
        }
    }

    public JedisPool buildPool(AtomsBroadCastConfigBean broadcastConfig) throws CacheException {
        JedisPoolConfig config = new JedisPoolConfig();

        String host = null2default(broadcastConfig.getHost(), "127.0.0.1");
        String password = broadcastConfig.getPassword();
        if (org.apache.commons.lang.StringUtils.isEmpty(password)) {
            password = null;
        }
        String sPort = null2default(broadcastConfig.getPort(), "6379");
        int port = Integer.parseInt(sPort);

        String sTimeout = null2default(broadcastConfig.getTimeout(), "2000");
        int timeout = Integer.parseInt(sTimeout);//getProperty(props, "timeout", 2000);

        String sDatabase = null2default(broadcastConfig.getDatabase(), "0");
        int database = Integer.parseInt(sDatabase);

        String sBlockWhenExhausted = null2default(broadcastConfig.getBlockWhenExhausted(), "true");
        config.setBlockWhenExhausted(Boolean.parseBoolean(sBlockWhenExhausted));

        String sMaxIdle = null2default(broadcastConfig.getMaxIdle(), "10");
        config.setMaxIdle(Integer.parseInt(sMaxIdle));

        String sMinIdle = null2default(broadcastConfig.getMinIdle(), "5");
        config.setMinIdle(Integer.parseInt(sMinIdle));

        String sMaxTotal = null2default(broadcastConfig.getMaxTotal(), "10000");
        config.setMaxTotal(Integer.parseInt(sMaxTotal));

        String sMaxWait = null2default(broadcastConfig.getMaxWaitMillis(), "100");
        config.setMaxWaitMillis(Integer.parseInt(sMaxWait));

        String sTestWhileIdle = null2default(broadcastConfig.getTestWhileIdle(), "false");
        config.setTestWhileIdle(Boolean.parseBoolean(sTestWhileIdle));

        String sTestOnBorrow = null2default(broadcastConfig.getTestOnBorrow(), "true");
        config.setTestOnBorrow(Boolean.parseBoolean(sTestOnBorrow));

        String sTestOnReturn = null2default(broadcastConfig.getTestOnReturn(), "false");
        config.setTestOnReturn(Boolean.parseBoolean(sTestOnReturn));

        String sNumTestsPerEvictionRun = null2default(broadcastConfig.getNumTestsPerEvictionRun(), "10");
        config.setNumTestsPerEvictionRun(Integer.parseInt(sNumTestsPerEvictionRun));

        String sMinEvictableIdelTimeMillis = null2default(broadcastConfig.getMinEvictableIdleTimeMillis(), "1000");
        config.setMinEvictableIdleTimeMillis(Integer.parseInt(sMinEvictableIdelTimeMillis));

        String sSoftMinEvictableIdleTimeMillis = null2default(broadcastConfig.getSoftMinEvictableIdleTimeMillis(), "10");
        config.setSoftMinEvictableIdleTimeMillis(Integer.parseInt(sSoftMinEvictableIdleTimeMillis));

        String timeBetweenEvictionRunsMillis = null2default(broadcastConfig.getTimeBetweenEvictionRunsMillis(), "10");
        config.setTimeBetweenEvictionRunsMillis(Integer.parseInt(timeBetweenEvictionRunsMillis));

        String lifo = null2default(broadcastConfig.getLifo(), "false");
        config.setLifo(Boolean.parseBoolean(lifo));
        pool = new JedisPool(config, host, port, timeout, password, database);
        return pool;
    }

    private String null2default(String value, String defalutValue) {
        if (org.apache.commons.lang.StringUtils.isEmpty(value)) {
            return defalutValue;
        } else {
            return value;
        }
    }
}
