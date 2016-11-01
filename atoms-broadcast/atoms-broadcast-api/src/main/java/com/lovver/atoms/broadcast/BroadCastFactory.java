package com.lovver.atoms.broadcast;

import com.lovver.atoms.common.extension.ExtensionLoader;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsBroadCastBean;

public class BroadCastFactory {
	private static ExtensionLoader<BroadCast> exloader = ExtensionLoader
			.getExtensionLoader(BroadCast.class);

	public  static BroadCast getBroadCast(AtomsBroadCastBean atomBean)
			throws InstantiationException, IllegalAccessException {
		String type = atomBean.getType();
		if (StringUtils.isEmpty(type)) {
			type = "redis";
		}
		BroadCast broadCast = exloader.getExtension(type);
		broadCast.init(atomBean); 
		return broadCast;
	}
}
