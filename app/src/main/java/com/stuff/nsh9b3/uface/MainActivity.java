package com.stuff.nsh9b3.uface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener
{
    // Intent IDs
    private final static int NEW_SERVICE_INTENT = 1;

    // List of Buttons (services) a user can select
    public ArrayList<Button> buttonList;

    // List of layouts (rows of services) to place new services
    private ArrayList<LinearLayout> layoutList;

    // These are offset values so btns and layouts have different IDs
    private final static int btnIDOffset = 1000;
    private final static int layIDOffset = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the add button and set the listener to this activity (which is a clickListener)
        Button addButton = (Button)findViewById(R.id.btn_add);
        addButton.setOnClickListener(this);

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
                newServiceIntent.putExtra(ServiceTask.START, ServiceTask.REGISTER);
                startActivityForResult(newServiceIntent, NEW_SERVICE_INTENT);
                break;
            // If any other button was pressed (the rest are all services)
            // Authenticate a created service
            default:
                // TODO: something other than nothing
                Button pressedButton = (Button)view;
                Intent selectServiceIntent = new Intent(this, WebService.class);
                Toast.makeText(this, ((Button) view).getText().toString(), Toast.LENGTH_SHORT).show();
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
            // And everything went okay
            if(resultCode == Activity.RESULT_OK)
            {

                boolean createdService = false;
                String serviceName = data.getStringExtra(ServiceTask.SELECTION);
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
                    makeNewServiceIcon(data.getStringExtra(ServiceTask.SELECTION));
                }
                else
                    Toast.makeText(this, "You've already registered with this service", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Back was pressed and no service was selected
            }
        }
    }

    private void makeNewServiceIcon(String serviceName)
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
        newServiceBtn.setText(serviceName);
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
