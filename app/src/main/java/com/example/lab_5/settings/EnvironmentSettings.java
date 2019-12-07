package com.example.lab_5.settings;

import android.graphics.PointF;

public class EnvironmentSettings
{
    public static  int MAX_LAUNCH_SPEED = 20;
    public static PointF ACCELERATION  = new PointF(0,-9.81f);
    public static  float BOUNCE_FRICTION = 0.9f;
    public static  float DRAG = 0.999f;
    public static boolean SPAWN_BOX = true;
}
