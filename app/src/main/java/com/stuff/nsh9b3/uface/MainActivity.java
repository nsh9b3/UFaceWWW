package com.stuff.nsh9b3.uface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class MainActivity extends Activity implements View.OnClickListener
{
    private final static String INTENT_EXTRA = "com.stuff.nsh9b3.uface.MainActivity";
    private final static int NEW_SERVICE_INTENT = 1;
    
    private ArrayList<Button> buttonList;
    private ArrayList<LinearLayout> layoutList;
    
    private final static int btnOffset = 1000;
    private final static int layOffset = 100;

    private int nextId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addButton = (Button)findViewById(R.id.btn_add);
        addButton.setOnClickListener(this);

        buttonList = new ArrayList<>();
        layoutList = new ArrayList<>();
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.btn_add:
                Intent newServiceIntent = new Intent(this, WebService.class);
                newServiceIntent.putExtra(ServiceTask.START, ServiceTask.REGISTER);
                startActivityForResult(newServiceIntent, NEW_SERVICE_INTENT);
                break;
            default:
                Button pressedButton = (Button)view;
                Intent selectServiceIntent = new Intent(this, WebService.class);
                Toast.makeText(this, ((Button) view).getText().toString(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == NEW_SERVICE_INTENT)
        {
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

            childLayout.setId(layoutList.size() + layOffset);

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
        newServiceBtn.setId(buttonList.size() + btnOffset);
        newServiceBtn.setOnClickListener(this);

        childLayout.addView(newServiceBtn);
        buttonList.add(newServiceBtn);
    }
}
