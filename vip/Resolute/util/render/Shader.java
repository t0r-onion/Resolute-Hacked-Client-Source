package vip.Resolute.util.render;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class Shader {
    protected Minecraft mc;

    private int program;

    private Map<String, Integer> uniformsMap;

    public Shader(String fragmentShader) {
        int vertexShaderID, fragmentShaderID;
        Minecraft mc = Minecraft.getMinecraft();
        this.mc = mc;
        try {
            InputStream vertexStream = getClass().getResourceAsStream("/assets/minecraft/resolute/shader/vertex.vert");
            vertexShaderID = createShader(IOUtils.toString(vertexStream), 35633);
            IOUtils.closeQuietly(vertexStream);
            InputStream fragmentStream = getClass().getResourceAsStream("/assets/minecraft/resolute/shader/" + fragmentShader);
            fragmentShaderID = createShader(IOUtils.toString(fragmentStream), 35632);
            IOUtils.closeQuietly(fragmentStream);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (vertexShaderID == 0 || fragmentShaderID == 0)
            return;
        this.program = ARBShaderObjects.glCreateProgramObjectARB();
        if (this.program == 0)
            return;
        ARBShaderObjects.glAttachObjectARB(this.program, vertexShaderID);
        ARBShaderObjects.glAttachObjectARB(this.program, fragmentShaderID);
        ARBShaderObjects.glLinkProgramARB(this.program);
        ARBShaderObjects.glValidateProgramARB(this.program);
        System.out.println("[Shader] Successfully loaded: " + fragmentShader);
    }

    public void startShader() {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<>();
            setupUniforms();
        }
        updateUniforms();
    }

    public void stopShader() {
        GL20.glUseProgram(0);
        GL11.glPopMatrix();
    }

    public abstract void setupUniforms();

    public abstract void updateUniforms();

    private int createShader(String shaderSource, int shaderType) {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
            if (shader == 0)
                return 0;
            ARBShaderObjects.glShaderSourceARB(shader, shaderSource);
            ARBShaderObjects.glCompileShaderARB(shader);
            if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0)
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));
            return shader;
        } catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw e;
        }
    }

    private String getLogInfo(int i) {
        return ARBShaderObjects.glGetInfoLogARB(i, ARBShaderObjects.glGetObjectParameteriARB(i, 35716));
    }

    public void setUniform(String uniformName, int location) {
        this.uniformsMap.put(uniformName, Integer.valueOf(location));
    }

    public void setupUniform(String uniformName) {
        setUniform(uniformName, GL20.glGetUniformLocation(this.program, uniformName));
    }

    public int getUniform(String uniformName) {
        return ((Integer)this.uniformsMap.get(uniformName)).intValue();
    }
}
