package com.myimageuploader.uploaders;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.myimageuploader.R;
import com.myimageuploader.views.MyImageLayout;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class UploadingHelper implements OnImageUploadComplete{

    Activity activity;

    public static final String TAG = UploadingHelper.class.getSimpleName();

    private final static int PICK_IMAGE = 1;

    private String url = "";

    private String fileObjName = "uploaded_file";

    private MyImageLayout myImageLayout;

    private int numberOfImages = 0;

    private int widthOfImages = 75;

    public void setWidthOfImages(int widthOfImages) {
        this.widthOfImages = widthOfImages;
    }

    public void setFileObjName(String fileObjName) {
        this.fileObjName = fileObjName;
    }

    private HashMap<String,String> headers;

    Queue<Uri> queue = new LinkedList<>();

    public UploadingHelper(Activity activity,String url){
        this.activity = activity;
        this.url = url;
        myImageLayout = new MyImageLayout(activity.getBaseContext());
       /* Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        widthOfImages = width / 3 ;
        widthOfImages = (int) (widthOfImages / activity.getResources().getDisplayMetrics().density);
        widthOfImages = widthOfImages - (getPixelsFromDP(5) *2);*/
    }


    public void startActivityForImagePick(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    public void setResult(int resultCode,int requestCode,Intent intent){
        if(resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE){
            if(intent.getClipData() !=null){
                myImageLayout.removeAllViews();
                numberOfImages = intent.getClipData().getItemCount();
                for (int i = 0; i <numberOfImages;i++){
                    try {
                        Bitmap bitmap = getBitmapFromUri(intent.getClipData().getItemAt(i).getUri());
                        Bitmap tmpBitmap = Bitmap.createScaledBitmap(bitmap, getPixelsFromDP(widthOfImages), getPixelsFromDP(widthOfImages), false);

                        View v = LayoutInflater.from(activity.getBaseContext()).inflate(R.layout.item_image, myImageLayout, false);
                        v.setTag("ImageView" + i);
                        ImageView iv = (ImageView) v.findViewById(R.id.iv_uploadingImage);
                        iv.setImageBitmap(tmpBitmap);

                        myImageLayout.addView(v);
                        if(i==0){
                            ImageUploader imageUploader;
                            if(headers !=null){
                                imageUploader = new ImageUploader(url,bitmap,headers);
                            }else{
                                imageUploader = new ImageUploader(url,bitmap);
                            }

                            imageUploader.setFileObjName(fileObjName);
                            imageUploader.uploadImage(this);
                        }else{
                            queue.add(intent.getClipData().getItemAt(i).getUri());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if(intent.getData() !=null){
                try {
                    numberOfImages = 1;
                    Bitmap bitmap = getBitmapFromUri(intent.getData());
                    Bitmap tmpBitmap = Bitmap.createScaledBitmap(bitmap, getPixelsFromDP(60), getPixelsFromDP(60), false);

                    View v = LayoutInflater.from(activity.getBaseContext()).inflate(R.layout.item_image, myImageLayout, false);
                    ImageView iv = (ImageView) v.findViewById(R.id.iv_uploadingImage);
                    v.setTag("ImageView"+0);
                    iv.setImageBitmap(tmpBitmap);

                    myImageLayout.addView(v);
                    ImageUploader imageUploader = new ImageUploader(url,bitmap);
                    imageUploader.setFileObjName(fileObjName);
                    imageUploader.uploadImage(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {

        ParcelFileDescriptor parcelFileDescriptor = activity.getContentResolver().openFileDescriptor(uri, "r");
        try {
            FileDescriptor fileDescriptor;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                return image;
            }
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void OnUploadComplate(int msg, String data) {
        View v = myImageLayout.findViewWithTag("ImageView"+(numberOfImages - queue.size()-1));
            if(v !=null){
                if(msg != 1){
                    ((ImageView)v.findViewById(R.id.iv_ok)).setImageResource(R.drawable.not_ok);
                }
                v.findViewById(R.id.iv_ok).setVisibility(View.VISIBLE);
                v.findViewById(R.id.pb_uploading).setVisibility(View.GONE);
            }

        if(!queue.isEmpty()){
            try {
                Bitmap bitmap = getBitmapFromUri(queue.poll());
                ImageUploader imageUploader;
                if(headers !=null){
                    imageUploader = new ImageUploader(url,bitmap,headers);
                }else{
                    imageUploader = new ImageUploader(url,bitmap);
                }
                imageUploader.setFileObjName(fileObjName);

                imageUploader.uploadImage(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public  int getPixelsFromDP(float dp)
    {
        Resources r = activity.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public MyImageLayout getLayout() {
        return myImageLayout;

    }

}
