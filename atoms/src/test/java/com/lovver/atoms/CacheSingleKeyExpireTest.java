package com.lovver.atoms;

import com.lovver.atoms.core.CacheChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * Created by Administrator on 2017/3/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:applicationContext.xml"})
@TransactionConfiguration(defaultRollback = false)
public class CacheSingleKeyExpireTest {

    @Test
    public void run() throws InterruptedException {
        CacheChannel cc = CacheChannel.getInstance();
//        cc.set("test_region", "dddd", "jobell_");"dddd"));
//        System.out.println("===============" + cc.get("test_region", "dddd1"));
        cc.set("test", "dddd1", "jobell_", 10);

//        System.out.println("===============" + cc.get("test_region",
//        Thread.sleep(10000);
//        cc.evict("test","dddd1");
//        System.out.println("===============" + cc.get("test_region", "dddd"));
        while (true) {
            Thread.sleep(1000);
            Object val=cc.get("test", "dddd1");
            if(val==null){
                break;
            }
//            System.out.println("===============" + val);
        }
    }
}
