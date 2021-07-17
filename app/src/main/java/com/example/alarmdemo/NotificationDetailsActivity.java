package com.example.alarmdemo;

import android.app.NotificationManager;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NotificationDetailsActivity extends AppCompatActivity {
    WebView webView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //初始标题栏
        Toolbar toolbar = findViewById(R.id.tb_register_back);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.content);
        webView = (WebView) findViewById(R.id.web_view);
        //显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("详情");
        }
        if(getIntent().hasExtra("content")){
            if(!TextUtils.isEmpty((getIntent().getStringExtra("content")))){
                textView.setVisibility(View.VISIBLE);
                textView.setText(Html.fromHtml(getIntent().getStringExtra("content")));
            }
        }
        if(getIntent().hasExtra("url")){
            if(!TextUtils.isEmpty((getIntent().getStringExtra("url")))){
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(getIntent().getStringExtra("url"));
                //系统默认会通过手机浏览器打开网页，为了能够直接通过WebView显示网页，则必须设置
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return super.shouldOverrideUrlLoading(view, request);
                    }
                });
            }
        }


//        当点击通知消息跳转到详情页时根据通知消息的id关闭通知栏的显示
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(Constant.TYPE1);
        manager.cancel(Constant.TYPE2);
        manager.cancel(Constant.TYPE2);
        AlarmManager.getInstance(getApplicationContext()).postReply(getIntent().getIntExtra("id", 0));
    }

    /**
     * 监听标题栏按钮点击事件.
     *
     * @param item 按钮
     * @return 结果
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //返回按钮点击事件
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

