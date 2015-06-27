package com.example.yl.imgviewflipper;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    /** Called when the activity is first created. */
    private String myIP ;
    private int myPort ;
    private ImageView imageView; // 图片
    private Button button01; // 按钮
    private Button button02; // 按钮
    Bitmap myBitmap;
    private byte[] mContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取IP地址
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        myIP = data.getString("ipname");
        myPort = data.getInt("portname");

        imageView = (ImageView) findViewById(R.id.imageView1);
        button01 = (Button) findViewById(R.id.button1);
        button02 = (Button) findViewById(R.id.button2);


        button01.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
             /*
                                 * // TODO Auto-generated method stub Intent intent = new
                                 * Intent("android.media.action.IMAGE_CAPTURE");
                                 * startActivityForResult(intent,Activity.DEFAULT_KEYS_DIALER);
                                 */
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("选择图像");

                builder.setPositiveButton("相机",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        "android.media.action.IMAGE_CAPTURE");
                                startActivityForResult(intent, 0);

                            }
                        });
                builder.setNegativeButton("相册",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, 1);

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        button02.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                /*
                // ImageView对象(iv_photo)必须做如下设置后，才能获取其中的图像
                imageView.setDrawingCacheEnabled(true);
                // 获取ImageView中的图像
                Bitmap sendBmp =Bitmap.createBitmap(imageView.getDrawingCache());
                // 从ImaggeView对象(iv_photo)中获取图像后，要记得调用setDrawingCacheEnabled(false)
                // 清空画图缓冲区
                imageView.setDrawingCacheEnabled(false);
                */
                if(myBitmap!=null)
                {
                    Bitmap sendBmp = resizeBmp(myBitmap,640,480);
                    imageView.setImageBitmap(sendBmp);

                    Thread sendTH = new MySendThread(sendBmp,myIP,myPort);
                    sendTH.start();
                }

            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ContentResolver resolver = getContentResolver();
        /**
         * 如果不拍照 或者不选择图片返回 不执行任何操作
         */

        if (data != null) {
            /**
             * 因为两种方式都用到了startActivityForResult方法，这个方法执行完后都会执行onActivityResult方法
             * ， 所以为了区别到底选择了那个方式获取图片要进行判断
             * ，这里的requestCode跟startActivityForResult里面第二个参数对应 1== 相册 2 ==相机
             */
            if (requestCode == 1) { //相册

                try {
                    // 获得图片的uri
                    Uri originalUri = data.getData();
                    // 将图片内容解析成字节数组
                    mContent = readStream(resolver.openInputStream(Uri.parse(originalUri.toString())));
                    // 将字节数组转换为ImageView可调用的Bitmap对象
                    myBitmap = getPicFromBytes(mContent, null);
                    // //把得到的图片绑定在控件上显示
                    imageView.setImageBitmap(myBitmap);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if (requestCode == 0) {  //相机

                String sdStatus = Environment.getExternalStorageState();
                if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
                    return;
                }
                Bundle bundle = data.getExtras();
                myBitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                FileOutputStream b = null;
                File file = new File("/sdcard/myImage/");
                file.mkdirs();// 创建文件夹，名称为myimage

                // 照片的命名，目标文件夹下，以当前时间数字串为名称，即可确保每张照片名称不相同。
                String str = null;
                Date date = null;
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");// 获取当前时间，进一步转化为字符串
                date = new Date();
                str = format.format(date);
                String fileName = "/sdcard/myImage/" + str + ".jpg";
                try {
                    b = new FileOutputStream(fileName);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        b.flush();
                        b.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (data != null) {
                        Bitmap cameraBitmap = (Bitmap) data.getExtras().get(
                                "data");
                        System.out.println("fdf================="
                                + data.getDataString());
                        imageView.setImageBitmap(cameraBitmap);

                        System.out.println("成功======" + cameraBitmap.getWidth()
                                + cameraBitmap.getHeight());
                    }

                }
            }
        }
    }

    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;

    }

    private Bitmap resizeBmp(Bitmap srcBmp ,int newWidth, int newHeight)
    {
        int srcWidth = srcBmp.getWidth();
        int srcHeight = srcBmp.getHeight();
        float scaleWidth = ((float)newWidth)/srcWidth;
        float scaleHeight = ((float)newHeight)/srcHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBmp = Bitmap.createBitmap(srcBmp,0,0,srcWidth,srcHeight,matrix,true);

        return resizedBmp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


class MySendThread extends Thread{

    private OutputStream os;
    private String ipname;
    private  int portnum;
    private Bitmap sendBmp;

    public MySendThread(Bitmap sendBmp,String ipname,int portnum){

        this.ipname = ipname;
        this.portnum = portnum;
        this.sendBmp = sendBmp;

    }

    public void run() {

       try {

           int bytes = sendBmp.getByteCount();
           ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
           sendBmp.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
           byte[] array = buffer.array();

           //将图像数据通过Socket发送
           Socket tempSocket = new Socket(ipname, portnum);
           os = tempSocket.getOutputStream();

           int i=0;
           while(i<bytes)
           {
               os.write(array,i,sendBmp.getWidth()*4);
               i+=sendBmp.getWidth()*4;
           }

//           ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
//           sendBmp.compress(Bitmap.CompressFormat.PNG, 100, outByteStream);
//           ByteArrayInputStream inputByteStream = new ByteArrayInputStream(outByteStream.toByteArray());
//           int amount;
//           while ((amount=inputByteStream.read(byteBuffer)) != -1) {
//                os.write(byteBuffer, 0, amount);
//           }
//
//           outByteStream.flush();
//           outByteStream.close();

           tempSocket.close();

       } catch (IOException e) {
                e.printStackTrace();
       }
    }

}

