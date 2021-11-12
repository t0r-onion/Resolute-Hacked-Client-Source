package vip.Resolute.modules.impl.combat;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventMove;
import vip.Resolute.events.impl.EventRender3D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ColorSetting;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.render.RenderUtils;
import vip.Resolute.util.misc.TimerUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

public class TargetStrafe extends Module {
    public static ModeSetting mode = new ModeSetting("Mode", "Dynamic", "Dynamic");
    public static NumberSetting range = new NumberSetting("Range", 2.0, 0.5, 5.0, 0.1);
    public static BooleanSetting onSpace = new BooleanSetting("On Space", true);
    public static BooleanSetting behind = new BooleanSetting("Behind", true);
    public BooleanSetting render = new BooleanSetting("Render", true);

    public NumberSetting points = new NumberSetting("Points", 40, 3, 40, 1);
    public NumberSetting width = new NumberSetting("Width", 1.0, 0.5, 10, 0.5);

    public static BooleanSetting onSpeed = new BooleanSetting("Only Speed", true);
    public BooleanSetting voidCheck = new BooleanSetting("Void Check", true);
    public BooleanSetting dots = new BooleanSetting("Dots", true);

    public ColorSetting colorValue = new ColorSetting("Active Color", new Color(-2147418368));
    public ColorSetting dormantcolorValue = new ColorSetting("Dormant Color", new Color(553648127));
    public ColorSetting invalidcolorValue = new ColorSetting("Invalid Color", new Color(553582592));

    public static int direction = -1;
    public static boolean enabled = false;
    private static EntityLivingBase currentTarget;
    private static List<Point3D> currentPoints;
    private static Point3D currentPoint;

    public static TargetStrafe instance;
    public TimerUtil timerUtil = new TimerUtil();

    public TargetStrafe() {
        super("TargetStrafe", 0, "Automatically strafes a target", Category.COMBAT);
        this.addSettings(mode, range, onSpace, behind, render, points, width, onSpeed, voidCheck, dots, colorValue, dormantcolorValue, invalidcolorValue);

        this.currentPoints = new ArrayList<Point3D>();
    }

    public void onEnable() {
        super.onEnable();
        enabled = true;
    }

    public void onDisable() {
        super.onDisable();
        enabled = false;
    }

