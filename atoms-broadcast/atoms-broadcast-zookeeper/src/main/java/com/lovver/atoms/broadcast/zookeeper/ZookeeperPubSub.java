package com.lovver.atoms.broadcast.zookeeper;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.api6.zkclient.ZKClient;
import com.api6.zkclient.ZKClientBuilder;
import com.api6.zkclient.listener.ZKChildDataListener;
import com.api6.zkclient.listener.ZKStateListener;
import com.api6.zkclient.serializer.SerializableSerializer;
import org.apache.commons.lang.StringUtils;
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

public class ZookeeperPubSub {

    private static Serializer serializer = AtomsContext.getSerializer();
    protected static ZKClient zkClient;
    private AtomsBroadCastConfigBean broadcastConfig;
    private AtomsBroadCastBean broadcastBean;
    private static final int SESSION_TIME = 2000;
    public static final String authScheme = "digest";
    protected final String root;

    public ZookeeperPubSub(final AtomsBroadCastBean broadcastBean,final String root) {

        System.out.println("<RedisPubSub>");
        this.broadcastBean = broadcastBean;
        this.broadcastConfig = broadcastBean.getBroadcastConfig();
        this.root="/"+root;
        try {
            connect();
            if(!zkClient.exists(this.root)) {
                zkClient.create(this.root, "", CreateMode.PERSISTENT);
            }
            sub(this.root);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("</RedisPubSub>");
    }

    private void connect() throws IOException, InterruptedException {
        String host = null2default(broadcastConfig.getHost(), "127.0.0.1");
        String password = broadcastConfig.getPassword();

        String sPort = null2default(broadcastConfig.getPort(), "6379");
        int port = Integer.parseInt(sPort);

        String sTimeout = null2default(broadcastConfig.getTimeout(), SESSION_TIME + "");
        int timeout = Integer.parseInt(sTimeout);//getProperty(props, "timeout", 2000);

        String connectString = host + ":" + port;
        zkClient = ZKClientBuilder.newZKClient(connectString)
//                .sessionTimeout(timeout)//可选
                .serializer(new SerializableSerializer())//可选
                .eventThreadPoolSize(1)//可选
                .retryTimeout(1000 * 60)//可选
                .connectionTimeout(Integer.MAX_VALUE)//可选
                .build();
        if(StringUtils.isNotEmpty(password)) {
            zkClient.addAuthInfo(authScheme, password.getBytes());
        }

    }


    public void pub(String message) {
        System.out.println("<RedisPubSub--pub>");
        Command cmd = JSON.parseObject(message, Command.class);
        byte op = cmd.getOperator();
        String region = cmd.getRegion();
        Object key=cmd.getKey();
        String regionPath = root + "/" + region;
        switch (op) {
            case Command.OPT_DELETE_KEY:
                zkClient.setData(regionPath, message,-1);
                break;
            case Command.OPT_CLEAR_KEY:
                zkClient.setData(regionPath, message,-1);
                break;
            case Command.OPT_PUT_KEY:
                if(!zkClient.exists(regionPath)){
                    zkClient.create(regionPath,"",CreateMode.PERSISTENT);
                }
                zkClient.setData(regionPath, message, -1);
//                if(key instanceof  String){
//                    String keyPath = regionPath + "/" + key;
//                    if (zkClient.exists(keyPath)) {
//                        zkClient.setData(keyPath, message, -1);
//                    } else {
//                        zkClient.createEphemerale(keyPath, message, false);
//                    }
//                }else{
//                    JSONArray keys= (JSONArray) cmd.getKey();
//                    Iterator iteKeys=keys.iterator();
//                    while(iteKeys.hasNext()){
//                        String keyPath = regionPath + "/" + key;
//                        if (zkClient.exists(keyPath)) {
//
//                        } else {
//                            zkClient.createEphemerale(keyPath, message, false);
//                        }
//                    }
//                }

                break;
        }

        sub(this.root);

        System.out.println("</RedisPubSub--pub>");
    }


    public void sub(String path) {

        zkClient.listenChildDataChanges(path, new ZKChildDataListener() {
            @Override
            public void handleSessionExpired(String path, Object data) throws Exception {//会话过期
                System.out.println("session expired");
                zkClient.reconnect();
            }

            @Override
            public void handleChildDataChanged(String path, Object data) throws Exception {//子节点数据发生改变
//                System.out.println("the child data is changed:[path:" + path + ",data:" + data + "]");
                onMessage((String)data);
            }

            @Override
            public void handleChildCountChanged(String path, List<String> children) throws Exception {//子节点数量发生改变
                for(String child:children) {
                    System.out.println("children change:" + path + "==========" + child);
                }
            }
        });
    }

    public void close() {
        zkClient.close();
    }

    public void onMessage(String message) {
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

    private String null2default(String value, String defalutValue) {
        if (org.apache.commons.lang.StringUtils.isEmpty(value)) {
            return defalutValue;
        } else {
            return value;
        }
    }

}
