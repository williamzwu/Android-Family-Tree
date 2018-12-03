package com.example.william.family;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final String EXTRA_RECEIVED = "com.example.myfirstapp.RECEIVED";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        String action = intent.getAction();
        String type = intent.getType();

        if ( (Intent.ACTION_SEND.equals(action)
//                || Intent.ACTION_SEND_MULTIPLE.equals(action)
//                || Intent.ACTION_VIEW.equals(action)
            ) && type != null) {
            if ("text/plain".equals(type) || "*/*".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }

        if ( (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) )
        {
            if ("text/plain".equals(type) || "*/*".equals(type) || "message/rfc822".equals(type)) {
                handleSendText(intent); // Handle email sent by WeChat
// debug version               handleEmailText(intent); // Handle email sent
            } else
                showReceived( DisplayMessageActivity.debug+":2\ntype\n"+type );
        }

        return;
    }

    /** called by send buttoon */
    public void sendMessage(View view)
    {
        EditText editText = (EditText)findViewById(R.id.editText);
        String message = editText.getText().toString();
        if( message.equalsIgnoreCase("setup")) {
            setup();
        } else if( message.equalsIgnoreCase("edit")) {
            editFamilyTree(view);
        } else {
            showFamilyTree(view);
        }
    }

    /** called by send buttoon */
    public void showFamilyTree(View view)
    {
        EditText editText = (EditText)findViewById(R.id.editText);
        String message = editText.getText().toString();
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void showReceived(String s)
    {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_RECEIVED, s);
        startActivity(intent);
    }

    /** called by send buttoon */
    public void editFamilyTree(View view)
    {
        EditText editText = (EditText)findViewById(R.id.editText);
        String message = editText.getText().toString();
        Intent intent = new Intent(this, EditMemberActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            showReceived( sharedText );
        }
    }

    /** called by send buttoon */
    public void importTree(View view)
    {
        EditText editText = (EditText)findViewById(R.id.editText);
        String message = editText.getText().toString();
        if( message.startsWith("position"))
            showReceived( message );
    }

    void handleEmailText(Intent intent) {
        ArrayList<Parcelable>textList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//        ArrayList<MyParcelable> textList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if( textList == null ) {
            showReceived(DisplayMessageActivity.debug + ":1\ntextList is null");
            return;
        } else {
            if( textList.size()==0 ) {
                showReceived(DisplayMessageActivity.debug + ":1\ntextList size is 0");
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if( sharedText == null )
                    showReceived(DisplayMessageActivity.debug + ":1\nsharedText is null");
                else {
                    showReceived(sharedText);
//                    String []x = sharedText.split("\n");
//                    showReceived(DisplayMessageActivity.debug + ":"+ x.length + "\n" + sharedText + "-" + x.length );
                }
                return;
            }
            Parcelable p = textList.get(0);
            if (p == null) {
                showReceived(DisplayMessageActivity.debug + ":1\ntextList.get(0) is null");
                return;
            }
            String sharedText = p.toString();
            if (sharedText != null) {
                showReceived(DisplayMessageActivity.debug + ":3\n" + sharedText);
            } else
                showReceived(DisplayMessageActivity.debug + ":3\n" + "no msg");
        }
    }

    void setup()
    {
        EditText editText = (EditText)findViewById(R.id.editText);
        String message = editText.getText().toString();
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
