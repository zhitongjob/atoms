package com.lovver.atoms.config;

import java.io.IOException;
import java.io.InputStream;

import com.thoughtworks.xstream.XStream;

public class AtomsConfig {

	private static AtomsBean atomsBean;
	
	public static AtomsBean getAtomsConfig(){
		return atomsBean;
	}
	
	static {
		InputStream is = AtomsConfig.class.getClassLoader()
				.getResourceAsStream("atoms.xml");
		xmlToJavaBean(is);
		if(is!=null){
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
