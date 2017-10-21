package cn.lambdalib2.render;

public class RenderStates {

    public enum TestMode {
        Always, NotEqual, Equal, Greater, GEqual, Less, LEqual
    }

    public enum BlendFunc {
        One,      Zero,
        SrcColor, OneMinusSrcColor,
        SrcAlpha, OneMinusSrcAlpha,
        DstColor, OneMinusDstColor,
        DstAlpha, OneMinusDstAlpha
    }

    public enum CullMode {
        Back, Front, Off
    }

    public TestMode depthTestMode = TestMode.Always;

    public boolean depthMask = true;

    public TestMode alphaTestMode = TestMode.Always;
    public float alphaTestRef = 0.1f;

    public CullMode cullMode = CullMode.Back;

    public boolean
            colorMaskR = true,
            colorMaskG = true,
            colorMaskB = true,
            colorMaskA = true;

    public boolean blending = false;
    public BlendFunc srcBlend = BlendFunc.SrcAlpha, dstBlend = BlendFunc.OneMinusSrcAlpha;

}
