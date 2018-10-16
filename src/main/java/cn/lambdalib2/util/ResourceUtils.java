package cn.lambdalib2.util;

import net.minecraft.util.ResourceLocation;

import java.io.InputStream;

public class ResourceUtils {

    public static InputStream getResourceStream(ResourceLocation res) {
        try {
            String domain = res.getNamespace(), path = res.getPath();
            return Debug.assertNotNull(ResourceUtils.class.getResourceAsStream("/assets/" + domain + "/" + path), () -> "Can't find resource " + res.toString());
        } catch(Exception e) {
            throw new RuntimeException("Invalid resource " + res, e);
        }
    }

}
