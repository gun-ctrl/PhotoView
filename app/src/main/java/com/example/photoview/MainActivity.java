package com.example.photoview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.photoview.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
//    Thread thread1,thread,thread2;
//    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
    public String execShell(String cmd) {
        StringBuilder s = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream)
            );
            try {
                process.waitFor();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            String line = null;
            while ((line=bufferedReader.readLine())!=null){
                s.append(line).append("\n");
            }
            inputStream.close();
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return s.toString();
    }
//    public void test{
//        btn = binding.update;
//        String s = "pm disable-user com.example.photoview";
//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    thread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                execShell(s);
//            }
//        });
//
//
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                thread.start();
//                btn.setText("安装");
//                thread1.start();
//            }
//        });
//
//    }
}