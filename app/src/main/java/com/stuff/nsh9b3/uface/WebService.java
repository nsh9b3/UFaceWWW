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
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    Bitmap gBitmap;
    int[][] pixels;
    byte[][] featureVector;
    BigInteger[] encryptedFV;
    Paillier paillier;
    boolean haveKey = false;
    boolean haveFV = false;
    String encryptedFVLoc;
    BigInteger[] publicKey;
    BigInteger[] privateKey;

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

    // Get the list of services from the UFace Data server
    private void getServicesList()
    {
        // Get this list from server (but don't show previously registered services)
        // TODO: talk to an actual server
        servicesList = new ArrayList<>();
        servicesList.add("Bank");
        servicesList.add("Health");
        servicesList.add("School");
        servicesList.add("etc.");

        // TODO: Compare list from server to already registered services and don't show them
        /*for(String service : servicesList)
        {

        }*/
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
                            Toast.makeText(getApplicationContext(), "This ID has already been used for registration. Please pick another.", Toast.LENGTH_LONG).show();
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
                        paillier = new Paillier("130608821557841900443360573990060231600720639068635884870756456142930941480804192983990854478613114648787582498011693243077173475850692956250017555398483238328099541842782019438340249031750695665874603477846790009046878241815288144729763574658057914680190158946544876393438515631155150484847668488183868105893",
                                "8715161750352038003855234302887932079801138728954207932245027351191172517353630598400311130000817553939984622880281320389904272535238541835806448941586218917232876912910443137583132034235838974323100942032187813695301571205816522358575761111283034852749185254197245814808374803657021294470498798380881148553580717433731785726037233004579908831642460826767151249645068973275412116024906036803392245770242535027189122010103221811510344288569977102817252563630275153915092831480899329906091413418200391735742340524277988104603739915616826436232606625654946447897928406710231256856380521783511388678981419291024236658823",
                                "65304410778920950221680286995030115800360319534317942435378228071465470740402096491995427239306557324393791249005846621538586737925346478125008777699241607680005207673965152353188838145624327895176457997361602266207450001402205712660875434148564101191018822712233348241627287112305993056373620460949250660454",
                                "44434977212474549420262809225036713658433520993151896021541158481720504859866799959494127361520327480145280505284544157413674629448437900830766576437226947299954992686129603112736407528759331072466437778674243883296282585308483025725199281344730934149325912450292620111994432444297320422768672241197675832052",
                                "1024");
                        publicKey = paillier.getPublicKey();
                        privateKey = paillier.getPrivateKey();

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid)
                    {
                        super.onPostExecute(aVoid);

                        // If this finishes after the camera encrypt the image
                        if(haveFV)
                        {
                            encryptedFVLoc = encryptFV();
                        } else
                        {
                            haveKey = true;
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
                featureVector = LBP.generateFeatureVector(pixels);

                // If we have the key already then encrypt the image; otherwise, wait to get the public key
                if(haveKey)
                {
                    encryptedFVLoc = encryptFV();
                } else
                {
                    haveFV = true;
                }
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
            encryptedFV[i] = paillier.Encryption(bigInt);
        }

        // Write the ciphertext to a File
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // context being the Activity pointer
        File outputFile = null;
        try
        {
            outputFile = File.createTempFile("encryptedHistogram", ".txt", outputDir);
        } catch (Exception e)
        {
            return null;
        }

        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(outputFile.getAbsoluteFile()));
            for (int i = 0; i < encryptedFV.length; i++)
            {
                writer.write(encryptedFV[i] + " ");
            }
        } catch (Exception e)
        {
            return null;
        } finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            } catch (IOException e)
            {
                return null;
            }
        }

        Log.d("TAG", outputFile.getAbsolutePath());
        return outputFile.getAbsolutePath();
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
}
