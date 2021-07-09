package com.example.f_fcsr_sc;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Scanner;

import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.PrivateCredentialPermission;

import org.apache.http.util.EncodingUtils;
import org.w3c.dom.Text;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.R.bool;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.text.StaticLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static String filePath = null; 
    private String filePath0 = null; 
	private String key=new String("");
	private String iv_short=new String("");  
	private String iv1=new String("");
	private String iv2=new String("");
	private String d_bin=new String("");
	private String spring=new String("");

	private String ciphertext=new String("");
	
	 
	private BigInteger key_int;
	private BigInteger iv_int;
	private BigInteger d_int;
	private BigInteger filter;
	private BigInteger main_reg;
	private BigInteger carry_reg;
	private BigInteger spring_int;
	private BigInteger main_reg_init;
	private BigInteger cipher_int;

	int counting;
	
	BigInteger main_reg_up;
	BigInteger carry_reg_up;
	BigInteger m0d;
	
	private int[][] filter_split=new int[16][8];
	private int[][] spring_split=new int[16][8];
	
	private Handler mHandler = new Handler();
	
	String s0=new String("0");
	String s1=new String("1");

	boolean done;
	byte[] bufferPlain;
	byte[] keystream;
	int testlen;
	int keystreamlen;


	private EditText edt_key;
	private EditText edt_iv;
	private Button btn_run;
	private Button btn_save;
	private EditText edt_sc;
	private TextView tv_chooseRadix;
	private EditText edt_plaintext;
	private Button btn_result;
	private ProgressBar pgb_running;
	private Button btn_file;
	private String fileName;
	private String fileNameProblem;

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && data != null) {
            try {
            	fileName=data.getData().toString();
            	fileNameProblem=data.getDataString();
            	fileName=fileName.substring(fileName.lastIndexOf("/")+1);
                bufferPlain=readBytes(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
		
	public byte[] readBytes(Uri inUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(inUri);
        int bufferSize=inputStream.available();
        byte[] buffer = new byte[bufferSize];
        testlen=bufferSize;

        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
      

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
          byteBuffer.write(buffer, 0, len);
          Log.i("welhzh_f", "" + len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
      }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btn_run=(Button)findViewById(R.id.btn_run);
        btn_save=(Button)findViewById(R.id.btn_save);
        edt_key=(EditText)findViewById(R.id.edt_key);
        edt_iv=(EditText)findViewById(R.id.edt_iv);
        edt_sc=(EditText)findViewById(R.id.edt_sc);
        edt_plaintext=(EditText)findViewById(R.id.edt_plaintext);
        btn_file=(Button)findViewById(R.id.btn_file);
        tv_chooseRadix=(TextView)findViewById(R.id.tv_radix);
        btn_result=(Button)findViewById(R.id.btn_result);
        pgb_running=(ProgressBar)findViewById(R.id.pgb_running);
        
        tv_chooseRadix.setTextSize(18); 
        btn_file.setVisibility(View.VISIBLE);//设置默认显示  
        pgb_running.setVisibility(View.GONE);
 
        //d的值存进BigInteger实例
        d_int=new BigInteger("B9C6A9EAB7E25FD69E86369A1856EC4A",16);
   
       
        //选择存文件按钮
        btn_file.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				// TODO Auto-generated method stub		
				Intent intent = new Intent();
	            intent = new Intent(Intent.ACTION_GET_CONTENT);
	            intent.setType("*/*"); 		//设置类型，我这里是任意类型，任意后缀的可以这样写。
	            intent.addCategory(Intent.CATEGORY_OPENABLE);   // 只有设置了这个，返回的uri才能使用 getContentResolver().openInputStream(uri) 打开。
	            startActivityForResult(intent, 1);			
		
			}
		});
        
        //启动按钮监听
       btn_run.setOnClickListener(new View.OnClickListener() {
    	   
        		
			public void onClick(View v) {
	//			iv_short="1101111101011101010111101010010101001010101010010101111010100101010010101010100";
	//			key="10101111010100101010010101010100"; 		
		 		byte[] keyAll = new byte[1024];
		 		byte[] ivAll = new byte[1024];
		 		try {
					keyAll=edt_key.getText().toString().getBytes("GBK");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		 		try {
					ivAll=edt_iv.getText().toString().getBytes("GBK");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
		 		byte test=1;
		 		for(int count=0;count<keyAll.length&&count<16;count++){
		 			for(int pos=0;pos<8;pos++){
		 				if((keyAll[count]>>pos&test)==test) key=s1.concat(key);
		 				else key=s0.concat(key);
		 			}
		 		}
		 		
		 		for(int count=0;count<ivAll.length&&count<16;count++){
		 			for(int pos=0;pos<8;pos++){
		 				if((ivAll[count]>>pos&test)==test) iv_short=s1.concat(iv_short);
		 				else iv_short=s0.concat(iv_short);
		 			}
		 		}
		 		
		 		while(key.length()<128)
		 			key=s1.concat(key);
		 		while(iv_short.length()<128)
		 			iv_short=s0.concat(iv_short);
				
		
		 		key_int=new BigInteger(key,2);
		 		iv_int=new BigInteger(iv_short,2);
		 		counting=0;
		 		
	 		
			//控制进度条显示
				new Thread(new Runnable() {
			         public void run() {
			            while(counting<testlen){
			                 // Update the progress bar
			                 mHandler.post(new Runnable() {
			                     public void run() {
			                    //     pgb_running.setProgress(0);
			                        	pgb_running.setVisibility(View.VISIBLE);
		             			       	try {
											edt_sc.setText(new String(bufferPlain,"GBK"));
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}			                     
			                     }
			                 });
			             
			            }
						if(counting>=testlen){
							 mHandler.post(new Runnable() {
			                     public void run() {
			                    //     pgb_running.setProgress(0);
			                        	pgb_running.setVisibility(View.GONE);             					                     
			                     }
			                 });
						}
			         }
			     }).start();
	
				//数据处理
				
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        	//step 1
            				main_reg=key_int;
            				if(goodFilter(main_reg)==false){
            					carry_reg=BigInteger.valueOf(0);
            					clock(6); 
            				}
            				filter=main_reg; //获得过滤函数
            				carry_reg=BigInteger.valueOf(0);
            				clock(6);
            				main_reg_init=main_reg; //获得Minit
            	
            				
            //step 2
            				main_reg=main_reg_init;
            				carry_reg=BigInteger.valueOf(0);
            				if(iv_prepare()==1){
            					carry_reg=carry_extension(iv1);
            					clock(64);
            				}
            				if(iv_prepare()==2){
            					carry_reg=carry_extension(iv1);
            					clock(64);
            					carry_reg=carry_extension(iv2);
            					clock(64);
            				}
            				main_reg=main_reg_init;
            //step3
            				int s_temp;
            				int row,column,column_sum=0;
            				byte a=(byte) 11111111;
            				
            				//由于一次输出8位，密钥流output的长度可能大于明文plaintext
            				for(counting=0;counting<testlen;counting++){
            					clock(1);
            					spring_int=main_reg.and(filter);
            					spring=new String(spring_int.toString(2));
            					for(s_temp=0;s_temp<128&&s_temp<spring.length();s_temp++){	//spring保存在12*8矩阵中
            			    		if(spring.charAt(s_temp)=='0')
            			    			spring_split[s_temp/8][s_temp%8]=0;
            			    		else spring_split[s_temp/8][s_temp%8]=1;	
            			    	}
            					while(s_temp<128){
            						spring_split[s_temp/8][s_temp%8]=0;
            						s_temp++;
            					}
            					
            					for(column=0;column<8;column++){
            						column_sum=0;
            						for(row=0;row<16;row++){
            							column_sum+=spring_split[row][column];
            						}
            						if(column_sum%2==0) a=(byte) (a^(1<<column));			
            					}
            					bufferPlain[counting]=(byte) (bufferPlain[counting]^a);
            					
            				}           								
        			      
        	              		
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
				
				 			

			//	m(t +1) := (m(t)div2)⊕c(t)⊕m0(t)d 
			//	c(t +1) := (m(t)div2)⊗c(t)⊕c(t)⊗m0(t)d⊕m0(t)d⊗(m(t)div2)

			}
			

		});
       
       //监听选择文件按钮
       tv_chooseRadix.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
           
            intent.setClass(MainActivity.this, IntroActivity.class);
            startActivity(intent);
			
		}
	});
        
       //保存文件按钮监听
        btn_save.setOnClickListener(new View.OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				filePath0 = Environment.getExternalStorageDirectory().toString() + File.separator + "FFCSR"+File.separator+fileName; 
				 File file = new File(filePath0);  
				 try{
			        if (!file.exists()) {  
			            File dir = new File(file.getParent());  
			            dir.mkdirs();  
			            file.createNewFile(); 
			        }  
			        FileOutputStream outStream = new FileOutputStream(file);  
					outStream.write(bufferPlain);  
					outStream.close();  
			    } catch (Exception e) {  
			        e.printStackTrace();  
			    }  
				 

			
				saveFile("buffersize is "+testlen+"\n"+
						"当前F-FCSR-8流密码生成器的参数如下：\n"+
						fileName+"\n"+fileNameProblem
						);
						
				
				
				//弹框提示保存路径
				final String save_hint=new String("结果保存到\n");
				Toast toast = Toast.makeText(edt_sc.getContext(),save_hint+filePath0, Toast.LENGTH_LONG);
				toast.show();
			
			}

				
		});
        
        //查看结果按钮监听
        btn_result.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//调用html显示txt文件
			  Intent intent = new Intent(Intent.ACTION_VIEW);
			  Uri uri = Uri.parse(filePath0);
			  intent.setDataAndType(uri, "text/html");
			  startActivity(intent);
			}
		});
     
    }
    
	public static void saveFile(String str) {   
	    boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);  
	    if (hasSDCard) { // SD卡根目录的hello.text  
	        filePath = Environment.getExternalStorageDirectory().toString() + File.separator + "FFCSR"+File.separator+"hello.txt";  
	    } else  // 系统下载缓存根目录的hello.text  
	        filePath = Environment.getDownloadCacheDirectory().toString() + File.separator +"FFCSR" +File.separator+"hello.txt";  
	      
       

	    try {  
	        File file = new File(filePath);  
	        if (!file.exists()) {  
	            File dir = new File(file.getParent());  
	            dir.mkdirs();  
	            file.createNewFile(); 
	        }  
	        FileOutputStream outStream = new FileOutputStream(file);  
	        outStream.write(str.getBytes("GBK"));  
	        outStream.close();  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
    
    //string转biginteger
	public BigInteger string_to_bigInteger(String s){
    	BigInteger num=BigInteger.valueOf(0);
    	for(int k=0;k<s.length();k++){
    		if(s.charAt(k)=='1'){
    			num=num.add(BigInteger.valueOf((long) Math.pow(2, k)));
    		}
    	}
    	return num;
    }
    

    
    //iv_short转化成iv1和iv2
    public int iv_prepare(){
  
    	int iv2_count;
    	if(iv_short.length()<=64){
    		iv1=new String(iv_short);
    		return 1;
    	}
    	else{
    		iv1=new String();
    		iv1=new String();
    		for(iv2_count=63;iv2_count>-1;iv2_count--){
    			if(iv_short.charAt(iv2_count)=='0')
    				iv1=iv1.concat(s0);
    			else iv1=iv1.concat(s1);
    		}
  
    		for(iv2_count=iv_short.length()-1;iv2_count>63;iv2_count--){
    			if(iv_short.charAt(iv2_count)=='0')
    				iv2=iv2.concat(s0);
    			else iv2=iv2.concat(s1);
    		}
   
    		return 2;
    	}    
    }
    	
    
   //本应该是由68位扩展成128，但由于68位iv由iv1或iv2加上高位的4位0位组成。故直接用64位作扩展 
    public BigInteger carry_extension(String iv_64){
    	int c_count=0,pos=-1;
    	
    	//1011 1001 1100 0110 1010 1001 1110 1010 1011 0111 1110 0010 0101 1111 1101 0110 
    	//1001 1110 1000 0110 0011 0110 1001 1010 0001 1000 0101 0110 1110 1100 0100 1010 此两行是d的二进制展开
    	d_bin=new String(d_int.toString(2));
    	for(c_count=0;c_count<iv_64.length();c_count++){
    		pos=d_bin.indexOf("1",pos+1);
    		if(pos<127)
    			carry_reg=carry_reg.add(BigInteger.valueOf(1<<pos));
  		
    	}
    	
    	return carry_reg;   	
    }
    
    //运行流密码一个周期
    public void clock(int count){
    	int clock_count;
    	for(clock_count=0;clock_count<count;clock_count++){
    	m0d = main_reg.remainder(BigInteger.valueOf(2)).multiply(d_int);
    	main_reg_up=(main_reg.divide(BigInteger.valueOf(2))).xor(carry_reg).xor(m0d);
    	//  m(t +1) := (m(t)div2)⊕c(t)⊕m0(t)d 

    	carry_reg_up=(main_reg.divide(BigInteger.valueOf(2))).and(carry_reg).xor(carry_reg).and(m0d).xor(m0d).and(carry_reg.divide(BigInteger.valueOf(2)));
		//	c(t +1) := (m(t)div2)⊗c(t)⊕c(t)⊗m0(t)d⊕m0(t)d⊗(m(t)div2)
    	
    	main_reg=main_reg_up;	//更新主寄存器内容
    	carry_reg=carry_reg_up;	
    	}
    }
    
    //挑选过滤函数
    public boolean goodFilter(BigInteger filter){
    	int temp,i,j;
    	String filter_str;  
    	filter_str=new String(filter.toString(2));	//filter转换成二进制字符串
    	for(temp=0;temp<filter_str.length();temp++){	//filter保存在16*8矩阵中
    		if(filter_str.charAt(temp)=='0')
    			filter_split[temp/8][temp%8]=0;
    		else filter_split[temp/8][temp%8]=1;	
    	}
    	while(temp<128){
    		filter_split[temp/8][temp%8]=0;
    		temp++;
    	}
    	temp=0;
    	for(j=0;j<8;j++){
    		for(i=0;i<16;i++){
    			temp+=filter_split[i][j];
    		}
    		if(temp<3)		//子filter权重小于3，filter不合格
    			return false;
    		else temp=0;
    	}
 	
		return true;   
    }
    
    


    //添加菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	//初始化加载菜单项
        getMenuInflater().inflate(R.menu.main, menu);
      //四个参数，groupid, itemid, orderid, title
        menu.add(1, Menu.FIRST, 1, "关于");   
		return true;
    }
    
    /**/
  
    
    //菜单响应事件
	@Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
          
        switch(item.getItemId()){  
        case 1:            //Menu.FIRST对应itemid为1
        //	super.finish();
        //	System.exit(0);
        	Toast.makeText(edt_sc.getContext(), "F-FCSR-8流密码 v1.0",
                    Toast.LENGTH_SHORT).show();
        	return true;
        default:  
            return false;  
           
        }     
    }  

    
}
