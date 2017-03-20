package com.lovver.atoms;

import com.lovver.atoms.core.CacheChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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

        for (int i = 0; i < 10; i++) {
            cc.set("hello", "dddd", "jobell_" + i, 1);
            System.out.println("===============" + cc.get("hello", "dddd"));
            Thread.sleep(2000);
        }
    }
}
