package com.stuff.nsh9b3.uface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class WebService extends Activity
{
    static final int REQUEST_TAKE_PHOTO = 1;
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
    Bitmap cBitmap;
    Bitmap gBitmap;
    int[][] pixels;
    byte[][] featureVector;
    int[] publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent startIntent = getIntent();

        switch(startIntent.getStringExtra(ServiceTask.START))
        {
            case ServiceTask.REGISTER:
                // Show that we are registering a service
                setContentView(R.layout.select_service);
                
                // Get Views in this Layout
                listViewServices = (ListView)findViewById(R.id.listView_services);
                textViewSelectedService = (TextView)findViewById(R.id.textView_selected_service);
                buttonGoToServicePage = (Button)findViewById(R.id.button_select_service);

                // Grab Services List from Data Server
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
        // TODO: talk to an actual server
        servicesList = new ArrayList<>();
        servicesList.add("Bank");
        servicesList.add("Health");
        servicesList.add("School");
        servicesList.add("etc.");
    }

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

        // Validate if the name selected was already taken on the service
        btnCheckValidName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Continue only if we are accepting new input
                if(!acceptUserID)
                    return;

                // Get the user ID and make sure it's not an empty string
                userID = editTextUserID.getText().toString();
                if(userID.compareTo("") == 0)
                    return;

                // Hide certain views unless the name is proven to be a valid name
                btnTakePic.setVisibility(View.GONE);
                textViewUserID.setVisibility(View.GONE);
                textViewLabelUserId.setVisibility(View.GONE);
                imageViewValidMark.setVisibility(View.GONE);
                progressBarIsValid.setVisibility(View.VISIBLE);
                final int max = 10000;

                // Used to check the Web Service if the name is valid
                // TODO: work with a server
                AsyncTask<Void, Void, Void> checkValidName = new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... voids)
                    {
                        // TODO: do something and not nothing
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
                        //TODO: Change this when done talking to a server
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

                checkValidName.execute();

            }
        });
    }

    public void showPasswordUpload()
    {
        // Show the user's selected VALID ID
        textViewUserID.setText(userID);

        // Show options for the user to take (take a picture)
        btnTakePic.setVisibility(View.VISIBLE);
        textViewLabelUserId.setVisibility(View.VISIBLE);
        textViewUserID.setVisibility(View.VISIBLE);

        // Take a picture to register
        // Also grab a newly generated public key from the Key Server
        // TODO: talk to the key server
        btnTakePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Start a background task to get a new public key
                AsyncTask<Void, Void, Void> getPublicKey = new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        Paillier paillier = new Paillier();

                        return null;
                    }
                };

                getPublicKey.execute();

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }

                    // Continue only if the File was successfully created
                    if (photoFile.exists()) {
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

        if(requestCode == REQUEST_TAKE_PHOTO)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                File file = new File(mCurrentPhotoPath);
                if(file.exists())
                {
                    cBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    gBitmap = ImageTransform.toGrayscale(cBitmap);
                    pixels = ImageTransform.setGridPixelMap(gBitmap);
                    featureVector = LBP.generateFeatureVector(pixels);
                }
                else
                {
                    // FIle wasn't created...
                }
            }
            else
            {
                // Back was pressed and no picture was taken
            }
        }
    }

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
}
