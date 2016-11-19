package com.lovver.atoms.cache.ssdb;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lovver.atoms.common.utils.CollectionUtils;
import com.lovver.ssdbj.core.BaseResultSet;
import com.lovver.ssdbj.pool.SSDBDataSource;
import com.lovver.ssdbj.pool.SSDBPoolConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lovver.atoms.cache.Cache;
import com.lovver.atoms.cache.CacheEventListener;
import com.lovver.atoms.common.exception.CacheException;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;

/**
 * Redis 缓存基于Hashs实现
 * @author wendal
 */
public class SsdbCache implements Cache {

	private final static Logger log = LoggerFactory.getLogger(SsdbCache.class);
	private static Serializer serializer=AtomsContext.getSerializer();
	// 记录region
	protected byte[] region2;
	protected String region;
	protected SSDBDataSource ssdbDs;
	private String srcRegion;
	
	private String namespace;
	private CacheEventListener listener;
	private String host;
	private Integer ttlSeconds;
	
	private String client_id;

	public SsdbCache(String region, SSDBDataSource ssdbDs, String namespace, CacheEventListener listener, String host, String ttlSeconds, String client_id) {
		if (region == null || region.isEmpty())
			region = "_"; // 缺省region
		this.srcRegion=region;
		this.namespace=namespace;
		this.region = getRegionName(region);
		this.listener=listener;
		this.ssdbDs = ssdbDs;
//		this.region = region;
		this.region2 = this.region.getBytes();
		this.host=host;
		if(StringUtils.isEmpty(ttlSeconds)){
			this.ttlSeconds=null;
		}else{
			this.ttlSeconds=Integer.parseInt(ttlSeconds);
		}
		this.client_id=client_id;
	}

	/**
	 * 在region里增加一个可选的层级,作为命名空间,使结构更加清晰
	 * 同时满足小型应用,多个J2Cache共享一个redis database的场景
	 * @param region
	 * @return
     */
	private String getRegionName(String region) {
		if(namespace != null && !namespace.isEmpty()) {
			region = namespace + ":" + region;
		}
		return region;
	}
	
	protected byte[] getKeyName(Object key) {
		if(key instanceof Number)
			return ("I:" + key).getBytes();
		else if(key instanceof String || key instanceof StringBuilder || key instanceof StringBuffer)
			return ("S:" + key).getBytes();
		return ("O:" + key).getBytes();
	}

	public Object get(Object key) throws CacheException {
		if (null == key)
			return null;
		Object obj = null;
		SSDBPoolConnection conn=null;
		try {
			conn= ssdbDs.getConnection();
			ArrayList params=new ArrayList();
			params.add(region2);
			params.add(getKeyName(key));
			BaseResultSet<byte[]> rs= conn.execute("hget",params );
//			System.out.println(new String(rs.getResult()));

			byte[] b = rs.getResult();
			if(b != null)
				obj = serializer.deserialize(b);
		} catch (Exception e) {
			log.error("Error occured when get data from ssdb cache", e);
			if(e instanceof IOException || e instanceof NullPointerException)
				evict(key);
		}finally {
			if(null!=conn){
				conn.close();
			}
		}
		return obj;
	}

	public void put(Object key, Object value) throws CacheException {
		System.out.println(this.host+" ==================put ssdb");
		if (key == null){
			return;
		}
		if (value == null){
			evict(key);
		}else {
			SSDBPoolConnection conn=null;
			try {
				conn= ssdbDs.getConnection();
				ArrayList<byte[]> setparams=new ArrayList<byte[]>();
				setparams.add(region2);
				setparams.add(getKeyName(key));
				setparams.add(serializer.serialize(value));
				conn.executeUpdate("hset",setparams);
//				if(ttlSeconds!=null){
//					cache.expire(region2, ttlSeconds);
//				}
				if(listener!=null&&AtomsContext.isMe(client_id)){
					listener.notifyElementPut(this.srcRegion, key, value,client_id);
				}
			} catch (Exception e) {
				throw new CacheException(e);
			}finally {
				if(null!=conn){
					conn.close();
				}
			}
		}
	}

	public void update(Object key, Object value) throws CacheException {
		put(key, value);
		if(listener!=null&&AtomsContext.isMe(client_id)){
			listener.notifyElementPut(this.srcRegion, key, value, client_id);
		}
	}
	
	
	public void expireUpdate(Object key, Object value) throws CacheException{
		System.out.println(this.host+" ==================expireUpdate ssdb");
		if (key == null){
			return;
		}
		if (value == null){
			evict(key);
		}else {
			SSDBPoolConnection conn=null;
			try {
				conn= ssdbDs.getConnection();
				ArrayList<byte[]> setparams=new ArrayList<byte[]>();
				setparams.add(region2);
				setparams.add(getKeyName(key));
				setparams.add(serializer.serialize(value));
				conn.executeUpdate("hset",setparams);
//				if(ttlSeconds!=null){
//					cache.expire(region2, ttlSeconds);
//				}
				if(listener!=null&&AtomsContext.isMe(client_id)){
					listener.notifyElementPut(this.srcRegion, key, value,client_id);
				}
			} catch (Exception e) {
				throw new CacheException(e);
			}finally {
				if(null!=conn){
					conn.close();
				}
			}
//			try (Jedis cache = pool.getResource()) {
//				cache.hset(region2, getKeyName(key), serializer.serialize(value));
//				if(ttlSeconds!=null){
//					cache.expire(region2, ttlSeconds);
//				}
//				if(listener!=null&&AtomsContext.isMe(client_id)){
//					listener.notifyElementPut(this.srcRegion, key, value,client_id);
//				}
//			} catch (Exception e) {
//				throw new CacheException(e);
//			}
		}
	}

