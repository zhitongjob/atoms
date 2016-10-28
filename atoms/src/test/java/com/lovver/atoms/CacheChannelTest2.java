package com.lovver.atoms;

import com.lovver.atoms.core.CacheChannel;

public class CacheChannelTest2 {

	public static void main(String[] args) throws InterruptedException {
		CacheChannel cc=CacheChannel.getInstance();
		cc.set("hello", "dddd", "nihaoya");
		cc.set("hello", "dddd2", "nihaoya");
//		while(true){
//			Object value=cc.get("hello", "dddd");
//			if(value==null){
//				System.out.println("==============="+value);
//			}else{
//				System.out.println("==============="+value);
//			}
//		}
//		cc.get("hello", "dddd");
		while(true){
			Object dd=cc.get("hello", "dddd");
			if(dd==null){
				System.out.println("=============== CacheChannelTest2");
			}
			Thread.sleep(3000);
		}
		
//		cc.set("jiangbiao", "hello", "ddddddd");
//		for(int i=0;i<100000;i++){
//			System.out.println("==============="+cc.get("jiangbiao", "hello"));
//		}
	}

}
