package cn.lambdalib2.vis.editor;

public abstract class ObjectEditor<T> {
    public static final int
        F_REQUIRE_S11n = 1;

    public abstract T inspect(T target, String fieldName);

}
