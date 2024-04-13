package my.edu.utar.call_group;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 2000;
    private static final int IMAGE_DELAY = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ImageView in your layout
        ImageView imageView = findViewById(R.id.imageViewLogo);

        // Delayed task to show the image after the specified delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Set the visibility of the ImageView to VISIBLE
                imageView.setVisibility(View.VISIBLE);
            }
        }, IMAGE_DELAY);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}