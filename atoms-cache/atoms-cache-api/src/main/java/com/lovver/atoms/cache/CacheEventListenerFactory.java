package com.lovver.atoms.cache;

import com.lovver.atoms.common.extension.ExtensionLoader;

public class CacheEventListenerFactory {
	private static ExtensionLoader<CacheEventListener> exloader = ExtensionLoader.getExtensionLoader(CacheEventListener.class);

	public  static CacheEventListener getCacheEventListener(String type,String level) {
		try{
			int iLevel=Integer.parseInt(level);
			CacheEventListener cacheEventListener = exloader.getExtension(type,iLevel);
			if(null==cacheEventListener){
				return cacheEventListener;
			}
			cacheEventListener.init(level); 
			return cacheEventListener;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
