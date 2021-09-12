package main.com.dash.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import main.com.dash.R;
import main.com.dash.constant.BaseUrl;
import main.com.dash.constant.MySession;
import main.com.dash.databinding.FragmentChooseCardBinding;
import main.com.dash.paymentclasses.CardBean;
import main.com.dash.paymentclasses.MyaddedCards;
import main.com.dash.paymentclasses.UpdateCard;

public class FragmentChooseCard extends BottomSheetDialogFragment {
    private FragmentChooseCardBinding binding;
    private MySession mySession;
    private String user_id;
    private ArrayList<CardBean> cardBeanArrayList=new ArrayList<>();
    private onSelectCard selectCard;

    public interface onSelectCard{
    void onSelected(String id);
}
public FragmentChooseCard Callback(onSelectCard selectCard){
    this.selectCard=selectCard;
    return this;
}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog=(BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        binding= DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.fragment_choose_card,null,false);
        dialog.setContentView(binding.getRoot());
        BindView();
        return dialog;
    }

    private void BindView() {
        mySession = new MySession(getContext());
        String user_log_data = mySession.getKeyAlldata();
        if (user_log_data != null) {
            try {
                Log.e("user_log_data >>"," <<"+user_log_data);
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
        new GetAddedCard().execute();
        binding.imgBack.setOnClickListener(v->dismiss());
    }
    private class GetAddedCard extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.swipreRefresh.setRefreshing(true);
            cardBeanArrayList = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String postReceiverUrl = BaseUrl.baseurl + "get_card_paypal?";
                URL url = new URL(postReceiverUrl);
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("user_id", user_id);
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
                Log.e("Get Added Cards", ">>>>>>>>>>>>" + response);
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
            binding.swipreRefresh.setRefreshing(false);
            if (result == null) {
            } else if (!result.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Log.e("CardDetails",""+jsonObject);
                    if (jsonObject.getString("status").equalsIgnoreCase("1")) {
                        JSONObject jsonObject1 = jsonObject.getJSONObject("result");
                        JSONArray jsonArray = jsonObject1.getJSONArray("items");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject3 = jsonArray.getJSONObject(i);
                            CardBean cardBean = new CardBean();
                            cardBean.setId(jsonObject3.getString("id"));
                            cardBean.setLast4(jsonObject3.getString("number"));
                            cardBean.setExp_month(jsonObject3.getString("expire_month"));
                            cardBean.setExp_year(jsonObject3.getString("expire_year"));
                            cardBean.setBrand(jsonObject3.getString("type"));
                            cardBean.setCustomer(jsonObject3.getString("first_name"));
                            cardBean.setCard_name(jsonObject3.getString("first_name"));
                            cardBean.setSetfullcardnumber(jsonObject3.getString("number"));
                            cardBean.setSetfullexpyearmonth(jsonObject3.getString("expire_month") + "/" + jsonObject3.getString("expire_year"));
                            cardBeanArrayList.add(cardBean);

                        }
                    }
                    binding.recycle.setAdapter(new MyAdapter());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }
    }
    public class MyAdapter extends RecyclerView.Adapter<MyView>{

        @NonNull
        @Override
        public MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(getContext()).inflate(R.layout.custom_card_itemlay,parent,false);

            return new MyView(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyView holder, int position) {
            String cardbrandstr = cardBeanArrayList.get(position).getBrand();
            String carnum = cardBeanArrayList.get(position).getLast4();
            if (cardbrandstr.length() > 4) {
                cardbrandstr = cardbrandstr.substring(0, 4);
            }
            holder.savedcardnumber.setText(""+cardbrandstr+" "+" "+carnum);
            holder.validdate.setText("" + cardBeanArrayList.get(position).getSetfullexpyearmonth());
            holder.cardbrand.setText("" + cardBeanArrayList.get(position).getBrand());
            holder.cardtype.setText("" + cardBeanArrayList.get(position).getFunding());
            holder.cardtype.setAllCaps(true);
            holder.itemView.setOnClickListener(v->{
                selectCard.onSelected(cardBeanArrayList.get(position).getId());
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return cardBeanArrayList.size();
        }
    }
    public class MyView extends RecyclerView.ViewHolder{
        TextView savedcardnumber,validdate,cardbrand,cardtype;
        public MyView(@NonNull View rowView) {
            super(rowView);
             savedcardnumber = rowView.findViewById(R.id.savedcardnumber);
             validdate = rowView.findViewById(R.id.validdate);
             cardbrand = rowView.findViewById(R.id.cardbrand);
             cardtype = rowView.findViewById(R.id.cardtype);
            ImageView delete_card = rowView.findViewById(R.id.delete_card);
            ImageView update_card = rowView.findViewById(R.id.update_card);
            delete_card.setVisibility(View.GONE);
            update_card.setVisibility(View.GONE);
        }
    }

}
