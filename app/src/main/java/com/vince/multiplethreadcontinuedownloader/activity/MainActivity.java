package com.vince.multiplethreadcontinuedownloader.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vince.multiplethreadcontinuedownloader.R;
import com.vince.multiplethreadcontinuedownloader.downloader.DownloadProgressListener;
import com.vince.multiplethreadcontinuedownloader.downloader.FileDownloader;

import java.io.File;

/**
 *description:单任务多线程下载
 */
public class MainActivity extends Activity {

    private static final int PROGRESSING = 1;
    private static final int FAILURE = -1;
    private EditText pathText;
    private TextView resultView;
    private Button downloadButton;
    private Button stopbutton;
    private ProgressBar progressBar;
    private Handler handler = new UIHandler();
    private DownloadTask task;

    private final class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case PROGRESSING:
                    int size = msg.getData().getInt("size");
                    progressBar.setProgress(size);
                    float num = (float)progressBar.getProgress()/(float)progressBar.getMax();
                    int result = (int)(num * 100);
                    resultView.setText(result + "%");
                    if(progressBar.getProgress() == progressBar.getMax()){
                        Toast.makeText(getApplicationContext(),R.string.success,Toast.LENGTH_SHORT).show();
                    }
                    break;
                case FAILURE:
                    Toast.makeText(getApplicationContext(),R.string.error,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathText = (EditText)findViewById(R.id.path);
        resultView = (TextView) findViewById(R.id.resultView);
        downloadButton = (Button) findViewById(R.id.downloadbutton);
        stopbutton = (Button) findViewById(R.id.stopbutton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ButtonClickListener listener = new ButtonClickListener();

        downloadButton.setOnClickListener(listener);

        stopbutton.setOnClickListener(listener);
    }

    private final class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.downloadbutton:
                    String path = pathText.getText().toString();

                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                        //  File saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                        File saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

                        download(path,saveDir);
                    }else{
                        Toast.makeText(getApplicationContext(),R.string.sdcarderror,Toast.LENGTH_SHORT).show();
                    }

                    downloadButton.setEnabled(false);
                    stopbutton.setEnabled(true);
                    break;
                case R.id.stopbutton:

                    exit();
                    downloadButton.setEnabled(true);
                    stopbutton.setEnabled(false);
                    break;
            }
        }
    }

    public void exit(){
        if(task != null){
            task.exit();
        }
    }

    public void download(String path,File saveDir){
        task = new DownloadTask(path,saveDir);
        new Thread(task).start();
    }

    public final class DownloadTask implements Runnable{
        private String path;
        private File saveDir;
        private FileDownloader loader;

        public DownloadTask(String path,File saveDir){
            this.path = path;
            this.saveDir = saveDir;
        }

        public void exit(){
            if(loader != null){
                loader.exit();
            }
        }

        DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
            @Override
            public void onDownloadSize(int size) {
                Message msg = new Message();

                msg.what = PROGRESSING;
                msg.getData().putInt("size",size);
                handler.sendMessage(msg);
            }
        };

        public void run(){
            try{
                loader = new FileDownloader(getApplicationContext(),path,saveDir,3);
                loader.download(downloadProgressListener);
                progressBar.setMax(loader.getFileSize());
            }catch (Exception e){
                e.printStackTrace();
                handler.sendMessage(handler.obtainMessage(FAILURE));
            }
        }
    }
}
