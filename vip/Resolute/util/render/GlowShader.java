package vip.Resolute.util.render;

import org.lwjgl.opengl.GL20;

public final class GlowShader extends FramebufferShader {
    public static final GlowShader GLOW_SHADER = new GlowShader();

    public GlowShader() {
        super("glow.frag");
    }

    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("color");
        setupUniform("divider");
        setupUniform("radius");
        setupUniform("maxSample");
    }

    public void updateUniforms() {
        GL20.glUniform1i(getUniform("texture"), 0);
        GL20.glUniform2f(getUniform("texelSize"), 1.0F / this.mc.displayWidth * this.radius * this.quality, 1.0F / this.mc.displayHeight * this.radius * this.quality);
        GL20.glUniform3f(getUniform("color"), this.red, this.green, this.blue);
        GL20.glUniform1f(getUniform("divider"), 140.0F);
        GL20.glUniform1f(getUniform("radius"), this.radius);
        GL20.glUniform1f(getUniform("maxSample"), 10.0F);
    }
}
