package com.example.lab_5.engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.lab_5.settings.EnvironmentSettings;
import com.example.lab_5.verlet.VerletPoint;
import com.example.lab_5.verlet.VerletSquare;

public class GameView extends GLSurfaceView {

    private GameRenderer renderer;

    public static float left;
    public static float right;
    public static float top;
    public static float bottom;

    public GameView(Context context){
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init(context);
    }

    private void init(Context context)
    {
        setEGLContextClientVersion(2);

        renderer = new GameRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        // Render the view only when there is a change in the drawing data
    }

    public void clearView()
    {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.clearGO();
            }
        });
    }


    public boolean onTouchEvent(MotionEvent event) {

        final float x = (event.getRawX()-getWidth()/2)*renderer.getRatioX();
        final float y = -(event.getY()-getHeight()/2)*renderer.getRatioY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:

                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        renderer.setPreviewItem(EnvironmentSettings.SPAWN_BOX ?new VerletSquare((int)x,(int)y):new VerletPoint((int)x,(int)y));
                    }
                });

                break;
//            case MotionEvent.ACTION_MOVE:
//                //lineRenderer.setPoints(previewItem.getPosition(),new PointF(event.getX(),event.getY()));
//                break;
            case MotionEvent.ACTION_UP:
                if(EnvironmentSettings.SPAWN_BOX)
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.getGameObjects().add(new VerletSquare((int)renderer.getPreviewItem().getPosition().x,(int)renderer.getPreviewItem().getPosition().y,
                                    (int)x,(int)y));

                            renderer.setPreviewItem(null);
                        }
                    });
                else
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        renderer.getGameObjects().add(new VerletPoint((int)renderer.getPreviewItem().getPosition().x,(int)renderer.getPreviewItem().getPosition().y,
                                (int)x,(int)y));

                        renderer.setPreviewItem(null);
                    }
                });

                break;
        }
        return true;
    }
}
