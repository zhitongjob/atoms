package com.lovver.atoms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lovver.atoms.common.extension.ExtensionLoader;
import com.lovver.atoms.serializer.Serializer;

/**
 * 对象序列化工具包
 *
 * @author jobell
 */
public class SerializerTest {

    private static ExtensionLoader<Serializer> exloader=ExtensionLoader.getExtensionLoader(Serializer.class);
    private static Serializer g_ser;

    static {
        try {
			g_ser = exloader.getExtension("kryopool");
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    }

    public static byte[] serialize(Object obj) throws IOException {
        return g_ser.serialize(obj);
    }

    public static Object deserialize(byte[] bytes) throws IOException {
        return g_ser.deserialize(bytes);
    }
    
    
    public static void main(String[] args) throws IOException {
        final List<String> obj = new ArrayList<String>();
        obj.addAll(Arrays.asList("哈啊啊", "农夫山泉", "康师傅", "啊百特C", "多大的", "反反复复"));

        byte[] bits = serialize(obj);
        for (byte b : bits) {
            System.out.print(Byte.toString(b) + " ");
        }
        System.out.println();
        System.out.println(bits.length);
        System.out.println(deserialize(bits));
    }

}
