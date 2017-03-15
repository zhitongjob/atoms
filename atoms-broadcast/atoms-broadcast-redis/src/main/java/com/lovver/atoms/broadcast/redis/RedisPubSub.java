package com.lovver.atoms.broadcast.redis;

import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheProvider;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsBroadCastBean;
import com.lovver.atoms.config.AtomsBroadCastConfigBean;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;
import java.util.concurrent.Executors;

public class RedisPubSub extends JedisPubSub {
    ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    private static Serializer serializer = AtomsContext.getSerializer();
    private Jedis jedis;//
    private AtomsBroadCastConfigBean broadcastConfig;
    private AtomsBroadCastBean broadcastBean;

    public RedisPubSub(AtomsBroadCastBean broadcastBean) {

        System.out.println("<RedisPubSub>");
        this.broadcastBean = broadcastBean;
        jedis = getJedis(broadcastBean);
        System.out.println("</RedisPubSub>");
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!jedis.ping().equals("PONG")) {
                        jedis.connect();
                    }
                }catch(Exception e){
                    jedis.connect();
                }

            }
        },0,1, TimeUnit.SECONDS);
    }



    private Jedis getJedis(AtomsBroadCastBean broadcastBean) {
        System.out.println("RedisPubSub--------getJedis");
        if (this.broadcastBean == null) {
            this.broadcastBean = AtomsContext.getAtomsBroadCastBean();
        }
        if (this.broadcastConfig == null) {
            this.broadcastConfig = this.broadcastBean.getBroadcastConfig();
        }
        if (jedis != null) {
            jedis.connect();
            System.out.println(jedis.asking());
        }

        if (this.jedis != null && jedis.isConnected()) {
            return jedis;
        } else {
            try {
                if (jedis != null) {
                    jedis.close();
                }
            } catch (Exception e) {
            }
        }
        broadcastConfig = broadcastBean.getBroadcastConfig();
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
        this.jedis = jedis;
        return jedis;
    }

    public void pub(String channel, String message) {
        System.out.println("<RedisPubSub--pub>|channel=" + channel);
        try {
            getJedis(broadcastBean).publish(channel, message);
        } catch (Exception ex ) {
            this.jedis.connect();
        }
        System.out.println("RedisPubSub.jedis==" + this.jedis);
        System.out.println("</RedisPubSub--pub>|channel=" + channel);
    }


    public void sub(JedisPubSub listener, String channel) {
        System.out.println("<RedisPubSub--sub>|channel=" + channel);
        try {
            getJedis(broadcastBean).subscribe(listener, channel);
        } catch (Exception ex) {
            this.jedis.connect();
        }
        System.out.println("</RedisPubSub--sub>|channel=" + channel);
    }

    public void close(String channel) {
        jedis.close();
    }

    @SuppressWarnings("rawtypes")
    public void onMessage(String channel, String message) {
        if (message != null && message.length() <= 0) {
            System.out.println("Message is empty.");
            return;
        }

        try {
            Command cmd = JSON.parseObject(message, Command.class);
            if (cmd == null)
                return;
            String client_id = cmd.getClient_id();

//			Map<String,CacheProvider> mCacheProvider=AtomsContext.getCacheProvider();
            switch (cmd.getOperator()) {
                case Command.OPT_DELETE_KEY:
//				for(int i=1;i<=mCacheProvider.size();i++){
                    if (AtomsContext.isMe(client_id)) {
                        return;
                    } else {
                        Cache cache = AtomsContext.getCache(cmd.getRegion(), 1, client_id);
                        Object key = cmd.getKey();
                        if (key instanceof List) {
                            cache.evict((List) key);
                        } else {
                            cache.evict(cmd.getKey());
                        }
                    }
//				}
                    break;
                case Command.OPT_CLEAR_KEY:
//				for(int i=1;i<=mCacheProvider.size();i++){
                    if (AtomsContext.isMe(client_id)) {
                        return;
                    } else {
                        Cache cache = AtomsContext.getCache(cmd.getRegion(), 1, client_id);
                        cache.clear();
                    }
//				}
                    break;
                case Command.OPT_PUT_KEY:
//				for(int i=1;i<=mCacheProvider.size();i++){
                    if (AtomsContext.isMe(client_id)) {
                        return;
                    } else {
                        Cache cache = AtomsContext.getCache(cmd.getRegion(), 1, client_id);
                        cache.put(cmd.getKey(), serializer.deserialize(cmd.getValue()));
                    }
//				}
                    break;
                default:
//				log.warn("Unknown message type = " + cmd.getOperator());
            }
        } catch (Exception e) {
            e.printStackTrace();
//			log.error("Unable to handle received msg", e);
        }

    }
}
