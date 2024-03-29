package main.com.dash.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Half;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import main.com.dash.MainActivity;
import main.com.dash.R;
import main.com.dash.app.Config;
import main.com.dash.constant.ACProgressCustom;
import main.com.dash.constant.BaseUrl;
import main.com.dash.constant.MyLanguageSession;
import main.com.dash.constant.MySession;
import main.com.dash.utils.NotificationUtils;
import www.develpoeramit.mapicall.ApiCallBuilder;

public class FeedbackUs extends AppCompatActivity {
    
    private TextView date_tv, totalamount, pickuplocation, droplocation, drivername;
    private TextView servicetax,nightcharge,time_tv,timefare,distancefare,distance,carcharge;
    private ProgressBar progressbar;
    private ImageView driver_img,remove_tips;
    private RelativeLayout exit_app_but;
    private RatingBar ratingbar;
    private final int PAYPAL_PAYMENT_REQUEST_CODE = 1234;
    private EditText comment_et;
    private Button submit;
    Context mContext = FeedbackUs.this;
    private String req_datetime = "",driver_id="",car_charge_str="",amount_str_main="", amount_str = "", request_id = "", comment_str = "", user_log_data = "", user_id = "";
    private float rating = 0;
    MySession mySession;
    RadioGroup paymentGroup;
    String payment_type_str = "Cash";
    float mywalletamount, rideamount;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    Dialog waitDialog;
    String time_zone="";
    ACProgressCustom ac_dialog;
    private TextView paymet_type,apply_tv,tipsamt_tv,discount_type;
    private CheckBox tips_check;
    private LinearLayout tipslay,tipsshowlay,tips_check_lay;
    String tips_amount_str = "0";
    private EditText tips_amount;
    private boolean tip_amt_sts=true;
    private String language = "",token_id="",customer_id="",tip_submit_sts="";
    MyLanguageSession myLanguageSession;
    private double total=0.0,totalAmount=0.0;
    private static final String TAG = "Payment";

    //TODO: paypal
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "ARc0StmRhSlsr-Kd4aKhnFsNPy6mz1UrVNk0wVv1lI_Sa1ZRZ9koviXAbzxm1vF5o7HsH0mho5Aq0p4Z";
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final String CONFIG_CLIENT_TOKEN = "access_token$production$7bkbnrzyhfmvny6r$e28b17c257cc84917087a8dcd5452ffa";

    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int REQUEST_CODE_PROFILE_SHARING = 3;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("Dash Texi")
            .merchantPrivacyPolicyUri(Uri.parse("http://lezdash.com/DashTaxi/privacy.php"))
            .merchantUserAgreementUri(Uri.parse("http://lezdash.com/DashTaxi/terms.php"));
    private String PaymentToken="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myLanguageSession = new MyLanguageSession(this);
        language = myLanguageSession.getLanguage();
        myLanguageSession.setLangRecreate(myLanguageSession.getLanguage());
        setContentView(R.layout.activity_feedback_us);
        ac_dialog = new ACProgressCustom.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text(getResources().getString(R.string.pleasewait))
                .textSize(20).textMarginTop(5)
                .fadeColor(Color.DKGRAY).build();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mySession = new MySession(this);
        user_log_data = mySession.getKeyAlldata();
        if (NotificationUtils.r != null && NotificationUtils.r.isPlaying()) {
            NotificationUtils.r.stop();
        }

