package cn.ll2test;

import cn.lambdalib2.render.mc.EntityRenderUtils;
import cn.lambdalib2.util.DebugDraw;
import cn.lambdalib2.util.GameTimer;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class TestDebugDraw {

    long beginTime = -1;

    @SubscribeEvent
    public void onDebugDraw(DebugDraw.DebugDrawEvent event) {
        if (beginTime == -1) {
            beginTime = GameTimer.getTime();
        }

        DebugDraw.setTransform(EntityRenderUtils.getPVPMatrix());
        DebugDraw.setColor(new Vector4f(1, 0.5f, 0.5f, 1f));

        float elapsed = (GameTimer.getTime() - beginTime) / 1000.0f;
        float x = 3 * MathHelper.cos(elapsed),
                z = 3 * MathHelper.sin(elapsed);
        float y = 2;

        DebugDraw.sphere(new Vector3f(x, y, z), 3);
    }

}
