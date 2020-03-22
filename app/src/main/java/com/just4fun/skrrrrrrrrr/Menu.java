package com.just4fun.skrrrrrrrrr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {
    TextView twAuthor, twVersion, twCheckUpdates;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        twAuthor = findViewById(R.id.tvAuthor);
        twVersion = findViewById(R.id.tvVersion);
        twCheckUpdates = findViewById(R.id.tvCheckUpdate);
        twVersion.setText("Version: "+ getResources().getString(R.string.ver));
        twCheckUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this,Version.class));
            }
        });
        twAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://space.bilibili.com/212338734";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}
