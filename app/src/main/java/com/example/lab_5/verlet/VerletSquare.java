package com.example.lab_5.verlet;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.lab_5.engine.GameRenderer;
import com.example.lab_5.engine.IGameObject;
import com.example.lab_5.settings.VisualSettings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VerletSquare implements IGameObject
{
    private PointF position;

    private VerletStick[] sticks;
    private VerletPoint[] points;

    private int size;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private int mProgram;


    private FloatBuffer vertexBuffer;
    private int vPMatrixHandle;
    private int positionHandle;
    private int colorHandle;

    private float[] modelMatrix = new float[16];
    private float[] mVPMatrix = new float[16];
    float color[] = { 1.0f, 0.76953125f, 0.22265625f, 1.0f };

    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private float[] squareCoords;

    public VerletSquare(int x, int y)
    {
        position = new PointF(x,y);

        points = new VerletPoint[4];
        sticks = new VerletStick[5];

        size = VisualSettings.BOX_SIZE;

        points[0] = new VerletPoint(x-size/2,y-size/2, position);
        points[1] = new VerletPoint(x-size/2,y+size/2, position);
        points[2] = new VerletPoint(x+size/2,y-size/2, position);
        points[3] = new VerletPoint(x+size/2,y+size/2, position);

        CreateSticks();
    }

    public VerletSquare(int x, int y, int xOld, int yOld)
    {
        position = new PointF(x,y);

        points = new VerletPoint[4];
        sticks = new VerletStick[5];

        size = VisualSettings.BOX_SIZE;

        points[0] = new VerletPoint(x-size/2,y-size/2,xOld-size/2,yOld-size/2, position);
        points[1] = new VerletPoint(x-size/2,y+size/2,xOld-size/2,yOld+size/2, position);
        points[2] = new VerletPoint(x+size/2,y-size/2,xOld+size/2,yOld-size/2, position);
        points[3] = new VerletPoint(x+size/2,y+size/2,xOld+size/2,yOld+size/2, position);

        CreateSticks();
    }

    private void CreateSticks()
    {
        sticks[0] = new VerletStick(points[0],points[1]);
        sticks[1] = new VerletStick(points[1],points[3]);
        sticks[2] = new VerletStick(points[3],points[2]);
        sticks[3] = new VerletStick(points[2],points[0]);
        sticks[4] = new VerletStick(points[0],points[3]);

        squareCoords = new float[]
                {
                        points[0].getPosition().x,points[0].getPosition().y,0,
                        points[2].getPosition().x,points[2].getPosition().y,0,
                        points[3].getPosition().x,points[3].getPosition().y,0,
                        points[1].getPosition().x,points[1].getPosition().y,0
                };

        initOpenGL();
    }


    public PointF getPosition() {
        return position;
    }


    @Override
    public void draw(float[] vPMatrix) {

        Matrix.multiplyMM(mVPMatrix,0,vPMatrix,0,modelMatrix,0);

        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mVPMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);

        for (VerletPoint point:points) {
            point.draw(vPMatrix);
        }

        for (VerletStick stick: sticks) {
            stick.draw(vPMatrix);
        }
    }

    @Override
    public void update() {

        for (VerletPoint point:points) {
            point.update();
        }

        for(int i = 0;i<3;i++) {
            for (VerletStick stick : sticks) {
                stick.update();
            }

            for (VerletPoint point:points) {
                point.ConstrainToScreen();
            }
        }

        float dx = points[0].getPosition().x + (points[3].getPosition().x - points[0].getPosition().x)/2 - position.x;
        float dy = points[0].getPosition().y + (points[3].getPosition().y - points[0].getPosition().y)/2 - position.y;

        position.x = points[0].getPosition().x + (points[3].getPosition().x - points[0].getPosition().x)/2;
        position.y = points[0].getPosition().y + (points[3].getPosition().y - points[0].getPosition().y)/2;

        Matrix.translateM(modelMatrix,0,dx,dy,0);
    }

    private void initOpenGL() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        int vertexShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        Matrix.setIdentityM(modelMatrix,0);
    }
}