    public void onEvent(Event e) {
        this.setSuffix("");

        if(e instanceof EventRender3D) {
            EventRender3D event = (EventRender3D) e;

            float partialTicks;
            final Iterator<Point3D> iterator;
            Point3D point;
            int color;
            double x;
            double y;
            double z;
            double pointSize;
            AxisAlignedBB bb;
            double renderX;
            double renderY;
            double renderZ;

            if (this.render.isEnabled() && KillAura.target != null && !dots.isEnabled()) {
                RenderUtils.drawRadius(KillAura.target, range.getValue(), event.getPartialTicks(), (int) points.getValue(), (float) width.getValue(), colorValue.getColor());
            } else if(this.render.isEnabled() && KillAura.target != null && dots.isEnabled()) {
                partialTicks = event.getPartialTicks();
                iterator = this.currentPoints.iterator();
                while (iterator.hasNext()) {
                    point = iterator.next();
                    if (this.currentPoint == point) {
                        color = this.colorValue.getColor();
                    }
                    else if (point.valid) {
                        color = this.dormantcolorValue.getColor();
                    }
                    else {
                        color = this.invalidcolorValue.getColor();
                    }
                    x = RenderUtils.interpolate(point.prevX, point.x, partialTicks);
                    y = RenderUtils.interpolate(point.prevY, point.y, partialTicks);
                    z = RenderUtils.interpolate(point.prevZ, point.z, partialTicks);
                    pointSize = 0.03;
                    bb = new AxisAlignedBB(x, y, z, x + pointSize, y + pointSize, z + pointSize);
                    RenderUtils.enableBlending();
                    RenderUtils.disableDepth();
                    RenderUtils.disableTexture2D();
                    RenderUtils.color(color);
                    renderX = RenderManager.renderPosX;
                    renderY = RenderManager.renderPosY;
                    renderZ = RenderManager.renderPosZ;
                    GL11.glTranslated(-renderX, -renderY, -renderZ);
                    RenderGlobal.func_181561_a(bb, false, true);
                    GL11.glTranslated(renderX, renderY, renderZ);
                    RenderUtils.disableBlending();
                    RenderUtils.enableDepth();
                    RenderUtils.enableTexture2D();
                }
            }
        }

        if(e instanceof EventMotion) {
            if(e.isPre()) {
                if (mc.thePlayer.isCollidedHorizontally || !isBlockUnder()) {
                    direction = (byte) -direction;
                    return;
                }

                if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                    direction = 1;
                    return;
                }

                if (mc.gameSettings.keyBindRight.isKeyDown())
                    direction = -1;

                if(KillAura.target != null) {
                    this.collectPoints(this.currentTarget = KillAura.target);
                    this.currentPoint = this.findOptimalPoint(KillAura.target, this.currentPoints);
                } else {
                    this.currentTarget = null;
                    this.currentPoint = null;
                }
            }
        }
    }

    public static TargetStrafe getInstance() {
        return instance;
    }

    public static void setSpeed(final EventMove event, final double speed) {
        MovementUtils.setTargetStrafeSpeed(event, speed, 1.0f, 0.0f, getYawToPoint(currentPoint));
    }

    private static float getYawToPoint(final Point3D point) {
        final EntityPlayerSP player = mc.thePlayer;
        final double xDist = point.x - player.posX;
        final double zDist = point.z - player.posZ;
        final float rotationYaw = player.rotationYaw;
        final float var1 = (float)(StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        return rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
    }

    private Point3D findOptimalPoint(final EntityLivingBase target, final List<Point3D> points) {
        if(behind.isEnabled()) {
            Point3D bestPoint = null;
            float biggestDif = -1.0f;
            for (final Point3D point : points) {
                if (point.valid) {
                    final float yawChange = Math.abs(this.getYawChangeToPoint(target, point));
                    if (yawChange <= biggestDif) {
                        continue;
                    }
                    biggestDif = yawChange;
                    bestPoint = point;
                }
            }
            return bestPoint;
        }

        return null;
    }

    private float getYawChangeToPoint(final EntityLivingBase target, final Point3D point) {
        final double xDist = point.x - target.posX;
        final double zDist = point.z - target.posZ;
        final float rotationYaw = target.rotationYaw;
        final float var1 = (float)(StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        return rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
    }

    private void collectPoints(final EntityLivingBase entity) {
        final int size = (int) this.points.getValue();
        final double radius = range.getValue();
        this.currentPoints.clear();
        final double x = entity.posX;
        final double y = entity.posY;
        final double z = entity.posZ;
        final double prevX = entity.prevPosX;
        final double prevY = entity.prevPosY;
        final double prevZ = entity.prevPosZ;
        for (int i = 0; i < size; ++i) {
            final double cos = radius * StrictMath.cos(i * 6.2831855f / size);
            final double sin = radius * StrictMath.sin(i * 6.2831855f / size);
            final double pointX = x + cos;
            final double pointZ = z + sin;
            final Point3D point = new Point3D(pointX, y, pointZ, prevX + cos, prevY, prevZ + sin, this.validatePoint(pointX, pointZ));
            this.currentPoints.add(point);
        }
    }

    private boolean validatePoint(final double x, final double z) {
        final Vec3 pointVec = new Vec3(x, mc.thePlayer.posY, z);
        final IBlockState blockState = mc.theWorld.getBlockState(new BlockPos(pointVec));
        final boolean canBeSeen = mc.theWorld.rayTraceBlocks(mc.thePlayer.getPositionVector(), pointVec, false, false, false) == null;
        return !this.isOverVoid(x, z) && !blockState.getBlock().canCollideCheck(blockState, false) && canBeSeen;
    }

    private boolean isOverVoid(final double x, final double z) {
        for (double posY = mc.thePlayer.posY; posY > 0.0; --posY) {
            if (!(mc.theWorld.getBlockState(new BlockPos(x, posY, z)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlockUnder() {
        for (int offset = 0; offset < mc.thePlayer.posY + mc.thePlayer.getEyeHeight(); offset += 2) {
            AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);

            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty())
                return true;
        }
        return false;
    }

    private static final class Point3D
    {
        private final double x;
        private final double y;
        private final double z;
        private final double prevX;
        private final double prevY;
        private final double prevZ;
        private final boolean valid;

        public Point3D(final double x, final double y, final double z, final double prevX, final double prevY, final double prevZ, final boolean valid) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.prevX = prevX;
            this.prevY = prevY;
            this.prevZ = prevZ;
            this.valid = valid;
        }
    }
}


