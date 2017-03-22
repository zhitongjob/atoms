package com.lovver.atoms;

import java.util.ArrayList;
import java.util.List;

import com.lovver.atoms.core.CacheChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:applicationContext.xml"})
@TransactionConfiguration(defaultRollback = false)
public class SetClient {

	@Test
	public  void main() throws InterruptedException {
		CacheChannel cc=CacheChannel.getInstance();
		for(int i=0;i<10;i++){
			cc.set("hello", "dddd", "jobell_"+i,1);
			System.out.println("==============="+cc.get("hello", "dddd"));
			Thread.sleep(2000);
		}
	}

}
