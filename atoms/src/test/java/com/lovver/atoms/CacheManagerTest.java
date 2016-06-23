package com.lovver.atoms;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.core.CacheManager;

public class CacheManagerTest {

	public static void main(String[] args) {
		Cache tCache=CacheManager.getCache(1, "hello", true);
		for(int i=0;i<10000;i++){
			tCache.put("test"+i, "wahahaha"+i); 
		}
		
		while(true){
			for(int i=0;i<10000;i++){
				System.out.println("==============="+tCache.get("test"+i));
			}
		}
	}

}
