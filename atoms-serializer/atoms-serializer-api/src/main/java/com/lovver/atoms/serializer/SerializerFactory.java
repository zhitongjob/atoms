package com.lovver.atoms.serializer;

import com.lovver.atoms.common.extension.ExtensionLoader;
import com.lovver.atoms.common.utils.StringUtils;
import com.lovver.atoms.config.AtomsSerializerBean;

public class SerializerFactory {
    private static ExtensionLoader<Serializer> exloader = ExtensionLoader
            .getExtensionLoader(Serializer.class);

    public static Serializer getSerializer(AtomsSerializerBean atomSerializerBean) {
        String type = atomSerializerBean.getType();
        if (StringUtils.isEmpty(type)) {
            type = "fst";
        }
        return getSerializer(type);
    }

    public static Serializer getSerializer(String type) {
        Serializer serializer = null;
        try {
            serializer = exloader.getExtension(type);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return serializer;
    }
}