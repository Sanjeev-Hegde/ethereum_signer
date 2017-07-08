package com.dapp.etc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by rs on 8/7/17.
 */
public class GetContractData extends AsyncTask<String, Void, String> {

    private Exception exception;
    private Context mContext;
    private String userAddress;

    public GetContractData(Context context,String _userAddress) {

        mContext = context;
        userAddress= _userAddress;
    }

    protected String doInBackground(String... urls) {
        URL url;
        HttpURLConnection client = null;
        StringBuilder responseOutput = null;
        try {
            String urlParameters = "referenceId="+urls[0];
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            url = new URL(ConnectionData.connectionString + "keymanagement/fetchContractData");
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
        //System.out.println("Output:"+result);
        JSONObject responseObj = null;
        FileOutputStream outputStream;

        // Web3j web3j = Web3jFactory.build(new HttpService("http://192.168.0.103:8000"));

        try {
            responseObj = new JSONObject(result);
            String contractData = responseObj.getString("contractData");
            String transactionCount = responseObj.getString("transactionCount");

            System.out.println("contractData:"+contractData);
            //System.out.println("keyObject:"+keyObject);

            // Storing the contents of keyObject to a file with file name 'fileName'

            SharedPreferences sharedPref = mContext.getSharedPreferences("keyData", Context.MODE_PRIVATE);
            String keyFileName = sharedPref.getString("keyFileName", "");
            System.out.println("key file name:" + keyFileName);

            //ClassLoader classLoader = getClass().getClassLoader();
            //String filePath  = classLoader.getResource(keyFileName).getPath();
            //System.out.println(filePath);

            File keyFile = mContext.getFileStreamPath(keyFileName);
            System.out.println(keyFile.getPath());


            //Credentials credentials = WalletUtils.loadCredentials("asdfg",keyFile.getPath());
            Gson gson = new Gson();
            String json = sharedPref.getString("credentials", "");
            Credentials credentials = gson.fromJson(json, Credentials.class);
            System.out.println("credentials:"+credentials);

            System.out.println("Address:" + userAddress);

            // get the next available nonce
     /*       EthGetTransactionCount ethGetTransactionCount = null;
            try {
                ethGetTransactionCount = web3j.ethGetTransactionCount(
                        userAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/

            BigInteger nonce = new BigInteger(transactionCount);
            System.out.println("nonce:"+nonce);
            BigInteger gasPrice = new BigInteger("5000000");
            BigInteger gasLimit = new BigInteger("5000000");
            // create our transaction
            RawTransaction rawTransaction  = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,"0x0000000000000000000000000000000000000000" , contractData);

            // sign & send our transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Hex.toHexString(signedMessage);
            System.out.println("Hex Value:"+hexValue);
            // EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            // System.out.println("txHash:"+ethSendTransaction.getError());
            new SendSignedTransaction(mContext).execute(hexValue);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
