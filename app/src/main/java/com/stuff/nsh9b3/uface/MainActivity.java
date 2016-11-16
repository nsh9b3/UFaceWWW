package com.stuff.nsh9b3.uface;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener
{
    // Intent IDs
    public final static int NEW_SERVICE_INTENT = 1;
    public final static int LOGIN_SERVICE_INTENT = 2;

    // List of Buttons (services) a user can select
    public static ArrayList<Button> buttonList;

    // List of layouts (rows of services) to place new services
    private ArrayList<LinearLayout> layoutList;

    // These are offset values so btns and layouts have different IDs
    private final static int btnIDOffset = 1000;
    private final static int layIDOffset = 100;

    // Paillier encrption public key information
    public static Paillier paillier;

    // Base Address for servers
    // This will need to change as soon as the servers stop changing
    public static String address = "131.151.8.33";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the add button and set the listener to this activity (which is a clickListener)
        final Button addButton = (Button)findViewById(R.id.btn_add);
        addButton.setOnClickListener(this);

        // Start a background task to get a new public key
        AsyncTask<Void, Void, Void> getPublicKey = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                try
                {
                    URL url = new URL("http://" + address + ":3002/public_key");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    urlConnection.connect();

                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    String result = sb.toString().replaceAll("\\\\", "");
                    JSONObject jObject = new JSONObject(result);
                    JSONObject public_key = jObject.getJSONObject("Public");

                    paillier = new Paillier(public_key.getString("n"), public_key.getString("g"), public_key.getString("size"));

                } catch (MalformedURLException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);
                if(paillier != null)
                {
                    Toast.makeText(getBaseContext(), "Got the public key!", Toast.LENGTH_SHORT).show();
                    addButton.setVisibility(View.VISIBLE);
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Could not get the public key!", Toast.LENGTH_SHORT).show();
                    addButton.setVisibility(View.GONE);
                }
            }

        }.execute();

        // Gets the list of buttons and layouts
        getServices();
    }

    // On Click Listener
    @Override
    public void onClick(View view)
    {
        // First figure out what was pressed
        switch(view.getId())
        {
            // If the add button was pressed
            // Register a new service
            case R.id.btn_add:
                Intent newServiceIntent = new Intent(this, WebService.class);
                newServiceIntent.putExtra(IntentInfo.START, IntentInfo.REGISTER);
                startActivityForResult(newServiceIntent, NEW_SERVICE_INTENT);
                break;
            // If any other button was pressed (the rest are all services)
            // Authenticate a created service
            default:
                Button pressedButton = (Button)view;
                Intent selectServiceIntent = new Intent(this, WebService.class);
                selectServiceIntent.putExtra(IntentInfo.START, IntentInfo.LOGIN);
                selectServiceIntent.putExtra(IntentInfo.SERVICE_USERID, pressedButton.getText());
                startActivityForResult(selectServiceIntent, LOGIN_SERVICE_INTENT);
                break;
        }
    }

    // Called when the previous activity returns to this activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // If we created a new service
        if(requestCode == NEW_SERVICE_INTENT)
        {
            if(data == null)
            {
                Toast.makeText(this, "No Service Registered with!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean createdService = false;
            String serviceName = data.getStringExtra(IntentInfo.SELECTION);
            String userID = data.getStringExtra(IntentInfo.USERID);
            int userIndex = data.getIntExtra(IntentInfo.USERINDEX, -1);
            for(Button btn : buttonList)
            {
                if(btn.getText().toString().compareTo(serviceName) == 0)
                {
                    createdService = true;
                    break;
                }
            }
            if(!createdService)
            {
                makeNewServiceIcon(serviceName, userID, userIndex);
            }
            else
                Toast.makeText(this, "You've already registered with this service", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeNewServiceIcon(String serviceName, String userID, int userIndex)
    {
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.parent_ll);

        int row = (int) (buttonList.size() / 3);
        int col = buttonList.size() % 3;

        String layoutID = "row_" + row;
        String buttonID = "btn_" + row + "_" + col + "-" + serviceName;

        LinearLayout childLayout;

        // Make a new row to place the button
        if(col == 0)
        {
            childLayout = new LinearLayout(this);
            childLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            childLayout.setLayoutParams(layoutParams);
            childLayout.setWeightSum(3);

            childLayout.setId(layoutList.size() + layIDOffset);

            parentLayout.addView(childLayout);
            layoutList.add(childLayout);
        }
        // Grab an existing location for the button
        else
        {
            childLayout = (LinearLayout)findViewById(layoutList.get((layoutList.size() - 1)).getId());
        }

        Button newServiceBtn = new Button(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(new GridView.LayoutParams(0, (int)(getResources().getDisplayMetrics().density * 100 + 0.5f)));
        btnParams.setMargins((int)getResources().getDimension(R.dimen.activity_horizontal_margin),
                (int)getResources().getDimension(R.dimen.activity_vertical_margin),
                (int)getResources().getDimension(R.dimen.activity_horizontal_margin),
                (int)getResources().getDimension(R.dimen.activity_vertical_margin));
        btnParams.weight = 1;

        newServiceBtn.setLayoutParams(btnParams);
        newServiceBtn.setText(serviceName + " - " + userID + " - " + userIndex);
        newServiceBtn.setId(buttonList.size() + btnIDOffset);
        newServiceBtn.setOnClickListener(this);

        childLayout.addView(newServiceBtn);
        buttonList.add(newServiceBtn);
    }

    private void getServices()
    {
        // TODO: Use sharedPreferences
        buttonList = new ArrayList<>();
        layoutList = new ArrayList<>();
    }
}
