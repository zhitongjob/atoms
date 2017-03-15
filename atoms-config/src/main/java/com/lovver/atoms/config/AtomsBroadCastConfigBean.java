package com.lovver.atoms.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("broadcastConfig")
public class AtomsBroadCastConfigBean {

	@XStreamAsAttribute
	private String configFile;

	@XStreamAsAttribute
	private String host;

	@XStreamAsAttribute
	private String usePool;
	
	@XStreamAsAttribute
	private String port;
	
	@XStreamAsAttribute
	private String timeout;
	
	@XStreamAsAttribute
	private String password;
	
	@XStreamAsAttribute
	private String database;
	
	@XStreamAsAttribute
	private String namespace;
	
	@XStreamAsAttribute
	private String maxTotal;
	
	@XStreamAsAttribute
	private String maxIdle;
	
	@XStreamAsAttribute
	private String maxWaitMillis;
	
	@XStreamAsAttribute
	private String minEvictableIdleTimeMillis;
	
	@XStreamAsAttribute
	private String minIdle;
	
	@XStreamAsAttribute
	private String numTestsPerEvictionRun;
	
	@XStreamAsAttribute
	private String lifo;
	
	@XStreamAsAttribute
	private String softMinEvictableIdleTimeMillis;
	
	@XStreamAsAttribute
	private String testOnBorrow;
	
	@XStreamAsAttribute
	private String testOnReturn;
	
	@XStreamAsAttribute
	private String testWhileIdle;
	
	@XStreamAsAttribute
	private String timeBetweenEvictionRunsMillis;
	
	@XStreamAsAttribute
	private String blockWhenExhausted;

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(String maxTotal) {
		this.maxTotal = maxTotal;
	}

	public String getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(String maxIdle) {
		this.maxIdle = maxIdle;
	}

	public String getMaxWaitMillis() {
		return maxWaitMillis;
	}

	public void setMaxWaitMillis(String maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
	}

	public String getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(String minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public String getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(String minIdle) {
		this.minIdle = minIdle;
	}

	public String getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun(String numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public String getLifo() {
		return lifo;
	}

	public void setLifo(String lifo) {
		this.lifo = lifo;
	}

	public String getSoftMinEvictableIdleTimeMillis() {
		return softMinEvictableIdleTimeMillis;
	}

	public void setSoftMinEvictableIdleTimeMillis(
			String softMinEvictableIdleTimeMillis) {
		this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
	}

	public String getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(String testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public String getTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(String testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public String getTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(String testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public String getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(
			String timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public String getBlockWhenExhausted() {
		return blockWhenExhausted;
	}

	public void setBlockWhenExhausted(String blockWhenExhausted) {
		this.blockWhenExhausted = blockWhenExhausted;
	}

	public String getUsePool() {
		return usePool;
	}

	public void setUsePool(String usePool) {
		this.usePool = usePool;
	}
}
