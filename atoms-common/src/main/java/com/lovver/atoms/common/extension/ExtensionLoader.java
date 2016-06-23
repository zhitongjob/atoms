package com.lovver.atoms.common.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;

import com.lovver.atoms.common.annotation.Extension;
import com.lovver.atoms.common.annotation.SPI;

public class ExtensionLoader<T> {

	public static final Map<Class<?>,Map<String,Class<?>>> cachedClass=new ConcurrentHashMap<Class<?>,Map<String,Class<?>>>();
	private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();
	private static final ConcurrentMap<Class<?>, Object> cachedInstances = new ConcurrentHashMap<Class<?>, Object>();
	
	protected Class<?> classEx;
	
	@SuppressWarnings("unchecked")
	public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type){
		ExtensionLoader<T> loader= (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		if(loader==null){
			Extension extension=type.getAnnotation(Extension.class);
			String ext_value=extension.value();
			if(StringUtils.isEmpty(ext_value)){
				loader= new ExtensionLoader<T>(type);
			}else{
				loader = ExtensionLoaderFactory.createExtensionLoader(ext_value,type);
			}
		}
		synchronized (EXTENSION_LOADERS) {
			EXTENSION_LOADERS.put(type, loader);
		}
		return loader;
	}
	
	public ExtensionLoader(Class<T> classEx){
		this.classEx=classEx;
	}
	
	@SuppressWarnings("unchecked")
	public T getExtension(String name) throws InstantiationException, IllegalAccessException{
		Map<String, Class<?>> mClass=cachedClass.get(classEx);
		if(mClass!=null){
			Class<T> clazz=(Class<T>) mClass.get(name);
			if(clazz!=null){
				T obj=(T)cachedInstances.get(clazz);
				if(obj==null){
					synchronized (cachedInstances) {
						obj=clazz.newInstance();
						cachedInstances.put(clazz, obj);
					}
				}
				return obj;
			}else{
				ExtensionLoader<?> loader=getExtensionLoader(classEx);
				T obj=(T)loader.getExtension(name);
				return obj;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public T getExtension(String name,int version) throws InstantiationException, IllegalAccessException{
		Map<String, Class<?>> mClass=cachedClass.get(classEx);
		if(mClass!=null){
			Class<T> clazz=(Class<T>) mClass.get(name);
			if(clazz!=null){
				Map<String,T> mapVersionObj=(Map<String, T>) cachedInstances.get(clazz);
				if(mapVersionObj==null){
					mapVersionObj=new ConcurrentHashMap<String, T>();
				}
				
				T obj= mapVersionObj.get(version+""); ;
				if(obj==null){
					synchronized (cachedInstances) {
						obj=clazz.newInstance();
						mapVersionObj.put(version+"", obj);
						cachedInstances.put(clazz, mapVersionObj);
					}
				}
				return mapVersionObj.get(version+""); 
			}else{
				if(EXTENSION_LOADERS.get(classEx)!=null){
					return null;
				}
				ExtensionLoader<?> loader=getExtensionLoader(classEx);
				T obj=(T)loader.getExtension(name);
				return obj;
			}
		}
		return null;
	}
	
	
	
	
	static class SpiExtensionLoader<T> extends ExtensionLoader<T>{
		private final String spi_prefix="META-INF/extension/";
		public SpiExtensionLoader(Class<T> classEx) {
			super(classEx);
			ClassLoader loader=ExtensionLoader.class.getClassLoader();
			try {
				String extension_interface=spi_prefix+classEx.getName();
				Enumeration<URL> urls=loader.getResources(extension_interface);
				if(urls==null){
					return;
				}
				while(urls.hasMoreElements()){
					URL url=urls.nextElement();
					BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                    try {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            final int ci = line.indexOf('#');
                            if (ci >= 0) line = line.substring(0, ci);
                            line = line.trim();
                            if (line.length() > 0) {
                               Class<?> clazzImpl=(Class<?>) Class.forName(line);
                               if(null!=clazzImpl){
                            	   SPI spi=clazzImpl.getAnnotation(SPI.class);
                            	   if(spi!=null){
                            		   String spi_val=spi.value();
                            		   Map<String, Class<?>> mClasses=cachedClass.get(classEx);
                            		   if(null==mClasses){
                            			   mClasses=new ConcurrentHashMap<String,Class<?>>();
                            		   }
                            		   mClasses.put(spi_val, clazzImpl);
                            		   cachedClass.put((Class<?>)classEx, mClasses);
                            	   }else{
                            		   throw new RuntimeException(line +" must has spi Annotation");
                            	   }
                               }else{
                            	   throw new RuntimeException(line +" is not exist");
                               }
                            }
                        }
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class ExtensionLoaderFactory {
		public static <T> ExtensionLoader<T> createExtensionLoader(String name,Class<T> classEx){
			if(name.equalsIgnoreCase("spi")){
				return new SpiExtensionLoader<T>(classEx);
			}
			
			throw new RuntimeException("not support");
		}
	}
}