package com.lovver.atoms;

import com.lovver.atoms.context.AtomsContext;
import com.lovver.atoms.serializer.Serializer;
import com.lovver.atoms.serializer.SerializerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对象序列化工具包
 *
 * @author jobell
 */
public class SerializerFactoryTest {

    @Test
    public  void main() throws IOException {

        Serializer g_ser= SerializerFactory.getSerializer("fst");

        final List<String> obj = new ArrayList<String>();
        obj.addAll(Arrays.asList("哈啊啊", "农夫山泉", "康师傅", "啊百特C", "多大的", "反反复复"));

        byte[] bits = g_ser.serialize(obj);
        for (byte b : bits) {
            System.out.print(Byte.toString(b) + " ");
        }
        System.out.println();
        System.out.println(bits.length);
        System.out.println(g_ser.deserialize(bits));
    }

}
