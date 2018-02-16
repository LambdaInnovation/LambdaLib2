package cn.lambdalib2.registry.impl;

import cn.lambdalib2.registry.StateEventCallback;
import jdk.internal.org.objectweb.asm.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation.EnumHolder;
import net.minecraftforge.fml.common.event.FMLStateEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public enum RegistryManager {
    INSTANCE;

    class ModContext {
        public String packageRoot;
        public HashMap<Class<? extends FMLStateEvent>, List<Method>> loadCallbacks;
    }

    Map<String, ModContext> registryMods;

    Set<ASMDataTable.ASMData> rawStateEventCallbacks;

    boolean initialized = false;

    public void readASMData(ASMDataTable table) {
        Set<String> removedClasses = new HashSet<>();
        { // Get removed classes
            String startSide = FMLCommonHandler.instance().getSide().toString();
            Set<ASMDataTable.ASMData> sideData = table.getAll("net.minecraftforge.fml.relauncher.SideOnly");
            for (ASMDataTable.ASMData asmData: sideData) {
                if (Objects.equals(asmData.getClassName(), asmData.getObjectName())) { // Is a class
                    EnumHolder enumHolder = (EnumHolder) asmData.getAnnotationInfo().get("value");
                    if (!Objects.equals(enumHolder.getValue(), startSide)) {
                        removedClasses.add(asmData.getClassName());
                    }
                }
            }
        }

        rawStateEventCallbacks = new HashSet<>();
        {
            Set<ASMData> loadCallbacks = table.getAll("cn.lambdalib2.registry.StateEventCallback");
            rawStateEventCallbacks.addAll(
                    loadCallbacks.stream()
                            .filter(it -> !removedClasses.contains(it.getClassName()))
                            .collect(Collectors.toList()));
        }

        registryMods = table.getAll("cn.lambdalib2.registry.RegistryMod")
                .stream()
                .filter(it -> !removedClasses.contains(it.getClassName()))
                .collect(Collectors.toMap(ASMData::getClassName, this::createModContext));

        RegistryTransformer.setRegistryMods(registryMods.keySet());
    }

    private String getPackageName(String className) {
        int idx = className.lastIndexOf('.');
        return className.substring(0, idx);
    }

    private ModContext createModContext(ASMData data) {
        ModContext ctx = new ModContext();
        ctx.packageRoot = (String) data.getAnnotationInfo().getOrDefault("rootPackage", getPackageName(data.getClassName()));
        ctx.loadCallbacks = new HashMap<>();
        return ctx;
    }

    private void onStateEvent(String mod, FMLStateEvent event) {
        checkInit();

        ModContext ctx = registryMods.get(mod);
        List<Method> methods = ctx.loadCallbacks.get(event.getClass());
        if (methods != null) {
            for (Method m : methods) {
                try {
                    m.invoke(null, event);
                } catch (Exception ex) {
                    throw new RuntimeException("Error when calling StateEventCallback " + m);
                }
            }
        }
    }

    private void checkInit() {
        if (!initialized) {
            HashMap<Method, Integer> priorityMap = new HashMap<>();

            for (ASMData data : rawStateEventCallbacks) {
                ModContext mod = findMod(data.getClassName());
                try {
                    if (mod != null) {
                        Class<?> klass = Class.forName(data.getClassName());

                        String fullDesc = data.getObjectName();
                        int idx = fullDesc.indexOf('(');
                        String methodName = fullDesc.substring(0, idx);
                        String desc = fullDesc.substring(idx);

                        Type[] rawArgs = Type.getArgumentTypes(desc);
                        Class[] args = new Class[rawArgs.length];
                        for (int i = 0; i < rawArgs.length; ++i) {
                            args[i] = Class.forName(rawArgs[i].getClassName());
                        }

                        Method method = klass.getMethod(methodName, args);
                        if (!Modifier.isStatic(method.getModifiers())) {
                            throw new IllegalArgumentException("@StateEventCallback methods must be static.");
                        }
                        if (args.length != 1) {
                            throw new IllegalArgumentException("@StateEventCallback methods requires exactly 1 argument.");
                        }

                        Class<?> eventType = args[0];
                        if (!FMLStateEvent.class.isAssignableFrom(eventType)) {
                            throw new IllegalArgumentException("@StateEventCallback method's first argument type must inherit FMLStateEvent");
                        }

                        if (!mod.loadCallbacks.containsKey(eventType)) {
                            mod.loadCallbacks.put((Class<? extends FMLStateEvent>) eventType, new ArrayList<>());
                        }

                        mod.loadCallbacks.get(eventType).add(method);
                        priorityMap.put(method, (int) data.getAnnotationInfo().getOrDefault("priority", 0));

                    } else {
                        throw new IllegalStateException("StateEventCallback " + data.getObjectName() + " doesn't have mod that registers it");
                    }
                } catch (ClassNotFoundException|NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            // sort by priority
            for (ModContext ctx : registryMods.values()) {
                for (List<Method> list : ctx.loadCallbacks.values()) {
                    list.sort((lhs, rhs) -> {
                        int lp = priorityMap.get(lhs);
                        int rp = priorityMap.get(rhs);
                        return rp - lp;
                    });
                }
            }

            rawStateEventCallbacks.clear();

            initialized = true;
        }
    }

    ModContext findMod(String path) {
        for (Entry<String, ModContext> entry : registryMods.entrySet()) {
            if (path.startsWith(entry.getValue().packageRoot)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static void asm_RegistrationEvent(String mod, FMLStateEvent event) {
        INSTANCE.onStateEvent(mod, event);
    }
}
