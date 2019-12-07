package com.example.lab_5.engine;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.lab_5.verlet.VerletPoint;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GameRenderer implements GLSurfaceView.Renderer {

    private VerletPoint point;

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    private IGameObject previewItem;
    private ArrayList<IGameObject> gameObjects;

    private float ratioY;
    private float ratioX;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        gameObjects = new ArrayList<>();
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 1.1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);


        if(previewItem !=null)
            previewItem.draw(vPMatrix);

        //lineRenderer.render(canvas);

        for(IGameObject gameObject : gameObjects) {
            gameObject.update();
            gameObject.draw(vPMatrix);
        }
    }

    public void setPreviewItem(IGameObject previewItem) {
        this.previewItem = previewItem;
    }

    public IGameObject getPreviewItem() {
        return previewItem;
    }

    public ArrayList<IGameObject> getGameObjects() {
        return gameObjects;
    }

    public float getRatioY() {
        return ratioY;
    }

    public float getRatioX() {
        return ratioX;
    }

    public void clearGO()
    {
        gameObjects.clear();
    }


    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float baseSize = width>height?width:height;

        ratioY = 1.0f;
        ratioX = 1.0f;
        float left = -baseSize/2;
        float right = baseSize/2;
        float bottom = -baseSize/2;
        float top = baseSize/2;
        float near = 1.0f;
        float far = 8.0f;
        if (width > height) {
            ratioX = (float) width / height;
            left *= ratioX;
            right *= ratioX;
        } else {
            ratioY = (float) height / width;
            bottom *= ratioY;
            top *= ratioY;
        }

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, near, far);
        GameView.left = left;
        GameView.right = right;
        GameView.top = top;
        GameView.bottom = bottom;
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
