package cn.ll2test.render;

import cn.lambdalib2.render.ShaderScript;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;

import static java.lang.System.out;

public class ShaderScriptLoadingTest {


    public static void main(String[] args) throws Exception {
        String shaderSource = IOUtils.toString(
                ShaderScriptLoadingTest.class.getResource("/render/test.shader"),
                Charset.forName("utf-8"));

        ShaderScript script = ShaderScript.load(shaderSource);
    }

}
