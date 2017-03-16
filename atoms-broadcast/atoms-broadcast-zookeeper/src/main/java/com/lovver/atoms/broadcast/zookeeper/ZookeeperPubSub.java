package com.lovver.atoms.broadcast.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.config.AtomsBroadCastBean;
import com.lovver.atoms.config.AtomsBroadCastConfigBean;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;
import java.util.concurrent.Executors;

public class ZookeeperPubSub implements Watcher {
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    private static Serializer serializer = AtomsContext.getSerializer();
    protected ZooKeeper zooKeeper;
    private AtomsBroadCastConfigBean broadcastConfig;
    private AtomsBroadCastBean broadcastBean;
    protected CountDownLatch countDownLatch=new CountDownLatch(1);
    private static final int SESSION_TIME   = 2000;
    public static final String authScheme = "digest";

    public ZookeeperPubSub(final AtomsBroadCastBean broadcastBean) {

        System.out.println("<RedisPubSub>");
        this.broadcastBean = broadcastBean;
        this.broadcastConfig = broadcastBean.getBroadcastConfig();
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("</RedisPubSub>");
    }

    private void connect() throws IOException, InterruptedException{
        String host = null2default(broadcastConfig.getHost(),"127.0.0.1");
        String password = broadcastConfig.getPassword();

        String sPort=null2default(broadcastConfig.getPort(),"6379");
        int port = Integer.parseInt(sPort);

        String sTimeout=null2default(broadcastConfig.getTimeout(),SESSION_TIME+"");
        int timeout = Integer.parseInt(sTimeout);//getProperty(props, "timeout", 2000);

        String connectString=host+":"+port;
        zooKeeper = new ZooKeeper(connectString,timeout,this);
        if(!org.apache.commons.lang.StringUtils.isEmpty(password)){
            zooKeeper.addAuthInfo(authScheme, password.getBytes());
        }
        countDownLatch.await();
    }

    private void create(String path,byte[] data)throws KeeperException, InterruptedException{
        /**
         * 此处采用的是CreateMode是PERSISTENT
         *  表示The znode will not be automatically deleted upon client's disconnect.
         * EPHEMERAL
         * 表示The znode will be deleted upon the client's disconnect.
         */
        this.zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    private byte[] getData(String path) throws KeeperException, InterruptedException {
        return  this.zooKeeper.getData(path, false,null);
    }

    private void setData(String path,byte[] data) throws KeeperException, InterruptedException {
        Stat st= this.zooKeeper.setData(path, data,-1);
    }


    public void pub(String channel, String message) {
        System.out.println("<RedisPubSub--pub>|channel=" + channel);

        System.out.println("</RedisPubSub--pub>|channel=" + channel);
    }


//    public void sub(JedisPubSub listener, String channel) {
//        System.out.println("<RedisPubSub--sub>|channel=" + channel);
//        try {
//            jedis.connect();
//            getJedis().subscribe(listener, channel);
//        } catch (Exception ex) {
//            if(jedis!=null){
//                jedis.close();
//            }
//            jedis = getJedis();
//        }
//        System.out.println("</RedisPubSub--sub>|channel=" + channel);
//    }

    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

            switch (cmd.getOperator()) {
                case Command.OPT_DELETE_KEY:
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
                    break;
                case Command.OPT_CLEAR_KEY:
                    if (AtomsContext.isMe(client_id)) {
                        return;
                    } else {
                        Cache cache = AtomsContext.getCache(cmd.getRegion(), 1, client_id);
                        cache.clear();
                    }
                    break;
                case Command.OPT_PUT_KEY:
                    if (AtomsContext.isMe(client_id)) {
                        return;
                    } else {
                        Cache cache = AtomsContext.getCache(cmd.getRegion(), 1, client_id);
                        cache.put(cmd.getKey(), serializer.deserialize(cmd.getValue()));
                    }
                    break;
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String null2default(String value,String defalutValue){
        if(org.apache.commons.lang.StringUtils.isEmpty(value)){
            return defalutValue;
        }else{
            return value;
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if(event.getState()== Event.KeeperState.SyncConnected){
            countDownLatch.countDown();
        }
    }
}
