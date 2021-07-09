package com.example.f_fcsr_sc;



import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Explorer2 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer2);
        findViewById(R.id.bt1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openSystemFile();
            }

        });

    }

    
    public void openSystemFile(){
    	   Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
           intent.setType("*/*");//设置类型，这里是任意类型，任意后缀的可以这样写
           intent.addCategory(Intent.CATEGORY_OPENABLE);
           try{
        	   startActivityForResult(intent,1);
           }catch(Exception e){
               Toast.makeText(this, "没有正确打开文件管理器", 1).show();
           }
    }
      
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
            File file = new File(img_path);
            Toast.makeText(Explorer2.this, file.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}