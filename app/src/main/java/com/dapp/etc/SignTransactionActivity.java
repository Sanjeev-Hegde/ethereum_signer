package com.dapp.etc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class SignTransactionActivity extends AppCompatActivity {

    private String referenceId = null;
    private String userAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_transaction);
        String dataString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                dataString= null;
                userAddress = null;
            } else {
                dataString= extras.getString("dataString");
                userAddress = extras.getString("userAddress");
            }
        } else {
            dataString= (String) savedInstanceState.getSerializable("dataString");
            userAddress= (String) savedInstanceState.getSerializable("userAddress");
        }
        if(dataString!=null && userAddress!=null){
            JSONObject dataObj = null;
            try {
                dataObj = new JSONObject(dataString);
                TextView tv1 = (TextView) findViewById(R.id.displayGoodsDescription);
                tv1.setText(dataObj.getJSONObject("data").getString("goodsDescription")); //hardcoded for now

                TextView tv2 = (TextView) findViewById(R.id.displayPrice);
                tv2.setText(dataObj.getJSONObject("data").getString("price"));

                referenceId = dataObj.getString("referenceId");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        System.out.println("dataString:"+dataString);
    }

    protected void ApproveSidnedTransaction(View view) {
        System.out.println("Approving Signed Transaction");
        if(referenceId!=null && userAddress!= null){
            new GetContractData(this,userAddress).execute(referenceId);
        }
    }
}
