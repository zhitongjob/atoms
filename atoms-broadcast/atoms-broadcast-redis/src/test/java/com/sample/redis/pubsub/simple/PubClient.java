package com.sample.redis.pubsub.simple;

import redis.clients.jedis.Jedis;

public class PubClient {

	private Jedis jedis;//
	public PubClient(String host,int port){
		jedis = new Jedis(host,port);
	}
	
	public void pub(String channel,String message){
		jedis.publish(channel, message);
	}
	
	public void close(String channel){
		jedis.publish(channel, "quit");
		jedis.del(channel);//
	}
	
	public void test(){
		jedis.set("pub-block", "15");
		String tmp = jedis.get("pub-block");
		System.out.println("TEST:" + tmp);
	}


}
