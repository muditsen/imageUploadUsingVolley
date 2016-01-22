package com.myimageuploader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.myimageuploader.uploaders.UploadingHelper;
import com.myimageuploader.volley.VolleySingleton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    RelativeLayout linearLayout;
    UploadingHelper uploadingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VolleySingleton.getInstance().init(getApplicationContext());
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_clickMe).setOnClickListener(this);
        linearLayout = (RelativeLayout) findViewById(R.id.rl_mainImage);
    }

    @Override
    public void onClick(View v) {
        String url = "http://192.168.27.204/qouteMaker/image_upload.php";
        uploadingHelper = new UploadingHelper(this,url);
        uploadingHelper.startActivityForImagePick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uploadingHelper.setResult(resultCode,requestCode,data);
        linearLayout.removeAllViews();
        linearLayout.addView(uploadingHelper.getLayout());
    }

}
