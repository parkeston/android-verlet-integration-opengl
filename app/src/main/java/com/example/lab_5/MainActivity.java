package com.example.lab_5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.lab_5.engine.GameView;
import com.example.lab_5.engine.IGameManager;
import com.example.lab_5.settings.SettingsDialog;

public class MainActivity extends  AppCompatActivity implements IGameManager {

    private GameView gameView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        //gLView = new GameView(this);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);


        View settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager manager = getSupportFragmentManager();
                SettingsDialog settingsDialog = new SettingsDialog();
                settingsDialog.show(manager,"dialog");
            }
        });

    }

    @Override
    public void onClearView() {
        gameView.clearView();
    }
}
