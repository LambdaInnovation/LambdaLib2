package cn.lambdalib2.util;

import net.minecraft.util.ResourceLocation;

import java.io.InputStream;

public class ResourceUtils {

    public static InputStream getResourceStream(ResourceLocation res) {
        try {
            String domain = res.getResourceDomain(), path = res.getResourcePath();
            return ResourceUtils.class.getResourceAsStream("/assets/" + domain + "/" + path);
        } catch(Exception e) {
            throw new RuntimeException("Invalid resource " + res, e);
        }
    }

}
