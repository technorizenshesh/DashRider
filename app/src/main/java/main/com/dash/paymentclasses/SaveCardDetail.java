package main.com.dash.paymentclasses;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import main.com.dash.R;
import main.com.dash.constant.ACProgressCustom;
import main.com.dash.constant.BaseUrl;
import main.com.dash.constant.MyLanguageSession;
import main.com.dash.constant.MySession;
import main.com.dash.draweractivity.BaseActivity;
import www.develpoeramit.mapicall.ApiCallBuilder;

public class SaveCardDetail extends AppCompatActivity {
    EditText namecard, edt_cardnumber, expiry_date, year, security_code, postalcode;
    private Button sending;
    String strnamecard = "", cardnumber = "", strexpiry_date = "", cvv_number = "", stryear = "";
    ACProgressCustom ac_dialog;
    private RelativeLayout exit_app_but;
    private MySession mySession;
    private String user_log_data = "", user_id = "", card_id = "", token_str = "";
    private TextView removecard;
    private CreditCardFormatTextWatcher tv;
    boolean cardnumber_bool = false;
    boolean cvv_bool = false;
    boolean expmonth_bool = false;
    boolean expyear_bool = false;
    private String language = "",customer_id="",first_name,last_name;
    MyLanguageSession myLanguageSession;
    private ProgressBar progress_bar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myLanguageSession = new MyLanguageSession(this);
        language = myLanguageSession.getLanguage();
        myLanguageSession.setLangRecreate(myLanguageSession.getLanguage());


        setContentView(R.layout.activity_save_card_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (myLanguageSession.getLanguage().equalsIgnoreCase("ar")) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            } else {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }

        }
        mySession = new MySession(this);
        user_log_data = mySession.getKeyAlldata();
        if (user_log_data == null) {

        } else {
            try {
                JSONObject jsonObject = new JSONObject(user_log_data);
                String message = jsonObject.getString("status");
                if (message.equalsIgnoreCase("1")) {
                    JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                    user_id = jsonObject1.getString("id");
                    first_name = jsonObject1.getString("first_name");
                    last_name = jsonObject1.getString("last_name");
                    Log.e("jsonObject1", "" + jsonObject1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ac_dialog = new ACProgressCustom.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text(getResources().getString(R.string.pleasewait))
                .textSize(20).textMarginTop(5)
                .fadeColor(Color.DKGRAY).build();

        idinti();
        clickevent();
      //  new GetCardDetail().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myLanguageSession.setLangRecreate(myLanguageSession.getLanguage());
        String oldLanguage = language;
        language = myLanguageSession.getLanguage();
        if (!oldLanguage.equals(language)) {
            finish();
            startActivity(getIntent());
        }
//        new GetUserProfile().execute();
    }

    private void clickevent() {
        exit_app_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        removecard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RemooveCardDetail().execute();
            }
        });
        sending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strnamecard = namecard.getText().toString().trim();
                cardnumber = edt_cardnumber.getText().toString().trim();
                strexpiry_date = expiry_date.getText().toString().trim();
                stryear = year.getText().toString().trim();
                cvv_number = security_code.getText().toString().trim();
                if (strnamecard == null || strnamecard.equals("")) {
                    namecard.setError(getResources().getString(R.string.cardnameempty));
                }
                if (cardnumber == null || cardnumber.equals("")) {
                    edt_cardnumber.setError(getResources().getString(R.string.cardnumnotempty));
                }
                if (strexpiry_date == null || strexpiry_date.equals("")) {
                    expiry_date.setError(getResources().getString(R.string.expdatenotempty));
                }
                if (stryear == null || stryear.equals("")) {
                    year.setError(getResources().getString(R.string.yearnotempty));
                }
                if (cvv_number == null || cvv_number.equals("")) {
                    security_code.setError(getResources().getString(R.string.securitynotempety));
                } else {
                    int month = Integer.parseInt(strexpiry_date);
                    int year_int = Integer.parseInt(stryear);
                    onClickSomething(cardnumber, month, year_int, cvv_number);

                }


            }
        });
    }

    public void onClickSomething(String cardNumber, int cardExpMonth, int cardExpYear, String cardCVC) {
        Card card = new Card(cardNumber, cardExpMonth, cardExpYear, cardCVC);
        if (card.validateCard()){
            SaveCard(card);
        }
    }

    private void idinti() {
        progress_bar = findViewById(R.id.progress_bar);
        removecard = findViewById(R.id.removecard);
        exit_app_but = findViewById(R.id.exit_app_but);
        namecard = (EditText) findViewById(R.id.namecard);
        edt_cardnumber = (EditText) findViewById(R.id.edt_cardnumber);
        expiry_date = (EditText) findViewById(R.id.expiry_date);
        year = (EditText) findViewById(R.id.year);
        security_code = (EditText) findViewById(R.id.security_code);
        postalcode = (EditText) findViewById(R.id.postalcode);

        sending = (Button) findViewById(R.id.sending_payment);
        tv = new CreditCardFormatTextWatcher(edt_cardnumber);
        edt_cardnumber.addTextChangedListener(tv);

    }

    private class RemooveCardDetail extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // prgressbar.setVisibility(View.VISIBLE);
            if (ac_dialog != null) {
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
//http://hitchride.net/webservice/delete_card?card_id=1
            try {
                String postReceiverUrl = BaseUrl.baseurl + "delete_card?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("cus_id", strings[0]);
                params.put("card_id", card_id);

                Log.e("Customer Card", ">>>>>>>>>>>>" + strings[0]);
                Log.e("Customer card_id", ">>>>>>>>>>>>" + strings[1]);

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
                Log.e("Delete Card", ">>>>>>>>>>>>" + response);
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
            // prgressbar.setVisibility(View.GONE);
            if (ac_dialog != null) {
                ac_dialog.dismiss();
            }

            if (result == null) {
            } else if (result.isEmpty()) {

            } else {
                BaseActivity.Card_Added_Sts = "";
                Toast.makeText(SaveCardDetail.this, getResources().getString(R.string.cardremoved), Toast.LENGTH_LONG).show();

                finish();
            }


        }
    }
    private void SaveCard(Card card){
        HashMap<String,String>param=new HashMap<>();
        param.put("user_id",user_id);
        param.put("number",""+card.getNumber());
        param.put("expire_month",""+card.getExpMonth());
        param.put("expire_year",""+card.getExpYear());
        param.put("cvv2",""+card.getCVC());
        param.put("first_name",""+first_name);
        param.put("last_name",""+last_name);
        ApiCallBuilder.build(this)
                .setUrl(BaseUrl.get().save_card_paypal())
                .setParam(param).isShowProgressBar(true)
                .execute(new ApiCallBuilder.onResponse() {
                    @Override
                    public void Success(String response) {
                        Log.e("Response",response);
                        try {
                            JSONObject object=new JSONObject(response);
                            if (object.getString("status").equals("1")){
                                finish();
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
