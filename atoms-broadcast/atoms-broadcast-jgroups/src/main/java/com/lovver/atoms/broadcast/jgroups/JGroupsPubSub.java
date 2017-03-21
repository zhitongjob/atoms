package com.lovver.atoms.broadcast.jgroups;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import com.lovver.atoms.config.AtomsConfig;
import org.jgroups.*;

import com.alibaba.fastjson.JSON;
import com.lovver.atoms.broadcast.Command;
import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.config.AtomsBroadCastBean;
import com.lovver.atoms.config.AtomsBroadCastConfigBean;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JGroupsPubSub extends ReceiverAdapter {
    private final static Logger log = LoggerFactory.getLogger(JGroupsPubSub.class);
    private static Serializer serializer = AtomsContext.getSerializer();
    protected JChannel channel;

    private AtomsBroadCastConfigBean broadcastConfig;
    private AtomsBroadCastBean broadcastBean;
    private String xmlFile;

    public JGroupsPubSub(final AtomsBroadCastBean broadcastBean) {

        log.debug("<JGroupsPubSub>");
        this.broadcastBean = broadcastBean;
        this.broadcastConfig = broadcastBean.getBroadcastConfig();
        this.xmlFile = broadcastConfig.getConfigFile();
        try {
            InputStream is = JGroupsPubSub.class.getClassLoader()
                    .getResourceAsStream(this.xmlFile);

            channel = new JChannel(is);
            channel.setReceiver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("</JGroupsPubSub>");

    }

    public void pub(String channel, byte[] message) {
        Message msg = new Message(null, null, message);
        try {
            this.channel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        channel.close();
    }

    /**
     * 组中成员变化时
     *
     * @param view group view
     */
    public void viewAccepted(View view) {
        StringBuffer sb = new StringBuffer("Group Members Changed, LIST: ");
        List<Address> addrs = view.getMembers();
        for (int i = 0; i < addrs.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(addrs.get(i).toString());
        }
    }

    @SuppressWarnings("rawtypes")
    public void receive(Message msg) {
        //无效消息
        byte[] buffers = msg.getBuffer();
        if (buffers.length < 1) {
            System.out.println("Message is empty.");
            return;
        }
        //不处理发送给自己的消息
        if (msg.getSrc().equals(channel.getAddress()))
            return;

        try {
            Command cmd = Command.parse(buffers);
            if (cmd == null)
                return;

            Cache cache = AtomsContext.getCache(cmd.getRegion(), 1);
            switch (cmd.getOperator()) {
                case Command.OPT_DELETE_KEY:
                    Object key = cmd.getKey();
                    if (key instanceof List) {
                        cache.evict((List) key);
                    } else {
                        cache.evict(cmd.getKey());
                    }
                    break;
                case Command.OPT_CLEAR_KEY:
                    cache.clear();
                    break;
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
