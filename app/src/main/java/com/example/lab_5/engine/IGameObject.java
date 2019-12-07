package com.example.lab_5.engine;

import android.graphics.Canvas;
import android.graphics.PointF;

public interface IGameObject
{
    public void draw(float[] vPMatrix);
    public void update();
    public PointF getPosition();
}
