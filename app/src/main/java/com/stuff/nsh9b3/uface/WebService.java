package com.stuff.nsh9b3.uface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.button;
import static java.lang.Integer.parseInt;

public class WebService extends Activity
{
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_AUTH_PHOTO = 2;
    String mCurrentPhotoPath;


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
    Bitmap gBitmap;
    int[][] pixels;
    byte[][] featureVector;
    BigInteger[] encryptedFV;
    boolean haveKey = false;
    boolean haveFV = false;
    String encryptedFVLoc;
    BigInteger[] publicKey;
    BigInteger[] privateKey;
    HashMap<String, String> servicesMap;
    int index = -1;
    int pixelCount = -1;
    private Thread worker;

    TextView textViewLoginService;
    TextView textViewLoginUserID;
    Button btnAuthUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent startIntent = getIntent();

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        switch(startIntent.getStringExtra(IntentInfo.START))
        {
            case IntentInfo.REGISTER:
                // Show that we are registering a service
                setContentView(R.layout.select_service);
                
                // Get Views in this Layout
                listViewServices = (ListView)findViewById(R.id.listView_services);
                textViewSelectedService = (TextView)findViewById(R.id.textView_selected_service);
                buttonGoToServicePage = (Button)findViewById(R.id.button_select_service);

                // Grab Services List from Data Server
                getServicesList();

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
            case IntentInfo.LOGIN:
                setContentView(R.layout.login_service);
                String authenticationInfo = startIntent.getStringExtra(IntentInfo.SERVICE_USERID);
                serviceString = authenticationInfo.split(" - ")[0];
                userID = authenticationInfo.split(" - ")[1];
                index = parseInt(authenticationInfo.split(" - ")[2]);


                // Get Views in this layout
                textViewLoginService = (TextView)findViewById(R.id.textView_registered_service);
                textViewLoginService.setText(authenticationInfo.split(" - ")[0]);
                textViewLoginUserID = (TextView)findViewById(R.id.textView_registered_userid);
                textViewLoginUserID.setText(authenticationInfo.split(" - ")[1]);
                btnAuthUser = (Button)findViewById(R.id.button_authenticate_user);

                // Take a picture to register
                btnAuthUser.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Used to check the Web Service if the name is valid
                        AsyncTask<Void, Void, Void> checkValidName = new AsyncTask<Void, Void, Void>()
                        {
                            boolean isValid = false;
                            String message = "";
                            @Override
                            protected Void doInBackground(Void... voids)
                            {
                                try
                                {
                                    URL url = new URL("http://10.106.70.18:3001/authenticate_user/");
                                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                    urlConnection.setRequestMethod("POST");

                                    urlConnection.setRequestProperty("Content-Type", "application/json");
                                    urlConnection.setRequestProperty("Accept", "application/json");

                                    urlConnection.connect();

                                    JSONObject jObject = new JSONObject();
                                    jObject.accumulate("User", userID);
                                    jObject.accumulate("Service", serviceString);
                                    String json = jObject.toString();

                                    OutputStream os = urlConnection.getOutputStream();
                                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                    writer.write(json);
                                    writer.flush();

                                    writer.close();
                                    os.close();

                                    InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                                    String response = convertStreamToString(is).replaceAll("\\\\", "");
                                    response = response.substring(1, response.length() - 2);
                                    JSONObject jResponse = new JSONObject(response);
                                    message = jResponse.getString("Message");

                                } catch(MalformedURLException e)
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

                                Random rand = new Random();
                                if(isValid)
                                {
                                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            }
                        }.execute();


                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try
                        {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }

                        // Continue only if the File was successfully created
                        if (photoFile.exists())
                        {
                            // Take a picture and place the information in the newly created file
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                    "com.stuff.nsh9b3.uface.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_AUTH_PHOTO);
                        }
                    }

                    }
                });
                break;
        }
    }

    // Get the list of services from the UFace Data server
    private void getServicesList()
    {
        // Get this list from server (but don't show previously registered services)
        servicesList = new ArrayList<>();
        servicesMap = new HashMap<>();

        AsyncTask<Void, Void, Void> getServices = new AsyncTask<Void, Void, Void>()
        {
            String json;
            @Override
            protected Void doInBackground(Void... voids)
            {
                try
                {
                    URL url = new URL("http://" + MainActivity.address + ":3000/service_list");
                    HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
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
                    result = result.substring(1, result.length() - 2);
                    JSONObject jObject = new JSONObject(result);
                    JSONArray jArray = jObject.getJSONArray("Services");
                    for(int i =0; i < jArray.length(); i++)
                    {
                        JSONObject service = jArray.getJSONObject(i);
                        servicesList.add(service.getString("Name"));
                        servicesMap.put(service.getString("Name"), service.getString("Url"));
                    }

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
                // Compare list from server to already registered services and don't show them
                ArrayList<String> removeList = new ArrayList<String>();
                for(String service : servicesList)
                {
                    for(Button button : MainActivity.buttonList)
                        if(service.compareTo(button.getText().toString().split(" - ")[0]) == 0)
                        {
                            removeList.add(service);
                        }
                }
                for(String remove : removeList)
                {
                    servicesList.remove(servicesList.indexOf(remove));
                }

                if(servicesList.size() == 0)
                {
                    Toast.makeText(getBaseContext(), "There are no new services!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Got the services!", Toast.LENGTH_SHORT).show();
                }

                // Show Services to User
                servicesAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1, servicesList);
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
            }

        }.execute();
    }

    // Show options for registering the selected web service
    public void showRegistration()
    {
        // Change the layout to show information on the selected service
        setContentView(R.layout.register_service);

        // Grab all the views
        textViewSelectedService = (TextView)findViewById(R.id.textView_service_name);
        editTextUserID = (EditText)findViewById(R.id.editText_user_id);
        progressBarIsValid = (ProgressBar)findViewById(R.id.progressBar_is_valid);
        imageViewValidMark = (ImageView)findViewById(R.id.imageView_valid_mark);
        btnCheckValidName = (Button)findViewById(R.id.button_check_valid_name);
        btnTakePic = (Button)findViewById(R.id.button_take_picture);
        textViewUserID = (TextView)findViewById(R.id.textView_user_id);
        textViewLabelUserId = (TextView)findViewById(R.id.textView_label_user_id);

        // Set the Title to be the name of the selected Service
        textViewSelectedService.setText(serviceString);

        // If the text is changed, be sure to NOT show options for the password
        editTextUserID.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                // Hide certain views unless the name is proven to be a valid name
                btnTakePic.setVisibility(View.GONE);
                textViewUserID.setVisibility(View.GONE);
                textViewLabelUserId.setVisibility(View.GONE);
                imageViewValidMark.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        // Validate if the name selected was already taken on the service
        btnCheckValidName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Continue only if we are accepting new input
                if(!acceptUserID)
                    return;

                // Get the user ID and make sure it's not an empty string or has bad characters
                userID = editTextUserID.getText().toString();
                if(!isGoodUserID(userID))
                    return;

                // Show the progressbar
                progressBarIsValid.setVisibility(View.VISIBLE);
                final int max = 10000;

                // Used to check the Web Service if the name is valid
                AsyncTask<Void, Void, Void> checkValidName = new AsyncTask<Void, Void, Void>()
                {
                    boolean isValid = false;
                    String message = "";
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        try
                        {
                            URL url = new URL(servicesMap.get(serviceString)+"add_user/");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");

                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");

                            urlConnection.connect();

                            JSONObject jObject = new JSONObject();
                            jObject.accumulate("User", userID);
                            jObject.accumulate("Service", serviceString);

                            String json = jObject.toString();

                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(json);
                            writer.flush();

                            writer.close();
                            os.close();

                            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertStreamToString(is).replaceAll("\\\\", "");
                            response = response.substring(1, response.length() - 2);
                            JSONObject jResponse = new JSONObject(response);
                            message = jResponse.getString("Message");
                            index = jResponse.getInt("Index");

                            if(jResponse.getBoolean("Result"))
                            {
                                isValid = true;
                            }
                            else
                            {
                                isValid = false;
                            }

                        } catch(MalformedURLException e)
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
                        if(isValid)
                        {
                            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                            imageViewValidMark.setBackgroundResource(R.drawable.check);
                            showPasswordUpload();
                        }
                        else
                        {
                            imageViewValidMark.setBackgroundResource(R.drawable.close);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            btnTakePic.setVisibility(View.GONE);
                            textViewUserID.setVisibility(View.GONE);
                            textViewLabelUserId.setVisibility(View.GONE);
                            imageViewValidMark.setVisibility(View.GONE);
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
                }.execute();
            }
        });
    }

    // Show options to finish registration after a valid name has been chosen
    public void showPasswordUpload()
    {
        // Show the user's selected VALID ID
        textViewUserID.setText(userID);

        // Show options for the user to take (take a picture)
        btnTakePic.setVisibility(View.VISIBLE);
        textViewLabelUserId.setVisibility(View.VISIBLE);
        textViewUserID.setVisibility(View.VISIBLE);

        // Take a picture to register
        btnTakePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try
                    {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }

                    // Continue only if the File was successfully created
                    if (photoFile.exists())
                    {
                        // Take a picture and place the information in the newly created file
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                "com.stuff.nsh9b3.uface.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are returning from taking a picture
        if(requestCode == REQUEST_TAKE_PHOTO)
        {
            // If no errors happened
            if(resultCode == Activity.RESULT_OK)
            {

                // Get the image and turn it to grayscale
                File file = new File(mCurrentPhotoPath);
                gBitmap = ImageTransform.toGrayscale(BitmapFactory.decodeFile(mCurrentPhotoPath));

                // Then delete the file
                file.delete();
                mCurrentPhotoPath = "";

                // Get the feature vector of this image
                pixels = ImageTransform.setGridPixelMap(gBitmap);
                pixelCount = pixels.length * pixels[0].length;
                featureVector = LBP.generateFeatureVector(pixels);

                final String password = encryptFV();

                final AsyncTask<Void, Void, Void> getOkayFromServiceAdd = new AsyncTask<Void, Void, Void>()
                {
                    String message = "";
                    boolean isValid = false;
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        try
                        {
                            URL url = new URL(servicesMap.get(serviceString)+"add_user/result_client");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");

                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");

                            urlConnection.connect();

                            JSONObject jObject = new JSONObject();
                            jObject.accumulate("User", userID);

                            String json = jObject.toString();

                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(json);
                            writer.flush();

                            writer.close();
                            os.close();

                            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertStreamToString(is).replaceAll("\\\\", "");
                            response = response.substring(1, response.length() - 2);
                            JSONObject jResponse = new JSONObject(response);
                            message = jResponse.getString("Message");

                            if(jResponse.getBoolean("Result"))
                            {
                                isValid = true;
                            }
                            else
                            {
                                isValid = false;
                            }

                        } catch(MalformedURLException e)
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
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        if(isValid)
                        {
                            Intent doneRegistering = new Intent();
                            doneRegistering.putExtra(IntentInfo.SELECTION, serviceString);
                            doneRegistering.putExtra(IntentInfo.USERID, userID);
                            doneRegistering.putExtra(IntentInfo.USERINDEX, index);
                            setResult(Activity.RESULT_OK, doneRegistering);
                            finish();
                        }

                    }
                };

                final AsyncTask<Void, Void, Void> addPasswordToDatabase = new AsyncTask<Void, Void, Void>()
                {
                    boolean isValid = false;
                    String message = "";
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        try
                        {
                            URL url = new URL("http://" + MainActivity.address + ":3000/add_password");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");

                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");

                            urlConnection.connect();

                            JSONObject jObject = new JSONObject();
                            jObject.accumulate("Password", password);
                            jObject.accumulate("Service", serviceString);
                            jObject.accumulate("Index", index);
                            jObject.accumulate("Size", pixelCount);

                            String json = jObject.toString();

                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(json);
                            writer.flush();

                            writer.close();
                            os.close();

                            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertStreamToString(is).replaceAll("\\\\", "");
                            response = response.substring(1, response.length() - 2);
                            JSONObject jResponse = new JSONObject(response);
                            message = jResponse.getString("Message");

                            if(jResponse.getBoolean("Result"))
                            {
                                isValid = true;
                            }
                            else
                            {
                                isValid = false;
                            }

                        } catch(MalformedURLException e)
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
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        if(isValid)
                        {
                            getOkayFromServiceAdd.execute();
                        }
                    }
                }.execute();
            }
            else
            {
                // Back was pressed and no picture was taken
                Toast.makeText(this, "This service hasn't been registered. Please take a selfie.", Toast.LENGTH_LONG).show();
            }
        }
        // If we are returning from taking a picture
        else if(requestCode == REQUEST_AUTH_PHOTO)
        {
            // If no errors happened
            if(resultCode == Activity.RESULT_OK)
            {

                // Get the image and turn it to grayscale
                File file = new File(mCurrentPhotoPath);
                gBitmap = ImageTransform.toGrayscale(BitmapFactory.decodeFile(mCurrentPhotoPath));

                // Then delete the file
                file.delete();
                mCurrentPhotoPath = "";

                // Get the feature vector of this image
                pixels = ImageTransform.setGridPixelMap(gBitmap);
                pixelCount = pixels.length * pixels[0].length;
                featureVector = LBP.generateFeatureVector(pixels);

                final String testPassword = encryptFV();

                /*
                final AsyncTask<Void, Void, Void> getOkayFromServiceAuth = new AsyncTask<Void, Void, Void>()
                {
                    String message = "";
                    boolean isValid = false;
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        try
                        {
                            URL url = new URL(servicesMap.get(serviceString)+"authentication_result_client");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");

                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");

                            urlConnection.connect();

                            JSONObject jObject = new JSONObject();
                            jObject.accumulate("User", userID);

                            String json = jObject.toString();

                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(json);
                            writer.flush();

                            writer.close();
                            os.close();

                            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertStreamToString(is).replaceAll("\\\\", "");
                            response = response.substring(1, response.length() - 2);
                            JSONObject jResponse = new JSONObject(response);
                            message = jResponse.getString("Message");

                            if(jResponse.getBoolean("Result"))
                            {
                                isValid = true;
                            }
                            else
                            {
                                isValid = false;
                            }

                        } catch(MalformedURLException e)
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
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        if(isValid)
                        {
                            Intent doneAuthenticating = new Intent();
                            setResult(Activity.RESULT_OK, doneAuthenticating);
                            finish();
                        }

                    }
                };

                final AsyncTask<Void, Void, Void> authPasswordWithDatabase = new AsyncTask<Void, Void, Void>()
                {
                    boolean isValid = false;
                    String message = "";
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        try
                        {
                            URL url = new URL("http://" + MainActivity.address + ":3000/authenticate_password");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");

                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");

                            urlConnection.connect();

                            JSONObject jObject = new JSONObject();
                            jObject.accumulate("Password", testPassword);
                            jObject.accumulate("Service", serviceString);
                            jObject.accumulate("Index", index);
                            jObject.accumulate("Size", pixelCount);

                            String json = jObject.toString();

                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(json);
                            writer.flush();

                            writer.close();
                            os.close();

                            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertStreamToString(is).replaceAll("\\\\", "");
                            response = response.substring(1, response.length() - 2);
                            JSONObject jResponse = new JSONObject(response);
                            message = jResponse.getString("Message");

                            if(jResponse.getBoolean("Result"))
                            {
                                isValid = true;
                            }
                            else
                            {
                                isValid = false;
                            }

                        } catch(MalformedURLException e)
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
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        if(isValid)
                        {
                            getOkayFromServiceAuth.execute();
                        }
                    }
                };

                final AsyncTask<Void, Void, Void> authUserWithDatabase = new AsyncTask<Void, Void, Void>()
                {
                    boolean isValid = false;
                    String message = "";
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        try
                        {
                            URL url = new URL(servicesMap.get(serviceString)+"authenticate_user/");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");

                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");

                            urlConnection.connect();

                            JSONObject jObject = new JSONObject();
                            jObject.accumulate("Service", serviceString);
                            jObject.accumulate("User", userID);

                            String json = jObject.toString();

                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(json);
                            writer.flush();

                            writer.close();
                            os.close();

                            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertStreamToString(is).replaceAll("\\\\", "");
                            response = response.substring(1, response.length() - 2);
                            JSONObject jResponse = new JSONObject(response);
                            message = jResponse.getString("Message");

                            if(jResponse.getBoolean("Result"))
                            {
                                isValid = true;
                            }
                            else
                            {
                                isValid = false;
                            }

                        } catch(MalformedURLException e)
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
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                        if(isValid)
                        {
                            authPasswordWithDatabase.execute();
                        }
                        else
                        {
                            pixelCount = -1;
                        }
                    }
                };
                */

                final Thread authenticateUser = new Thread(new Runnable()
                {
                    boolean isValid = false;
                    String message = "";

                    @Override
                    public void run()
                    {
                        try
                        {
                            URL url = new URL("http://131.151.8.33:3001/" + "authenticate_user/");
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("POST");

                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("Accept", "application/json");

                            urlConnection.connect();

                            JSONObject jObject = new JSONObject();
                            jObject.accumulate("Service", serviceString);
                            jObject.accumulate("User", userID);

                            String json = jObject.toString();

                            OutputStream os = urlConnection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(json);
                            writer.flush();

                            writer.close();
                            os.close();

                            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertStreamToString(is).replaceAll("\\\\", "");
                            response = response.substring(1, response.length() - 2);
                            JSONObject jResponse = new JSONObject(response);
                            message = jResponse.getString("Message");

                            if (jResponse.getBoolean("Result"))
                            {
                                isValid = true;
                            } else
                            {
                                isValid = false;
                            }

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

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        });

                        if(isValid)
                        {
                            isValid = false;
                            try
                            {
                                URL url = new URL("http://" + MainActivity.address + ":3000/authenticate_password");
                                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                urlConnection.setRequestMethod("POST");

                                urlConnection.setRequestProperty("Content-Type", "application/json");
                                urlConnection.setRequestProperty("Accept", "application/json");

                                urlConnection.connect();

                                JSONObject jObject = new JSONObject();
                                jObject.accumulate("Password", testPassword);
                                jObject.accumulate("Service", serviceString);
                                jObject.accumulate("Index", index);
                                jObject.accumulate("Size", pixelCount);

                                String json = jObject.toString();

                                OutputStream os = urlConnection.getOutputStream();
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                writer.write(json);
                                writer.flush();

                                writer.close();
                                os.close();

                                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                                String response = convertStreamToString(is).replaceAll("\\\\", "");
                                response = response.substring(1, response.length() - 2);
                                JSONObject jResponse = new JSONObject(response);
                                message = jResponse.getString("Message");

                                if (jResponse.getBoolean("Result"))
                                {
                                    isValid = true;
                                } else
                                {
                                    isValid = false;
                                }

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

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                            if(isValid)
                            {
                                isValid = false;

                                try
                                {
                                    URL url = new URL("http://131.151.8.33:3001/" + "authentication_result_client");
                                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                    urlConnection.setRequestMethod("POST");

                                    urlConnection.setRequestProperty("Content-Type", "application/json");
                                    urlConnection.setRequestProperty("Accept", "application/json");

                                    urlConnection.connect();

                                    JSONObject jObject = new JSONObject();
                                    jObject.accumulate("User", userID);

                                    String json = jObject.toString();

                                    OutputStream os = urlConnection.getOutputStream();
                                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                    writer.write(json);
                                    writer.flush();

                                    writer.close();
                                    os.close();

                                    InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                                    String response = convertStreamToString(is).replaceAll("\\\\", "");
                                    response = response.substring(1, response.length() - 2);
                                    JSONObject jResponse = new JSONObject(response);
                                    message = jResponse.getString("Message");

                                    if (jResponse.getBoolean("Result"))
                                    {
                                        isValid = true;
                                    } else
                                    {
                                        isValid = false;
                                    }

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

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                                        if (isValid)
                                        {
                                            Intent doneAuthenticating = new Intent();
                                            setResult(Activity.RESULT_OK, doneAuthenticating);
                                            finish();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                authenticateUser.start();
            }
            else
            {
                // Back was pressed and no picture was taken
                Toast.makeText(this, "This service hasn't been registered. Please take a selfie.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Creates a temporary image
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Encrypts the feature vector using paillier encryption
    // Outputs the feature vector to a separate file (till it's sent off)
    private String encryptFV()
    {
        encryptedFV = new BigInteger[featureVector.length];

        for(int i = 0; i < featureVector.length; i++)
        {
            BigInteger bigInt = new BigInteger(featureVector[i]);
            encryptedFV[i] = MainActivity.paillier.Encryption(bigInt);
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < encryptedFV.length; i++)
        {
            sb.append(encryptedFV[i]).append(" ");
        }

        return sb.toString();
    }

    // Checks if the username contains only valid characters
    // TODO: stop /n from working at the end of a name
    private boolean isGoodUserID(String name)
    {
        boolean isGood = true;

        // Alphanumeric characters only
        String pattern = "^[a-zA-Z0-9]*$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r. matcher(name);

        // Make sure the name is long enough
        if(name.length() < 8)
        {
            isGood = false;
            Toast.makeText(this, "Please use at least 8 characters", Toast.LENGTH_LONG).show();
        }
        // Make sure the name contains only alphanumeric characters
        else if(!m.find())
        {
            isGood = false;
            Toast.makeText(this, "Please only use alphanumeric characters", Toast.LENGTH_LONG).show();
        }

        return isGood;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
