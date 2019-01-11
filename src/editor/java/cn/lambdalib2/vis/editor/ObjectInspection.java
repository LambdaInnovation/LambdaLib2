package cn.lambdalib2.vis.editor;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Color;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ObjectInspection {
    public static final String UNNAMED_FIELD = "<unnamed>";

    private final Map<Class<?>, ObjectEditor<?>> editorMap = new HashMap<>();

    public ObjectInspection() {
        registerMulti(new ObjectEditor<Integer>() {
            @Override
            public Integer inspect(Integer target, String fieldName) {
                return ImGui.inputInt(fieldName, target, 0);
            }
        }, int.class, Integer.class);

        registerMulti(new ObjectEditor<Float>() {
            @Override
            public Float inspect(Float target, String fieldName) {
                return ImGui.inputFloat(fieldName, target, 0);
            }
        }, float.class, Float.class);

        registerMulti(new ObjectEditor<Double>() {
            @Override
            public Double inspect(Double target, String fieldName) {
                return ImGui.inputDouble(fieldName, target);
            }
        }, double.class, Double.class);

        registerMulti(new ObjectEditor<String>() {
            @Override
            public String inspect(String target, String fieldName) {
                return ImGui.inputText(fieldName, target);
            }
        }, String.class);

        registerMulti(new ObjectEditor<ResourceLocation>() {
            @Override
            public ResourceLocation inspect(ResourceLocation target, String fieldName) {
                String domain = target.getNamespace();
                String path = target.getPath();
                if (ImGui.treeNode(fieldName)) {
                    domain = ImGui.inputText("domain", domain, 0);
                    path = ImGui.inputText("path", path, 0);
                    ImGui.treePop();
                }

                if (!domain.equals(target.getNamespace()) || !path.equals(target.getPath()))
                    return new ResourceLocation(domain, path);
                return target;
            }
        }, ResourceLocation.class);

        registerMulti(new ObjectEditor<Color>() {
            @Override
            public Color inspect(Color target, String fieldName) {
                ImGui.colorEdit4(fieldName, target);
                return target;
            }
        }, Color.class);

        registerMulti(new ObjectEditor<Boolean>() {
            @Override
            public Boolean inspect(Boolean target, String fieldName) {
                return ImGui.checkbox(fieldName, target);
            }
        }, boolean.class, Boolean.class);

        registerMulti(new ObjectEditor<Enum>() {
            private final Map<Class<? extends Enum>, String[]> namesCache = new HashMap<>();

            @Override
            public Enum inspect(Enum target, String fieldName) {
                Object[] constants = target.getDeclaringClass().getEnumConstants();
                int newIdx = ImGui.combo(
                    fieldName,
                    target.ordinal(),
                    namesCache.computeIfAbsent( target.getDeclaringClass(),
                        klass ->
                            Arrays.stream(klass.getEnumConstants())
                                .map(Enum::toString)
                                .toArray(String[]::new)
                    )
                );
                return (Enum) constants[newIdx];
            }
        }, Enum.class);

    }

    @SuppressWarnings("unchecked")
    public <T> void registerMulti(ObjectEditor<T> editor, Class... clzs) {
        for (Class c : clzs)
            register(editor, c);
    }

    public <T> void register(ObjectEditor<T> editor, Class<? extends T> klass) {
        editorMap.put((Class) klass, (ObjectEditor) editor);
    }

    public void inspect(Object obj) {
        inspect(obj, UNNAMED_FIELD);
    }

    public Object inspect(Object obj, String fieldName) {
        ObjectEditor<Object> editor = getCurEditor(obj);
        if (editor == null)
            editor = createDefaultEditor(obj.getClass());

        return editor.inspect(obj, fieldName);
    }

    @SuppressWarnings("unchecked")
    private ObjectEditor<Object> getCurEditor(Object obj) {
        Class cls = obj.getClass();
        while (cls != null) {
            if (editorMap.containsKey(cls))
                return (ObjectEditor) editorMap.get(cls);
            cls = cls.getSuperclass();
        }
        return null;
    }

    private ObjectEditor<Object> createDefaultEditor(Class klass) {
        List<Field> exposedFields = getExposedFields(klass);
        ObjectEditor<Object> ret = new ObjectEditor<Object>() {
            @Override
            public Object inspect(Object target, String fieldName) {
                try {
                    String headerName = fieldName.equals(UNNAMED_FIELD) ?
                        target.getClass().getSimpleName() : fieldName;
                    if (ImGui.treeNode(headerName)) {
                        for (Field f : exposedFields) {
                            Object fieldVal = f.get(target);
                            Object editedVal = fieldVal;
                            if (fieldVal == null) {
                                float x = ImGui.getCursorPos().x;
                                ImGui.pushItemWidth(0);
                                ImGui.labelText(f.getName(), "");
                                ImGui.popItemWidth();
                                ImGui.sameLine();
                                ImGui.setCursorPosX(x);
                                boolean editable = !Modifier.isFinal(f.getModifiers());
                                if (!editable) {
                                    ImGui.button("<null> - final field");
                                } else {
                                    Constructor ctor = null;
                                    try {
                                        ctor = f.getType().getDeclaredConstructor();
                                    } catch (NoSuchMethodException ex) {}
                                    if (ctor != null)
                                        ctor.setAccessible(true);

                                    if (ctor == null)
                                        ImGui.button("<null> - No default ctor");
                                    else {
                                        if (ImGui.button("<null> - new instance?")) {
                                            editedVal = ctor.newInstance();
                                        }
                                    }
                                }
                            } else {
                                String childName = f.getName();
                                if (Modifier.isFinal(f.getModifiers()))
                                    childName += "(final)";
                                editedVal = ObjectInspection.this.inspect(fieldVal, childName);
                            }
                            if (fieldVal != editedVal && !Modifier.isFinal(f.getModifiers())) {
                                f.set(target, editedVal);
                            }
                        }
                        ImGui.treePop();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                return target;
            }
        };

        editorMap.put(klass, ret);
        return ret;
    }

    protected List<Field> getExposedFields(Class klass) {
        Field[] raw = klass.getDeclaredFields();
        List<Field> ret = new ArrayList<>();
        for (Field f : raw) {
            int mod = f.getModifiers();
            if (!Modifier.isStatic(mod)) {
                f.setAccessible(true);
                ret.add(f);
            }
        }
        return ret;
    }


}
