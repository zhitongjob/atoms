package com.sample.redis;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class RedisClient {
	private Jedis jedis;//
	RedisClient(String host,int port){
		jedis = new Jedis(host,port);
	}
	
	public void pipeline(){
		String key = "pipeline-test";
		String old = jedis.get(key);
		if(old != null){
			System.out.println("Key:" + key + ",old value:" + old);
		}
		//代码模式1,这种模式是最常见的方式
		Pipeline p1 = jedis.pipelined();
		p1.incr(key);
		System.out.println("Request incr");
		p1.incr(key);
		System.out.println("Request incr");
		//结束pipeline，并开始从相应中获得数据
		List<Object> responses = p1.syncAndReturnAll();
		if(responses == null || responses.isEmpty()){
			throw new RuntimeException("Pipeline error: no response...");
		}
		for(Object resp : responses){
			System.out.println("Response:" + resp.toString());//注意，此处resp的类型为Long
		}
		//代码模式2
		Pipeline p2 = jedis.pipelined();
		Response<Long> r1 = p2.incr(key);
		try{
			r1.get();
		}catch(Exception e){
			System.out.println("Error,you cant get() before sync,because IO of response hasn't begin..");
		}
		Response<Long> r2 = p2.incr(key);
		p2.sync();
		System.out.println("Pipeline,mode 2,--->" + r1.get());
		System.out.println("Pipeline,mode 2,--->" + r2.get());
		
	}
	
	public void txPipeline(){
		String key = "pipeline-test";
		String old = jedis.get(key);
		if(old != null){
			System.out.println("Key:" + key + ",old value:" + old);
		}
		//代码模式1,这种模式是最常见的方式
		Pipeline p1 = jedis.pipelined();
		//p1.multi();
		p1.incr(key);
		System.out.println("Request incr");
		p1.incr(key);
		//System.out.println(jedis.get(key));
		jedis.set("txptest", "1");
		System.out.println("Request incr");
		//Response<List<Object>> txresult= p1.exec();
		p1.sync();
		//结束pipeline，并开始从相应中获得数据
//		List<Object> responses = txresult.get();
//		if(responses == null || responses.isEmpty()){
//			throw new RuntimeException("Pipeline error: no response...");
//		}
//		for(Object resp : responses){
//			System.out.println("Response:" + resp.toString());//注意，此处resp的类型为Long
//		}
	}
	
	public void transaction(){
		String key = "transaction-key";
		jedis.set(key, "20");
		jedis.watch(key);
		Transaction tx = jedis.multi();
		tx.incr(key);
		tx.incr(key);
		tx.incr(key);
		List<Object> result = tx.exec();
		if(result == null || result.isEmpty()){
			System.out.println("Transaction error...");//可能是watch-key被外部修改，或者是数据操作被驳回
			return;
		}
		for(Object rt : result){
			System.out.println(rt.toString());
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		RedisClient client = new RedisClient(Constants.host, Constants.port);
//		client.pipeline();
//		client.pipeline();
//		client.txPipeline();
//		client.transaction();
	}

}
