package com.lovver.atoms;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.core.CacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:applicationContext.xml"})
@TransactionConfiguration(defaultRollback = false)
public class CacheManagerTest {

	@Test
	public void main() {
		Cache tCache=CacheManager.getCache(1, "hello", true);
		for(int i=0;i<10000;i++){
			tCache.put("test"+i, "wahahaha"+i); 
		}
		

		for(int i=0;i<10000;i++){
			System.out.println("==============="+tCache.get("test"+i));
		}

	}

}
