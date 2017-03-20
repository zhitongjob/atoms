package com.lovver.atoms;

import com.lovver.atoms.config.AtomsBean;
import com.lovver.atoms.config.AtomsConfig;
import com.lovver.atoms.core.CacheChannel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Administrator on 2017/3/20.
 */
public class CacheSpringTest {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ac=new ClassPathXmlApplicationContext(new String[]{"classpath:applicationContext.xml"});
        AtomsBean config= AtomsConfig.getAtomsConfig();
        System.out.println(config.getSerializer());

        CacheChannel cc=CacheChannel.getInstance();

        for(int i=0;i<10;i++){
            cc.set("hello", "dddd", "jobell_"+i,1);
            System.out.println("==============="+cc.get("hello", "dddd"));
            Thread.sleep(2000);
        }
        cc.evict("hello","dddd");
    }

}
