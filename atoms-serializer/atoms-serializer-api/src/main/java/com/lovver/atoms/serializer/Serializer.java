package com.lovver.atoms.serializer;

import java.io.IOException;

import com.lovver.atoms.common.annotation.Extension;

/**
 * 对象序列化接口
 * @author jobell
 */
@Extension("spi")
public interface Serializer {
	
	public String name();

	public byte[] serialize(Object obj) throws IOException ;
	
	public Object deserialize(byte[] bytes) throws IOException ;
	
}
