package com.dapp.etc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by rs on 8/7/17.
 */
public class SendSignedTransaction extends AsyncTask<String, Void, String> {

    private Exception exception;
    private Context mContext;

    public SendSignedTransaction(Context context) {

        mContext = context;
    }

    protected String doInBackground(String... urls) {
        URL url;
        HttpURLConnection client = null;
        StringBuilder responseOutput = null;
        try {
            String urlParameters = "signedTransactionData="+urls[0];
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            url = new URL(ConnectionData.connectionString + "keymanagement/sendSignedTransaction");
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

    protected void onPostExecute(String result) {
        // TODO: check this.exception
        // TODO: do something with the feed
        System.out.println("Output:"+result);
        Toast.makeText(mContext, "Transaction Singned and sent Successfully", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(mContext.getApplicationContext(),MainActivity.class);
        mContext.startActivity(intent);
    }
}