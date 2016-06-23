package com.lovver.atoms.cache;

import com.lovver.atoms.common.extension.ExtensionLoader;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsCacheBean;

public class CacheProviderFactory {
	private static ExtensionLoader<CacheProvider> exloader = ExtensionLoader.getExtensionLoader(CacheProvider.class);

	public  static CacheProvider getCacheProvider(AtomsCacheBean atomBean,int level)
			throws InstantiationException, IllegalAccessException {
		String type = atomBean.getType();
		if (StringUtils.isEmpty(type)) {
			throw new RuntimeException("plese give the CacheProvider type");
		}
		CacheProvider cacheProvider = exloader.getExtension(type,level);
		cacheProvider.start(atomBean); 
		return cacheProvider;
	}
}
