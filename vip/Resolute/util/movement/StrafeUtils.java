package vip.Resolute.util.movement;

import vip.Resolute.events.impl.StrafeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class StrafeUtils {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static void customSilentMoveFlying(StrafeEvent event, float yaw) {
        float d;
        int dif = (int)((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 23.5f - 135.0f) + 180.0f) / 45.0f);
        float strafe = event.getStrafe();
        float forward = event.getForward();
        float friction = event.getFriction();
        float calcForward = 0.0f;
        float calcStrafe = 0.0f;
        switch (dif) {
            case 0: {
                calcForward = forward;
                calcStrafe = strafe;
                break;
            }
            case 1: {
                calcForward += forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe += strafe;
                break;
            }
            case 2: {
                calcForward = strafe;
                calcStrafe = -forward;
                break;
            }
            case 3: {
                calcForward -= forward;
                calcStrafe -= forward;
                calcForward += strafe;
                calcStrafe -= strafe;
                break;
            }
            case 4: {
                calcForward = -forward;
                calcStrafe = -strafe;
                break;
            }
            case 5: {
                calcForward -= forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe -= strafe;
                break;
            }
            case 6: {
                calcForward = -strafe;
                calcStrafe = forward;
                break;
            }
            case 7: {
                calcForward += forward;
                calcStrafe += forward;
                calcForward -= strafe;
                calcStrafe += strafe;
            }
        }
        if (calcForward > 1.0f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1.0f || calcForward > -0.9f && calcForward < -0.3f) {
            calcForward *= 0.5f;
        }
        if (calcStrafe > 1.0f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1.0f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
            calcStrafe *= 0.5f;
        }
        if ((d = calcStrafe * calcStrafe + calcForward * calcForward) >= 1.0E-4f) {
            if ((d = MathHelper.sqrt_float(d)) < 1.0f) {
                d = 1.0f;
            }
            d = friction / d;
            float yawSin = MathHelper.sin((float)((double)yaw * Math.PI / 180.0));
            float yawCos = MathHelper.cos((float)((double)yaw * Math.PI / 180.0));
            mc.thePlayer.motionX += (double)((calcStrafe *= d) * yawCos - (calcForward *= d) * yawSin);
            mc.thePlayer.motionZ += (double)(calcForward * yawCos + calcStrafe * yawSin);
        }
    }
}
