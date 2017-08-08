package com.lovver.atoms;

import com.lovver.atoms.spring.cache.CacheChannel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Administrator on 2017/3/20.
 */
public class CacheSpringTest {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ac=new ClassPathXmlApplicationContext(new String[]{"classpath:applicationContext.xml"});
        //AtomsBean config= AtomsConfig.getAtomsConfig();
        //System.out.println(config.getSerializer());

        CacheChannel cc=CacheChannel.getInstance();

        while(true){
            //cc.set("test", "dddd1", "jobell_"+i);
            System.out.println("==============="+cc.get("test", "dddd1"));
            Thread.sleep(1000);
        }

//        System.exit(0);
//        cc.evict("hello","dddd");
    }

}
