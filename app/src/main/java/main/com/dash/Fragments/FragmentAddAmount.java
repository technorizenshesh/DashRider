package main.com.dash.Fragments;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import main.com.dash.R;
import main.com.dash.activity.FeedbackUs;
import main.com.dash.constant.BaseUrl;
import main.com.dash.constant.MySession;
import main.com.dash.databinding.FragmentAddAmountBinding;
import main.com.dash.paymentclasses.MyCardsPayment;
import www.develpoeramit.mapicall.ApiCallBuilder;

public class FragmentAddAmount extends Fragment {
    private  onPaymentListener listener;
    private FragmentAddAmountBinding binding;
    private String amount_str="0";
    //TODO: paypal
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "ARc0StmRhSlsr-Kd4aKhnFsNPy6mz1UrVNk0wVv1lI_Sa1ZRZ9koviXAbzxm1vF5o7HsH0mho5Aq0p4Z";
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("Dash Texi")
            .merchantPrivacyPolicyUri(Uri.parse("http://lezdash.com/DashTaxi/privacy.php"))
            .merchantUserAgreementUri(Uri.parse("http://lezdash.com/DashTaxi/terms.php"));
    private String PaymentToken="";
    private static final String TAG = "FragmentAddAmount";
    private MySession mySession;
    private String user_log_data;
    private String time_zone;
    private String user_id;
    private String cust_id;

    public FragmentAddAmount() {
        // Required empty public constructor
    }   public FragmentAddAmount(onPaymentListener listener) {
        this.listener=listener;
        // Required empty public constructor
    }
public interface onPaymentListener{
        void onRefresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_add_amount, container, false);
        bindView();
        return binding.getRoot();
    }

    private void bindView() {
        mySession = new MySession(getContext());
        user_log_data = mySession.getKeyAlldata();
        Calendar c = Calendar.getInstance();
        TimeZone tz = c.getTimeZone();
        time_zone = tz.getID();
        if (user_log_data == null) {

        } else {
            try {
                Log.e("user_log_data >>"," <<"+user_log_data);
                JSONObject jsonObject = new JSONObject(user_log_data);
                String message = jsonObject.getString("status");
                if (message.equalsIgnoreCase("1")) {
                    JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                    user_id = jsonObject1.getString("id");
                    cust_id = jsonObject1.getString("cust_id");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        binding.addmoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount_str = binding.amountEt.getText().toString();
                if (amount_str.equalsIgnoreCase("")){
                    Toast.makeText(getActivity(),getResources().getString(R.string.enteramount), Toast.LENGTH_LONG).show();
                } else {
                    onBuyPressed(v);
                  /*  Intent i = new Intent(getActivity(), MyCardsPayment.class);
                    i.putExtra("amount_str",amount_str);
                    startActivity(i);
*/
                }
            }
        });
        binding.fiftyBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.amountEt.setText("50");
                binding.fiftyBut.setBackgroundResource(R.drawable.border_yellowrounddrab);
                binding.hundredBut.setBackgroundResource(R.drawable.border_grey_rec);
                binding.onefiftyBut.setBackgroundResource(R.drawable.border_grey_rec);

            }
        });
        binding.hundredBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.amountEt.setText("100");
                binding.hundredBut.setBackgroundResource(R.drawable.border_yellowrounddrab);
                binding.onefiftyBut.setBackgroundResource(R.drawable.border_grey_rec);
                binding.fiftyBut.setBackgroundResource(R.drawable.border_grey_rec);
            }
        });

        binding.onefiftyBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.amountEt.setText("150");
                binding.onefiftyBut.setBackgroundResource(R.drawable.border_yellowrounddrab);
                binding.hundredBut.setBackgroundResource(R.drawable.border_grey_rec);
                binding.fiftyBut.setBackgroundResource(R.drawable.border_grey_rec);
            }
        });
        initPaypal();
    }
    //TODO: =============Paypal Payment==========
    private void initPaypal() {
        Intent intent = new Intent(getActivity(), PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        getActivity().startService(intent);
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

        Intent intent = new Intent(getActivity(), PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }
    private PayPalPayment getThingToBuy(String paymentIntent) {
        return new PayPalPayment(new BigDecimal(amount_str), "USD", "Add Amount",
                paymentIntent);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
//                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        JSONObject object=new JSONObject(confirm.toJSONObject().toString(4));
                        Log.i(TAG, object.getJSONObject("response").getString("id"));
                        PaymentToken=object.getJSONObject("response").getString("id");
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        displayResultText("PaymentConfirmation info received from PayPal");
                       AddAmountWallet();
                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }
    protected void displayResultText(String result) {
        Log.e(TAG,result);
        Toast.makeText(
                getContext(),
                result, Toast.LENGTH_LONG)
                .show();
    }
    private void AddAmountWallet(){
        HashMap<String,String>params=new HashMap<>();
        params.put("transaction_type", "add_to_wallet");
        params.put("amount", amount_str);
        params.put("payment_type", "Card");
        params.put("user_id", user_id);
        params.put("time_zone", time_zone);
        params.put("token", PaymentToken);
        params.put("currency", "usd");
        params.put("customer", cust_id);
        ApiCallBuilder.build(getContext()).setUrl(BaseUrl.get().strips_payment())
                .setParam(params)
                .isShowProgressBar(true)
                .execute(new ApiCallBuilder.onResponse() {
                    @Override
                    public void Success(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getString("status").equalsIgnoreCase("1")) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.amountaddedinyourwallet), Toast.LENGTH_LONG).show();
                                listener.onRefresh();
                            }
                            else {
                                Toast.makeText(getActivity(), getResources().getString(R.string.somethingwrong), Toast.LENGTH_LONG).show();

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
