package vip.Resolute.util.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class Draw {

    public static void drawRoundedRect(double x, double y, double width, double height, float cornerRadius) {
        final int slices = 10;

        drawFillRectangle(x + cornerRadius, y, width - 2 * cornerRadius, height);
        drawFillRectangle(x, y + cornerRadius, cornerRadius, height - 2 * cornerRadius);
        drawFillRectangle(x + width - cornerRadius, y + cornerRadius, cornerRadius, height - 2 * cornerRadius);

        drawCirclePart(x + cornerRadius, y + cornerRadius, -MathHelper.PI, -MathHelper.PId2, cornerRadius, slices);
        drawCirclePart(x + cornerRadius, y + height - cornerRadius, -MathHelper.PId2, 0.0F, cornerRadius, slices);

        drawCirclePart(x + width - cornerRadius, y + cornerRadius, MathHelper.PId2, MathHelper.PI, cornerRadius, slices);
        drawCirclePart(x + width - cornerRadius, y + height - cornerRadius, 0, MathHelper.PId2, cornerRadius, slices);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GlStateManager.disableBlend();

        //GlStateManager.enableAlpha();
        //GlStateManager.alphaFunc(GL11.GL_NOTEQUAL, 0);
    }

    public static void drawHollowRect(float x, float y, float w, float h, int color) {

        Gui.drawHorizontalLine((int) x, (int)w, (int)y, color);
        Gui.drawHorizontalLine((int)x,  (int)w, (int)h, color);

        Gui.drawVerticalLine((int)x, (int)h, (int)y, color);
        Gui.drawVerticalLine((int)w,(int) h,(int) y, color);

    }

    public static void drawBorderedRectangle(double left, double top, double right, double bottom, double borderWidth, int insideColor, int borderColor, boolean borderIncludedInBounds) {
        drawRectangle(left - (!borderIncludedInBounds ? borderWidth : 0), top - (!borderIncludedInBounds ? borderWidth : 0), right + (!borderIncludedInBounds ? borderWidth : 0), bottom + (!borderIncludedInBounds ? borderWidth : 0), borderColor);
        drawRectangle(left + (borderIncludedInBounds ? borderWidth : 0), top + (borderIncludedInBounds ? borderWidth : 0), right - ((borderIncludedInBounds ? borderWidth : 0)), bottom - ((borderIncludedInBounds ? borderWidth : 0)), insideColor);
    }

    public static void drawRectangle(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void drawFillRectangle(double x, double y, double width, double height) {
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static void drawCirclePart(double x, double y, float fromAngle, float toAngle, float radius, int slices) {
        GlStateManager.enableBlend();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x, y);
        final float increment = (toAngle - fromAngle) / slices;

        for (int i = 0; i <= slices; i++) {
            final float angle = fromAngle + i * increment;

            final float dX = MathHelper.sin(angle);
            final float dY = MathHelper.cos(angle);

            GL11.glVertex2d(x + dX * radius, y + dY * radius);
        }
        GL11.glEnd();
    }

    public static void color(int color) {
        float red = (color & 255) / 255f,
                green = (color >> 8 & 255) / 255f,
                blue = (color >> 16 & 255) / 255f,
                alpha = (color >> 24 & 255) / 255f;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void colorRGBA(int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);
    }

}
