package main.com.dash.Fragments;

import android.app.Dialog;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import main.com.dash.R;
import main.com.dash.constant.BaseUrl;
import main.com.dash.constant.MyLanguageSession;
import main.com.dash.databinding.FragmentShareTripBinding;

public class FragmentShareTrip extends BottomSheetDialogFragment {
    private FragmentShareTripBinding binding;
    private MyLanguageSession session;
    private String TripID;

    public FragmentShareTrip setTripID(String id){
        this.TripID=id;
        return this;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog=(BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        binding= DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.fragment_share_trip,null,false);
        dialog.setContentView(binding.getRoot());
        BindView();
        return dialog;
    }

    private void BindView() {
        session= MyLanguageSession.get(getActivity());
        binding.imgBack.setOnClickListener(v->dismiss());
        binding.btnShare.setOnClickListener(v->{
            if (Validation()){
               openWhatsApp();
            }
        });
    }
    private void openWhatsApp() {
        String smsNumber = binding.etMobile.getText().toString();
        String name = binding.etName.getText().toString();
        try {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            //sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, name+" share ride with you, click link blow \n"+TripID);
            sendIntent.putExtra(name, smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch(Exception e) {
            Toast.makeText(getContext(), "Error/n" + e.toString(), Toast.LENGTH_SHORT).show();
        }

    }
    private class ShareRide extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String postReceiverUrl = BaseUrl.baseurl + "get_rout_map?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("trip_id", TripID);
                params.put("name", binding.etName.getText().toString());
                params.put("mobile", binding.etMobile.getText().toString());
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
            Toast.makeText(getActivity(), "Ride share successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }
    private boolean Validation(){
        if (binding.etName.getText().toString().isEmpty()){
            binding.etName.setError("Required");
            binding.etName.requestFocus();
            return false;
        }
        if (binding.etMobile.getText().toString().isEmpty()){
            binding.etMobile.setError("Required");
            binding.etMobile.requestFocus();
            return false;
        }
        return true;
    }
}
