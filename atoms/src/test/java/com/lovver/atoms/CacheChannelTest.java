package com.lovver.atoms;

import com.lovver.atoms.core.CacheChannel;

public class CacheChannelTest {

	public static void main(String[] args) {
		CacheChannel cc=CacheChannel.getInstance();
		cc.set("jiangbiao", "hello", "nihaoya");
//		cc.set("jiangbiao", "hello1", "nihaoya1");
//		List keys=new ArrayList<String>();
//		keys.add("hello");
//		keys.add("hello1");
//		cc.batchEvict("jiangbiao", keys);
		cc.evict("jiangbiao", "hello");
		while(true){
			Object value=cc.get("jiangbiao", "hello");
			if(value==null){
				System.out.println("==============="+value);
			}else{
				System.out.println("==============="+value);
			}
		}
//		for(int i=0;i<100000;i++){
//			System.out.println("==============="+cc.get("jiangbiao", "hello"));
//		}
		
//		cc.set("jiangbiao", "hello", "ddddddd");
//		for(int i=0;i<100000;i++){
//			System.out.println("==============="+cc.get("jiangbiao", "hello"));
//		}
	}

}
