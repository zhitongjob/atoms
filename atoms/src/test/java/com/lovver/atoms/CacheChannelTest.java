package com.lovver.atoms;

import java.util.ArrayList;
import java.util.List;

import com.lovver.atoms.core.CacheChannel;

public class CacheChannelTest {

	public static void main(String[] args) throws InterruptedException {
		CacheChannel cc=CacheChannel.getInstance();
//		cc.set("hello", "dddd", "nihaoya");
//		cc.set("hello", "dddd2", "nihaoya");
//		while(true){
//			Object value=cc.get("hello", "dddd");
//			if(value==null){
//				System.out.println("==============="+value);
//			}else{
//				System.out.println("==============="+value);
//			}
//		}
//		Thread.sleep(3000);
		
		cc.set("hello", "dddd", "jobell");
//		System.out.println("removed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		while(true){
			System.out.println("==============="+cc.get("hello", "dddd"));
			Thread.sleep(5000);
		}
//		
//		cc.set("jiangbiao", "hello", "ddddddd");
//		for(int i=0;i<100000;i++){
//			System.out.println("==============="+cc.get("jiangbiao", "hello"));
//		}
	}

}
