package com.dapp.etc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static android.R.id.input;

public class CreateKeyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_key);
    }

    protected void createAndFetchKey(View view) {
        System.out.println("Getting Data");
        /*TextView phNoTxt = (TextView) findViewById(R.id.phone_number);
        Integer input = Integer.parseInt(phNoTxt.getText().toString());*/
        TextView password = (TextView) findViewById(R.id.key_password);
        String key_password = password.getText().toString();

        new GetKeyTask(this).execute(key_password);

    }
}
class GetKeyTask extends AsyncTask<String, Void, String> {

    private Exception exception;
    private Context mContext;
    ProgressDialog progress = null;
    private String password;

    public GetKeyTask(Context context) {
        mContext = context;
    }

    protected String doInBackground(String... urls) {
        URL url;
        HttpURLConnection client = null;
        StringBuilder responseOutput = null;
        try {
            password = urls[0];
            String urlParameters = "password="+urls[0];
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            url = new URL(ConnectionData.connectionString + "keymanagement/createKey");
            client = (HttpURLConnection) url.openConnection();
            System.out.println("connection opend:" + client);
            client.setRequestMethod("POST");
            //client.setRequestProperty("api_key","3e765d10d6e29db571c08795a5bca7f73a841450428d789b58a6cda4a4a947fd");
            client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            client.setDoOutput(true);
            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            try (DataOutputStream wr = new DataOutputStream(client.getOutputStream())) {
                wr.write(postData);
            }
            outputPost.flush();
            outputPost.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line = "";
            responseOutput = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
            br.close();
            client.disconnect();

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseOutput.toString();
    }
    protected void onPreExecute() {

        System.out.println("ShowingProgress");
        progress = new ProgressDialog(mContext);
        progress.setTitle("Please Wait!!");
        progress.setMessage("Your Private Key is getting Created");
        progress.setCancelable(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
    }
    protected void onPostExecute(String result) {
        // TODO: check this.exception
        // TODO: do something with the feed
        //System.out.println("Output:"+result);
        JSONObject responseObj = null;
        FileOutputStream outputStream;
        try {
            responseObj = new JSONObject(result);
            String fileName = responseObj.getString("fileName");
            String keyObject = responseObj.getString("keyObject").toString();
            //System.out.println("fileName:"+fileName);
            //System.out.println("keyObject:"+keyObject);

            // Storing the contents of keyObject to a file with file name 'fileName'
            outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(keyObject.getBytes());
            outputStream.close();

            File keyFile = mContext.getFileStreamPath(fileName);
            System.out.println("FilePath:"+ keyFile.getPath());
            Credentials credentials = WalletUtils.loadCredentials(password,keyFile.getPath());
            System.out.println("Storing Credentials");
            Gson gson = new Gson();
            String credentialObjString = gson.toJson(credentials);


            SharedPreferences sharedPref =  mContext.getSharedPreferences("keyData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("keyFilePresent", true);
            editor.putString("keyFileName",fileName);
            editor.putString("credentials", credentialObjString);
            editor.commit();
            progress.dismiss();

            Intent intent = new Intent(mContext.getApplicationContext(),MainActivity.class);
            mContext.startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }


    }
}