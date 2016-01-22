package com.myimageuploader.uploaders;

import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.myimageuploader.volley.MultipartRequest;
import com.myimageuploader.volley.VolleySingleton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;

public class ImageUploader {

    private String url;

    private String fileName="tempImage";

    private Bitmap bitmap;

    private String fileObjName = "uploaded_file";

    public void setFileObjName(String fileObjName) {
        this.fileObjName = fileObjName;
    }

    private HashMap<String,String> headers = new HashMap<>();

    public ImageUploader(String url,Bitmap bitmap){
        this.url = url;
        this.bitmap = bitmap;
    }

    public ImageUploader(String url,Bitmap bitmap,HashMap<String,String> headers){
        this.url = url;
        this.bitmap = bitmap;
        this.headers = headers;
    }

    public void uploadImage(final OnImageUploadComplete onImageUploadComplete){

        final String twoHyphens = "--";
        final String lineEnd = "\r\n";
        final String boundary = "apiclient-" + System.currentTimeMillis();
        final String mimeType = "multipart/form-data;boundary=" + boundary;
        byte[] multipartBody;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] fileData = byteArrayOutputStream.toByteArray();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\""+fileObjName+"\"; filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
            int bytesAvailable = fileInputStream.available();

            int maxBufferSize = 1024 * 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            multipartBody = bos.toByteArray();

            MultipartRequest multipartRequest = new MultipartRequest(url,headers,mimeType,multipartBody,new Response.Listener<NetworkResponse>(){

                @Override
                public void onResponse(NetworkResponse networkResponse) {
                    if(onImageUploadComplete !=null){
                        Log.e("mudit", new String(networkResponse.data));
                        onImageUploadComplete.OnUploadComplate(1,new String(networkResponse.data));
                    }
                }
            },new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(onImageUploadComplete !=null){
                        onImageUploadComplete.OnUploadComplate(0,"");
                    }
                }
            });

            VolleySingleton.getInstance().addToRequestQueue(multipartRequest);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