        if (user_log_data == null) {

        } else {
            try {
                JSONObject jsonObject = new JSONObject(user_log_data);
                String message = jsonObject.getString("status");
                if (message.equalsIgnoreCase("1")) {
                    JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                    user_id = jsonObject1.getString("id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        idinit();
        clickevent();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                    Log.e("Push notification: ", "" + message);
                    JSONObject data = null;
                    try {
                        data = new JSONObject(message);
                        String keyMessage = data.getString("key").trim();
                        Log.e("KEY ACCEPT REJ", "" + keyMessage);
                        if (keyMessage.equalsIgnoreCase("your booking request is Finish")) {
                            if (waitDialog != null && waitDialog.isShowing()) {
                                waitDialog.dismiss();
                                receivedConfirmPayment();
                            }
                        }
                        if (keyMessage.equalsIgnoreCase("your payment is denied")) {
                            if (waitDialog != null && waitDialog.isShowing()) {
                                waitDialog.dismiss();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new GetCurrentBooking().execute();
        try {
            initPaypal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(FeedbackUs.this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(FeedbackUs.this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(FeedbackUs.this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(FeedbackUs.this.getApplicationContext());
        super.onResume();
        myLanguageSession.setLangRecreate(myLanguageSession.getLanguage());
        String oldLanguage = language;
        language = myLanguageSession.getLanguage();
        if (!oldLanguage.equals(language)) {
            finish();
            startActivity(getIntent());
        }
    }

    private void clickevent() {

        exit_app_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating = ratingbar.getRating();
                comment_str = comment_et.getText().toString();
                if (rating == 0) {
                    Toast.makeText(FeedbackUs.this, getResources().getString(R.string.plsgiverating), Toast.LENGTH_LONG).show();
                    return;
                }
//                if (comment_str.equalsIgnoreCase("")) {
//                    Toast.makeText(FeedbackUs.this, getResources().getString(R.string.plsraterider), Toast.LENGTH_LONG).show();
//                    return;
//                }
                if (payment_type_str.equalsIgnoreCase("Card")) {
//                  onBuyPressed(v);
                    Intent intent = new Intent(mContext,PaypalWebviewAct.class);
                    intent.putExtra("amount",String.valueOf(totalAmount));
                    intent.putExtra("id",user_id);
                    startActivityForResult(intent,PAYPAL_PAYMENT_REQUEST_CODE);
                    // OnPay();
                } else {
                   new GiveReviewRating().execute();
                }

            }
        });
    }

    private void idinit() {
        nightcharge =findViewById(R.id.nightcharge);
        servicetax =findViewById(R.id.servicetax);
        discount_type = findViewById(R.id.discount_type);
        carcharge = findViewById(R.id.carcharge);
        distancefare = findViewById(R.id.distancefare);
        distance = findViewById(R.id.distance);
        time_tv = findViewById(R.id.time_tv);
        timefare = findViewById(R.id.timefare);
        tipsshowlay = findViewById(R.id.tipsshowlay);
        tips_amount = findViewById(R.id.tips_amount);
        tipsamt_tv = findViewById(R.id.tipsamt_tv);
        tips_check_lay = findViewById(R.id.tips_check_lay);
        apply_tv = findViewById(R.id.apply_tv);
        remove_tips = findViewById(R.id.remove_tips);
        tips_check = findViewById(R.id.tips_check);
        tipslay = findViewById(R.id.tipslay);
        submit = (Button) findViewById(R.id.submit);
        paymet_type = (TextView) findViewById(R.id.paymet_type);
        comment_et = (EditText) findViewById(R.id.comment_et);
        ratingbar = (RatingBar) findViewById(R.id.ratingbar);
        driver_img = (ImageView) findViewById(R.id.driver_img);
        date_tv = (TextView) findViewById(R.id.date_tv);
        droplocation = (TextView) findViewById(R.id.droplocation);
        drivername = (TextView) findViewById(R.id.drivername);
        pickuplocation = (TextView) findViewById(R.id.pickuplocation);
        totalamount = (TextView) findViewById(R.id.totalamount);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        exit_app_but = (RelativeLayout) findViewById(R.id.exit_app_but);
        tips_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tipslay.setVisibility(View.VISIBLE);
                } else {
                    tipslay.setVisibility(View.GONE);
                }
            }
        });
        remove_tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tip_amt_sts = false;
                tips_amount_str = "0";
                new Applytip().execute(tips_amount_str);
            }
        });
        apply_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tip_amt_sts = true;
                tips_amount_str = tips_amount.getText().toString();
                if ( tips_amount_str.equalsIgnoreCase("")) {

                } else {
                    new Applytip().execute(tips_amount_str);
                }
            }
        });
    }
    private class Applytip extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                super.onPreExecute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(ac_dialog!=null){
                ac_dialog.show();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
//http://hitchride.net/webservice/apply_tip_amount?tip_amount=18&request_id=476
            try {
                String postReceiverUrl = BaseUrl.baseurl + "apply_tip_amount?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("request_id", request_id);
                params.put("tip_amount", strings[0]);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                String urlParameters = postData.toString();
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParameters);
                writer.flush();
                String response = "";
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                writer.close();
                reader.close();
                Log.e("Apply Tip ", ">>>>>>>>>>>>" + response);
                return response;
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(ac_dialog!=null){
                ac_dialog.dismiss();
            }

            if (result == null) {
            } else if (result.isEmpty()) {
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getString("status").equalsIgnoreCase("1")){
                        tip_submit_sts="success";

                       if (tip_amt_sts){
                           tips_amount_str = tips_amount.getText().toString();
                           if (tips_amount_str == null || tips_amount_str.equalsIgnoreCase("")) {

                           } else {

                               tipsamt_tv.setText("$" + tips_amount_str);
                               tipsshowlay.setVisibility(View.VISIBLE);
                               tips_check_lay.setVisibility(View.GONE);
                               double tipamt = Double.parseDouble(tips_amount_str);
                               if (amount_str != null && !amount_str.equalsIgnoreCase("")) {
                                   double totalrideamt = Double.parseDouble(amount_str);
                                   double total = tipamt + totalrideamt;
                                   totalAmount = total;
                                   Log.e("jfahjsdghjagsdh","1 = " + total);
                                   totalamount.setText("Total :" + "$" + total);
                               }

                           }
                       }
                       else {
                           tips_amount_str = "0";
                           try {
                               totalAmount = Double.parseDouble(amount_str);
                           } catch (Exception e) {}
                           Log.e("jfahjsdghjagsdh","2 = " + amount_str);
                           totalamount.setText("Total :" + "$" + amount_str);
                           tipsshowlay.setVisibility(View.GONE);
                           tips_check.setChecked(false);
                           tips_check_lay.setVisibility(View.VISIBLE);
                           tipslay.setVisibility(View.GONE);
                           amount_str=amount_str_main;
                       }
                    }
                    else {

                        if (tip_amt_sts){
                            tips_amount_str = tips_amount.getText().toString();
                            if (tips_amount_str == null || tips_amount_str.equalsIgnoreCase("")) {

                            } else {

                                tipsamt_tv.setText("$" + tips_amount_str);
                                tipsshowlay.setVisibility(View.VISIBLE);
                                tips_check_lay.setVisibility(View.GONE);
                                double tipamt = Double.parseDouble(tips_amount_str);
                                if (amount_str != null && !amount_str.equalsIgnoreCase("")) {
                                    double totalrideamt = Double.parseDouble(amount_str);
                                    double total = tipamt + totalrideamt;
                                    totalAmount = total;
                                    Log.e("jfahjsdghjagsdh","3 = " + total);
                                    totalamount.setText("Total :" + "$" + total);
                                }
                            }
                        } else {
                            tips_amount_str = "0";
                            try {
                                totalAmount = Double.parseDouble(amount_str);
                            } catch (Exception e){}
                            Log.e("jfahjsdghjagsdh","4 = " + amount_str);
                            totalamount.setText("Total :" + "$" + amount_str);
                            tipsshowlay.setVisibility(View.GONE);
                            tips_check.setChecked(false);
                            tips_check_lay.setVisibility(View.VISIBLE);
                            tipslay.setVisibility(View.GONE);
                            amount_str=amount_str_main;
                        }


                        tip_submit_sts="unsuccess";
                       // Toast.makeText(FeedbackUs.this,getResources().getString(R.string.sorryridecompleted),Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetPayment extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressbar.setVisibility(View.VISIBLE);
            if(ac_dialog!=null) {
                ac_dialog.show();
            }

            try {
                super.onPreExecute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String postReceiverUrl = BaseUrl.baseurl + "get_payment?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("request_id", request_id);
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                String urlParameters = postData.toString();
                Log.e("PaymentURL","===>"+postReceiverUrl+urlParameters);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParameters);
                writer.flush();
                String response = "";
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                writer.close();
                reader.close();
                return response;
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // progressbar.setVisibility(View.GONE);

            if(ac_dialog!=null) {
                ac_dialog.dismiss();
            }

            if (result == null) {
            } else if (result.isEmpty()) {
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("message");
                    if (msg.equalsIgnoreCase("successful")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            amount_str = jsonObject1.getString("total");
                            amount_str_main = jsonObject1.getString("total");
                            distancefare.setText("$" + jsonObject1.getString("per_miles_charge"));
                            timefare.setText("$" + jsonObject1.getString("per_min_charge"));
                            carcharge.setText("" + "$" + jsonObject1.getInt("car_charge"));
                            distance.setText("Distance(" + jsonObject1.getString("miles") + " km)");
                            time_tv.setText("Time(" + jsonObject1.getString("perMin") + " min)");
                            nightcharge.setText("$" + jsonObject1.getString("night_charge_amount"));
                            servicetax.setText("$" + jsonObject1.getString("service_tax_amount"));
                            car_charge_str = String.valueOf(jsonObject1.getString("car_charge"));
                            discount_type.setText(getResources().getString(R.string.discountapplied) + "$ " + jsonObject1.getString("discount"));
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("booking_detail");
                            pickuplocation.setText("" + jsonObject2.getString("picuplocation"));
                            droplocation.setText("" + jsonObject2.getString("dropofflocation"));
                           String tip_amount_str = jsonObject2.getString("tip_amount");
                            total = Double.parseDouble(jsonObject1.getString("total"));
                            if (tip_amount_str == null || tip_amount_str.equalsIgnoreCase("") || tip_amount_str.equalsIgnoreCase("0")) {
                                totalAmount = total;
                                Log.e("jfahjsdghjagsdh","5 = " + total);
                                totalamount.setText("Total :" + "$ " + total);
                            } else {
                                tipsamt_tv.setText("$" + tip_amount_str);
                                tipsshowlay.setVisibility(View.VISIBLE);
                                tips_check_lay.setVisibility(View.GONE);
                                double tipamt = Double.parseDouble(tip_amount_str);
                                totalAmount = total;
                                Log.e("jfahjsdghjagsdh","6 = " + total);
                                totalamount.setText("Total :" + "$" + total);
                                if (jsonObject1.getString("total") != null && !jsonObject1.getString("total").equalsIgnoreCase("")) {
                                    total = tipamt + total;
                                    totalAmount = total;
                                    Log.e("jfahjsdghjagsdh","7 = " + total);
                                    totalamount.setText("Total :" + "$" + total);
                                }
                            }
                            Log.e("PayTotal","==>" + total);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class GiveReviewRating extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //  progressbar.setVisibility(View.VISIBLE);
            if(ac_dialog!=null) {
                ac_dialog.show();
            }

            try {
                super.onPreExecute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String postReceiverUrl = BaseUrl.baseurl + "add_rating_review?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("request_id", request_id);
                params.put("user_id", user_id);
                params.put("driver_id", driver_id);
                params.put("rating", rating);
                params.put("timezone", time_zone);
                params.put("payment_token", PaymentToken);
                if (comment_str == null) {
                    params.put("review", "");
                } else {
                    params.put("review", comment_str);
                }

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                String urlParameters = postData.toString();
                Log.e("AddReview","===>"+postReceiverUrl+urlParameters);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParameters);
                writer.flush();
                String response = "";
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                writer.close();
                reader.close();
                return response;
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // progressbar.setVisibility(View.GONE);
            if(ac_dialog!=null){
                ac_dialog.dismiss();
            }
            Log.e("DDDD "," >> "+result);
            if (result == null) {
            } else if (result.isEmpty()) {
            } else {
                // waitDriverAccept();
                //   finish();
                if (tip_submit_sts.equalsIgnoreCase("unsuccess"))
                {
                    if (tips_amount_str!=null&&!tips_amount_str.equalsIgnoreCase("")&&!tips_amount_str.equalsIgnoreCase("0")){
                        new SubmitTipPayment().execute();
                    }
                    else {
                        Intent i = new Intent(FeedbackUs.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(i);
                    }
                }
                else {
                    Intent i = new Intent(FeedbackUs.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                }

            }
        }
    }


    private class SubmitTipPayment extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //  progressbar.setVisibility(View.VISIBLE);

            try {
                super.onPreExecute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String postReceiverUrl = BaseUrl.baseurl + "tip_payment?";
                Log.e("TipSubmit >>> "," >> "+postReceiverUrl+"&request_id="+request_id+"&user_id="+driver_id+"&tip="+tips_amount_str+"&time_zone="+time_zone+"&token="+token_id+"&currency=usd&customer="+customer_id);
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("request_id", request_id);
                params.put("user_id", driver_id);
                params.put("tip", tips_amount_str);
                params.put("time_zone", time_zone);
                params.put("token", token_id);
                params.put("currency", "usd");
                params.put("customer", customer_id);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                String urlParameters = postData.toString();
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParameters);
                writer.flush();
                String response = "";
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                writer.close();
                reader.close();
                return response;
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // progressbar.setVisibility(View.GONE);

            if (result == null) {
            } else if (result.isEmpty()) {
            } else {
                // waitDriverAccept();
                // finish();
                Intent i = new Intent(FeedbackUs.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
            }
        }
    }

    private class GetCurrentBooking extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //   progressbar.setVisibility(View.VISIBLE);
            if(ac_dialog!=null) {
                ac_dialog.show();
            }

            try {
                super.onPreExecute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
//http://mobileappdevelop.co/NAXCAN/webservice/get_current_booking?user_id=1
                String postReceiverUrl = BaseUrl.baseurl + "get_current_booking?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                Log.e("User id,", "" + user_id);
                params.put("user_id", user_id);
                params.put("timezone", time_zone);
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                String urlParameters = postData.toString();
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParameters);
                writer.flush();
                String response = "";
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                writer.close();
                reader.close();
                return response;
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // progressbar.setVisibility(View.GONE);
            if(ac_dialog!=null){
                ac_dialog.dismiss();
            }

            if (result == null) {
            } else if (result.isEmpty()) {
            } else {
                try {
                    Log.e("Resposne in my Booking", "" + result);
                    JSONObject jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("message");
                    if (msg.equalsIgnoreCase("successfull")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            request_id = jsonObject1.getString("id");
                            driver_id = jsonObject1.getString("driver_id");
                            req_datetime = jsonObject1.getString("req_datetime");
                            payment_type_str = jsonObject1.getString("payment_type");
                            paymet_type.setText("Payment Type : "+payment_type_str);
                            try {
                                Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(req_datetime);
                                SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                                String strDate = formatter.format(date1);
                                date_tv.setText("" + strDate);

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            String Status = jsonObject1.getString("status");
                            String pay_status = jsonObject1.getString("pay_status");

                            if (Status.equalsIgnoreCase("End") && pay_status.equalsIgnoreCase("Processing")){
                                waitDriverAccept();

                            }

                            JSONArray jsonArray3 = jsonObject1.getJSONArray("user_details");
                            for (int user = 0; user < jsonArray3.length(); user++) {
                                JSONObject jsonObject21 = jsonArray3.getJSONObject(user);
                                token_id = jsonObject21.getString("card_id");
                                customer_id = jsonObject21.getString("cust_id");

                            }

                            JSONArray jsonArray1 = jsonObject1.getJSONArray("driver_details");
                            for (int k = 0; k < jsonArray1.length(); k++) {
                                JSONObject jsonObject2 = jsonArray1.getJSONObject(k);

                                drivername.setText("" + jsonObject2.getString("first_name") + " " + jsonObject2.getString("last_name"));
                                if (jsonObject2.getString("profile_image") == null || jsonObject2.getString("profile_image").equalsIgnoreCase("") || jsonObject2.getString("profile_image").equalsIgnoreCase("http://mobileappdevelop.co/NAXCAN/uploads/images/")) {

                                } else {
                                  //  Picasso.with(FeedbackUs.this).load(jsonObject2.getString("profile_image")).into(driver_img);

                                }

                            }
                        }

                        new GetPayment().execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void waitDriverAccept() {


        //   Log.e("War Msg in dialog", war_msg);
        waitDialog = new Dialog(FeedbackUs.this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setCancelable(false);
        waitDialog.setContentView(R.layout.wait_driver_accept);
        waitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        waitDialog.show();


    }

    private void receivedConfirmPayment() {
        final Dialog recdialog = new Dialog(FeedbackUs.this);
        recdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        recdialog.setCancelable(false);
        recdialog.setContentView(R.layout.paymentconfirm_lay);
        recdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView yes = (TextView) recdialog.findViewById(R.id.yes_tv);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recdialog.dismiss();
                //finish();
                Intent i = new Intent(FeedbackUs.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
            }
        });
        recdialog.show();
    }

    // TODO: =============Paypal Payment==========
    private void initPaypal() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }

    public void onBuyPressed(View pressed) {
        /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */

        Intent intent = new Intent(this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    private PayPalPayment getThingToBuy(String paymentIntent) {
        return new PayPalPayment(new BigDecimal(total), "USD", "Trp Charge",
                paymentIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
//                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        JSONObject object=new JSONObject(confirm.toJSONObject().toString(4));
                        Log.i(TAG, object.getJSONObject("response").getString("id"));
                        PaymentToken=object.getJSONObject("response").getString("id");
                        displayResultText("PaymentConfirmation info received from PayPal");
                        new GiveReviewRating().execute();
                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (resultCode == PAYPAL_PAYMENT_REQUEST_CODE) {
            new GiveReviewRating().execute();
        }
    }

    protected void displayResultText(String result) {
        Log.e(TAG,result);
        Toast.makeText(
                getApplicationContext(),
                result, Toast.LENGTH_LONG)
                .show();
    }

    //TODO: on pay using api
    private void OnPay() {
        HashMap<String,String>param=new HashMap<>();
        param.put("user_id" , user_id);
        param.put("card_id" , mySession.getCardID());
        param.put("amount" , "" + total);
        ApiCallBuilder.build(this).setUrl(BaseUrl.get().pay_with_paypal())
                .setParam(param).isShowProgressBar(true)
                .execute(new ApiCallBuilder.onResponse() {
                    @Override
                    public void Success(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            if (object.getString("state").equals("approved")) {
                                PaymentToken=object.getString("id");
                                new GiveReviewRating().execute();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void Failed(String error) {

                    }
                });
    }
}
