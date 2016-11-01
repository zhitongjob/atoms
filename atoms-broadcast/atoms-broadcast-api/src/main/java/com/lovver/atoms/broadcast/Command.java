package com.lovver.atoms.broadcast;

import java.util.UUID;



/**
 * 命令消息
 * 
 * @author jobell
 */
public class Command {		

	public final static byte OPT_DELETE_KEY = 0x01; 	//删除缓存
	public final static byte OPT_CLEAR_KEY = 0x02; 		//清除缓存
	public final static byte OPT_PUT_KEY = 0x03; 		//添加或更新缓存
	
	private byte operator;
	private byte expire_operator;
	private String region;
	private Object key;
	private byte[] value;
	private String client_id;
	
	public final static byte EXPIRE_UPDATE=0x10;//失效后更新，即从多级缓存中拿出数据从新设置
	public final static byte EXPIRE_DELETE=0x11;//失效后删除，删除后面多级缓存中的数据
	

	public Command(){
	}
	
	
	public Command(byte operator, String region, Object key,String client_id){
		this.operator = operator;
		this.region = region;
		this.key = key;
		this.expire_operator = EXPIRE_DELETE;
		this.client_id=client_id;
	}
	
	public Command(byte operator, String region,String client_id){
		this.operator = operator;
		this.region = region;
		this.expire_operator = EXPIRE_DELETE;
		this.client_id=client_id;
	}
	
	public Command(byte operator, byte expire_operator,String region, Object key,String client_id){
		this.operator = operator;
		this.region = region;
		this.key = key;
		this.expire_operator = expire_operator;
		this.client_id=client_id;
	}
	
   public Command(byte o, String r, Object k,byte[] value,String client_id){
		this.operator = o;
		this.region = r;
		this.key = k;
		this.value=value;
		this.client_id=client_id;
	}
	
	public byte getOperator() {
		return operator;
	}

	public void setOperator(byte operator) {
		this.operator = operator;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}
	public byte getExpire_operator() {
		return expire_operator;
	}
	public void setExpire_operator(byte expire_operator) {
		this.expire_operator = expire_operator;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}


	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
}