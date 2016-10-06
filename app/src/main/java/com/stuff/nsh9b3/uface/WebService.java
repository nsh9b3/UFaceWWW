package com.stuff.nsh9b3.uface;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class WebService extends Activity
{
    ArrayList<String> servicesList;
    ListView listViewServices;
    ArrayAdapter servicesAdapter;
    TextView textViewSelectedService;
    TextView textViewUserID;
    TextView textViewLabelUserId;
    Button buttonGoToServicePage;
    Button btnCheckValidName;
    Button btnTakePic;
    ProgressBar progressBarIsValid;
    ImageView imageViewValidMark;
    EditText editTextUserID;
    String serviceString = "";
    String userID = "";
    boolean acceptUserID = true;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent startIntent = getIntent();

        switch(startIntent.getStringExtra(ServiceTask.START))
        {
            case ServiceTask.REGISTER:
                setContentView(R.layout.select_service);
                
                // Get Views in this Layout
                listViewServices = (ListView)findViewById(R.id.listView_services);
                textViewSelectedService = (TextView)findViewById(R.id.textView_selected_service);
                buttonGoToServicePage = (Button)findViewById(R.id.button_select_service);

                // Grab Services List
                getServicesList();

                // Show Services to User
                servicesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, servicesList);
                listViewServices.setAdapter(servicesAdapter);
                listViewServices.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                    {
                        // Show button now that the user made a selection
                        buttonGoToServicePage.setVisibility(View.VISIBLE);

                        // Grab the user's selection
                        serviceString = (String) adapterView.getItemAtPosition(i);

                        // Show it as the selected service
                        textViewSelectedService.setText(serviceString);
                    }
                });

                // Show the services information page (if pressed)
                buttonGoToServicePage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // As long as the user made a selection
                        if(serviceString.compareTo("") != 0)
                            showRegistration();
                    }
                });

                break;
        }
    }

    private void getServicesList()
    {
        // Get this list from server
        servicesList = new ArrayList<>();
        servicesList.add("Test1");
        servicesList.add("Test2");
        servicesList.add("Test3");
        servicesList.add("Test4");
        servicesList.add("Test5");
        servicesList.add("Test6");
        servicesList.add("Test7");
        servicesList.add("Test8");
        servicesList.add("Test9");
        servicesList.add("Test10");
        servicesList.add("Test11");
        servicesList.add("Test12");
    }

    public void showRegistration()
    {
        setContentView(R.layout.register_service);

        textViewSelectedService = (TextView)findViewById(R.id.textView_service_name);
        editTextUserID = (EditText)findViewById(R.id.editText_user_id);
        progressBarIsValid = (ProgressBar)findViewById(R.id.progressBar_is_valid);
        imageViewValidMark = (ImageView)findViewById(R.id.imageView_valid_mark);
        btnCheckValidName = (Button)findViewById(R.id.button_check_valid_name);
        btnTakePic = (Button)findViewById(R.id.button_take_picture);
        textViewUserID = (TextView)findViewById(R.id.textView_user_id);
        textViewLabelUserId = (TextView)findViewById(R.id.textView_label_user_id);

        textViewSelectedService.setText(serviceString);
        btnCheckValidName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!acceptUserID)
                    return;
                userID = editTextUserID.getText().toString();
                if(userID.compareTo("") == 0)
                    return;
                btnTakePic.setVisibility(View.GONE);
                textViewUserID.setVisibility(View.GONE);
                textViewLabelUserId.setVisibility(View.GONE);
                imageViewValidMark.setVisibility(View.GONE);
                progressBarIsValid.setVisibility(View.VISIBLE);
                final int max = 10000;

                AsyncTask<Void, Void, Void> test = new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        while(counter < max)
                        {
                            counter++;
                            publishProgress();
                        }
                        return null;
                    }

                    @Override
                    protected void onPreExecute()
                    {
                        super.onPreExecute();
                        acceptUserID = false;
                        counter = 0;
                        progressBarIsValid.setProgress(counter);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid)
                    {
                        super.onPostExecute(aVoid);
                        progressBarIsValid.setVisibility(View.GONE);
                        Random rand = new Random();
                        if(true)
                        {
                            imageViewValidMark.setBackgroundResource(R.drawable.check);
                            showPasswordUpload();
                        }
                        else
                        {
                            imageViewValidMark.setBackgroundResource(R.drawable.close);
                            Toast.makeText(getApplicationContext(), "Please enter a different User ID", Toast.LENGTH_LONG).show();
                        }
                        imageViewValidMark.setVisibility(View.VISIBLE);
                        acceptUserID = true;
                    }

                    @Override
                    protected void onProgressUpdate(Void... values)
                    {
                        super.onProgressUpdate(values);
                        progressBarIsValid.setProgress(counter/max);
                    }
                };

                test.execute();

            }
        });
    }

    public void showPasswordUpload()
    {
        textViewUserID.setText(userID);

        btnTakePic.setVisibility(View.VISIBLE);
        textViewLabelUserId.setVisibility(View.VISIBLE);
        textViewUserID.setVisibility(View.VISIBLE);
    }
}
