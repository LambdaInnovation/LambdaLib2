package cn.lambdalib2.test;

import cn.lambdalib2.s11n.SerializeType;
import cn.lambdalib2.s11n.nbt.NBTS11n;
import cn.lambdalib2.s11n.network.NetworkS11n;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.lambdalib2.util.RandUtils.*;

/**
 * A test benchmark for serialization methods.
 */
public class S11nBenchmark {

    public enum TestEnum {
        FAT, THIN, DIRTY
    }

    @SerializeType
    static class TestInner {
        public int a, b, c;
        public String msg;
        public TestEnum charc;

        public TestInner() {
            a = nextInt(100);
            b = nextInt(100);
            c = nextInt(100);
            msg = String.valueOf(nextDouble());
            charc = TestEnum.values()[nextInt(TestEnum.values().length)];
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("msg", msg)
                    .add("a", a)
                    .add("b", b)
                    .add("c", c)
                    .add("charc", charc)
                    .toString();
        }
    }

    static class TestOuter {
        public float x, y, z;
        public TestInner inner = new TestInner();
        public Map map = new HashMap<>();
        public int[] array = new int[] { 1, 2, 3 };
        public List<Object> objects = new ArrayList<>();

        public TestOuter() {
            x = nextFloat();
            y = nextFloat();
            z = nextFloat();

            map.put(nextFloat(), 233);
            map.put("aaa", nextFloat());
            map.put("bbb", nextDouble());

            objects.add("aaa");
            objects.add("bbb");

            objects.add(nextFloat());
            objects.add(nextFloat());
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("inner", inner)
                    .add("x", x)
                    .add("y", y)
                    .add("z", z)
                    .add("map", map)
                    .add("objects", objects)
                    .toString();
        }
    }

    public static void main(String[] args) {
        benchmark();
    }

    private static void benchmark() {
        for (int i = 0; i < 100000; ++i) { //Let code JIT Compile
            testBytebuf();
        }
        System.out.println("Warm complete");

        int objects = 1000000;
        long cur = System.currentTimeMillis();

        for (int i = 0; i < objects; ++i) {
            testBytebuf();
        }

        long dt = System.currentTimeMillis() - cur;

        System.out.println("Spent " + dt + " ms serializing " + objects + " objects");
    }

    // Allocation should be no part of serialization, so src and target are created in advance
    private static TestOuter sample = new TestOuter();
    private static TestOuter target = new TestOuter();

    private static void testNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTS11n.write(tag, sample);
        NBTS11n.read(tag, target);

        // System.out.println("In: " + data);
        // System.out.println("Out: " + readed);
    }

    private static void testBytebuf() {
        ByteBuf buffer = Unpooled.buffer();
        NetworkS11n.serializeWithHint(buffer, sample, TestOuter.class);
        NetworkS11n.deserializeRecursivelyInto(buffer, target, TestOuter.class);
    }

}
