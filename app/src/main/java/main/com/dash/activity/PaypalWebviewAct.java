package main.com.dash.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;

import main.com.dash.R;
import main.com.dash.databinding.ActivityPaypalWebviewBinding;

public class PaypalWebviewAct extends AppCompatActivity {

    Context mContext = PaypalWebviewAct.this;
    ActivityPaypalWebviewBinding binding;
    HashMap<String,String> param = new HashMap<>();
    String type = "",amount = "",id="",taxiPayType="";
    String loadPaymentURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_paypal_webview);

        amount = getIntent().getStringExtra("amount");
        id = getIntent().getStringExtra("id");

        loadPaymentURL = "http://lezdash.com/paypal/products/buy?id="+id+"&price="+amount;

        Log.e("sfasdfdassdsf","loadPaymentURL = " + loadPaymentURL);

        init();

    }

    private void init() {
        binding.webView.getSettings().setJavaScriptEnabled(true); // enable javascript
        binding.webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(PaypalWebviewAct.this, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("paypal_web_url", url);
                String title = binding.webView.getTitle();
                Log.d("title", title);
                String completed = "PayPal checkout - Payment complete.";
                if (url.contains("success")) {
                    Toast.makeText(PaypalWebviewAct.this, "Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    setResult(1234,intent);
                    finish();
                    // doPayment();
                }
                if (title.contains("cancel")) {
                    Toast.makeText(mContext, "Payment Cancel", Toast.LENGTH_SHORT).show();
                }

            }

        });

        binding.webView.loadUrl(loadPaymentURL);

    }

    private void doPayment() {

    }

    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            finish();
        }
    }

}
