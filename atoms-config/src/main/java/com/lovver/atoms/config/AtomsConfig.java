package com.lovver.atoms.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

public class AtomsConfig {

	protected static AtomsBean atomsBean;
	
	public static AtomsBean getAtomsConfig(){
		return atomsBean;
	}
	
	static {
		InputStream is = AtomsConfig.class.getClassLoader()
				.getResourceAsStream("atoms.xml");
		if(is!=null){
			xmlToJavaBean(is);
			mergeAtomsProperty(atomsBean);
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void mergeAtomsProperty(AtomsBean atomsBean){
		InputStream is = AtomsConfig.class.getClassLoader().getResourceAsStream("atoms.properties");
		if(is!=null){
			try {
				List<AtomsCacheBean> lstCache= atomsBean.getCache();
				Properties prop=new Properties();
				prop.load(is);
				for(AtomsCacheBean cacheBean:lstCache){
					mergeCacheConfig(prop,cacheBean);
				}
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void mergeCacheConfig(Properties prop,AtomsCacheBean cacheBean){
			String level=cacheBean.getLevel();
			String host=prop.getProperty("cache."+level+".host");
			if(StringUtils.isNotEmpty(host)){
				cacheBean.getCacheConfig().setHost(host);
			}
			
			String port=prop.getProperty("cache."+level+".port");
			if(StringUtils.isNotEmpty(port)){
				cacheBean.getCacheConfig().setPort(port);
			}
			
			String timeout=prop.getProperty("cache."+level+".timeout");
			if(StringUtils.isNotEmpty(timeout)){
				cacheBean.getCacheConfig().setTimeout(timeout);
			}
	}
	/**
	 * 把xml转化为java对象
	 */
	public static void xmlToJavaBean(InputStream is) {
		XStream stream = new XStream();
		stream.autodetectAnnotations(true);
		try {
			stream.alias("atoms", AtomsBean.class);
			atomsBean = (AtomsBean) stream.fromXML(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
