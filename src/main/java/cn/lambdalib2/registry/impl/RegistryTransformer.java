package cn.lambdalib2.registry.impl;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.objectweb.asm.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RegistryTransformer implements IClassTransformer {
    static final Set<String> registryMods = new HashSet<>();

    public static void setRegistryMods(Collection<String> mods) {
        registryMods.addAll(mods);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (registryMods.contains(name)) {
            System.out.println(name + "is registry mod");
            ClassWriter cw = new ClassWriter(Opcodes.ASM5);
            ClassReader cr = new ClassReader(bytes);
            ClassVisitor cv =  new RegistryModTransformer(cw, name);
            cr.accept(cv, 0);
            return cw.toByteArray();
        }

        return bytes;
    }

    class RegistryModTransformer extends ClassVisitor {
        private final String modClassName;

        public RegistryModTransformer(ClassVisitor cv, String _modClassName) {
            super(Opcodes.ASM5, cv);
            modClassName = _modClassName;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            visitRegistryHook(FMLServerStoppedEvent.class);
            visitRegistryHook(FMLServerStartedEvent.class);
            visitRegistryHook(FMLServerStoppingEvent.class);
            visitRegistryHook(FMLServerStartingEvent.class);
            visitRegistryHook(FMLServerAboutToStartEvent.class);

            visitRegistryHook(FMLConstructionEvent.class);
            visitRegistryHook(FMLPreInitializationEvent.class);
            visitRegistryHook(FMLInitializationEvent.class);
            visitRegistryHook(FMLPostInitializationEvent.class);
            visitRegistryHook(FMLLoadCompleteEvent.class);
        }

        private void visitRegistryHook(Class<? extends FMLStateEvent> eventType) {
            MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC, "_loadHook_" + eventType.getSimpleName(),
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(eventType)), null, new String[0]);

            AnnotationVisitor av = mv.visitAnnotation(Type.getType(Mod.EventHandler.class).getDescriptor(), true);
            av.visitEnd();

            mv.visitCode();
            mv.visitLdcInsn(modClassName);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/lambdalib2/registry/impl/RegistryManager", "asm_RegistrationEvent",
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(FMLStateEvent.class)), false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitEnd();
        }
    }
}
