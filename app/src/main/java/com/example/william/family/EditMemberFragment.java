package com.example.william.family;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;

/**
 * A placeholder fragment containing a simple view.
 */
public class EditMemberFragment extends Fragment {

    public EditMemberFragment() {
    }

    protected void addContent( View v )
    {
        TableRow model_person = (TableRow)v.findViewById(R.id.model_person);

        CheckBox model_select = (CheckBox)v.findViewById(R.id.model_select);
        TextView model_name = (TextView)v.findViewById(R.id.model_name);
        ToggleButton model_sex = (ToggleButton)v.findViewById(R.id.model_sex);

        TableLayout personList = (TableLayout)v.findViewById(R.id.personlist);

        if( model_person==null || model_select==null || model_name==null || model_sex==null || personList==null )
            return;

        String err = "";
        try
        {
            /* read the resource folder
            String[] errlist = getResources().getAssets().list("");
            for( int x=0; x<errlist.length; ++x )
            {
                String[] xx = getResources().getAssets().list(errlist[x]);
                int l = xx.length;
            }
            */
//            String myname = getResources().getResourceName();
//            InputStream is = getResources().getAssets().open(myname+".csv");

            File pathOut = model_person.getContext().getExternalFilesDir(null);
            File fOut = new File(pathOut, "myfamily.csv");
//            String ex = fOut.exists() ? " exists " : " not exists ";
//            String m = " External file "+ fOut.getAbsolutePath() + ex;
            String m = fOut.exists() ? fOut.getAbsolutePath() : "Sample";
            TextView msg = (TextView)v.findViewById(R.id.loadedfile);
            msg.setText(m);
            InputStream is = fOut.exists()
                    ? new FileInputStream(fOut) // read from sdcard
                    : getResources().openRawResource( R.raw.myfamily ); // read from raw resource
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String st;
            int state = -1;
            while ((st=br.readLine())!=null)
            {
                if( st.startsWith("person")) { state = 0; continue; }
                if( st.startsWith("position")) { state = 1; continue; }
                if( st.startsWith("family")) { state = 2; continue; }
                if( st.startsWith("jz")) { state = 3; continue; }
                switch (state) {
                    case 0:
                        String[] person = st.split(",");
                        TableRow oneperson = new TableRow(model_person.getContext());

                        CheckBox one_select = new CheckBox(oneperson.getContext());
                        one_select.setLayoutParams(model_select.getLayoutParams());
                        oneperson.addView(one_select);

                        TextView one_name = new TextView(oneperson.getContext());
                        one_name.setText( person[0] );
                        one_name.setLayoutParams(model_name.getLayoutParams());
                        one_name.setMinWidth(model_name.getMinWidth());
                        oneperson.addView(one_name);

                        ToggleButton one_sex = new ToggleButton(oneperson.getContext());
                        one_sex.setLayoutParams(model_sex.getLayoutParams());
                        one_sex.setTextOn(model_sex.getTextOn());
                        one_sex.setTextOff(model_sex.getTextOff());
                        one_sex.setChecked(person[1].equalsIgnoreCase("female"));
                        oneperson.addView(one_sex);

                        personList.addView(oneperson);
                        continue;
                    default:
                        continue;
                }
            }

            br.close();

        } catch (IOException e)
        {
//            err = e.toString();
        }

        for( int i=0; i<50; ++i ) {
            // one person
            Integer ii = new Integer(i);
            TableRow oneperson = new TableRow(model_person.getContext());

            CheckBox one_select = new CheckBox(oneperson.getContext());
            one_select.setLayoutParams(model_select.getLayoutParams());
            oneperson.addView(one_select);

            TextView one_name = new TextView(oneperson.getContext());
            one_name.setText( (i%2==0 ? "林黛玉" : "贾宝玉")+ii.toString()+err );
            one_name.setLayoutParams(model_name.getLayoutParams());
            one_name.setMinWidth(model_name.getMinWidth());
            oneperson.addView(one_name);

            ToggleButton one_sex = new ToggleButton(oneperson.getContext());
            one_sex.setLayoutParams(model_sex.getLayoutParams());
            one_sex.setTextOn(model_sex.getTextOn());
            one_sex.setTextOff(model_sex.getTextOff());
            one_sex.setChecked(i%2==0);
//            one_sex.setEditableFactory(model_sex.);
            oneperson.addView(one_sex);

            personList.addView(oneperson);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_member, container, false);
        addContent( v );
        return v;
    }

    /*
    @Override
    public View onStart()
    */
}
