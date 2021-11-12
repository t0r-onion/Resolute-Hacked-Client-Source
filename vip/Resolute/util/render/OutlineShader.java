package vip.Resolute.util.render;

import org.lwjgl.opengl.GL20;
public final class OutlineShader extends FramebufferShader {
    public static final OutlineShader OUTLINE_SHADER = new OutlineShader();

    public OutlineShader() {
        super("outline.frag");
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
        GL20.glUniform4f(getUniform("color"), this.red, this.green, this.blue, this.alpha);
        GL20.glUniform1f(getUniform("radius"), this.radius);
    }
}