	public void evict(Object key) throws CacheException {
		if (key == null)
			return;
		SSDBPoolConnection conn=null;
		try {
			conn= ssdbDs.getConnection();
			ArrayList<byte[]> setparams=new ArrayList<byte[]>();
			setparams.add(region2);
			setparams.add(getKeyName(key));
			conn.executeUpdate("hdel",setparams);
//				if(ttlSeconds!=null){
//					cache.expire(region2, ttlSeconds);
//				}
			if(listener!=null&&AtomsContext.isMe(client_id)){
				listener.notifyElementRemoved(this.srcRegion, key,client_id);
			}
		} catch (Exception e) {
			throw new CacheException(e);
		}finally {
			if(null!=conn){
				conn.close();
			}
		}

//		try (Jedis cache = pool.getResource()) {
//			cache.hdel(region2, getKeyName(key));
//			if(listener!=null&&AtomsContext.isMe(client_id)){
//				listener.notifyElementRemoved(this.srcRegion, key,client_id);
//			}
//		} catch (Exception e) {
//			throw new CacheException(e);
//		}
	}

	@SuppressWarnings("rawtypes")
	public void evict(List keys) throws CacheException {
		if(keys == null || keys.size() == 0)
			return ;
		SSDBPoolConnection conn=null;
		try {
			int size = keys.size();
			byte[][] okeys = new byte[size][];
			for(int i=0; i<size; i++){
				okeys[i] = getKeyName(keys.get(i));
			}
			conn= ssdbDs.getConnection();
			ArrayList<byte[]> setparams=new ArrayList<byte[]>();
			setparams.add(region2);
			setparams.addAll(CollectionUtils.arrayToList(okeys));
			conn.executeUpdate("hdel",setparams);
			if(listener!=null&&AtomsContext.isMe(client_id)){
				for(Object key:keys){
					listener.notifyElementRemoved(this.srcRegion, key,client_id);
				}
			}
		} catch (Exception e) {
			throw new CacheException(e);
		}finally {
			if(null!=conn){
				conn.close();
			}
		}

//		try (Jedis cache = pool.getResource()) {
//			int size = keys.size();
//			byte[][] okeys = new byte[size][];
//			for(int i=0; i<size; i++){
//				okeys[i] = getKeyName(keys.get(i));
//			}
//			cache.hdel(region2, okeys);
//			if(listener!=null&&AtomsContext.isMe(client_id)){
//				for(Object key:keys){
//					listener.notifyElementRemoved(this.srcRegion, key,client_id);
//				}
//			}
//		} catch (Exception e) {
//			throw new CacheException(e);
//		}
	}

	public List<String> keys() throws CacheException {
//		try (Jedis cache = pool.getResource()) {
//			return new ArrayList<String>(cache.hkeys(region));
//		} catch (Exception e) {
//			throw new CacheException(e);
//		}

		SSDBPoolConnection conn=null;
		try {
			conn= ssdbDs.getConnection();
			ArrayList<byte[]> setparams=new ArrayList<byte[]>();
			setparams.add(region2);
			BaseResultSet<byte[]> rs=conn.execute("hkeys",setparams);
			byte[] b = rs.getResult();
			if(b != null)
				return (List<String> )serializer.deserialize(b);
		} catch (Exception e) {
			throw new CacheException(e);
		}finally {
			if(null!=conn){
				conn.close();
			}
		}
		return new ArrayList();
	}

	public void clear() throws CacheException {
//		try (Jedis cache = pool.getResource()) {
//			cache.del(region2);
//			if(listener!=null&&AtomsContext.isMe(client_id)){
//				listener.notifyRemoveAll(this.srcRegion,client_id);
//			}
//		} catch (Exception e) {
//			throw new CacheException(e);
//		}

		SSDBPoolConnection conn=null;
		try {
			conn= ssdbDs.getConnection();
			ArrayList<byte[]> setparams=new ArrayList<byte[]>();
			setparams.add(region2);
			conn.executeUpdate("del",setparams);
			if(listener!=null&&AtomsContext.isMe(client_id)){
				listener.notifyRemoveAll(this.srcRegion,client_id);
			}
		} catch (Exception e) {
			throw new CacheException(e);
		}finally {
			if(null!=conn){
				conn.close();
			}
		}
	}

	public void destroy() throws CacheException {
		this.clear();
		if(listener!=null&&AtomsContext.isMe(client_id)){
			listener.notifyRemoveAll(this.srcRegion,client_id);
		} 
	}
}
