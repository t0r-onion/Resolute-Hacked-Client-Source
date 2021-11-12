package vip.Resolute.util.world;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;

public class LocationUtils {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public LocationUtils(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public LocationUtils(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0.0F;
        this.pitch = 0.0F;
    }

    public LocationUtils(BlockPos pos) {
        this.x = (double)pos.getX();
        this.y = (double)pos.getY();
        this.z = (double)pos.getZ();
        this.yaw = 0.0F;
        this.pitch = 0.0F;
    }

    public LocationUtils(int x, int y, int z) {
        this.x = (double)x;
        this.y = (double)y;
        this.z = (double)z;
        this.yaw = 0.0F;
        this.pitch = 0.0F;
    }

    public LocationUtils(EntityLivingBase entity) {
        this.x = entity.posX;
        this.y = entity.posY;
        this.z = entity.posZ;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
    }


    public LocationUtils add(int x, int y, int z) {
        this.x += (double)x;
        this.y += (double)y;
        this.z += (double)z;
        return this;
    }

    public LocationUtils add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public LocationUtils subtract(int x, int y, int z) {
        this.x -= (double)x;
        this.y -= (double)y;
        this.z -= (double)z;
        return this;
    }

    public LocationUtils subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Block getBlock() {
        return Minecraft.getMinecraft().theWorld.getBlockState(this.toBlockPos()).getBlock();
    }

    public double getX() {
        return this.x;
    }

    public LocationUtils setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return this.y;
    }

    public LocationUtils setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return this.z;
    }

    public LocationUtils setZ(double z) {
        this.z = z;
        return this;
    }

    public float getYaw() {
        return this.yaw;
    }

    public LocationUtils setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public float getPitch() {
        return this.pitch;
    }

    public LocationUtils setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public static LocationUtils fromBlockPos(BlockPos blockPos) {
        return new LocationUtils(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.getX(), this.getY(), this.getZ());
    }

    public double distanceTo(LocationUtils loc) {
        double dx = loc.x - this.x;
        double dz = loc.z - this.z;
        double dy = loc.y - this.y;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double distanceToY(LocationUtils loc) {
        double dy = loc.y - this.y;
        return Math.sqrt(dy * dy);
    }
}
