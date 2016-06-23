package com.lovver.atoms.broadcast;


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
	
	public final static byte EXPIRE_UPDATE=0x10;//失效后更新，即从多级缓存中拿出数据从新设置
	public final static byte EXPIRE_DELETE=0x11;//失效后删除，删除后面多级缓存中的数据
	

	public Command(){
	}
	
	public Command(byte o, String r, Object k){
		this.operator = o;
		this.region = r;
		this.key = k;
		this.expire_operator = EXPIRE_DELETE;
	}
	
	public Command(byte o, String r){
		this.operator = o;
		this.region = r;
		this.expire_operator = EXPIRE_DELETE;
	}
	
	public Command(byte o, byte eo,String r, Object k){
		this.operator = o;
		this.region = r;
		this.key = k;
		this.expire_operator = eo;
	}
	
	public Command(byte o, String r, Object k,byte[] value) {
		this.operator = o;
		this.region = r;
		this.key = k;
		this.value=value;
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
}