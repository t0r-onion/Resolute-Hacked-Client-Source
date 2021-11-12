package vip.Resolute.util.render;

import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class FramebufferShader extends Shader {
    private static Framebuffer framebuffer;

    protected float red;

    protected float green;

    protected float blue;

    protected float alpha = 1.0F;

    protected float radius = 2.0F;

    protected float quality = 1.0F;

    private boolean entityShadows;

    public FramebufferShader(String fragmentShader) {
        super(fragmentShader);
    }

    public void startDraw(float partialTicks) {
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        framebuffer = setupFrameBuffer(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        this.entityShadows = this.mc.gameSettings.entityshadow;
        this.mc.gameSettings.entityshadow = false;
        this.mc.entityRenderer.setupCameraTransform(partialTicks, 0);
    }

    public void stopDraw(Color color, float radius, float quality) {
        this.mc.gameSettings.entityshadow = this.entityShadows;
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.red = color.getRed() / 255.0F;
        this.green = color.getGreen() / 255.0F;
        this.blue = color.getBlue() / 255.0F;
        this.alpha = color.getAlpha() / 255.0F;
        this.radius = radius;
        this.quality = quality;
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        startShader();
        this.mc.entityRenderer.setupOverlayRendering();
        drawFramebuffer(framebuffer);
        stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
        if (frameBuffer != null)
            frameBuffer.deleteFramebuffer();
        frameBuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
        return frameBuffer;
    }

    public void drawFramebuffer(Framebuffer framebuffer) {
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        GL11.glBindTexture(3553, framebuffer.framebufferTexture);
        GL11.glBegin(7);
        GL11.glTexCoord2d(0.0D, 1.0D);
        GL11.glVertex2d(0.0D, 0.0D);
        GL11.glTexCoord2d(0.0D, 0.0D);
        GL11.glVertex2d(0.0D, scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1.0D, 0.0D);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1.0D, 1.0D);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), 0.0D);
        GL11.glEnd();
        GL20.glUseProgram(0);
    }
}
