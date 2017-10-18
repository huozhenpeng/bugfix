package com.example.huozhenpeng.andfixdemo;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import dalvik.system.DexFile;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);

        Button b_fix= (Button) findViewById(R.id.b_fix);
        b_fix.setOnClickListener(this);
        Button b_caculate= (Button) findViewById(R.id.b_caculate);
        b_caculate.setOnClickListener(this);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void fixFromJNI(int version,Method wrongMethod,Method rightMethod);

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.b_caculate:
                int a=caculate();
                Toast.makeText(MainActivity.this,"值为："+a,Toast.LENGTH_LONG).show();
                break;
            case R.id.b_fix:
                Toast.makeText(MainActivity.this,"开始修复",Toast.LENGTH_LONG).show();
                try {
                    DexFile dexFile=DexFile.loadDex(Environment.getExternalStorageDirectory()+ File.separator+"fix.dex",new File(getCacheDir(),"opt").getAbsolutePath(), Context.MODE_PRIVATE);
                    //得到dexFile中的所有的class
                    Enumeration<String> entry=dexFile.entries();
                    while (entry.hasMoreElements())
                    {
                        //拿到全类名
                        String className=entry.nextElement();
                        Class clazz=dexFile.loadClass(className,getClassLoader());
                        if(clazz!=null)
                        {
                            fixClazz(clazz);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }
    private int  caculate() {
        return 1/0;
    }

    private void fixClazz(Class clazz) {
        //修复好的method
        Method[] methods=clazz.getDeclaredMethods();
        for(Method rightMethod:methods)
        {
            Replace replace=rightMethod.getAnnotation(Replace.class);
            if(replace==null)
            {
                continue;
            }
            String wrongClazzName=replace.clazz();
            String wrongMethodName=replace.method();
            try {
                Class wrongClazz=Class.forName(wrongClazzName);
                Method wrongMethod=wrongClazz.getDeclaredMethod(wrongMethodName,rightMethod.getParameterTypes());
                if(Build.VERSION.SDK_INT<=19)
                {
                    fixFromJNI(Build.VERSION.SDK_INT,wrongMethod,rightMethod);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
