package com.just4fun.skrrrrrrrrr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Version extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private TextView twVersion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version);
        twVersion = (TextView)findViewById(R.id.tvVersion);
        twVersion.setText("Ver "+getResources().getString(R.string.ver));
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("danmuji").document("S72Ku2SW44UBy8k2kWAd").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String version = documentSnapshot.getString("version");
                    String changelog = documentSnapshot.getString("Log");
                    Log.v("UpdateManager",version);
                    Log.v("UpdateManager1",changelog);
                    if(!version.equals(getResources().getString(R.string.ver))){
                    new AlertDialog.Builder(Version.this)
                            .setTitle("有可用更新-版本: "+version)
                            .setMessage("更新日志: \n"+changelog)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = "https://github.com/TroyTheGod/DanMuGay/releases";
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    startActivity(i);
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    }
                }else{
                    Log.v("Updatemanager:Firelog","ERROR"+task.getException().getMessage());
                    new AlertDialog.Builder(Version.this)
                            .setTitle("檢查更新失敗")
                            .setMessage("自己上Githu看")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });



    }
}
