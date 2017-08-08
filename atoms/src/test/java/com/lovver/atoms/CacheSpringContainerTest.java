package com.lovver.atoms;

import com.lovver.atoms.spring.cache.CacheChannel;
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
public class CacheSpringContainerTest {

    @Test
    public void run() throws InterruptedException {
        CacheChannel cc = CacheChannel.getInstance();

        cc.set("test", "dddd1", "jobell" );
        String val=(String)cc.get("test","dddd1");
        assert ("jobell".equals(val));
    }
}
