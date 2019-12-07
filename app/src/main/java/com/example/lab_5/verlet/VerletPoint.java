package com.example.lab_5.verlet;

import android.graphics.Paint;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.lab_5.engine.GameRenderer;
import com.example.lab_5.engine.GameView;
import com.example.lab_5.engine.IGameObject;
import com.example.lab_5.utils.Time;
import com.example.lab_5.settings.EnvironmentSettings;
import com.example.lab_5.settings.VisualSettings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class VerletPoint implements IGameObject {
    private PointF position;
    private PointF oldPosition;
    private Paint paint;
    private PointF rotationCenter;
    //


    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "uniform lowp float pSize;"+
                    "void main() {" +
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    "gl_PointSize = pSize;"+
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
    private int pointSizeHandle;
    private int colorHandle;

    private float[] modelMatrix = new float[16];
    private float[] mVPMatrix = new float[16];
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };



    // number of coordinates per vertex in this array


    public VerletPoint(int x, int y) {
        position = new PointF();
        position.x = x;
        position.y = y;
        oldPosition = new PointF(position.x, position.y);
        rotationCenter = null;

        paint = new Paint();
        paint.setColor(VisualSettings.POINT_COLOR);

        initOpenGL();
    }

    public VerletPoint(int x, int y, PointF rotationCenter) {
        this(x, y);
        this.rotationCenter = rotationCenter;
    }

    public VerletPoint(int x, int y, int xOld, int yOld) {
        this(x, y);

        double magnitude = Math.sqrt((x - xOld) * (x - xOld) + (y - yOld) * (y - yOld));

        xOld = x + (int) (((x - xOld) / magnitude) * EnvironmentSettings.MAX_LAUNCH_SPEED);
        yOld = y + (int) (((y - yOld) / magnitude) * EnvironmentSettings.MAX_LAUNCH_SPEED);

        oldPosition.x = xOld;
        oldPosition.y = yOld;
    }

    public VerletPoint(int x, int y, int xOld, int yOld, PointF rotationCenter)
    {
        this(x,y,xOld,yOld);
        this.rotationCenter = rotationCenter;
    }

    @Override
    public void draw(float[] vPMatrix) {

        Matrix.translateM(modelMatrix,0,position.x-oldPosition.x,position.y-oldPosition.y,0);
        Matrix.multiplyMM(mVPMatrix,0,vPMatrix,0,modelMatrix,0);

        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                3*4, vertexBuffer);

        pointSizeHandle = GLES20.glGetUniformLocation(mProgram,"pSize");
        GLES20.glUniform1f(pointSizeHandle,VisualSettings.POINT_SIZE);

        // get handle to fragment shader's vColor member
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mVPMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    @Override
    public void update()
    {
        float vx = (position.x - oldPosition.x ) * EnvironmentSettings.DRAG;
        float vy = (position.y - oldPosition.y) * EnvironmentSettings.DRAG;

        //2*pos - oldPos + adt^2 = pos + pos - olpod + adt^2

        oldPosition.x = position.x;
        oldPosition.y = position.y;

        position.offset(vx + EnvironmentSettings.ACCELERATION.x * Time.deltaTime, vy + EnvironmentSettings.ACCELERATION.y * Time.deltaTime);
        ConstrainToScreen();
    }

    @Override
    public PointF getPosition() {
        return position;
    }

    public void ConstrainToScreen() {
        float vx = (position.x - oldPosition.x) * EnvironmentSettings.DRAG;
        float vy = (position.y - oldPosition.y) * EnvironmentSettings.DRAG;

        float halfSize = VisualSettings.POINT_SIZE / 2f;
        float bounceFriction = EnvironmentSettings.BOUNCE_FRICTION;

        if (position.x >= GameView.right - halfSize) {
            position.x = GameView.right - halfSize;
            oldPosition.x = position.x + vx * bounceFriction;
        } else if (position.x <= GameView.left+halfSize) {
            position.x = GameView.left+halfSize;
            oldPosition.x = position.x + vx * bounceFriction;
        }

        if (position.y >= GameView.top - halfSize) {
            position.y = GameView.top - halfSize;
            oldPosition.y = position.y + vy * bounceFriction;
        } else if (position.y <= GameView.bottom+halfSize) {
            position.y = GameView.bottom+halfSize;
            oldPosition.y = position.y + vy * bounceFriction;
        }
    }

    private void initOpenGL() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                3 * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(new float[]{position.x,position.y,0});
        // set the buffer to read the first coordinate
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
