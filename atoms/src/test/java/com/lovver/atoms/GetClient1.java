package com.lovver.atoms;

import com.lovver.atoms.core.CacheChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:applicationContext.xml"})
@TransactionConfiguration(defaultRollback = false)
public class GetClient1 {

	@Test
	public  void main() throws InterruptedException {
		CacheChannel cc=CacheChannel.getInstance();
		
		while(true){
			Object dd=cc.get("hello", "dddd");
			System.out.println("=============== CacheChannelTest2"+dd);
			Thread.sleep(1000);
		}
		
	}

}
