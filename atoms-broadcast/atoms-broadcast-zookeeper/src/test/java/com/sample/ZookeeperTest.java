package com.sample;

import org.apache.zookeeper.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/3/17.
 */
public class ZookeeperTest {
    // 根节点
    public static final String ROOT = "/root-ktv";

    public static void main(String[] args) throws Exception {
        // 创建一个与服务器的连接
        ZooKeeper zk = new ZooKeeper("192.168.1.87:2181", 30000, new Watcher() {
            // 监控所有被触发的事件
            public void process(WatchedEvent event) {
                System.out.println("状态:" + event.getState()+":"+event.getType()+":"+event.getWrapper()+":"+event.getPath());
            }
        });
        // 创建一个总的目录ktv，并不控制权限，这里需要用持久化节点，不然下面的节点创建容易出错
        zk.create(ROOT, "root-ktv".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // 然后杭州开一个KTV ,       PERSISTENT_SEQUENTIAL 类型会自动加上 0000000000 自增的后缀
        zk.create(ROOT+"/杭州KTV", "杭州KTV".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

        // 也可以在北京开一个,       EPHEMERAL session 过期了就会自动删除
        zk.create(ROOT+"/北京KTV", "北京KTV".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        // 同理，我可以在北京开多个，EPHEMERAL_SEQUENTIAL  session 过期自动删除，也会加数字的后缀
        zk.create(ROOT+"/北京KTV-分店", "北京KTV-分店".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        // 我们也可以 来看看 一共监视了多少家的ktv
        List<String> ktvs = zk.getChildren(ROOT, true);
        System.out.println(Arrays.toString(ktvs.toArray()));
        for(String node : ktvs){
            // 删除节点
            zk.delete(ROOT+"/"+node,-1);
        }
        // 根目录得最后删除的
        zk.delete(ROOT, -1);
        zk.close();
    }
}
