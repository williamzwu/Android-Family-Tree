package com.example.william.family;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;

public class DisplayMessageActivity extends AppCompatActivity {

//    private String html;
//    private String familyjs;
    public static final String debug = "debug132435";
    static final String emailtag = "=*=*=*=*= family =-=-=-=-=";
    static String lastReceived = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispplay_message);

        Intent intent = getIntent();
        String familyReceived = intent.getStringExtra(MainActivity.EXTRA_RECEIVED);
        String familyName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        if( familyReceived != null )
        {
            if( familyReceived.startsWith(debug)) {
                String[] s = familyReceived.split("\n");
                String[] r1 = s[0].split(":");
                int n = r1.length>1?Integer.parseInt(r1[1]):1;
                if( n > s.length-1 ) n = s.length - 1;
                if( n < 1 )
                    ShowFamilyDescription("Not found", "Not found");
                else
                    ShowFamilyDescription(s[n], s[n]);
                return;
            }
            lastReceived = familyReceived;
            DisplayFamilyTreeFromString( familyReceived );
        }
        if( familyReceived == null &&  familyName != null ) {
            if (familyName.startsWith("share ")) {
                String[] s = familyName.split(" ");
                if (s.length > 1)
                    ShareFamily(s[1]);
            } else if (familyName.startsWith("email ")) {
                String[] s = familyName.split(" ");
                if (s.length > 1)
                    EmailFamily(s[1]);
            } else if (familyName.startsWith("list")) {
                ListFamilies();
            } else if (lastReceived != null && familyName.startsWith("received")) {
                DisplayFamilyTreeFromString(lastReceived);
            } else if (lastReceived != null && familyName.startsWith("text received")) {
                ShowFamilyDescription("Last received", lastReceived);
            } else if (lastReceived != null && familyName.startsWith("saveas ")) {
                String[] s = familyName.split(" ");
                if (s.length > 1) {
                    SaveFamilyTree(lastReceived, s[1]);
                    ShowFamilyDescription("Saved last received family description as " + s[1], "");
                }
            } else
                DisplayFamilyTreeFromFile(familyName);
        }
    }
    protected void ListFamilies() {
        Context context = getApplicationContext();
//        String [] list = context.databaseList();
        StringBuffer text = new StringBuffer();
        File pathOut = context.getFilesDir();
        String [] list = pathOut.list();

        for( int k=0; k<list.length; ++k )
        {
            text.append(list[k]);
            text.append('\n');
        }
        ShowFamilyDescription( pathOut.getAbsolutePath(), text.toString() );
    }

    protected void SaveFamilyTree( String received, String familyName ) {
        Context context = getApplicationContext();

        String familyFile = familyName + ".csv";
        String familyBkup = familyName + ".bak";
        String familyTemp = familyName + ".tmp";
        File pathOut = context.getFilesDir();
        File fOut = new File(pathOut, familyFile);
        File fBak = new File(pathOut, familyBkup);
        File fTmp = new File(pathOut, familyTemp);

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(familyTemp, MODE_APPEND)));
            BufferedReader br = new BufferedReader(new StringReader(received));
            String st;
            while ((st = br.readLine()) != null) {
                bw.write(st);
                bw.newLine();
            }
            br.close();
            bw.close();
        } catch (IOException e) {
            ShowFamilyDescription("failed to save "+familyName, "" );
            return;
        }

        if (fOut.exists())
            fOut.renameTo(fBak);
        fTmp.renameTo(fOut);
    }

    protected void ShowFamilyDescription(String name, String familyDesc )
    {
        WebView webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
            }
        });

        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText(name);

        String sourceCodeText = familyDesc;
//        sourceCodeText = HtmlEscapers.htmlEscaper().escape(sourceCodeText);
        webView.loadDataWithBaseURL("file:///android_asset/", "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body><pre class=\"prettyprint \">" + sourceCodeText + "</pre></body></html>", "text/html", "UTF-8", null);
    }

    protected void ShareFamily( String familyName ) {
        Context context = getApplicationContext();

        String familyFile = familyName + ".csv";
        File pathIn = context.getFilesDir();
        File fIn = new File(pathIn, familyFile);
        if (fIn.exists()) {
            // using user provided data
            StringBuffer text = new StringBuffer();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(familyFile)));
                String st;
                while ((st = br.readLine()) != null) {
                    text.append(st);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                ShowFamilyDescription(familyName+" not found.", "");
                return;
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text.toString());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    protected void EmailFamily( String familyName ) {
        Context context = getApplicationContext();

        String familyFile = familyName + ".csv";
        File pathIn = context.getFilesDir();
        File fIn = new File(pathIn, familyFile);
        if (fIn.exists()) {
            // using user provided data
            StringBuffer text = new StringBuffer(emailtag + "\n");
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(familyFile)));
                String st;
                while ((st = br.readLine()) != null) {
                    text.append(st);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                ShowFamilyDescription(familyName + " not found.", "");
                return;
            }


            if (false) {
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("text/plain");
//            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.putExtra(Intent.EXTRA_EMAIL, "me@myfamily.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "family tree");
                intent.putExtra(Intent.EXTRA_TEXT, text.toString());
//            startActivity(Intent.createChooser(intent, "Send Email"));
                startActivity(Intent.createChooser(intent, "Send Email"));
            }

            if (true) {
                Parcelable.Creator<String> c = Parcel.STRING_CREATOR;
                Parcel p = Parcel.obtain();
                p.writeString(text.toString());
                p.setDataPosition(0);
                String s = p.readString();
                p.setDataPosition(0);
                Parcelable.Creator<MyParcelable> creator = MyParcelable.CREATOR;
                MyParcelable x = creator.createFromParcel(p);
                ArrayList<MyParcelable> textList = new ArrayList<>();
                textList.add(x); // Add your image URIs here

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, textList);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share Email"));

            }
        }
    }

    protected void DisplayFamilyTreeFromString( String treeinfo )
    {
//        boolean filebad = true;
//        try {
                // using user provided data
        String msg;
                BufferedReader br = new BufferedReader(new StringReader(treeinfo));
                if( DisplayFamilyTree("收到",br) )
                    msg = "收到家谱错误, 样本红楼梦家谱如下";
                else
                    msg = "收到家谱如下";
//                filebad = false;
//        } catch (IOException e)
//        {
//        }
//        if( filebad ) {
            TextView textView = (TextView)findViewById(R.id.textView);
            textView.setText(msg);

            return;
//        }
    }

    protected void DisplayFamilyTreeFromFile( String familyName )
    {
        Context context = getApplicationContext();

        String familyFile = familyName + ".csv";
        File pathIn = context.getFilesDir();
        File fIn = new File(pathIn, familyFile);
        /*
        String ex = fIn.exists() ? " exists " : " not exists ";
        String m = " Internal file "+ fIn.getAbsolutePath() + ex;
        */

        /*
        File pathOut = context.getExternalFilesDir(null);
        File fOut = new File(pathOut, "myfamily.csv");
        String ex = fOut.exists() ? " exists " : " not exists ";
        String m = " External file " + fOut.getAbsolutePath() + ex;
*/

        /*
        File myf = new File( "/storage/sdcard/Download/myfamily.csv");
        String ex = myf.exists() ? " exists " : " not exists ";
        String m = " Internal path "+ pathIn.getAbsolutePath() + " External path " + pathOut.getAbsolutePath() + " " + myf.getAbsolutePath() + " " + ex;
*/
        String msg;
        try {
            if( fIn.exists() ) {
                // using user provided data
                BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(familyFile)));
                if( DisplayFamilyTree(familyName,br) )
                    msg = familyName+"错误, 样本红楼梦家谱如下";
                else
                    msg = familyName+"家谱如下";
                br.close();
//                filebad = false;
            } else
                if( DisplayFamilyTree(familyName,null) )
                    msg = familyName+"找不到, 样本红楼梦家谱如下";
                else
                    msg = familyName+"家谱如下";
        } catch (IOException e)
        {
            msg = familyName+"信息错误, 样本红楼梦家谱如下";
        }
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText(msg);
    }

    // return true if sample it shown
    protected boolean DisplayFamilyTree( String familyName, BufferedReader br )
    {
        boolean sample = true;
        Context context = getApplicationContext();
        String n = "x";
        try {
            StringBuffer f = new StringBuffer("<script charset=\"utf-8\">");
            if( br != null ) {
                final int STAGE_NONE = 0;
                final int STAGE_PERSON = 1;
                final String STAGE_PERSON_TEXT = "person";
                final int STAGE_FAMILY = 2;
                final String STAGE_FAMILY_TEXT = "family";
                final int STAGE_HEAD = 3;
                final String STAGE_HEAD_TEXT = "jz";
                final int STAGE_POSITION = 4;
                final String STAGE_POSITION_TEXT = "position";
                int stage = STAGE_NONE;
                String [] data;
                StringBuffer fperson = new StringBuffer("function SetPersons()\n{");
                StringBuffer frelation = new StringBuffer("function SetRelations()\n{");
                StringBuffer fposition = new StringBuffer("var origin = new Point(0,0);\n");
                StringBuffer ffamly = new StringBuffer();
                String fhead = "";
                String st;

                while ((st = br.readLine()) != null) {
                    if( st.equalsIgnoreCase(STAGE_PERSON_TEXT)) { stage = STAGE_PERSON; continue; }
                    else if( st.equalsIgnoreCase(STAGE_FAMILY_TEXT)) { stage = STAGE_FAMILY; continue; }
                    else if( st.equalsIgnoreCase(STAGE_HEAD_TEXT)) { stage = STAGE_HEAD; continue; }
                    else if( st.equalsIgnoreCase(STAGE_POSITION_TEXT)) { stage = STAGE_POSITION; continue; }
                    switch (stage) {
                    case STAGE_PERSON:
                        data = st.split(",");
                        // 贾政,male
                        // new Person( '贾政', 'male');
                        if( data.length==2 ) fperson.append("new Person( '"+data[0]+"', '"+data[1]+"');\n");
                        break;
                    case STAGE_FAMILY:
                        data = st.split(",");
                        // tree,2,贾政,王夫人,贾宝玉
                        // new StarFamily( 'tree', 2, '贾政', '王夫人', '贾宝玉' );
                        if( data.length>=2 )
                        {
                            ffamly.append("new StarFamily('"+data[0]+"',"+data[1]);

                            for( int k=2; k<data.length; ++k ) ffamly.append(", '"+data[k]+"'");
                            ffamly.append(");\n");
                        }
                        break;
                    case STAGE_HEAD:
                        fhead = "var jz = '"+st+"';\n";
                        break;
                    case STAGE_POSITION:
                        data = st.split(",");
                        // origin,x,y
                        // shift1,x,y
                        // 贾政,male,30,100,[shift1]
                        // new Subject('贾政', new Point( 30,100 ));
                        if( data[0].startsWith("origin")) {
                            if( data.length==3 ) fposition.append("origin = new Point("+data[1]+", "+data[2]+");\n");
                            break;
                        }
                        if( data[0].startsWith("shift")) {
                            if( data.length==3 )
                            {
                                fposition.append("var " + data[0] + " = new Point("+data[1]+", "+data[2]+");\n");
                                fposition.append(data[0] + ".Add( origin );\n");
                            }
                            break;
                        }
                        if( data.length>3 ) fperson.append("new Person( '"+data[0]+"', '"+data[1]+"');\n");
                        if( data.length==4 ) fposition.append("new Subject( '"+data[0]+"', new Point("+data[2]+", "+data[3]+"));\n");
                        if( data.length==5 ) fposition.append("new Subject( '"+data[0]+"', new Point("+data[2]+", "+data[3]+"), "+data[4] + ");\n");
                        break;
                    default:
                        continue;
                    }
                }
 //               br.close();
                f.append(fperson); f.append("}\n");
                f.append(frelation); f.append("}\n");
                f.append("SetPersons();\n" +
                        "SetRelations();");
                f.append(fhead);
                f.append(fposition);
                f.append(ffamly);
                sample = false;
            } else {
                // use default data
                BufferedReader jsbr = new BufferedReader(new InputStreamReader(context.getAssets().open("family.js")));
                String st;
                while ((st = jsbr.readLine()) != null) {
                    f.append(st);
                    f.append('\n');
                }
                jsbr.close();
            }

            BufferedReader htmlbr = new BufferedReader(new InputStreamReader(context.getAssets().open("star.html")));
            StringBuffer h = new StringBuffer();
            String st;
            while ((st=htmlbr.readLine())!=null)
            {
                if( st.endsWith("src=\"family.js\">"))
                    h.append(f);
                else
                    h.append(st);
                h.append('\n');
            }
            htmlbr.close();
            n = h.toString();
        } catch (IOException e)
        {
            n = e.toString();
        }

        // capture the layut's TextView and set the string as its text
//        TextView textView = (TextView)findViewById(R.id.textView);
//        textView.setText(familyName+(br==null?"家谱如下":"没找到, 样本红楼梦家谱如下"));


        WebView webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.setWebViewClient(new WebViewClient() {
             @Override
             public void onPageFinished(WebView view, String url) {
             }
         });


        /*
        String sourceCodeText = message;
        sourceCodeText = HtmlEscapers.htmlEscaper().escape(sourceCodeText);
        webView.loadDataWithBaseURL("file:///android_asset/", "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body><pre class=\"prettyprint \">" + sourceCodeText + "</pre></body></html>", "text/html", "UTF-8", null);
        */
//        webView.loadUrl("http:///www.wenxuecity.com/");
//        webView.loadDataWithBaseURL("file:///android_asset/", getHTML(), "text/html", "UTF-8", null);
//        webView.loadUrl("file:///android_asset/star.html");
        webView.loadDataWithBaseURL("file:///android_asset/", n, "text/html", "UTF-8", null);
        return sample;
    }

    protected String getHTML()
    {
        String prog = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "text.male { font-size: 12px; fill: blue; stroke-width: 1; text-anchor: middle; }\n" +
                "text.female { font-size: 12px; fill: green; stroke-width: 1; text-anchor: middle; }\n" +
                "text.malex { font-size: 12px; fill:blue; stroke: none; text-decoration: underline; text-anchor: middle; }\n" +
                "text.femalex { font-size: 12px; fill: green; stroke: none; text-decoration: underline; text-anchor: middle; }\n" +
                "text.person { font-size: 12px; fill: gray; stroke-width: 1; text-anchor: middle; }\n" +
                "text.father { font-size: 12px; fill: red; stroke-width: 1; text-anchor: middle; }\n" +
                "text.mother { font-size: 12px; fill: gold; stroke-width: 1; text-anchor: middle; }\n" +
                "text.son { font-size: 12px; fill: lightblue; stroke-width: 1; text-anchor: middle; }\n" +
                "text.daughter { font-size: 12px; fill: lightgold; stroke-width: 1; text-anchor: middle; }\n" +
                "</style>\n" +
                "\n" +
                "<!-- css/polygon.css -->\n" +
                "<style>\n" +
                ".vertex { r:2; fill:black; }\n" +
                ".center { r:5; fill:red; stroke:red; }\n" +
                ".startpoint { r:1; fill:blue; }\n" +
                ".vertexlabel { font-size: 10px; fill: red; stroke-width: 1; text-anchor: middle; }\n" +
                ".vertexarc1 { fill:none; stroke:lightgreen; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc1 { fill:none; stroke:lightgreen; opacity: 0.7 }\n" +
                ".vertexarc2 { fill:none; stroke:lightblue; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc2 { fill:none; stroke:lightblue; opacity: 0.7 }\n" +
                ".vertexarc3 { fill:none; stroke:yellow; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc3 { fill:none; stroke:yellow; opacity: 0.7 }\n" +
                ".vertexarc4 { fill:none; stroke:purple; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc4 { fill:none; stroke:purple; opacity: 0.7 }\n" +
                ".vertexarc5 { fill:none; stroke:blue; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc5 { fill:none; stroke:blue; opacity: 0.7 }\n" +
                ".vertexarc6 { fill:none; stroke:DarkSlateBlue ; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc6 { fill:none; stroke:DarkSlateBlue ; opacity: 0.7 }\n" +
                ".vertexarc7 { fill:none; stroke:green; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc7 { fill:none; stroke:green; opacity: 0.7 }\n" +
                ".vertexarc8 { fill:none; stroke:HotPink ; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc8 { fill:none; stroke:HotPink ; opacity: 0.7 }\n" +
                ".vertexarc9 { fill:none; stroke:red ; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc9 { fill:none; stroke:red ; opacity: 0.7 }\n" +
                ".circletest { fill:none; stroke:yellow; }\n" +
                ".anchor { r:5; fill:white; opacity: 0; stroke:blue; }\n" +
                "</style>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "<!-- js/family_tree_unicode.js -->\n" +
                "<script>\n" +
                "// This file has to be saved in unicode\n" +
                "var svgNS = \"http://www.w3.org/2000/svg\";\n" +
                "\n" +
                "function Point( x, y )\n" +
                "{\n" +
                "\tif ( x instanceof Point ) {\n" +
                "\t\tthis.X = x.X;\n" +
                "\t\tthis.Y = x.Y;\n" +
                "\t} else {\n" +
                "\t\tthis.X = x;\n" +
                "\t\tthis.Y = y;\n" +
                "\t}\n" +
                "\tthis.Add = function (ap) { this.X += ap.X; this.Y += ap.Y; return this; };\n" +
                "\tthis.Sub = function (ap) { this.X -= ap.X; this.Y -= ap.Y; return this; };\n" +
                "\tthis.Shift = function( x, y ) { this.X += x; this.Y += y; return this; };\n" +
                "\tthis.Transform = function( matrix ) {\n" +
                "\t\t// http://www.w3.org/TR/SVGTiny12/coords.html#TransformMatrixDefined\n" +
                "\t\treturn new Point( (matrix.a * this.X) + (matrix.c * this.Y) + matrix.e, (matrix.b * this.X) + (matrix.d * this.Y) + matrix.f );\n" +
                "\t};\n" +
                "}\n" +
                "\n" +
                "function Distance( a, b ) {\n" +
                "\treturn Math.sqrt( (b.X-a.X)*(b.X-a.X)+(b.Y-a.Y)*(b.Y-a.Y));\n" +
                "}\n" +
                "\n" +
                "var persons = new Map();\n" +
                "\n" +
                "function Person( _name, _sex, _live )\n" +
                "{\n" +
                "\tthis.name = _name;\n" +
                "\tthis.sex = _sex;\n" +
                "\tthis.live = _live==null?'':_live\n" +
                "\tpersons.set( this.name, this );\n" +
                "\tthis.role = new Map();\n" +
                "\tthis.AddRole = function ( familyName, role ) { // role: father, mother, 1, 2, 3 for children in order\n" +
                "\t\tthis.role.set( familyName, role );\n" +
                "\t};\n" +
                "}\n" +
                "\n" +
                "var validRelations = [\n" +
                "\t 'husband-wife',\n" +
                "\t 'father-child',\n" +
                "\t 'mother-child',\n" +
                "\t 'sibling-elder'\n" +
                "\t ];\n" +
                "\n" +
                "function SetRelation( name1, rel, name2 )\n" +
                "{\n" +
                "\tvar p1 = persons.get( name1 );\n" +
                "\tvar p2 = persons.get( name2 );\n" +
                "\tswitch ( rel ) {\n" +
                "\tcase 'husband-wife':\n" +
                "\t\tp1.wife = name2;\n" +
                "\t\tp2.husband = name1;\n" +
                "\t\tbreak;\n" +
                "\tcase 'father-child':\n" +
                "\t\tif( p1.children == undefined || ! (p1.children instanceof Array) ) {\n" +
                "\t\t\tp1.children = [];\n" +
                "\t\t}\n" +
                "\t\tp1.children.push( name2 );\n" +
                "\t\tp2.father = name1;\n" +
                "\t\tbreak;\n" +
                "\tcase 'mother-child':\n" +
                "\t\tif( p1.children == undefined || ! (p1.children instanceof Array) ) {\n" +
                "\t\t\tp1.children = [];\n" +
                "\t\t}\n" +
                "\t\tp1.children.push( name2 );\n" +
                "\t\tp2.mother = name1;\n" +
                "\t\tbreak;\n" +
                "\tcase 'sibling-elder':\n" +
                "\t\tp1.younger = name2;\n" +
                "\t\tp2.elder = name1;\n" +
                "\t\tbreak;\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "var families = new Map();\n" +
                "\t \n" +
                "function Family( father, mother )\n" +
                "{\n" +
                "\tthis.father = father;\n" +
                "\tthis.mother = mother;\n" +
                "\tthis.name = '';\n" +
                "\tif( father ) {\n" +
                "\t\tthis.name += \":F:\"+father.name;\n" +
                "\t}\n" +
                "\tif( mother ) {\n" +
                "\t\tthis.name += \":M:\"+mother.name;\n" +
                "\t}\n" +
                "\tif( father ) father.AddRole( this.name, 'father' );\n" +
                "\tif( mother ) mother.AddRole( this.name, 'mother' );\n" +
                "\tif( father && mother ) {\n" +
                "\t\tthis.childList = [];\n" +
                "\t\tfor (var index = 0; mother.children && index < mother.children.length; index++) {\n" +
                "\t\t\tvar childName = this.mother.children[index];\n" +
                "\t\t\tvar child = persons.get( childName );\n" +
                "\t\t\tif( child.father == father.name ) {\n" +
                "\t\t\t\tchild.AddRole( this.name, this.childList.length );\n" +
                "\t\t\t\tthis.childList.push( child );\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tif( father && ! mother ) {\n" +
                "\t\tthis.childList = [];\n" +
                "\t\tfor (var index = 0; father.children && index < father.children.length; index++) {\n" +
                "\t\t\tchildName = father.children[index];\n" +
                "\t\t\tchild = persons.get( childName );\n" +
                "\t\t\tchild.AddRole( this.name, this.childList.length );\n" +
                "\t\t\tthis.childList.push( child );\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tif( ! father && mother ) {\n" +
                "\t\tthis.childList = [];\n" +
                "\t\tfor (var index = 0; mother.children && index < mother.children.length; index++) {\n" +
                "\t\t\tchildName = mother.children[index];\n" +
                "\t\t\tchild = persons.get( childName );\n" +
                "\t\t\tchild.AddRole( this.name, this.childList.length );\n" +
                "\t\t\tthis.childList.push( child );\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tfamilies.set( this.name, this );\n" +
                "\tthis.SetPanel = function ( panel ) {\n" +
                "\t\tthis.panel = panel;\n" +
                "\t};\n" +
                "}\n" +
                "\n" +
                "// Case 1: Given father and/or mother and panel position\n" +
                "// Case 2: Given father and/or mother positions\n" +
                "// Case 3: Given one of the child's position\n" +
                "// Case 4: Given two children' positions\n" +
                "// Case A: Main family, given absolute position of the box (x, y)\n" +
                "\n" +
                "function FamilyPanel( family, loc, size )\n" +
                "{\n" +
                "\tthis.family = family;\n" +
                "\tthis.translate = loc;\n" +
                "\tthis.xMargin = 30;\n" +
                "\tthis.yTopMargin = 20; // The margin from a top y of a text element position. This is because the y of text is at the bottom. \n" +
                "\tthis.yBotMargin = 10; // The margin from a bottom y of a text element position\n" +
                "\tthis.size = size ? new Point( size, 'clone') : new Point( 100, 100 );\n" +
                "\tthis.nameWidth = 36;\n" +
                "\tthis.nameHeight = 15;\n" +
                "\tthis.nameHeightMargin = 10;\n" +
                "\tthis.nameWidthMargin = 10;\n" +
                "\t// x\n" +
                "\t// xMargin  + 1/2 father + nameWidthMargin + 1/2 mother + xMargin\n" +
                "\t//   30         20         10             20        30\n" +
                "\t// y 2- children\n" +
                "\t// yTopMargin + (Parent)nameHeight + nameHeightMargin + (child)nameHeight + yBotMargin\n" +
                "\t// 20         +        15          +     10           +        15         +     10      = 70\n" +
                "\t// y 4- children\n" +
                "\t// yTopMargin + (Parent)nameHeight + nameHeightMargin + (child)nameHeight + nameHeightMargin + (child)nameHeight + yBotMargin\n" +
                "\t// 20         +        15          +     10           +        15         +     10           +        15         +     10      = 95\n" +
                "\tthis.Transform = function ( person, pos, matrix ) {\n" +
                "\t\tif( person.location )\n" +
                "\t\t\treturn;\n" +
                "\t\tperson.location = matrix ? pos.Transform( matrix ) : new Point( pos, 'clone' );\n" +
                "\t};\n" +
                "\t// Calculate children' positions\n" +
                "\tthis.childLevel = Math.round( 0.1+family.childList ? family.childList.length / 2 : 0 );\n" +
                "\tif( this.childLevel > 2 ) {\n" +
                "\t\tthis.size.Y += (this.childLevel-2)*(this.nameHeight + this.nameHeightMargin)\n" +
                "\t}\n" +
                "\tthis.firstChildPosY = this.size.Y - (this.childLevel-1)*(this.nameHeightMargin+this.nameHeight) - this.yBotMargin;\n" +
                "\tthis.position = {\n" +
                "\t\tfather: new Point( this.xMargin, this.yTopMargin),\n" +
                "\t\tmother: new Point( this.size.X - this.xMargin, this.yTopMargin ),\n" +
                "\t\tchild: new Array(this.family.childList.length)\n" +
                "\t  };\n" +
                "\t  \n" +
                "\tthis.SetWidth = function ( w ) {\n" +
                "\t\tif( this.family.mother ) this.position.mother.X += w - this.size.X;\n" +
                "\t\tfor (var index = 1; index < this.position.child.length; index += 2 )\n" +
                "\t\t\tthis.position.child[index].X += w - this.size.X;\n" +
                "\t\tthis.size.X = w;\n" +
                "\t}\n" +
                "\tthis.SetChildrenPos = function() {\n" +
                "\t\tvar posY = this.firstChildPosY;\n" +
                "\t\tfor (var index = 0; index < this.position.child.length; index += 2 ) {\n" +
                "\t\t\tthis.position.child[index] = new Point( this.position.father.X, posY );\n" +
                "\t\t\tif( index+1 < this.position.child.length )\n" +
                "\t\t\t\tthis.position.child[index+1] = new Point( this.position.mother.X, posY );\n" +
                "\t\t\tposY += this.nameHeightMargin+this.nameHeight;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tthis.transformText = function() {\n" +
                "\t\tvar transformTxt = \"\";\n" +
                "\t\tif( this.translate && ( this.translate.X != 0 || this.translate.Y != 0 ) )\n" +
                "\t\t\ttransformTxt += \"translate(\"+this.translate.X+\",\"+this.translate.Y+\") \";\n" +
                "\t\tif( this.skewYAngle && this.skewYAngle != 0 )\n" +
                "\t\t\ttransformTxt += \"skewY(\"+this.skewYAngle+\") \";\n" +
                "\t\tif( this.cos && this.cos != 1 )\n" +
                "\t  \t\ttransformTxt += \" scale(\"+this.cos+\",1)\";\n" +
                "\t\tif( this.moveBack && (this.moveBack.X != 0 || this.moveBack.Y != 0 ) )\n" +
                "\t  \t\ttransformTxt += \" translate(\"+this.moveBack.X+\",\"+this.moveBack.Y+\")\";\n" +
                "\t\tif( transformTxt == \"\")\n" +
                "\t\t\ttransformTxt = undefined;\n" +
                "\t\treturn transformTxt;\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "// Case 1: Given father and/or mother and panel position.\n" +
                "// This is a main panel to decide other family panels' positions.\n" +
                "// Because it's a main panel, the position p will determine all the relative and absolute positions.\n" +
                "function MainPanel( family, location, size )\n" +
                "{\n" +
                "\tFamilyPanel.call( this, family, location, size ); // Create the base object\n" +
                "\tfamily.SetPanel( this );\n" +
                "\tthis.SetChildrenPos();\n" +
                "}\n" +
                "\n" +
                "// Case 2: Given father and/or mother positions\n" +
                "function ParentPanel( family )\n" +
                "{\n" +
                "\tFamilyPanel.call( this, family ); // Create the base object\n" +
                "\tfamily.SetPanel( this );\n" +
                "\tthis.SetChildrenPos();\n" +
                "\tif( family.father.location && ! family.mother.location ) {\n" +
                "\t\t// single father, use father's position to determine panel location\n" +
                "\t\tthis.translate = new Point( family.father.location, 'clone' );\n" +
                "\t \tthis.translate.Sub( this.position.father );\n" +
                "\t} else if( ! family.father.location && family.mother.location ) {\n" +
                "\t\t// single mother, use mother's position to determine panel location\n" +
                "\t\tthis.translate = new Point( family.mother.location, 'clone' );\n" +
                "\t \tthis.translate.Shift( this.size.X-this.position.mother.X, -this.position.mother.Y );\n" +
                "\t} else {\n" +
                "\t\tthis.translate = new Point( family.father.location, 'clone' );\n" +
                "\t \tthis.translate.Sub( this.position.father );\n" +
                "\t    this.distance = Distance( family.father.location, family.mother.location );\n" +
                "\t\t// Our goal is to skew the panel to match the angle of the father-mother line.\n" +
                "\t\t// and at the same time, keep the absolute location of both father and mother.\n" +
                "\t\t// Before skewing it, we set the X-axis distance to the real distance between father and mother\n" +
                "\t\t// because this is the distance when we look at the panel from straight angle.\n" +
                "\t\t// So the panel's width is the distance plus the margin (both left and right)\n" +
                "\t\tthis.SetWidth(this.xMargin + this.distance + this.xMargin);\n" +
                "\t\t// The angle can be calcualated by the cosin of that angle\n" +
                "\t  \tthis.cos = (family.mother.location.X-family.father.location.X) / this.distance;\n" +
                "\t\tthis.tan = (family.mother.location.Y-family.father.location.Y) / (family.mother.location.X-family.father.location.X);\n" +
                "\t  \tthis.skewYAngle = Math.atan( this.tan ) * 180 / Math.PI;\n" +
                "\t\t// After skewing it in Y, the distance is enlarged by 1/cos(a) where a is the skew angle.\n" +
                "\t\t// Therefore, we need scale it back by cos(a) to maintain the distance.\n" +
                "\t\t// However, another issue arises, the X of father is also scaled back, we need to move it back to the original position\n" +
                "\t\t// i.e. the difference between the current positon and the original position = (this.position.father.X - this.position.father.X * cos)\n" +
                "\t\t// For Y, the skew move downs father's Y by X*tg(a), so we need to move up by the same.\n" +
                "\t  \tthis.moveBack = new Point( this.position.father.X * (1-this.cos),\n" +
                "\t\t\t  -this.xMargin*(family.mother.location.Y-family.father.location.Y)/(family.mother.location.X-family.father.location.X) );\n" +
                "\t\t// When father-mother line is horizontal, i.e. family.father.location.Y=family.mother.location.Y,\n" +
                "\t\t// cos = 1, skewYAngle = 0, moveBack=(0,0)\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "// Case 3, Given a child's position\n" +
                "function ChildPanel( family, child )\n" +
                "{\n" +
                "\tFamilyPanel.call( this, family ); // Create the base object\n" +
                "\tfamily.SetPanel( this );\n" +
                "\tthis.SetChildrenPos();\n" +
                "\t\n" +
                "\tfor (var cn = 0; family.childList && cn < family.childList.length; cn++) {\n" +
                "\t\tvar ch = family.childList[cn];\n" +
                "\t\tif( ch.name == child.name )\n" +
                "\t\t\tbreak;\n" +
                "\t}\n" +
                "\tvar thispos = this.position.child[cn];\n" +
                "\tthis.translate = child.location.Sub( thispos );\n" +
                "}\n" +
                "\n" +
                "function DisplayPanel()\n" +
                "{\n" +
                "\t//\n" +
                "\tvar g = document.createElementNS( svgNS, 'g' );\n" +
                "\tg.setAttributeNS( null, \"id\", this.family.name );\n" +
                "\tg.setAttributeNS( null, \"style\", \"opacity:0.5\");\n" +
                "\t\n" +
                "\tvar matrix;\n" +
                "\tvar transform = this.transformText();\n" +
                "\tif( transform ) {\n" +
                "\t\tg.setAttributeNS( null, \"transform\", transform );\n" +
                "\t    matrix = g.getScreenCTM();\n" +
                "\t}\n" +
                "\t\n" +
                "\tvar rect = document.createElementNS( svgNS, 'rect' );\n" +
                "\trect.setAttributeNS( null, \"class\", \"icon_frame\");\n" +
                "\trect.setAttributeNS( null, \"x\", \"0\");\n" +
                "\trect.setAttributeNS( null, \"y\", \"0\");\n" +
                "\trect.setAttributeNS( null, \"width\", this.size.X );\n" +
                "\trect.setAttributeNS( null, \"height\", this.size.Y );\n" +
                "\trect.setAttributeNS( null, \"fill\", \"url(#icon_background)\" );\n" +
                "\trect.setAttributeNS( null, \"stroke\", \"#000000\" );\n" +
                "\trect.setAttributeNS( null, \"fill-opacity\", \"0.7\" );\n" +
                "\tg.appendChild( rect );\n" +
                "\t\n" +
                "\tvar txt;\n" +
                "\tvar tn;\n" +
                "\t\n" +
                "\tif( this.family.father ) {\n" +
                "\t\ttxt = document.createElementNS( svgNS, 'text' );\n" +
                "\t\ttxt.setAttribute( \"class\", \"father\");\n" +
                "\t\ttxt.setAttribute( \"x\", this.position.father.X );\n" +
                "\t\ttxt.setAttribute( \"y\", this.position.father.Y );\n" +
                "\t\ttxt.setAttribute( \"comp-op\", \"soft-light\");\n" +
                "\t\ttn = document.createTextNode(this.family.father.name);\n" +
                "\t\ttxt.appendChild(tn);\n" +
                "\t\tg.appendChild( txt );\n" +
                "\t\tthis.Transform( this.family.father, this.position.father, matrix );\n" +
                "\t}\n" +
                "\t\n" +
                "\tif( this.family.mother ) {\n" +
                "\t\ttxt = document.createElementNS( svgNS, 'text' );\n" +
                "\t\ttxt.setAttribute( \"class\", \"mother\");\n" +
                "\t\ttxt.setAttribute( \"x\", this.position.mother.X );\n" +
                "\t\ttxt.setAttribute( \"y\", this.position.mother.Y );\n" +
                "\t\ttxt.setAttribute( \"comp-op\", \"soft-light\");\n" +
                "\t\ttn = document.createTextNode(this.family.mother.name);\n" +
                "\t\ttxt.appendChild(tn);\n" +
                "\t\tg.appendChild( txt );\n" +
                "\t\tthis.Transform( this.family.mother, this.position.mother, matrix );\n" +
                "\t}\n" +
                "\n" +
                "\tfor (var c = 0; c < this.family.childList.length; c++) {\n" +
                "\t\tvar child = this.family.childList[c];\n" +
                "\t\ttxt = document.createElementNS( svgNS, 'text' );\n" +
                "\t\ttxt.setAttribute( \"class\", child.sex=='male'?'son':'daughter');\n" +
                "\t\ttxt.setAttribute( \"x\", this.position.child[c].X );\n" +
                "\t\ttxt.setAttribute( \"y\", this.position.child[c].Y );\n" +
                "\t\ttxt.setAttribute( \"comp-op\", \"soft-light\");\n" +
                "\t\ttn = document.createTextNode(child.name);\n" +
                "\t\ttxt.appendChild(tn);\n" +
                "\t\tg.appendChild( txt );\n" +
                "\t\tthis.Transform( child, this.position.child[c], matrix );\n" +
                "\t}\n" +
                "\tvar svg = document.getElementById( 'myroot' );\n" +
                "\tsvg.appendChild( g );\n" +
                "}\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "<!-- js/star.js -->\n" +
                "\n" +
                "<script charset=\"ansi\">\n" +
                "\n" +
                "/*\n" +
                "\n" +
                "To do:\n" +
                "\n" +
                "create a bounding box anchor: done 7/27/2015\n" +
                "\n" +
                "refactor to have model-conrol-view: 7/29/2015\n" +
                "\n" +
                "make vertex position update to redraw: done if vertex order does not change. 7/29/2015 Disallowing angle order change. 7/31/2015\n" +
                "\n" +
                "Output positions of vertex so that a static version can be published. done 8/2/2015\n" +
                "\n" +
                "Refactor to make star.html general enough to be used for any family. The author needs only modify stonestory_unicode.js for his own family. 8/2/2015.\n" +
                "\n" +
                "Make a static (no move) version of the document\n" +
                "\n" +
                "Defects:\n" +
                "  For DINK family, sometimes the arc flips to outside. That's because the center is on the line between the two points. The choosing algorithm using line equation value sign does not work because for the center the calculated line value is 0. It seems that the points outside (on the left when going clock-wise) always have the negative line equation value. This seems to fix the problem but not proven mathematically yet. 7/30/2015\n" +
                "  \n" +
                "  When two persons are very far, the edge arc may cross the mid-line, need to detect it and make arc radius bigger. Fixed. 8/1/2015 A minimum gap between the arc and the mid-point between two vertices can be specified. With that, the minimum radious of the connecting arc can be calculated. See in-line comments in Edge class for exact mathematical proof.\n" +
                "\n" +
                "*/\n" +
                "\n" +
                "function Point( x, y )\n" +
                "{\n" +
                "\tif ( x instanceof Point ) {\n" +
                "\t\tthis.X = x.X;\n" +
                "\t\tthis.Y = x.Y;\n" +
                "\t} else {\n" +
                "\t\tthis.X = x;\n" +
                "\t\tthis.Y = y;\n" +
                "\t}\n" +
                "\tthis.Add = function (ap) { this.X += ap.X; this.Y += ap.Y; return this; };\n" +
                "\tthis.Sub = function (ap) { this.X -= ap.X; this.Y -= ap.Y; return this; };\n" +
                "\tthis.Shift = function( x, y ) { this.X += x; this.Y += y; return this; };\n" +
                "  this.func = [];\n" +
                "  this.RegisterEventListener = function (func, client) {\n" +
                "    this.func.push( { callback: func, client: client } );\n" +
                "  }\n" +
                "  // Notify the client functions that are registed with the move of this point\n" +
                "  // Each function is called with registered client and with a notification message as the parameter of the function\n" +
                "  // If a function call returns false, the whole notification will return false.\n" +
                "  // The action to the return value is determined by the notifier (the one sends the notification).\n" +
                "  // Normally, the nofifier may void the move and reset the position to the original position before the move.\n" +
                "  this.Notify = function ( msg ) {\n" +
                "    var allowed = true;\n" +
                "    for (var n = 0; n < this.func.length; n++) {\n" +
                "      var f = this.func[n];\n" +
                "      allowed = allowed && f.callback.call( f.client, msg );\n" +
                "    }\n" +
                "    return allowed;\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "// http://math.stackexchange.com/questions/256100/how-can-i-find-the-points-at-which-two-circles-intersect\n" +
                "function Cross2Circles( c1, r1, c2, r2 )\n" +
                "{\n" +
                "  var d = Math.sqrt((c2.X-c1.X)*(c2.X-c1.X)+(c2.Y-c1.Y)*(c2.Y-c1.Y));\n" +
                "  var l = ( r1*r1 - r2*r2 + d*d ) / (d+d);\n" +
                "  var h = Math.sqrt( r1*r1 - l*l );\n" +
                "  \n" +
                "  return [ new Point( ( l / d ) * (c2.X-c1.X) + ( h / d ) * (c2.Y-c1.Y) + c1.X, ( l / d ) * (c2.Y-c1.Y) - ( h / d ) * (c2.X-c1.X) + c1.Y ),\n" +
                "           new Point( ( l / d ) * (c2.X-c1.X) - ( h / d ) * (c2.Y-c1.Y) + c1.X, ( l / d ) * (c2.Y-c1.Y) + ( h / d ) * (c2.X-c1.X) + c1.Y )\n" +
                "         ];\n" +
                "}\n" +
                "\n" +
                "function Vector( c, p )\n" +
                "{\n" +
                "\tthis.center = c;\n" +
                "\tthis.p = p;\n" +
                "\n" +
                "  // arc start point will be set by the edge starting from this vertex\n" +
                "  // arc end point will be set by the edge ending to this vertex\n" +
                "  this.arc = new Arc( this.p, 20 );\n" +
                "  \n" +
                "  this.VectorReCalc = function () {\n" +
                "  \tthis.dx = p.X - c.X;\n" +
                "    this.dy = p.Y - c.Y;\n" +
                "  \tthis.distance = Math.sqrt( this.dx*this.dx+this.dy*this.dy );\n" +
                "    var a = this.dx==0 ? 90 : Math.atan( this.dy/this.dx ) * 180 / Math.PI;\n" +
                "    if( this.dx == 0 )\n" +
                "        a = this.dy < 0 ? 270 : 90;\n" +
                "    else if( this.dx < 0 )\n" +
                "      if( this.dy == 0 )\n" +
                "        a = 180;\n" +
                "      else if( this.dy < 0 )\n" +
                "        a += 180; // III 45 + 180 = 225\n" +
                "      else\n" +
                "        a += 180; // II -45 + 180 = 135\n" +
                "    else\n" +
                "      if( this.dy == 0 )\n" +
                "        a = 0;\n" +
                "      else if( this.dy < 0 )\n" +
                "         a += 360; // IV\n" +
                "      // else I 45 = 45 \n" +
                "    this.angle = a;\n" +
                "  \tthis.dx /= this.distance;\n" +
                "    this.dy /= this.distance;\n" +
                "  }\n" +
                "  \n" +
                "  this.VectorReCalc();\n" +
                "  \n" +
                "  this.AwayFrom1 = function( d, oldPoint ) { // Calculate a point in the line  with a distance from center point\n" +
                "    if( oldPoint && oldPoint instanceof Point ) {\n" +
                "      oldPoint.X = this.center.X+this.dx*d;\n" +
                "      oldPoint.Y = this.center.Y+this.dy*d;\n" +
                "      return oldPoint;\n" +
                "    } else\n" +
                "      return new Point( this.center.X+this.dx*d, this.center.Y+this.dy*d);\n" +
                "  }\n" +
                "  this.SideSign = function (p) { // Calculate the side value for a point to decide the side of the point \n" +
                "    return (p.Y-this.center.Y)*this.dx - (p.X-this.center.X)*this.dy;\n" +
                "  }\n" +
                "  /*\n" +
                "  this.testdraw1 = function () {\n" +
                "    var cc = document.createElementNS( svgNS, 'circle' );\n" +
                "    cc.setAttribute( 'class', 'circletest' );\n" +
                "    cc.setAttribute( 'cx', this.center.X );\n" +
                "    cc.setAttribute( 'cy', this.center.Y );\n" +
                "    cc.setAttribute( 'r', this.arcRadius );\n" +
                "    svg.appendChild( cc );\n" +
                "  }\n" +
                "  */\n" +
                "  //  this.testdraw1();\n" +
                "}\n" +
                "\n" +
                "function Arc( center, radius, color )\n" +
                "{\n" +
                "  // Note: arc always draws clock-wise.\n" +
                "  // The center of an edge arc is on the outside of the polygon. \n" +
                "  // The center of a vertex arc is on the inside of the polygon, which is the same side of of the center of the polygon.\n" +
                "  // Therefore, edge are draws from v2 to v1.\n" +
                "  // Below, c is the center of the edge arc\n" +
                "  //  1 is the start point of the edge arc and also the start point of v2 arc,\n" +
                "  //  3 is the end point of the edge arc and also the end point of v1 arc.\n" +
                "  //  Edge arc draws from 1, 2, 2, 2, 3.\n" +
                "  //  v1 arc draws a, 2, 3\n" +
                "  //  v2 arc draws 1, 2, b\n" +
                "  //\n" +
                "  //           c       \n" +
                "  //\n" +
                "  //     2 3      1 2\n" +
                "  // v1 a \\  2 2 2 / b   v2\n" +
                "  //       \\      /\n" +
                "  //        \\    /  \n" +
                "  //         \\  /\n" +
                "  //          \\/\n" +
                "  \n" +
                "  this.center = center;\n" +
                "  this.radius = radius;\n" +
                "  this.color = color;\n" +
                "  this.SetArcPositionAndOrientation = function ( v1, v2 ) {\n" +
                "    // The arc is specified by two vectors. That's enough to calcualte the orientation\n" +
                "    // However, need to calculate the start and end points by calculating the cross points on the circle with the vector\n" +
                "    var v_st = new Vector( this.center, v2.p );\n" +
                "    var v_ed = new Vector( this.center, v1.p );\n" +
                "    var a = v_ed.angle - v_st.angle;\n" +
                "    if( a < 0 ) a += 360;\n" +
                "    this.largeArc = ( a > 180 ? 1 : 0 );\n" +
                "    v2.arc.start = this.start = v_st.AwayFrom1( this.radius ); // start point of the edge arc and the start point of the arc for v2\n" +
                "    v1.arc.end = this.end = v_ed.AwayFrom1( this.radius ); // end point of the edge arc and the end point of the arc for v1\n" +
                "  }\n" +
                "  this.SetArcOrientation  = function () {\n" +
                "    // The start and end point are known already, set by the edge\n" +
                "    // Only need to calcualte the orientation\n" +
                "    var v_st = new Vector( this.center, this.start );\n" +
                "    var v_ed = new Vector( this.center, this.end );\n" +
                "    var a = v_ed.angle - v_st.angle;\n" +
                "    if( a < 0 ) a += 360;\n" +
                "    this.largeArc = ( a > 180 ? 1 : 0 );\n" +
                "  }\n" +
                "  this.CreateView = function (parent, cls) {\n" +
                "      this.view = document.createElementNS( svgNS, 'path' );\n" +
                "      this.view.setAttribute( 'class', cls );\n" +
                "      this.view.setAttribute( 'stroke', this.color );\n" +
                "      parent.appendChild( this.view );\n" +
                "  };\n" +
                "  this.Draw = function () {\n" +
                "      // draw the edge arc from start to end point, clockwise\n" +
                "      var pathD = \"M\"+this.start.X+\",\"+this.start.Y+\" \"; //    <path d=\"M40,20 A10,10 0 0,1 38,25\" style=\"fill:none;stroke:lightblue\" />\n" +
                "      pathD += \"A\"+ this.radius+\",\"+this.radius+\" \";\n" +
                "      pathD += \"0 \";\n" +
                "      pathD += this.largeArc; // large 1 or small 0 arc\n" +
                "      pathD += \",1 \"; // clockwise\n" +
                "      pathD += this.end.X+\",\"+this.end.Y;\n" +
                "      this.view.setAttribute( 'd', pathD );\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "// An edge is specified by 2 vectors and is v2 - v1\n" +
                "function Edge( v1, v2, color )\n" +
                "{\n" +
                "  Vector.call( this, v1.p, v2.p, color );\n" +
                "  this.v1 = v1;\n" +
                "  this.v2 = v2;\n" +
                "  \n" +
                "  this.EdgeReCalc = function () {\n" +
                "    // ReCalc the base vector\n" +
                "    this.VectorReCalc();\n" +
                "    this.centerSign = this.SideSign( v1.center );\n" +
                "    var edgeArcRadius = this.distance - v1.arc.radius - v2.arc.radius;\n" +
                "    // make v1.arcRadius+edgeArcRadius + v2.arcRadius+edgeArcRadius > distance\n" +
                "    // Let c be the center point between v1.p and v2.p\n" +
                "    // Let r = v1.arc.radius = v2.arc.radius\n" +
                "    // Let d = the distance between v1.p and v2.p\n" +
                "    // Let h = the distance between the center of the arc and the center point c\n" +
                "    // Let R = edgeArcRadius\n" +
                "    // h^2 + (d/2)^2 = (R+r)^2\n" +
                "    // We need h >= R + a where a is the smallest gap between the arc and the center point c.\n" +
                "    // h^2 = (R+r)^2 - (d/2)^2 = R^2 + 2Rr + r^2 - (d/2)^2\n" +
                "    // Therefore, h^2 = R^2 + 2Rr + r^2 - (d/2)^2 >= (R+a)^2 = R^2 + 2Ra + a^2\n" +
                "    // 2Rr - 2Ra >= a^2 + (d/2)^2 - r^2\n" +
                "    // R >= ( a^2 + (d/2)^2 - r^2 ) / (2r-2a) ( if r > a )\n" +
                "    var D = this.distance / 2;\n" +
                "    var r = v1.arc.radius;\n" +
                "    var a = 3;\n" +
                "    D = a*a + D*D - r*r;\n" +
                "    D /= (r-a)+(r-a);\n" +
                "    if( edgeArcRadius < 5 ) { // if too close\n" +
                "      edgeArcRadius = 5;\n" +
                "    }\n" +
                "    if( edgeArcRadius < D ) {\n" +
                "      edgeArcRadius = D;\n" +
                "    }\n" +
                "    var cross = Cross2Circles( v1.p, v1.arc.radius+edgeArcRadius, v2.p, v2.arc.radius+edgeArcRadius );\n" +
                "    var side = 0;\n" +
                "    // want to choose the cross point that is on the opposite side of the line v1-v2 with the center point \n" +
                "    var signC = this.SideSign( cross[side] );\n" +
                "    //    if( this.centerSign > 0 && signC > 0 || this.centerSign < 0 && signC < 0 ) {\n" +
                "    if( signC > 0 ) {\n" +
                "      side = 1;\n" +
                "      signC = this.SideSign( cross[side] );\n" +
                "    }\n" +
                "    // figure out the tangent point between the connecting circle and the vertex circle\n" +
                "    this.arc.center = cross[side];\n" +
                "    this.arc.radius = edgeArcRadius;\n" +
                "    this.arc.SetArcPositionAndOrientation( v1, v2 );\n" +
                "  }\n" +
                "  \n" +
                "  this.EdgeReCalc();\n" +
                "}\n" +
                "\n" +
                "function Polygon( color, pos )\n" +
                "{ // https://en.wikipedia.org/wiki/Centroid\n" +
                "  this.color = color;\n" +
                "  var vec = [];\n" +
                "  /*\n" +
                "  for (var i = 0; i < arguments.length; i++) {\n" +
                "      if( arguments[i] instanceof Point ) {\n" +
                "        vec.push( arguments[i] );\n" +
                "      }\n" +
                "  }\n" +
                "  */\n" +
                "\n" +
                "  this.mode = 0; // default mode, 0: family layout is fixed, 1: family can be moved around the family head, the first member of the person\n" +
                "  this.SetAdjMode  = function(mode) {\n" +
                "    this.mode = mode;\n" +
                "  }\n" +
                "\n" +
                "  for (var i = 0; i < pos.length; i++) {\n" +
                "      if( pos[i] instanceof Point ) {\n" +
                "        vec.push( pos[i] );\n" +
                "      }\n" +
                "  }\n" +
                "  \n" +
                "  // Calculate the center of this Polygon.\n" +
                "  // if c is passed in as a parameter, it is set to the calculated center and returned\n" +
                "  // if parameter is passed in as null, a new center point is calculated and returned. ?? Why ? need to revisit later\n" +
                "  this.CalcCenter = function ( c ) {\n" +
                "    var x = 0.0;\n" +
                "    var y = 0.0;\n" +
                "    for (var i = 0; i < vec.length; i++) {\n" +
                "      x += vec[i].X;\n" +
                "      y += vec[i].Y;\n" +
                "    }\n" +
                "    if( c ) {\n" +
                "      c.X = x/vec.length;\n" +
                "      c.Y = y/vec.length;\n" +
                "      return c;\n" +
                "    } else\n" +
                "      return new Point( x/vec.length, y/vec.length );\n" +
                "  }\n" +
                "  \n" +
                "  // figure out the order of vertices by the angle\n" +
                "  this.swapped = true;\n" +
                "  this.SortByAngle = function () {\n" +
                "    for (var i = 0; i < this.order.length-1; i++) {\n" +
                "      var minAngle = this.vector[this.order[i]].angle;\n" +
                "      for (var j = i+1; j < this.order.length; j++) {\n" +
                "        if( this.vector[this.order[j]].angle < minAngle ) {\n" +
                "          minAngle = this.vector[this.order[j]].angle;\n" +
                "          var minId = this.order[j];\n" +
                "          this.order[j] = this.order[i];\n" +
                "          this.order[i] = minId;\n" +
                "          this.swapped = true; // this indicates the edges need to be recalculated.\n" +
                "        }\n" +
                "      }\n" +
                "    }   \n" +
                "  }\n" +
                "  this.AngleOrderChanged = function () {\n" +
                "    for (var i = 0; i < this.order.length-1; i++) {\n" +
                "      var minAngle = this.vector[this.order[i]].angle;\n" +
                "      for (var j = i+1; j < this.order.length; j++) {\n" +
                "        if( this.vector[this.order[j]].angle < minAngle ) {\n" +
                "          return true; // this indicates the angle order has changed.\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    return false; // Angle order has not changed.\n" +
                "  }\n" +
                "\n" +
                "  this.center = this.CalcCenter();\n" +
                "  this.order = [];\n" +
                "  this.vector = [];\n" +
                "  // Create center to vertex vectors\n" +
                "  for (var i = 0; i < vec.length; i++) {\n" +
                "     var p = vec[i];\n" +
                "     var v = new Vector( this.center, p, this.color );\n" +
                "     this.order.push(i);\n" +
                "     this.vector.push(v);\n" +
                "  }\n" +
                "  this.SortByAngle();\n" +
                "  // create edges, each edge is a vector of V2-V1 where V1 and V2 are center vectors of two vertices.\n" +
                "  // Edges may change due to the move of vertices.\n" +
                "  // Therefore, it has to be determined after vertex angles are sorted.\n" +
                "  \n" +
                "  this.CreateEdges = function () {\n" +
                "    this.edge = [];\n" +
                "    for (var i = 0; i < this.order.length; i++) {\n" +
                "      var j = i + 1;\n" +
                "      if( j==this.order.length ) j = 0;\n" +
                "      var e = new Edge( this.vector[this.order[i]], this.vector[this.order[j]], this.color );\n" +
                "      this.edge.push( e );\n" +
                "    }\n" +
                "  }\n" +
                "  \n" +
                "  this.CreateEdges();\n" +
                "\n" +
                "  this.SetSubjects = function () {\n" +
                "    this.subject = [];\n" +
                "    for (var i = 0; i < arguments.length; i++) {\n" +
                "        this.subject.push( arguments[i] );\n" +
                "    }    \n" +
                "  }\n" +
                "  \n" +
                "  /*  \n" +
                "  this.draw1 = function ( parent ) {\n" +
                "    var redraw = this.pol ? true : false;\n" +
                "    // <polygon points=\"200,10 250,190 160,210\" style=\"fill:lime;stroke:purple;stroke-width:1\" />\n" +
                "    if( ! this.pol ) {\n" +
                "      this.pol = document.createElementNS( svgNS, 'polygon' );\n" +
                "      this.label = [];\n" +
                "      this.cc = [];\n" +
                "    }\n" +
                "    this.points = '';\n" +
                "    for (var i = 0; i < this.order.length; i++) {\n" +
                "      var v = this.vector[this.order[i]]\n" +
                "      var pt = v.p;\n" +
                "      this.points += pt.X + ',' + pt.Y + ' ';\n" +
                "      // If there is no subject defined, \n" +
                "      // label each vertex with the order number of the vertices when they are provided\n" +
                "      // It's not the order they are drawn.\n" +
                "      var labelPos = v.AwayFrom1( v.distance + 5 ); // outside 5\n" +
                "      if( redraw ) {\n" +
                "  \t\t  this.label[i].setAttribute( \"x\", labelPos.X );\n" +
                "  \t\t  this.label[i].setAttribute( \"y\", labelPos.Y );\n" +
                "      } else {\n" +
                "        var label = document.createElementNS( svgNS, 'text' );\n" +
                "        label.setAttribute( \"class\", \"vertexlabel\");\n" +
                "  \t\t  label.setAttribute( \"x\", labelPos.X );\n" +
                "  \t\t  label.setAttribute( \"y\", labelPos.Y );\n" +
                "  \t\t  var tn = document.createTextNode(this.order[i]);\n" +
                "    \t\tlabel.appendChild(tn);\n" +
                "  \t\t  parent.appendChild( label );\n" +
                "        this.label.push(label);\n" +
                "      }\n" +
                "\n" +
                "      if( redraw ) {\n" +
                "        this.cc[i].setAttribute( 'cx', pt.X );\n" +
                "        this.cc[i].setAttribute( 'cy', pt.Y );\n" +
                "      } else {\n" +
                "        var cc = document.createElementNS( svgNS, 'circle' );\n" +
                "        cc.setAttribute( 'class', 'vertex' );\n" +
                "        cc.setAttribute( 'cx', pt.X );\n" +
                "        cc.setAttribute( 'cy', pt.Y );\n" +
                "        parent.appendChild( cc );\n" +
                "        this.cc.push( cc );\n" +
                "      }       \n" +
                "\n" +
                "      if( i==0 ) { // mark the starting point\n" +
                "        labelPos = v.AwayFrom1( v.distance - 5 ); // inside 5\n" +
                "        if( redraw ) {\n" +
                "          this.first.setAttribute( 'cx', labelPos.X );\n" +
                "          this.first.setAttribute( 'cy', labelPos.Y );\n" +
                "        } else {\n" +
                "          cc = document.createElementNS( svgNS, 'circle' );\n" +
                "          cc.setAttribute( 'class', 'startpoint' );\n" +
                "          cc.setAttribute( 'cx', labelPos.X );\n" +
                "          cc.setAttribute( 'cy', labelPos.Y );\n" +
                "          parent.appendChild( cc );\n" +
                "          this.first = cc;\n" +
                "        }       \n" +
                "      }      \n" +
                "    }\n" +
                "\n" +
                "    this.pol.setAttribute( 'points', this.points );\n" +
                " \t\tthis.pol.setAttribute( \"style\", \"fill:none;stroke:purple;stroke-width:1\");\n" +
                "    if( ! redraw )\n" +
                "      parent.appendChild( this.pol );\n" +
                "    // comment starts\n" +
                "    var cc = document.createElementNS( svgNS, 'circle' );\n" +
                "    cc.setAttribute( 'class', 'center' );\n" +
                "    cc.setAttribute( 'cx', this.center.X );\n" +
                "    cc.setAttribute( 'cy', this.center.Y );\n" +
                "    svg.appendChild( cc );\n" +
                "    // comment ends\n" +
                "    if( redraw ) {\n" +
                "      this.centerView.setAttribute( 'cx', this.center.X );\n" +
                "      this.centerView.setAttribute( 'cy', this.center.Y );\n" +
                "    } else {\n" +
                "      cc = document.createElementNS( svgNS, 'circle' );\n" +
                "      cc.setAttribute( 'class', 'center' );\n" +
                "      cc.setAttribute( 'cx', this.center.X );\n" +
                "      cc.setAttribute( 'cy', this.center.Y );\n" +
                "      parent.appendChild( cc );\n" +
                "      this.centerView = cc;\n" +
                "    }\n" +
                "    \n" +
                "  }\n" +
                "  */\n" +
                "\n" +
                "  this.DrawEdges = function () {\n" +
                "    for( i=0; i < this.edge.length; i++ ) {\n" +
                "      var arc = this.edge[i].arc;\n" +
                "      var objclass = 'edgearc' + this.color;\n" +
                "      /*\n" +
                "      if( this.subject ) {\n" +
                "        var j = i + 1;\n" +
                "        if( j==this.order.length ) j = 0;\n" +
                "        var thissubject = this.subject[this.order[i]];\n" +
                "        var relasubject = this.subject[this.order[j]];\n" +
                "        objclass = thissubject.Role( relasubject.Name() );\n" +
                "      }\n" +
                "      */\n" +
                "      arc.CreateView( this.parent, objclass || 'edgearc' );\n" +
                "      arc.Draw();\n" +
                "    }\n" +
                "  }\n" +
                "  \n" +
                "  this.DrawVertices = function () {\n" +
                "    for( var i=0; i < this.order.length; i++ ) {\n" +
                "      var arc = this.vector[this.order[i]].arc;\n" +
                "      arc.SetArcOrientation();\n" +
                "      arc.CreateView( this.parent, 'vertexarc'+this.color );\n" +
                "      arc.Draw();\n" +
                "      /*\n" +
                "      var v_st = new Vector( e.p, e.arcEnd );\n" +
                "      var v_ed = new Vector( e.p, n.arcStart );\n" +
                "      var a = v_ed.angle - v_st.angle;\n" +
                "      if( a < 0 ) a += 360;\n" +
                "      pathD = \"M\"+e.arcEnd.X+\",\"+e.arcEnd.Y+\" \"; //    <path d=\"M40,20 A10,10 0 0,1 38,25\" style=\"fill:none;stroke:lightblue\" />\n" +
                "      pathD += \"A\"+ e.v2.arcRadius+\",\"+e.v2.arcRadius+\" \";\n" +
                "      pathD += \"0 \";\n" +
                "      pathD += (a < 180 ? 0 : 1); // small 0 or large 1 arc depending on the open angle\n" +
                "      pathD += \",1 \"; // clockwise\n" +
                "      pathD += n.arcStart.X+\",\"+n.arcStart.Y;\n" +
                "      cc = document.createElementNS( svgNS, 'path' );\n" +
                "      cc.setAttribute( 'class',  );\n" +
                "      cc.setAttribute( 'd', pathD );\n" +
                "      this.parent.appendChild( cc );\n" +
                "      */\n" +
                "    }\n" +
                "  } \n" +
                "\n" +
                "  this.dragging = {\n" +
                "    svgRoot: null,\n" +
                "    on: false,\n" +
                "    mouseOffset: null,\n" +
                "    newpos: new Point( pos.X, pos.Y )\n" +
                "  };\n" +
                "  \n" +
                "  this.DrawCenter = function (m) {\n" +
                "    if( m==1 ) {\n" +
                "      if( this.cc==null) {\n" +
                "        this.cc = document.createElementNS( svgNS, 'circle' );\n" +
                "        this.cc.setAttribute( 'class', 'center' );\n" +
                "        this.cc.setAttribute( 'cx', this.center.X );\n" +
                "        this.cc.setAttribute( 'cy', this.center.Y );\n" +
                "        this.cc.setAttribute( 'r', 20 /*this.arcRadius*/ );\n" +
                "        this.parent.appendChild(this.cc);\n" +
                "        var me = this\n" +
                "        this.cc.addEventListener( \"mousedown\", (function (evt) {\n" +
                "              var fixedSubjectDetermined = 0;\n" +
                "              me.subject.forEach(function(element) {\n" +
                "                if( element.selected) ++fixedSubjectDetermined;\n" +
                "              });\n" +
                "              if( fixedSubjectDetermined > 0 ) {\n" +
                "                me.dragging.on = true;\n" +
                "                var p = svg.createSVGPoint();\n" +
                "                p.x = evt.clientX;\n" +
                "                p.y = evt.clientY;\n" +
                "                var m = me.cc.getScreenCTM();\n" +
                "                p = p.matrixTransform(m.inverse());\n" +
                "                me.dragging.mouseOffset = new Point( p.x-me.center.X, p.y-me.center.Y );\n" +
                "              }\n" +
                "            }));\n" +
                "        this.cc.addEventListener( \"mouseup\", (function () {\n" +
                "            if( ! me.dragging.on ) return;\n" +
                "                me.dragging.on = false;\n" +
                "            }));\n" +
                "        this.cc.addEventListener( \"mousemove\", (function (evt) {\n" +
                "            if( ! me.dragging.on ) return;\n" +
                "                var p = svg.createSVGPoint();\n" +
                "                p.x = evt.clientX;\n" +
                "                p.y = evt.clientY;\n" +
                "                var m = me.cc.getScreenCTM();\n" +
                "                p = p.matrixTransform(m.inverse());\n" +
                "                me.dragging.newpos.X = p.x - me.dragging.mouseOffset.X;\n" +
                "                me.dragging.newpos.Y = p.y - me.dragging.mouseOffset.Y;\n" +
                "                var oldpos = new Point( me.center.X, me.center.Y );            \n" +
                "                me.cc.setAttribute(\"x\", me.dragging.newpos.X );\n" +
                "                me.cc.setAttribute(\"y\", me.dragging.newpos.Y );\n" +
                "                \n" +
                "                p.x = p.x\n" +
                "                // me.pos.X = me.dragging.newpos.X;\n" +
                "                //me.pos.Y = me.dragging.newpos.Y;\n" +
                "                /*\n" +
                "                if( ! me.center.Notify( me ) ) {\n" +
                "                  me.center.X = oldpos.X;\n" +
                "                  me.center.Y = oldpos.Y;\n" +
                "                  me.cc.setAttribute(\"x\", me.center.X );\n" +
                "                  me.cc.setAttribute(\"y\", me.center.Y );\n" +
                "                  //me.pos.Notify( me );\n" +
                "                  me.dragging.on = false;\n" +
                "                }\n" +
                "                */\n" +
                "            }));\n" +
                "\n" +
                "\n" +
                "      }\n" +
                "    } else {\n" +
                "      if( this.cc != null) {\n" +
                "        this.parent.removeChild(this.cc)\n" +
                "        this.cc = null\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  \n" +
                "  // Draw this polygon.\n" +
                "  // 1. Draw all vertices\n" +
                "  // 2. Draw all edges\n" +
                "  // 3. Draw the center of this polygon if in the mode of adjusting layout (mode=1)\n" +
                "  this.Draw = function ( g ) {\n" +
                "    this.parent = svg;\n" +
                "    if( g ) {\n" +
                "      this.parent = document.getElementById( g );\n" +
                "    }\n" +
                "    /* each subject should show only once, so it should not be shown here.\n" +
                "    if( this.subject ) {\n" +
                "      for (var i = 0; i < this.order.length; i++) {\n" +
                "        var s = this.subject[this.order[i]];\n" +
                "        var v = this.vector[this.order[i]];\n" +
                "        s.Draw( parent, v.p );\n" +
                "      }\n" +
                "    }\n" +
                "    */\n" +
                "    this.DrawVertices();\n" +
                "    this.DrawEdges();\n" +
                "    this.DrawCenter( this.mode );\n" +
                "  //    this.draw1( parent );\n" +
                "  }\n" +
                "\n" +
                "  // Drawing\n" +
                "  // Redraw the whole Polygon due to a move of vertex or the layout change (center move)\n" +
                "  // Before the redrawing, the positions of all vertices are set already.\n" +
                "  // 1. Recalculate the center of the polygon\n" +
                "  // 2. Based on the new center, recalculate the vector from the center to each vertex\n" +
                "  // 3. Based on the new vectors for all vertices, recalculate each edge\n" +
                "  // 4. Redraw the arcs for each vertex (represented by the vectors)\n" +
                "  // 5. Redraw the arcs for the eadges\n" +
                "  this.Redraw = function ( s ) {\n" +
                "    /*\n" +
                "    for (var i = 0; i < this.subject.length; i++) {\n" +
                "        if( s == this.subject[i] ) {\n" +
                "          alert( \"Position moved: \"+s.name );\n" +
                "        }\n" +
                "    } \n" +
                "    */   \n" +
                "    this.center = this.CalcCenter( this.center );\n" +
                "    //    this.draw1( parent );\n" +
                "    for (var i = 0; i < this.order.length; i++)\n" +
                "      this.vector[this.order[i]].VectorReCalc();\n" +
                "    //this.swapped = false;\n" +
                "    //this.SortByAngle();\n" +
                "    // order may have changed.\n" +
                "    if( this.AngleOrderChanged() ) {\n" +
                "      // order changed, edges changed. discard all edges and recreate them\n" +
                "    //      alert( \"Angle order changed, not allowed.\" );\n" +
                "      return false; // This version does not allow order to be changed.\n" +
                "    } else {\n" +
                "      // order has not changed, edges remain, but need to be recalculated.\n" +
                "      //      alert( \"Recalc edges\" );\n" +
                "      for (var i = 0; i < this.edge.length; i++)\n" +
                "        this.edge[i].EdgeReCalc();\n" +
                "      // now redraw vertex arcs\n" +
                "      for (i=0; i<this.vector.length; i++ ) {\n" +
                "        var arc = this.vector[i].arc; \n" +
                "        arc.SetArcOrientation();\n" +
                "        arc.Draw();\n" +
                "      }\n" +
                "      for (i=0; i<this.edge.length; i++ ) {\n" +
                "        arc = this.edge[i].arc;\n" +
                "        arc.SetArcOrientation();\n" +
                "        arc.Draw();\n" +
                "      }\n" +
                "    }\n" +
                "    return true;\n" +
                "  }\n" +
                "\n" +
                "  // Lastly, register event handler to redraw when a vertex moves.\n" +
                "  // The redraw is called in responding to only one vertex move when the move of a vertex notifies the listeners.  \n" +
                "  // Although the redraw is registered to all the vertices, it normally responds to only one vertex move as only one vertex can move at a time.\n" +
                "  for (var i = 0; i < this.order.length; i++) {\n" +
                "    var px = this.vector[this.order[i]].p;\n" +
                "    px.RegisterEventListener( this.Redraw, this );\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "var subjects = new Map();\n" +
                "\n" +
                "function Subject( name, pos, transform )\n" +
                "{\n" +
                "  this.person =  persons.get(name);\n" +
                "  this.name = name;\n" +
                "  this.pos = pos;\n" +
                "  this.selected = false;\n" +
                "  if( transform ) {\n" +
                "    this.transform = transform;\n" +
                "    this.pos.Add( this.transform );\n" +
                "  }\n" +
                "  this.dragging = {\n" +
                "    svgRoot: null,\n" +
                "    on: false,\n" +
                "    mouseOffset: null,\n" +
                "    newpos: new Point( pos.X, pos.Y )\n" +
                "  };\n" +
                "    \n" +
                "  this.Draw = function ( parent ) {\n" +
                "    this.dragging.svgRoot = parent;\n" +
                "    var label = document.createElementNS( svgNS, 'text' );\n" +
                "    label.setAttribute( \"class\", this.person.sex+this.person.live );\n" +
                "\t  label.setAttribute( \"x\", this.pos.X );\n" +
                "\t  label.setAttribute( \"y\", this.pos.Y );\n" +
                "\t  var tn = document.createTextNode(this.person ? this.person.name : '');\n" +
                "\t\tlabel.appendChild(tn);\n" +
                "    \n" +
                "    var me = this;\n" +
                "\t  parent.appendChild( label );\n" +
                "    this.label = label;\n" +
                "\n" +
                "    var bb = label.getBBox();\n" +
                "    var anchor = document.createElementNS( svgNS, 'rect' );\n" +
                "    anchor.setAttribute( \"class\", \"anchor\");\n" +
                "\t  anchor.setAttribute( \"x\", bb.x );\n" +
                "\t  anchor.setAttribute( \"y\", bb.y );\n" +
                "\t  anchor.setAttribute( \"width\", bb.width );\n" +
                "\t  anchor.setAttribute( \"height\", bb.height );\n" +
                "    //\t  anchor.setAttribute( \"style\", \"fill:yellow;opacity:0.5\" );\n" +
                "    //\t  parent.appendChild( anchor );\n" +
                "    /*\n" +
                "    var anchor = document.createElementNS( svgNS, 'circle' );\n" +
                "    anchor.setAttribute( \"class\", \"anchor\");\n" +
                "    anchor.setAttribute( \"cx\", this.pos.X );\n" +
                "    anchor.setAttribute( \"cy\", this.pos.Y );\n" +
                "    */\n" +
                "    this.anchor = anchor;\n" +
                "    /*\n" +
                "    anchor.addEventListener( \"mouseover\", (function () {\n" +
                "         if( ! me.dragging.on ) return;\n" +
                "            me.pos.X = me.dragging.newpos.X;\n" +
                "            me.pos.Y = me.dragging.newpos.Y;\n" +
                "            me.dragging.on = false;\n" +
                "         }));\n" +
                "    anchor.addEventListener( \"mouseout\", (function () {\n" +
                "         if( ! me.dragging.on ) return;\n" +
                "            me.pos.X = me.dragging.newpos.X;\n" +
                "            me.pos.Y = me.dragging.newpos.Y;\n" +
                "            me.dragging.on = false;\n" +
                "         }));\n" +
                "         */\n" +
                "    // http://www.codedread.com/dragtest.svg\n" +
                "    anchor.addEventListener( \"mousedown\", (function (evt) {\n" +
                "      if( opmode==1 ) {\n" +
                "        me.selected = !me.selected\n" +
                "        if( me.selected ) {\n" +
                "          me.anchor.setAttribute( \"style\", \"fill:yellow;opacity:0.5\" );\n" +
                "        } else {\n" +
                "          me.anchor.setAttribute( \"style\", \"\" );\n" +
                "        }\n" +
                "      } else {\n" +
                "            me.dragging.on = true;\n" +
                "            var p = svg.createSVGPoint();\n" +
                "            p.x = evt.clientX;\n" +
                "            p.y = evt.clientY;\n" +
                "            var m = me.anchor.getScreenCTM();\n" +
                "            p = p.matrixTransform(m.inverse());\n" +
                "            me.dragging.mouseOffset = new Point( p.x-me.pos.X, p.y-me.pos.Y );\n" +
                "      }\n" +
                "      }));\n" +
                "    anchor.addEventListener( \"mouseup\", (function () {\n" +
                "         if( ! me.dragging.on ) return;\n" +
                "            me.dragging.on = false;\n" +
                "            /*\n" +
                "            me.pos.X = me.dragging.newpos.X;\n" +
                "            me.pos.Y = me.dragging.newpos.Y;\n" +
                "            me.pos.Notify( me );\n" +
                "            */\n" +
                "         }));\n" +
                "    anchor.addEventListener( \"mousemove\", (function (evt) {\n" +
                "        if( ! me.dragging.on ) return;\n" +
                "        if( evt.which != 1) {\n" +
                "          // mouse is not in down position anymore\n" +
                "          me.dragging.on = false;\n" +
                "          return;\n" +
                "        }\n" +
                "        var p = svg.createSVGPoint();\n" +
                "        p.x = evt.clientX;\n" +
                "        p.y = evt.clientY;\n" +
                "        var m = me.anchor.getScreenCTM();\n" +
                "        p = p.matrixTransform(m.inverse());\n" +
                "        me.dragging.newpos.X = p.x - me.dragging.mouseOffset.X;\n" +
                "        me.dragging.newpos.Y = p.y - me.dragging.mouseOffset.Y;\n" +
                "        me.label.setAttribute(\"x\", me.dragging.newpos.X );\n" +
                "        me.label.setAttribute(\"y\", me.dragging.newpos.Y );\n" +
                "        bb = me.label.getBBox();\n" +
                "        me.anchor.setAttribute(\"x\", bb.x );\n" +
                "        me.anchor.setAttribute(\"y\", bb.y );\n" +
                "        \n" +
                "        var oldpos = new Point( me.pos.X, me.pos.Y );            \n" +
                "        me.pos.X = me.dragging.newpos.X;\n" +
                "        me.pos.Y = me.dragging.newpos.Y;\n" +
                "        if( ! me.pos.Notify( me ) ) {\n" +
                "          me.pos.X = oldpos.X;\n" +
                "          me.pos.Y = oldpos.Y;\n" +
                "          me.label.setAttribute(\"x\", me.pos.X );\n" +
                "          me.label.setAttribute(\"y\", me.pos.Y );\n" +
                "          bb = me.label.getBBox();\n" +
                "          me.anchor.setAttribute(\"x\", bb.x );\n" +
                "          me.anchor.setAttribute(\"y\", bb.y );\n" +
                "          me.pos.Notify( me );\n" +
                "          me.dragging.on = false;\n" +
                "        }\n" +
                "         }));\n" +
                "    /* This is not supported by SVG 2. It is proposed by SVG 3.\n" +
                "    anchor.addEventListener( \"drag\", (function (event) {\n" +
                "            var cX = event.clientX;     // Get the horizontal coordinate\n" +
                "            var cY = event.clientY;     // Get the vertical coordinate\n" +
                "            var coords1 = \"client - X: \" + cX + \", Y coords: \" + cY;\n" +
                "            var sX = event.screenX;\n" +
                "            var sY = event.screenY;\n" +
                "            var coords2 = \"screen - X: \" + sX + \", Y coords: \" + sY;\n" +
                "            alert( me.person.name + \" mouse drag @ \"+coords1+\" \"+coords2);\n" +
                "         }));\n" +
                "    */\n" +
                "\t  parent.appendChild( anchor );\n" +
                "  };\n" +
                "  this.Name = function () {\n" +
                "    return this.person ? this.person.name : '';\n" +
                "  }\n" +
                "  this.Role = function () {\n" +
                "      return undefined;\n" +
                "  }\n" +
                "  if( subjects.get(this.name) ) {\n" +
                "    alert( \"Duplicate subject name : \"+this.name );\n" +
                "  }\n" +
                "  subjects.set( this.name, this );\n" +
                "}\n" +
                "\n" +
                "function DisplayMembers( treeId )\n" +
                "{\n" +
                "  var tree = document.getElementById( treeId );\n" +
                "  subjects.forEach(function(s) {\n" +
                "    s.Draw(tree); \n" +
                "  }, this);\n" +
                "}\n" +
                "\n" +
                "var stars = new Map();\n" +
                "\n" +
                "function StarFamily( treeId, color )\n" +
                "{\n" +
                "  this.treeId = treeId; // first argument is the \n" +
                "  this.mode = 0;\n" +
                "  var subjectList = [];\n" +
                "  var posList = [];\n" +
                "  var name = ':';\n" +
                "  for (var i = 2; i < arguments.length; i++) {\n" +
                "      var p = subjects.get( arguments[i] );\n" +
                "      subjectList.push( p );\n" +
                "      name += arguments[i]+':';\n" +
                "      posList.push( p.pos );\n" +
                "  }\n" +
                "  this.polygon = new Polygon( color, posList );\n" +
                "  this.polygon.SetSubjects.apply( this.polygon, subjectList );\n" +
                "  this.polygon.SetAdjMode(this.mode);\n" +
                "  this.Draw = function () { this.polygon.Draw(this.treeId); }\n" +
                "  this.SetAdjMode = function(mode) {\n" +
                "    this.mode = mode;\n" +
                "    this.polygon.SetAdjMode( mode )\n" +
                "  }\n" +
                "  stars.set( name, this )\n" +
                "}\n" +
                "\n" +
                "function DisplayStarFamilies()\n" +
                "{\n" +
                "  //  var tree = document.getElementById( treeId );\n" +
                "  stars.forEach(function(s) {\n" +
                "    s.Draw(); \n" +
                "  }, this);\n" +
                "}\n" +
                "\n" +
                "function ReportPositions()\n" +
                "{\n" +
                "  //  alert( \"Reporting\" );\n" +
                "  if( document.getElementById( \"tree\" ).style.display == 'none') {\n" +
                "    document.getElementById( \"tree\" ).style.display = '';\n" +
                "    document.getElementById( \"reporting\" ).style.display = 'none';\n" +
                "    document.getElementById( 'button' ).innerHTML = 'Report adjusted positions';\n" +
                "  } else {\n" +
                "    document.getElementById( \"tree\" ).style.display = 'none';\n" +
                "    var text = '';\n" +
                "    var transformList = new Map();\n" +
                "    var tn = 1;\n" +
                "    subjects.forEach(function(s) {\n" +
                "      var p = new Point( s.pos, 'clone' );\n" +
                "      if( s.transform )\n" +
                "          p.Sub( s.transform );\n" +
                "      var t = \"new Subject('\"+s.name+\"', new Point( \"+Math.round(p.X*100)/100+', '+Math.round(p.Y*100)/100+' )';\n" +
                "      if( s.transform ) {\n" +
                "        var varname = transformList.get( s.transform );\n" +
                "        if( ! varname ) {\n" +
                "          varname = \"shift\"+(tn++);\n" +
                "          text += \"var \" +varname + \" = new Point(\"+Math.round(s.transform.X*100)/100+', '+Math.round(s.transform.Y*100)/100+' );\\n'\n" +
                "          transformList.set( s.transform, varname );\n" +
                "        }\n" +
                "        t += \", \" + varname;\n" +
                "      }\n" +
                "      t += ');\\n';\n" +
                "      text += t;\n" +
                "    }, this);\n" +
                "    document.getElementById( \"reporting\" ).innerHTML = text;\n" +
                "    document.getElementById( \"button\" ).innerHTML = 'Show Family Tree';\n" +
                "    document.getElementById( \"reporting\" ).style.display = '';\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "var opmode = 0 // normal\n" +
                "\n" +
                "function ToggleFamilyLayoutAdj()\n" +
                "{\n" +
                "  if( document.getElementById( 'button2' ).innerHTML == 'Adjust Family Layout') {\n" +
                "    document.getElementById( 'button2' ).innerHTML = 'Freeze Family Layout';\n" +
                "    opmode = 1\n" +
                "    stars.forEach(function(s) {\n" +
                "      s.SetAdjMode(opmode); \n" +
                "      s.Draw()\n" +
                "    }, this);\n" +
                "  } else {\n" +
                "    document.getElementById( 'button2' ).innerHTML = 'Adjust Family Layout';\n" +
                "    opmode = 0\n" +
                "    stars.forEach(function(s) {\n" +
                "      s.SetAdjMode(opmode); \n" +
                "      s.Draw()\n" +
                "    }, this);\n" +
                "  }\n" +
                "}    \n" +
                "</script>\n" +
                "\n";

        String data = "<!-- stonestory_unicode -->\n" +
                "<script type=\"text/javascript\" src=\"file:///android_asset/family.js\" charset=\"unicode\"></script>";
        /*
        String data = "<!-- stonestory_unicode -->\n" +
                "<script charset=\"unicode\">\n" +
                "  \n" +
                "function SetPersons()\n" +
                "{\n" +
                "\tvar jz = new Person( '贾政', 'male');\n" +
                "\tvar wfr = new Person( '王夫人', 'female');\n" +
                "\tvar jby = new Person( '贾宝玉', 'male');\n" +
                "\tvar xym = new Person( '薛姨妈', 'female');\n" +
                "\tvar bcf = new Person( '宝钗父', 'male');\n" +
                "\tvar xbc = new Person( '薛宝钗', 'female');\n" +
                "\tvar jds = new Person( '贾代善', 'male');\n" +
                "\tvar jm = new Person( '贾母', 'female');\n" +
                "}\n" +
                "\n" +
                "function SetRelations()\n" +
                "{\n" +
                "\tSetRelation( '贾政', 'husband-wife', '王夫人' );\n" +
                "\tSetRelation( '贾政', 'father-child', '贾宝玉' );\t\n" +
                "\tSetRelation( '王夫人', 'mother-child', '贾宝玉' );\t\n" +
                "\tSetRelation( '薛姨妈', 'mother-child', '薛宝钗' );\n" +
                "\tSetRelation( '贾宝玉', 'husband-wife', '薛宝钗' );\n" +
                "\tSetRelation( '贾代善', 'husband-wife', '贾母' );\n" +
                "\tSetRelation( '贾代善', 'father-child', '贾政' );\t\n" +
                "\tSetRelation( '贾母', 'mother-child', '贾政' );\n" +
                "}\n" +
                "\n" +
                "SetPersons();\n" +
                "SetRelations();\n" +
                "\n" +
                "var family1 = new Family( persons.get('贾政'), persons.get('王夫人') );\n" +
                "var family2 = new Family( undefined, persons.get('薛姨妈') );\n" +
                "var family3 = new Family( persons.get('贾宝玉'), persons.get('薛宝钗') );\n" +
                "var family4 = new Family( persons.get('贾代善'), persons.get('贾母') );\n" +
                "\n" +
                "var jz = '贾政';\n" +
                "\n" +
                "new Subject('贾政', new Point( 30,100 ));\n" +
                "new Subject('王夫人', new Point( 70,100 ));\n" +
                "new Subject('贾宝玉', new Point( 30,160 ));\n" +
                "new Subject('宝钗父', new Point( 100,140 ));\n" +
                "new Subject('薛姨妈', new Point( 191.16,122.95 ));\n" +
                "new Subject('薛宝钗', new Point( 156.85,207.24 ));\n" +
                "new Subject('贾代善', new Point( 30,20 ));\n" +
                "new Subject('贾母', new Point( 143.9,18.97 ));\n" +
                "\n" +
                "var jzwfr = new StarFamily( 'tree', 2, '贾政', '王夫人', '贾宝玉' );\n" +
                "var xymfamily = new StarFamily( 'tree', 3, '宝钗父', '薛姨妈', '薛宝钗' );\n" +
                "var bybc = new StarFamily( 'tree', 4, '贾宝玉', '薛宝钗' );\n" +
                "var jdsjm = new StarFamily( 'tree', 5, '贾代善', '贾母', '贾政' );\n" +
                "\n";
                */

        String tail = "\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "<p>\n" +
                "Family Tree of <b id=\"viewpoint\"></b>\n" +
                "</p>\n" +
                "\n" +
                "<button id='button' type=\"button\" onclick=\"ReportPositions()\">Report adjusted positions</button>\n" +
                "<button id='button2' type=\"button\" onclick=\"ToggleFamilyLayoutAdj()\">Adjust Family Layout</button>\n" +
                "\n" +
                "<textarea id=reporting rows=\"20\"\" cols=\"50\" style=\"display:none\">\n" +
                "</textarea>\n" +
                "\n" +
                "\n" +
                "<svg id=myroot width=\"100%\" height=\"100%\" version=\"1.2\" baseProfile=\"tiny\"\n" +
                "  viewBox=\"0 0 1000 1000\"\n" +
                "  xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "  <!--\n" +
                "  <g transform=\"translate(150, 0)\">\n" +
                "    <circle class=center cx=80 cy=60 /><circle cx=80 cy=60 r=30 style=\"fill:none;stroke-width:1;stroke:red\" /> \n" +
                "    <circle class=center cx=150 cy=80 /><circle cx=150 cy=80 r=40 style=\"fill:none;stroke-width:1;stroke:red\" /> \n" +
                "   \n" +
                "    <circle cx=80 cy=60 r=50 style=\"fill:none;stroke-width:1;stroke:yellow\" /> \n" +
                "    <circle cx=150 cy=80 r=60 style=\"fill:none;stroke-width:1;stroke:yellow\" /> \n" +
                "  \n" +
                "    <circle class=center cx=118.95 cy=28.65 /><circle cx=118.9 cy=28.65 r=20 style=\"fill:none;stroke-width:1;stroke:lightblue\" />\n" +
                "  </g> \n" +
                "-->\n" +
                "  <g id='tree' transform=\"translate(40, 20)\">\n" +
                "  </g>\n" +
                "\n" +
                "</svg>\n" +
                "\n" +
                "<script>\n" +
                "var svgNS = \"http://www.w3.org/2000/svg\";\n" +
                "var svg = document.getElementById( 'myroot' );\n" +
                "\n" +
                "DisplayMembers( 'tree' );\n" +
                "DisplayStarFamilies();\n" +
                "\n" +
                "document.getElementById(\"viewpoint\").innerHTML = jz;\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";
        return prog+data+tail;
    }

    protected String getHTMLfull()
    {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "text.male { font-size: 12px; fill: blue; stroke-width: 1; text-anchor: middle; }\n" +
                "text.female { font-size: 12px; fill: green; stroke-width: 1; text-anchor: middle; }\n" +
                "text.malex { font-size: 12px; fill:blue; stroke: none; text-decoration: underline; text-anchor: middle; }\n" +
                "text.femalex { font-size: 12px; fill: green; stroke: none; text-decoration: underline; text-anchor: middle; }\n" +
                "text.person { font-size: 12px; fill: gray; stroke-width: 1; text-anchor: middle; }\n" +
                "text.father { font-size: 12px; fill: red; stroke-width: 1; text-anchor: middle; }\n" +
                "text.mother { font-size: 12px; fill: gold; stroke-width: 1; text-anchor: middle; }\n" +
                "text.son { font-size: 12px; fill: lightblue; stroke-width: 1; text-anchor: middle; }\n" +
                "text.daughter { font-size: 12px; fill: lightgold; stroke-width: 1; text-anchor: middle; }\n" +
                "</style>\n" +
                "\n" +
                "<!-- css/polygon.css -->\n" +
                "<style>\n" +
                ".vertex { r:2; fill:black; }\n" +
                ".center { r:5; fill:red; stroke:red; }\n" +
                ".startpoint { r:1; fill:blue; }\n" +
                ".vertexlabel { font-size: 10px; fill: red; stroke-width: 1; text-anchor: middle; }\n" +
                ".vertexarc1 { fill:none; stroke:lightgreen; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc1 { fill:none; stroke:lightgreen; opacity: 0.7 }\n" +
                ".vertexarc2 { fill:none; stroke:lightblue; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc2 { fill:none; stroke:lightblue; opacity: 0.7 }\n" +
                ".vertexarc3 { fill:none; stroke:yellow; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc3 { fill:none; stroke:yellow; opacity: 0.7 }\n" +
                ".vertexarc4 { fill:none; stroke:purple; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc4 { fill:none; stroke:purple; opacity: 0.7 }\n" +
                ".vertexarc5 { fill:none; stroke:blue; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc5 { fill:none; stroke:blue; opacity: 0.7 }\n" +
                ".vertexarc6 { fill:none; stroke:DarkSlateBlue ; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc6 { fill:none; stroke:DarkSlateBlue ; opacity: 0.7 }\n" +
                ".vertexarc7 { fill:none; stroke:green; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc7 { fill:none; stroke:green; opacity: 0.7 }\n" +
                ".vertexarc8 { fill:none; stroke:HotPink ; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc8 { fill:none; stroke:HotPink ; opacity: 0.7 }\n" +
                ".vertexarc9 { fill:none; stroke:red ; stroke-width: 3; opacity: 0.7 }\n" +
                ".edgearc9 { fill:none; stroke:red ; opacity: 0.7 }\n" +
                ".circletest { fill:none; stroke:yellow; }\n" +
                ".anchor { r:5; fill:white; opacity: 0; stroke:blue; }\n" +
                "</style>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "<!-- js/family_tree_unicode.js -->\n" +
                "<script>\n" +
                "// This file has to be saved in unicode\n" +
                "var svgNS = \"http://www.w3.org/2000/svg\";\n" +
                "\n" +
                "function Point( x, y )\n" +
                "{\n" +
                "\tif ( x instanceof Point ) {\n" +
                "\t\tthis.X = x.X;\n" +
                "\t\tthis.Y = x.Y;\n" +
                "\t} else {\n" +
                "\t\tthis.X = x;\n" +
                "\t\tthis.Y = y;\n" +
                "\t}\n" +
                "\tthis.Add = function (ap) { this.X += ap.X; this.Y += ap.Y; return this; };\n" +
                "\tthis.Sub = function (ap) { this.X -= ap.X; this.Y -= ap.Y; return this; };\n" +
                "\tthis.Shift = function( x, y ) { this.X += x; this.Y += y; return this; };\n" +
                "\tthis.Transform = function( matrix ) {\n" +
                "\t\t// http://www.w3.org/TR/SVGTiny12/coords.html#TransformMatrixDefined\n" +
                "\t\treturn new Point( (matrix.a * this.X) + (matrix.c * this.Y) + matrix.e, (matrix.b * this.X) + (matrix.d * this.Y) + matrix.f );\n" +
                "\t};\n" +
                "}\n" +
                "\n" +
                "function Distance( a, b ) {\n" +
                "\treturn Math.sqrt( (b.X-a.X)*(b.X-a.X)+(b.Y-a.Y)*(b.Y-a.Y));\n" +
                "}\n" +
                "\n" +
                "var persons = new Map();\n" +
                "\n" +
                "function Person( _name, _sex, _live )\n" +
                "{\n" +
                "\tthis.name = _name;\n" +
                "\tthis.sex = _sex;\n" +
                "\tthis.live = _live==null?'':_live\n" +
                "\tpersons.set( this.name, this );\n" +
                "\tthis.role = new Map();\n" +
                "\tthis.AddRole = function ( familyName, role ) { // role: father, mother, 1, 2, 3 for children in order\n" +
                "\t\tthis.role.set( familyName, role );\n" +
                "\t};\n" +
                "}\n" +
                "\n" +
                "var validRelations = [\n" +
                "\t 'husband-wife',\n" +
                "\t 'father-child',\n" +
                "\t 'mother-child',\n" +
                "\t 'sibling-elder'\n" +
                "\t ];\n" +
                "\n" +
                "function SetRelation( name1, rel, name2 )\n" +
                "{\n" +
                "\tvar p1 = persons.get( name1 );\n" +
                "\tvar p2 = persons.get( name2 );\n" +
                "\tswitch ( rel ) {\n" +
                "\tcase 'husband-wife':\n" +
                "\t\tp1.wife = name2;\n" +
                "\t\tp2.husband = name1;\n" +
                "\t\tbreak;\n" +
                "\tcase 'father-child':\n" +
                "\t\tif( p1.children == undefined || ! (p1.children instanceof Array) ) {\n" +
                "\t\t\tp1.children = [];\n" +
                "\t\t}\n" +
                "\t\tp1.children.push( name2 );\n" +
                "\t\tp2.father = name1;\n" +
                "\t\tbreak;\n" +
                "\tcase 'mother-child':\n" +
                "\t\tif( p1.children == undefined || ! (p1.children instanceof Array) ) {\n" +
                "\t\t\tp1.children = [];\n" +
                "\t\t}\n" +
                "\t\tp1.children.push( name2 );\n" +
                "\t\tp2.mother = name1;\n" +
                "\t\tbreak;\n" +
                "\tcase 'sibling-elder':\n" +
                "\t\tp1.younger = name2;\n" +
                "\t\tp2.elder = name1;\n" +
                "\t\tbreak;\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "var families = new Map();\n" +
                "\t \n" +
                "function Family( father, mother )\n" +
                "{\n" +
                "\tthis.father = father;\n" +
                "\tthis.mother = mother;\n" +
                "\tthis.name = '';\n" +
                "\tif( father ) {\n" +
                "\t\tthis.name += \":F:\"+father.name;\n" +
                "\t}\n" +
                "\tif( mother ) {\n" +
                "\t\tthis.name += \":M:\"+mother.name;\n" +
                "\t}\n" +
                "\tif( father ) father.AddRole( this.name, 'father' );\n" +
                "\tif( mother ) mother.AddRole( this.name, 'mother' );\n" +
                "\tif( father && mother ) {\n" +
                "\t\tthis.childList = [];\n" +
                "\t\tfor (var index = 0; mother.children && index < mother.children.length; index++) {\n" +
                "\t\t\tvar childName = this.mother.children[index];\n" +
                "\t\t\tvar child = persons.get( childName );\n" +
                "\t\t\tif( child.father == father.name ) {\n" +
                "\t\t\t\tchild.AddRole( this.name, this.childList.length );\n" +
                "\t\t\t\tthis.childList.push( child );\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tif( father && ! mother ) {\n" +
                "\t\tthis.childList = [];\n" +
                "\t\tfor (var index = 0; father.children && index < father.children.length; index++) {\n" +
                "\t\t\tchildName = father.children[index];\n" +
                "\t\t\tchild = persons.get( childName );\n" +
                "\t\t\tchild.AddRole( this.name, this.childList.length );\n" +
                "\t\t\tthis.childList.push( child );\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tif( ! father && mother ) {\n" +
                "\t\tthis.childList = [];\n" +
                "\t\tfor (var index = 0; mother.children && index < mother.children.length; index++) {\n" +
                "\t\t\tchildName = mother.children[index];\n" +
                "\t\t\tchild = persons.get( childName );\n" +
                "\t\t\tchild.AddRole( this.name, this.childList.length );\n" +
                "\t\t\tthis.childList.push( child );\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tfamilies.set( this.name, this );\n" +
                "\tthis.SetPanel = function ( panel ) {\n" +
                "\t\tthis.panel = panel;\n" +
                "\t};\n" +
                "}\n" +
                "\n" +
                "// Case 1: Given father and/or mother and panel position\n" +
                "// Case 2: Given father and/or mother positions\n" +
                "// Case 3: Given one of the child's position\n" +
                "// Case 4: Given two children' positions\n" +
                "// Case A: Main family, given absolute position of the box (x, y)\n" +
                "\n" +
                "function FamilyPanel( family, loc, size )\n" +
                "{\n" +
                "\tthis.family = family;\n" +
                "\tthis.translate = loc;\n" +
                "\tthis.xMargin = 30;\n" +
                "\tthis.yTopMargin = 20; // The margin from a top y of a text element position. This is because the y of text is at the bottom. \n" +
                "\tthis.yBotMargin = 10; // The margin from a bottom y of a text element position\n" +
                "\tthis.size = size ? new Point( size, 'clone') : new Point( 100, 100 );\n" +
                "\tthis.nameWidth = 36;\n" +
                "\tthis.nameHeight = 15;\n" +
                "\tthis.nameHeightMargin = 10;\n" +
                "\tthis.nameWidthMargin = 10;\n" +
                "\t// x\n" +
                "\t// xMargin  + 1/2 father + nameWidthMargin + 1/2 mother + xMargin\n" +
                "\t//   30         20         10             20        30\n" +
                "\t// y 2- children\n" +
                "\t// yTopMargin + (Parent)nameHeight + nameHeightMargin + (child)nameHeight + yBotMargin\n" +
                "\t// 20         +        15          +     10           +        15         +     10      = 70\n" +
                "\t// y 4- children\n" +
                "\t// yTopMargin + (Parent)nameHeight + nameHeightMargin + (child)nameHeight + nameHeightMargin + (child)nameHeight + yBotMargin\n" +
                "\t// 20         +        15          +     10           +        15         +     10           +        15         +     10      = 95\n" +
                "\tthis.Transform = function ( person, pos, matrix ) {\n" +
                "\t\tif( person.location )\n" +
                "\t\t\treturn;\n" +
                "\t\tperson.location = matrix ? pos.Transform( matrix ) : new Point( pos, 'clone' );\n" +
                "\t};\n" +
                "\t// Calculate children' positions\n" +
                "\tthis.childLevel = Math.round( 0.1+family.childList ? family.childList.length / 2 : 0 );\n" +
                "\tif( this.childLevel > 2 ) {\n" +
                "\t\tthis.size.Y += (this.childLevel-2)*(this.nameHeight + this.nameHeightMargin)\n" +
                "\t}\n" +
                "\tthis.firstChildPosY = this.size.Y - (this.childLevel-1)*(this.nameHeightMargin+this.nameHeight) - this.yBotMargin;\n" +
                "\tthis.position = {\n" +
                "\t\tfather: new Point( this.xMargin, this.yTopMargin),\n" +
                "\t\tmother: new Point( this.size.X - this.xMargin, this.yTopMargin ),\n" +
                "\t\tchild: new Array(this.family.childList.length)\n" +
                "\t  };\n" +
                "\t  \n" +
                "\tthis.SetWidth = function ( w ) {\n" +
                "\t\tif( this.family.mother ) this.position.mother.X += w - this.size.X;\n" +
                "\t\tfor (var index = 1; index < this.position.child.length; index += 2 )\n" +
                "\t\t\tthis.position.child[index].X += w - this.size.X;\n" +
                "\t\tthis.size.X = w;\n" +
                "\t}\n" +
                "\tthis.SetChildrenPos = function() {\n" +
                "\t\tvar posY = this.firstChildPosY;\n" +
                "\t\tfor (var index = 0; index < this.position.child.length; index += 2 ) {\n" +
                "\t\t\tthis.position.child[index] = new Point( this.position.father.X, posY );\n" +
                "\t\t\tif( index+1 < this.position.child.length )\n" +
                "\t\t\t\tthis.position.child[index+1] = new Point( this.position.mother.X, posY );\n" +
                "\t\t\tposY += this.nameHeightMargin+this.nameHeight;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tthis.transformText = function() {\n" +
                "\t\tvar transformTxt = \"\";\n" +
                "\t\tif( this.translate && ( this.translate.X != 0 || this.translate.Y != 0 ) )\n" +
                "\t\t\ttransformTxt += \"translate(\"+this.translate.X+\",\"+this.translate.Y+\") \";\n" +
                "\t\tif( this.skewYAngle && this.skewYAngle != 0 )\n" +
                "\t\t\ttransformTxt += \"skewY(\"+this.skewYAngle+\") \";\n" +
                "\t\tif( this.cos && this.cos != 1 )\n" +
                "\t  \t\ttransformTxt += \" scale(\"+this.cos+\",1)\";\n" +
                "\t\tif( this.moveBack && (this.moveBack.X != 0 || this.moveBack.Y != 0 ) )\n" +
                "\t  \t\ttransformTxt += \" translate(\"+this.moveBack.X+\",\"+this.moveBack.Y+\")\";\n" +
                "\t\tif( transformTxt == \"\")\n" +
                "\t\t\ttransformTxt = undefined;\n" +
                "\t\treturn transformTxt;\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "// Case 1: Given father and/or mother and panel position.\n" +
                "// This is a main panel to decide other family panels' positions.\n" +
                "// Because it's a main panel, the position p will determine all the relative and absolute positions.\n" +
                "function MainPanel( family, location, size )\n" +
                "{\n" +
                "\tFamilyPanel.call( this, family, location, size ); // Create the base object\n" +
                "\tfamily.SetPanel( this );\n" +
                "\tthis.SetChildrenPos();\n" +
                "}\n" +
                "\n" +
                "// Case 2: Given father and/or mother positions\n" +
                "function ParentPanel( family )\n" +
                "{\n" +
                "\tFamilyPanel.call( this, family ); // Create the base object\n" +
                "\tfamily.SetPanel( this );\n" +
                "\tthis.SetChildrenPos();\n" +
                "\tif( family.father.location && ! family.mother.location ) {\n" +
                "\t\t// single father, use father's position to determine panel location\n" +
                "\t\tthis.translate = new Point( family.father.location, 'clone' );\n" +
                "\t \tthis.translate.Sub( this.position.father );\n" +
                "\t} else if( ! family.father.location && family.mother.location ) {\n" +
                "\t\t// single mother, use mother's position to determine panel location\n" +
                "\t\tthis.translate = new Point( family.mother.location, 'clone' );\n" +
                "\t \tthis.translate.Shift( this.size.X-this.position.mother.X, -this.position.mother.Y );\n" +
                "\t} else {\n" +
                "\t\tthis.translate = new Point( family.father.location, 'clone' );\n" +
                "\t \tthis.translate.Sub( this.position.father );\n" +
                "\t    this.distance = Distance( family.father.location, family.mother.location );\n" +
                "\t\t// Our goal is to skew the panel to match the angle of the father-mother line.\n" +
                "\t\t// and at the same time, keep the absolute location of both father and mother.\n" +
                "\t\t// Before skewing it, we set the X-axis distance to the real distance between father and mother\n" +
                "\t\t// because this is the distance when we look at the panel from straight angle.\n" +
                "\t\t// So the panel's width is the distance plus the margin (both left and right)\n" +
                "\t\tthis.SetWidth(this.xMargin + this.distance + this.xMargin);\n" +
                "\t\t// The angle can be calcualated by the cosin of that angle\n" +
                "\t  \tthis.cos = (family.mother.location.X-family.father.location.X) / this.distance;\n" +
                "\t\tthis.tan = (family.mother.location.Y-family.father.location.Y) / (family.mother.location.X-family.father.location.X);\n" +
                "\t  \tthis.skewYAngle = Math.atan( this.tan ) * 180 / Math.PI;\n" +
                "\t\t// After skewing it in Y, the distance is enlarged by 1/cos(a) where a is the skew angle.\n" +
                "\t\t// Therefore, we need scale it back by cos(a) to maintain the distance.\n" +
                "\t\t// However, another issue arises, the X of father is also scaled back, we need to move it back to the original position\n" +
                "\t\t// i.e. the difference between the current positon and the original position = (this.position.father.X - this.position.father.X * cos)\n" +
                "\t\t// For Y, the skew move downs father's Y by X*tg(a), so we need to move up by the same.\n" +
                "\t  \tthis.moveBack = new Point( this.position.father.X * (1-this.cos),\n" +
                "\t\t\t  -this.xMargin*(family.mother.location.Y-family.father.location.Y)/(family.mother.location.X-family.father.location.X) );\n" +
                "\t\t// When father-mother line is horizontal, i.e. family.father.location.Y=family.mother.location.Y,\n" +
                "\t\t// cos = 1, skewYAngle = 0, moveBack=(0,0)\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "// Case 3, Given a child's position\n" +
                "function ChildPanel( family, child )\n" +
                "{\n" +
                "\tFamilyPanel.call( this, family ); // Create the base object\n" +
                "\tfamily.SetPanel( this );\n" +
                "\tthis.SetChildrenPos();\n" +
                "\t\n" +
                "\tfor (var cn = 0; family.childList && cn < family.childList.length; cn++) {\n" +
                "\t\tvar ch = family.childList[cn];\n" +
                "\t\tif( ch.name == child.name )\n" +
                "\t\t\tbreak;\n" +
                "\t}\n" +
                "\tvar thispos = this.position.child[cn];\n" +
                "\tthis.translate = child.location.Sub( thispos );\n" +
                "}\n" +
                "\n" +
                "function DisplayPanel()\n" +
                "{\n" +
                "\t//\n" +
                "\tvar g = document.createElementNS( svgNS, 'g' );\n" +
                "\tg.setAttributeNS( null, \"id\", this.family.name );\n" +
                "\tg.setAttributeNS( null, \"style\", \"opacity:0.5\");\n" +
                "\t\n" +
                "\tvar matrix;\n" +
                "\tvar transform = this.transformText();\n" +
                "\tif( transform ) {\n" +
                "\t\tg.setAttributeNS( null, \"transform\", transform );\n" +
                "\t    matrix = g.getScreenCTM();\n" +
                "\t}\n" +
                "\t\n" +
                "\tvar rect = document.createElementNS( svgNS, 'rect' );\n" +
                "\trect.setAttributeNS( null, \"class\", \"icon_frame\");\n" +
                "\trect.setAttributeNS( null, \"x\", \"0\");\n" +
                "\trect.setAttributeNS( null, \"y\", \"0\");\n" +
                "\trect.setAttributeNS( null, \"width\", this.size.X );\n" +
                "\trect.setAttributeNS( null, \"height\", this.size.Y );\n" +
                "\trect.setAttributeNS( null, \"fill\", \"url(#icon_background)\" );\n" +
                "\trect.setAttributeNS( null, \"stroke\", \"#000000\" );\n" +
                "\trect.setAttributeNS( null, \"fill-opacity\", \"0.7\" );\n" +
                "\tg.appendChild( rect );\n" +
                "\t\n" +
                "\tvar txt;\n" +
                "\tvar tn;\n" +
                "\t\n" +
                "\tif( this.family.father ) {\n" +
                "\t\ttxt = document.createElementNS( svgNS, 'text' );\n" +
                "\t\ttxt.setAttribute( \"class\", \"father\");\n" +
                "\t\ttxt.setAttribute( \"x\", this.position.father.X );\n" +
                "\t\ttxt.setAttribute( \"y\", this.position.father.Y );\n" +
                "\t\ttxt.setAttribute( \"comp-op\", \"soft-light\");\n" +
                "\t\ttn = document.createTextNode(this.family.father.name);\n" +
                "\t\ttxt.appendChild(tn);\n" +
                "\t\tg.appendChild( txt );\n" +
                "\t\tthis.Transform( this.family.father, this.position.father, matrix );\n" +
                "\t}\n" +
                "\t\n" +
                "\tif( this.family.mother ) {\n" +
                "\t\ttxt = document.createElementNS( svgNS, 'text' );\n" +
                "\t\ttxt.setAttribute( \"class\", \"mother\");\n" +
                "\t\ttxt.setAttribute( \"x\", this.position.mother.X );\n" +
                "\t\ttxt.setAttribute( \"y\", this.position.mother.Y );\n" +
                "\t\ttxt.setAttribute( \"comp-op\", \"soft-light\");\n" +
                "\t\ttn = document.createTextNode(this.family.mother.name);\n" +
                "\t\ttxt.appendChild(tn);\n" +
                "\t\tg.appendChild( txt );\n" +
                "\t\tthis.Transform( this.family.mother, this.position.mother, matrix );\n" +
                "\t}\n" +
                "\n" +
                "\tfor (var c = 0; c < this.family.childList.length; c++) {\n" +
                "\t\tvar child = this.family.childList[c];\n" +
                "\t\ttxt = document.createElementNS( svgNS, 'text' );\n" +
                "\t\ttxt.setAttribute( \"class\", child.sex=='male'?'son':'daughter');\n" +
                "\t\ttxt.setAttribute( \"x\", this.position.child[c].X );\n" +
                "\t\ttxt.setAttribute( \"y\", this.position.child[c].Y );\n" +
                "\t\ttxt.setAttribute( \"comp-op\", \"soft-light\");\n" +
                "\t\ttn = document.createTextNode(child.name);\n" +
                "\t\ttxt.appendChild(tn);\n" +
                "\t\tg.appendChild( txt );\n" +
                "\t\tthis.Transform( child, this.position.child[c], matrix );\n" +
                "\t}\n" +
                "\tvar svg = document.getElementById( 'myroot' );\n" +
                "\tsvg.appendChild( g );\n" +
                "}\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "<!-- js/star.js -->\n" +
                "\n" +
                "<script charset=\"ansi\">\n" +
                "\n" +
                "/*\n" +
                "\n" +
                "To do:\n" +
                "\n" +
                "create a bounding box anchor: done 7/27/2015\n" +
                "\n" +
                "refactor to have model-conrol-view: 7/29/2015\n" +
                "\n" +
                "make vertex position update to redraw: done if vertex order does not change. 7/29/2015 Disallowing angle order change. 7/31/2015\n" +
                "\n" +
                "Output positions of vertex so that a static version can be published. done 8/2/2015\n" +
                "\n" +
                "Refactor to make star.html general enough to be used for any family. The author needs only modify stonestory_unicode.js for his own family. 8/2/2015.\n" +
                "\n" +
                "Make a static (no move) version of the document\n" +
                "\n" +
                "Defects:\n" +
                "  For DINK family, sometimes the arc flips to outside. That's because the center is on the line between the two points. The choosing algorithm using line equation value sign does not work because for the center the calculated line value is 0. It seems that the points outside (on the left when going clock-wise) always have the negative line equation value. This seems to fix the problem but not proven mathematically yet. 7/30/2015\n" +
                "  \n" +
                "  When two persons are very far, the edge arc may cross the mid-line, need to detect it and make arc radius bigger. Fixed. 8/1/2015 A minimum gap between the arc and the mid-point between two vertices can be specified. With that, the minimum radious of the connecting arc can be calculated. See in-line comments in Edge class for exact mathematical proof.\n" +
                "\n" +
                "*/\n" +
                "\n" +
                "function Point( x, y )\n" +
                "{\n" +
                "\tif ( x instanceof Point ) {\n" +
                "\t\tthis.X = x.X;\n" +
                "\t\tthis.Y = x.Y;\n" +
                "\t} else {\n" +
                "\t\tthis.X = x;\n" +
                "\t\tthis.Y = y;\n" +
                "\t}\n" +
                "\tthis.Add = function (ap) { this.X += ap.X; this.Y += ap.Y; return this; };\n" +
                "\tthis.Sub = function (ap) { this.X -= ap.X; this.Y -= ap.Y; return this; };\n" +
                "\tthis.Shift = function( x, y ) { this.X += x; this.Y += y; return this; };\n" +
                "  this.func = [];\n" +
                "  this.RegisterEventListener = function (func, client) {\n" +
                "    this.func.push( { callback: func, client: client } );\n" +
                "  }\n" +
                "  // Notify the client functions that are registed with the move of this point\n" +
                "  // Each function is called with registered client and with a notification message as the parameter of the function\n" +
                "  // If a function call returns false, the whole notification will return false.\n" +
                "  // The action to the return value is determined by the notifier (the one sends the notification).\n" +
                "  // Normally, the nofifier may void the move and reset the position to the original position before the move.\n" +
                "  this.Notify = function ( msg ) {\n" +
                "    var allowed = true;\n" +
                "    for (var n = 0; n < this.func.length; n++) {\n" +
                "      var f = this.func[n];\n" +
                "      allowed = allowed && f.callback.call( f.client, msg );\n" +
                "    }\n" +
                "    return allowed;\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "// http://math.stackexchange.com/questions/256100/how-can-i-find-the-points-at-which-two-circles-intersect\n" +
                "function Cross2Circles( c1, r1, c2, r2 )\n" +
                "{\n" +
                "  var d = Math.sqrt((c2.X-c1.X)*(c2.X-c1.X)+(c2.Y-c1.Y)*(c2.Y-c1.Y));\n" +
                "  var l = ( r1*r1 - r2*r2 + d*d ) / (d+d);\n" +
                "  var h = Math.sqrt( r1*r1 - l*l );\n" +
                "  \n" +
                "  return [ new Point( ( l / d ) * (c2.X-c1.X) + ( h / d ) * (c2.Y-c1.Y) + c1.X, ( l / d ) * (c2.Y-c1.Y) - ( h / d ) * (c2.X-c1.X) + c1.Y ),\n" +
                "           new Point( ( l / d ) * (c2.X-c1.X) - ( h / d ) * (c2.Y-c1.Y) + c1.X, ( l / d ) * (c2.Y-c1.Y) + ( h / d ) * (c2.X-c1.X) + c1.Y )\n" +
                "         ];\n" +
                "}\n" +
                "\n" +
                "function Vector( c, p )\n" +
                "{\n" +
                "\tthis.center = c;\n" +
                "\tthis.p = p;\n" +
                "\n" +
                "  // arc start point will be set by the edge starting from this vertex\n" +
                "  // arc end point will be set by the edge ending to this vertex\n" +
                "  this.arc = new Arc( this.p, 20 );\n" +
                "  \n" +
                "  this.VectorReCalc = function () {\n" +
                "  \tthis.dx = p.X - c.X;\n" +
                "    this.dy = p.Y - c.Y;\n" +
                "  \tthis.distance = Math.sqrt( this.dx*this.dx+this.dy*this.dy );\n" +
                "    var a = this.dx==0 ? 90 : Math.atan( this.dy/this.dx ) * 180 / Math.PI;\n" +
                "    if( this.dx == 0 )\n" +
                "        a = this.dy < 0 ? 270 : 90;\n" +
                "    else if( this.dx < 0 )\n" +
                "      if( this.dy == 0 )\n" +
                "        a = 180;\n" +
                "      else if( this.dy < 0 )\n" +
                "        a += 180; // III 45 + 180 = 225\n" +
                "      else\n" +
                "        a += 180; // II -45 + 180 = 135\n" +
                "    else\n" +
                "      if( this.dy == 0 )\n" +
                "        a = 0;\n" +
                "      else if( this.dy < 0 )\n" +
                "         a += 360; // IV\n" +
                "      // else I 45 = 45 \n" +
                "    this.angle = a;\n" +
                "  \tthis.dx /= this.distance;\n" +
                "    this.dy /= this.distance;\n" +
                "  }\n" +
                "  \n" +
                "  this.VectorReCalc();\n" +
                "  \n" +
                "  this.AwayFrom1 = function( d, oldPoint ) { // Calculate a point in the line  with a distance from center point\n" +
                "    if( oldPoint && oldPoint instanceof Point ) {\n" +
                "      oldPoint.X = this.center.X+this.dx*d;\n" +
                "      oldPoint.Y = this.center.Y+this.dy*d;\n" +
                "      return oldPoint;\n" +
                "    } else\n" +
                "      return new Point( this.center.X+this.dx*d, this.center.Y+this.dy*d);\n" +
                "  }\n" +
                "  this.SideSign = function (p) { // Calculate the side value for a point to decide the side of the point \n" +
                "    return (p.Y-this.center.Y)*this.dx - (p.X-this.center.X)*this.dy;\n" +
                "  }\n" +
                "  /*\n" +
                "  this.testdraw1 = function () {\n" +
                "    var cc = document.createElementNS( svgNS, 'circle' );\n" +
                "    cc.setAttribute( 'class', 'circletest' );\n" +
                "    cc.setAttribute( 'cx', this.center.X );\n" +
                "    cc.setAttribute( 'cy', this.center.Y );\n" +
                "    cc.setAttribute( 'r', this.arcRadius );\n" +
                "    svg.appendChild( cc );\n" +
                "  }\n" +
                "  */\n" +
                "  //  this.testdraw1();\n" +
                "}\n" +
                "\n" +
                "function Arc( center, radius, color )\n" +
                "{\n" +
                "  // Note: arc always draws clock-wise.\n" +
                "  // The center of an edge arc is on the outside of the polygon. \n" +
                "  // The center of a vertex arc is on the inside of the polygon, which is the same side of of the center of the polygon.\n" +
                "  // Therefore, edge are draws from v2 to v1.\n" +
                "  // Below, c is the center of the edge arc\n" +
                "  //  1 is the start point of the edge arc and also the start point of v2 arc,\n" +
                "  //  3 is the end point of the edge arc and also the end point of v1 arc.\n" +
                "  //  Edge arc draws from 1, 2, 2, 2, 3.\n" +
                "  //  v1 arc draws a, 2, 3\n" +
                "  //  v2 arc draws 1, 2, b\n" +
                "  //\n" +
                "  //           c       \n" +
                "  //\n" +
                "  //     2 3      1 2\n" +
                "  // v1 a \\  2 2 2 / b   v2\n" +
                "  //       \\      /\n" +
                "  //        \\    /  \n" +
                "  //         \\  /\n" +
                "  //          \\/\n" +
                "  \n" +
                "  this.center = center;\n" +
                "  this.radius = radius;\n" +
                "  this.color = color;\n" +
                "  this.SetArcPositionAndOrientation = function ( v1, v2 ) {\n" +
                "    // The arc is specified by two vectors. That's enough to calcualte the orientation\n" +
                "    // However, need to calculate the start and end points by calculating the cross points on the circle with the vector\n" +
                "    var v_st = new Vector( this.center, v2.p );\n" +
                "    var v_ed = new Vector( this.center, v1.p );\n" +
                "    var a = v_ed.angle - v_st.angle;\n" +
                "    if( a < 0 ) a += 360;\n" +
                "    this.largeArc = ( a > 180 ? 1 : 0 );\n" +
                "    v2.arc.start = this.start = v_st.AwayFrom1( this.radius ); // start point of the edge arc and the start point of the arc for v2\n" +
                "    v1.arc.end = this.end = v_ed.AwayFrom1( this.radius ); // end point of the edge arc and the end point of the arc for v1\n" +
                "  }\n" +
                "  this.SetArcOrientation  = function () {\n" +
                "    // The start and end point are known already, set by the edge\n" +
                "    // Only need to calcualte the orientation\n" +
                "    var v_st = new Vector( this.center, this.start );\n" +
                "    var v_ed = new Vector( this.center, this.end );\n" +
                "    var a = v_ed.angle - v_st.angle;\n" +
                "    if( a < 0 ) a += 360;\n" +
                "    this.largeArc = ( a > 180 ? 1 : 0 );\n" +
                "  }\n" +
                "  this.CreateView = function (parent, cls) {\n" +
                "      this.view = document.createElementNS( svgNS, 'path' );\n" +
                "      this.view.setAttribute( 'class', cls );\n" +
                "      this.view.setAttribute( 'stroke', this.color );\n" +
                "      parent.appendChild( this.view );\n" +
                "  };\n" +
                "  this.Draw = function () {\n" +
                "      // draw the edge arc from start to end point, clockwise\n" +
                "      var pathD = \"M\"+this.start.X+\",\"+this.start.Y+\" \"; //    <path d=\"M40,20 A10,10 0 0,1 38,25\" style=\"fill:none;stroke:lightblue\" />\n" +
                "      pathD += \"A\"+ this.radius+\",\"+this.radius+\" \";\n" +
                "      pathD += \"0 \";\n" +
                "      pathD += this.largeArc; // large 1 or small 0 arc\n" +
                "      pathD += \",1 \"; // clockwise\n" +
                "      pathD += this.end.X+\",\"+this.end.Y;\n" +
                "      this.view.setAttribute( 'd', pathD );\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "// An edge is specified by 2 vectors and is v2 - v1\n" +
                "function Edge( v1, v2, color )\n" +
                "{\n" +
                "  Vector.call( this, v1.p, v2.p, color );\n" +
                "  this.v1 = v1;\n" +
                "  this.v2 = v2;\n" +
                "  \n" +
                "  this.EdgeReCalc = function () {\n" +
                "    // ReCalc the base vector\n" +
                "    this.VectorReCalc();\n" +
                "    this.centerSign = this.SideSign( v1.center );\n" +
                "    var edgeArcRadius = this.distance - v1.arc.radius - v2.arc.radius;\n" +
                "    // make v1.arcRadius+edgeArcRadius + v2.arcRadius+edgeArcRadius > distance\n" +
                "    // Let c be the center point between v1.p and v2.p\n" +
                "    // Let r = v1.arc.radius = v2.arc.radius\n" +
                "    // Let d = the distance between v1.p and v2.p\n" +
                "    // Let h = the distance between the center of the arc and the center point c\n" +
                "    // Let R = edgeArcRadius\n" +
                "    // h^2 + (d/2)^2 = (R+r)^2\n" +
                "    // We need h >= R + a where a is the smallest gap between the arc and the center point c.\n" +
                "    // h^2 = (R+r)^2 - (d/2)^2 = R^2 + 2Rr + r^2 - (d/2)^2\n" +
                "    // Therefore, h^2 = R^2 + 2Rr + r^2 - (d/2)^2 >= (R+a)^2 = R^2 + 2Ra + a^2\n" +
                "    // 2Rr - 2Ra >= a^2 + (d/2)^2 - r^2\n" +
                "    // R >= ( a^2 + (d/2)^2 - r^2 ) / (2r-2a) ( if r > a )\n" +
                "    var D = this.distance / 2;\n" +
                "    var r = v1.arc.radius;\n" +
                "    var a = 3;\n" +
                "    D = a*a + D*D - r*r;\n" +
                "    D /= (r-a)+(r-a);\n" +
                "    if( edgeArcRadius < 5 ) { // if too close\n" +
                "      edgeArcRadius = 5;\n" +
                "    }\n" +
                "    if( edgeArcRadius < D ) {\n" +
                "      edgeArcRadius = D;\n" +
                "    }\n" +
                "    var cross = Cross2Circles( v1.p, v1.arc.radius+edgeArcRadius, v2.p, v2.arc.radius+edgeArcRadius );\n" +
                "    var side = 0;\n" +
                "    // want to choose the cross point that is on the opposite side of the line v1-v2 with the center point \n" +
                "    var signC = this.SideSign( cross[side] );\n" +
                "    //    if( this.centerSign > 0 && signC > 0 || this.centerSign < 0 && signC < 0 ) {\n" +
                "    if( signC > 0 ) {\n" +
                "      side = 1;\n" +
                "      signC = this.SideSign( cross[side] );\n" +
                "    }\n" +
                "    // figure out the tangent point between the connecting circle and the vertex circle\n" +
                "    this.arc.center = cross[side];\n" +
                "    this.arc.radius = edgeArcRadius;\n" +
                "    this.arc.SetArcPositionAndOrientation( v1, v2 );\n" +
                "  }\n" +
                "  \n" +
                "  this.EdgeReCalc();\n" +
                "}\n" +
                "\n" +
                "function Polygon( color, pos )\n" +
                "{ // https://en.wikipedia.org/wiki/Centroid\n" +
                "  this.color = color;\n" +
                "  var vec = [];\n" +
                "  /*\n" +
                "  for (var i = 0; i < arguments.length; i++) {\n" +
                "      if( arguments[i] instanceof Point ) {\n" +
                "        vec.push( arguments[i] );\n" +
                "      }\n" +
                "  }\n" +
                "  */\n" +
                "\n" +
                "  this.mode = 0; // default mode, 0: family layout is fixed, 1: family can be moved around the family head, the first member of the person\n" +
                "  this.SetAdjMode  = function(mode) {\n" +
                "    this.mode = mode;\n" +
                "  }\n" +
                "\n" +
                "  for (var i = 0; i < pos.length; i++) {\n" +
                "      if( pos[i] instanceof Point ) {\n" +
                "        vec.push( pos[i] );\n" +
                "      }\n" +
                "  }\n" +
                "  \n" +
                "  // Calculate the center of this Polygon.\n" +
                "  // if c is passed in as a parameter, it is set to the calculated center and returned\n" +
                "  // if parameter is passed in as null, a new center point is calculated and returned. ?? Why ? need to revisit later\n" +
                "  this.CalcCenter = function ( c ) {\n" +
                "    var x = 0.0;\n" +
                "    var y = 0.0;\n" +
                "    for (var i = 0; i < vec.length; i++) {\n" +
                "      x += vec[i].X;\n" +
                "      y += vec[i].Y;\n" +
                "    }\n" +
                "    if( c ) {\n" +
                "      c.X = x/vec.length;\n" +
                "      c.Y = y/vec.length;\n" +
                "      return c;\n" +
                "    } else\n" +
                "      return new Point( x/vec.length, y/vec.length );\n" +
                "  }\n" +
                "  \n" +
                "  // figure out the order of vertices by the angle\n" +
                "  this.swapped = true;\n" +
                "  this.SortByAngle = function () {\n" +
                "    for (var i = 0; i < this.order.length-1; i++) {\n" +
                "      var minAngle = this.vector[this.order[i]].angle;\n" +
                "      for (var j = i+1; j < this.order.length; j++) {\n" +
                "        if( this.vector[this.order[j]].angle < minAngle ) {\n" +
                "          minAngle = this.vector[this.order[j]].angle;\n" +
                "          var minId = this.order[j];\n" +
                "          this.order[j] = this.order[i];\n" +
                "          this.order[i] = minId;\n" +
                "          this.swapped = true; // this indicates the edges need to be recalculated.\n" +
                "        }\n" +
                "      }\n" +
                "    }   \n" +
                "  }\n" +
                "  this.AngleOrderChanged = function () {\n" +
                "    for (var i = 0; i < this.order.length-1; i++) {\n" +
                "      var minAngle = this.vector[this.order[i]].angle;\n" +
                "      for (var j = i+1; j < this.order.length; j++) {\n" +
                "        if( this.vector[this.order[j]].angle < minAngle ) {\n" +
                "          return true; // this indicates the angle order has changed.\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    return false; // Angle order has not changed.\n" +
                "  }\n" +
                "\n" +
                "  this.center = this.CalcCenter();\n" +
                "  this.order = [];\n" +
                "  this.vector = [];\n" +
                "  // Create center to vertex vectors\n" +
                "  for (var i = 0; i < vec.length; i++) {\n" +
                "     var p = vec[i];\n" +
                "     var v = new Vector( this.center, p, this.color );\n" +
                "     this.order.push(i);\n" +
                "     this.vector.push(v);\n" +
                "  }\n" +
                "  this.SortByAngle();\n" +
                "  // create edges, each edge is a vector of V2-V1 where V1 and V2 are center vectors of two vertices.\n" +
                "  // Edges may change due to the move of vertices.\n" +
                "  // Therefore, it has to be determined after vertex angles are sorted.\n" +
                "  \n" +
                "  this.CreateEdges = function () {\n" +
                "    this.edge = [];\n" +
                "    for (var i = 0; i < this.order.length; i++) {\n" +
                "      var j = i + 1;\n" +
                "      if( j==this.order.length ) j = 0;\n" +
                "      var e = new Edge( this.vector[this.order[i]], this.vector[this.order[j]], this.color );\n" +
                "      this.edge.push( e );\n" +
                "    }\n" +
                "  }\n" +
                "  \n" +
                "  this.CreateEdges();\n" +
                "\n" +
                "  this.SetSubjects = function () {\n" +
                "    this.subject = [];\n" +
                "    for (var i = 0; i < arguments.length; i++) {\n" +
                "        this.subject.push( arguments[i] );\n" +
                "    }    \n" +
                "  }\n" +
                "  \n" +
                "  /*  \n" +
                "  this.draw1 = function ( parent ) {\n" +
                "    var redraw = this.pol ? true : false;\n" +
                "    // <polygon points=\"200,10 250,190 160,210\" style=\"fill:lime;stroke:purple;stroke-width:1\" />\n" +
                "    if( ! this.pol ) {\n" +
                "      this.pol = document.createElementNS( svgNS, 'polygon' );\n" +
                "      this.label = [];\n" +
                "      this.cc = [];\n" +
                "    }\n" +
                "    this.points = '';\n" +
                "    for (var i = 0; i < this.order.length; i++) {\n" +
                "      var v = this.vector[this.order[i]]\n" +
                "      var pt = v.p;\n" +
                "      this.points += pt.X + ',' + pt.Y + ' ';\n" +
                "      // If there is no subject defined, \n" +
                "      // label each vertex with the order number of the vertices when they are provided\n" +
                "      // It's not the order they are drawn.\n" +
                "      var labelPos = v.AwayFrom1( v.distance + 5 ); // outside 5\n" +
                "      if( redraw ) {\n" +
                "  \t\t  this.label[i].setAttribute( \"x\", labelPos.X );\n" +
                "  \t\t  this.label[i].setAttribute( \"y\", labelPos.Y );\n" +
                "      } else {\n" +
                "        var label = document.createElementNS( svgNS, 'text' );\n" +
                "        label.setAttribute( \"class\", \"vertexlabel\");\n" +
                "  \t\t  label.setAttribute( \"x\", labelPos.X );\n" +
                "  \t\t  label.setAttribute( \"y\", labelPos.Y );\n" +
                "  \t\t  var tn = document.createTextNode(this.order[i]);\n" +
                "    \t\tlabel.appendChild(tn);\n" +
                "  \t\t  parent.appendChild( label );\n" +
                "        this.label.push(label);\n" +
                "      }\n" +
                "\n" +
                "      if( redraw ) {\n" +
                "        this.cc[i].setAttribute( 'cx', pt.X );\n" +
                "        this.cc[i].setAttribute( 'cy', pt.Y );\n" +
                "      } else {\n" +
                "        var cc = document.createElementNS( svgNS, 'circle' );\n" +
                "        cc.setAttribute( 'class', 'vertex' );\n" +
                "        cc.setAttribute( 'cx', pt.X );\n" +
                "        cc.setAttribute( 'cy', pt.Y );\n" +
                "        parent.appendChild( cc );\n" +
                "        this.cc.push( cc );\n" +
                "      }       \n" +
                "\n" +
                "      if( i==0 ) { // mark the starting point\n" +
                "        labelPos = v.AwayFrom1( v.distance - 5 ); // inside 5\n" +
                "        if( redraw ) {\n" +
                "          this.first.setAttribute( 'cx', labelPos.X );\n" +
                "          this.first.setAttribute( 'cy', labelPos.Y );\n" +
                "        } else {\n" +
                "          cc = document.createElementNS( svgNS, 'circle' );\n" +
                "          cc.setAttribute( 'class', 'startpoint' );\n" +
                "          cc.setAttribute( 'cx', labelPos.X );\n" +
                "          cc.setAttribute( 'cy', labelPos.Y );\n" +
                "          parent.appendChild( cc );\n" +
                "          this.first = cc;\n" +
                "        }       \n" +
                "      }      \n" +
                "    }\n" +
                "\n" +
                "    this.pol.setAttribute( 'points', this.points );\n" +
                " \t\tthis.pol.setAttribute( \"style\", \"fill:none;stroke:purple;stroke-width:1\");\n" +
                "    if( ! redraw )\n" +
                "      parent.appendChild( this.pol );\n" +
                "    // comment starts\n" +
                "    var cc = document.createElementNS( svgNS, 'circle' );\n" +
                "    cc.setAttribute( 'class', 'center' );\n" +
                "    cc.setAttribute( 'cx', this.center.X );\n" +
                "    cc.setAttribute( 'cy', this.center.Y );\n" +
                "    svg.appendChild( cc );\n" +
                "    // comment ends\n" +
                "    if( redraw ) {\n" +
                "      this.centerView.setAttribute( 'cx', this.center.X );\n" +
                "      this.centerView.setAttribute( 'cy', this.center.Y );\n" +
                "    } else {\n" +
                "      cc = document.createElementNS( svgNS, 'circle' );\n" +
                "      cc.setAttribute( 'class', 'center' );\n" +
                "      cc.setAttribute( 'cx', this.center.X );\n" +
                "      cc.setAttribute( 'cy', this.center.Y );\n" +
                "      parent.appendChild( cc );\n" +
                "      this.centerView = cc;\n" +
                "    }\n" +
                "    \n" +
                "  }\n" +
                "  */\n" +
                "\n" +
                "  this.DrawEdges = function () {\n" +
                "    for( i=0; i < this.edge.length; i++ ) {\n" +
                "      var arc = this.edge[i].arc;\n" +
                "      var objclass = 'edgearc' + this.color;\n" +
                "      /*\n" +
                "      if( this.subject ) {\n" +
                "        var j = i + 1;\n" +
                "        if( j==this.order.length ) j = 0;\n" +
                "        var thissubject = this.subject[this.order[i]];\n" +
                "        var relasubject = this.subject[this.order[j]];\n" +
                "        objclass = thissubject.Role( relasubject.Name() );\n" +
                "      }\n" +
                "      */\n" +
                "      arc.CreateView( this.parent, objclass || 'edgearc' );\n" +
                "      arc.Draw();\n" +
                "    }\n" +
                "  }\n" +
                "  \n" +
                "  this.DrawVertices = function () {\n" +
                "    for( var i=0; i < this.order.length; i++ ) {\n" +
                "      var arc = this.vector[this.order[i]].arc;\n" +
                "      arc.SetArcOrientation();\n" +
                "      arc.CreateView( this.parent, 'vertexarc'+this.color );\n" +
                "      arc.Draw();\n" +
                "      /*\n" +
                "      var v_st = new Vector( e.p, e.arcEnd );\n" +
                "      var v_ed = new Vector( e.p, n.arcStart );\n" +
                "      var a = v_ed.angle - v_st.angle;\n" +
                "      if( a < 0 ) a += 360;\n" +
                "      pathD = \"M\"+e.arcEnd.X+\",\"+e.arcEnd.Y+\" \"; //    <path d=\"M40,20 A10,10 0 0,1 38,25\" style=\"fill:none;stroke:lightblue\" />\n" +
                "      pathD += \"A\"+ e.v2.arcRadius+\",\"+e.v2.arcRadius+\" \";\n" +
                "      pathD += \"0 \";\n" +
                "      pathD += (a < 180 ? 0 : 1); // small 0 or large 1 arc depending on the open angle\n" +
                "      pathD += \",1 \"; // clockwise\n" +
                "      pathD += n.arcStart.X+\",\"+n.arcStart.Y;\n" +
                "      cc = document.createElementNS( svgNS, 'path' );\n" +
                "      cc.setAttribute( 'class',  );\n" +
                "      cc.setAttribute( 'd', pathD );\n" +
                "      this.parent.appendChild( cc );\n" +
                "      */\n" +
                "    }\n" +
                "  } \n" +
                "\n" +
                "  this.dragging = {\n" +
                "    svgRoot: null,\n" +
                "    on: false,\n" +
                "    mouseOffset: null,\n" +
                "    newpos: new Point( pos.X, pos.Y )\n" +
                "  };\n" +
                "  \n" +
                "  this.DrawCenter = function (m) {\n" +
                "    if( m==1 ) {\n" +
                "      if( this.cc==null) {\n" +
                "        this.cc = document.createElementNS( svgNS, 'circle' );\n" +
                "        this.cc.setAttribute( 'class', 'center' );\n" +
                "        this.cc.setAttribute( 'cx', this.center.X );\n" +
                "        this.cc.setAttribute( 'cy', this.center.Y );\n" +
                "        this.cc.setAttribute( 'r', 20 /*this.arcRadius*/ );\n" +
                "        this.parent.appendChild(this.cc);\n" +
                "        var me = this\n" +
                "        this.cc.addEventListener( \"mousedown\", (function (evt) {\n" +
                "              var fixedSubjectDetermined = 0;\n" +
                "              me.subject.forEach(function(element) {\n" +
                "                if( element.selected) ++fixedSubjectDetermined;\n" +
                "              });\n" +
                "              if( fixedSubjectDetermined > 0 ) {\n" +
                "                me.dragging.on = true;\n" +
                "                var p = svg.createSVGPoint();\n" +
                "                p.x = evt.clientX;\n" +
                "                p.y = evt.clientY;\n" +
                "                var m = me.cc.getScreenCTM();\n" +
                "                p = p.matrixTransform(m.inverse());\n" +
                "                me.dragging.mouseOffset = new Point( p.x-me.center.X, p.y-me.center.Y );\n" +
                "              }\n" +
                "            }));\n" +
                "        this.cc.addEventListener( \"mouseup\", (function () {\n" +
                "            if( ! me.dragging.on ) return;\n" +
                "                me.dragging.on = false;\n" +
                "            }));\n" +
                "        this.cc.addEventListener( \"mousemove\", (function (evt) {\n" +
                "            if( ! me.dragging.on ) return;\n" +
                "                var p = svg.createSVGPoint();\n" +
                "                p.x = evt.clientX;\n" +
                "                p.y = evt.clientY;\n" +
                "                var m = me.cc.getScreenCTM();\n" +
                "                p = p.matrixTransform(m.inverse());\n" +
                "                me.dragging.newpos.X = p.x - me.dragging.mouseOffset.X;\n" +
                "                me.dragging.newpos.Y = p.y - me.dragging.mouseOffset.Y;\n" +
                "                var oldpos = new Point( me.center.X, me.center.Y );            \n" +
                "                me.cc.setAttribute(\"x\", me.dragging.newpos.X );\n" +
                "                me.cc.setAttribute(\"y\", me.dragging.newpos.Y );\n" +
                "                \n" +
                "                p.x = p.x\n" +
                "                // me.pos.X = me.dragging.newpos.X;\n" +
                "                //me.pos.Y = me.dragging.newpos.Y;\n" +
                "                /*\n" +
                "                if( ! me.center.Notify( me ) ) {\n" +
                "                  me.center.X = oldpos.X;\n" +
                "                  me.center.Y = oldpos.Y;\n" +
                "                  me.cc.setAttribute(\"x\", me.center.X );\n" +
                "                  me.cc.setAttribute(\"y\", me.center.Y );\n" +
                "                  //me.pos.Notify( me );\n" +
                "                  me.dragging.on = false;\n" +
                "                }\n" +
                "                */\n" +
                "            }));\n" +
                "\n" +
                "\n" +
                "      }\n" +
                "    } else {\n" +
                "      if( this.cc != null) {\n" +
                "        this.parent.removeChild(this.cc)\n" +
                "        this.cc = null\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  \n" +
                "  // Draw this polygon.\n" +
                "  // 1. Draw all vertices\n" +
                "  // 2. Draw all edges\n" +
                "  // 3. Draw the center of this polygon if in the mode of adjusting layout (mode=1)\n" +
                "  this.Draw = function ( g ) {\n" +
                "    this.parent = svg;\n" +
                "    if( g ) {\n" +
                "      this.parent = document.getElementById( g );\n" +
                "    }\n" +
                "    /* each subject should show only once, so it should not be shown here.\n" +
                "    if( this.subject ) {\n" +
                "      for (var i = 0; i < this.order.length; i++) {\n" +
                "        var s = this.subject[this.order[i]];\n" +
                "        var v = this.vector[this.order[i]];\n" +
                "        s.Draw( parent, v.p );\n" +
                "      }\n" +
                "    }\n" +
                "    */\n" +
                "    this.DrawVertices();\n" +
                "    this.DrawEdges();\n" +
                "    this.DrawCenter( this.mode );\n" +
                "  //    this.draw1( parent );\n" +
                "  }\n" +
                "\n" +
                "  // Drawing\n" +
                "  // Redraw the whole Polygon due to a move of vertex or the layout change (center move)\n" +
                "  // Before the redrawing, the positions of all vertices are set already.\n" +
                "  // 1. Recalculate the center of the polygon\n" +
                "  // 2. Based on the new center, recalculate the vector from the center to each vertex\n" +
                "  // 3. Based on the new vectors for all vertices, recalculate each edge\n" +
                "  // 4. Redraw the arcs for each vertex (represented by the vectors)\n" +
                "  // 5. Redraw the arcs for the eadges\n" +
                "  this.Redraw = function ( s ) {\n" +
                "    /*\n" +
                "    for (var i = 0; i < this.subject.length; i++) {\n" +
                "        if( s == this.subject[i] ) {\n" +
                "          alert( \"Position moved: \"+s.name );\n" +
                "        }\n" +
                "    } \n" +
                "    */   \n" +
                "    this.center = this.CalcCenter( this.center );\n" +
                "    //    this.draw1( parent );\n" +
                "    for (var i = 0; i < this.order.length; i++)\n" +
                "      this.vector[this.order[i]].VectorReCalc();\n" +
                "    //this.swapped = false;\n" +
                "    //this.SortByAngle();\n" +
                "    // order may have changed.\n" +
                "    if( this.AngleOrderChanged() ) {\n" +
                "      // order changed, edges changed. discard all edges and recreate them\n" +
                "    //      alert( \"Angle order changed, not allowed.\" );\n" +
                "      return false; // This version does not allow order to be changed.\n" +
                "    } else {\n" +
                "      // order has not changed, edges remain, but need to be recalculated.\n" +
                "      //      alert( \"Recalc edges\" );\n" +
                "      for (var i = 0; i < this.edge.length; i++)\n" +
                "        this.edge[i].EdgeReCalc();\n" +
                "      // now redraw vertex arcs\n" +
                "      for (i=0; i<this.vector.length; i++ ) {\n" +
                "        var arc = this.vector[i].arc; \n" +
                "        arc.SetArcOrientation();\n" +
                "        arc.Draw();\n" +
                "      }\n" +
                "      for (i=0; i<this.edge.length; i++ ) {\n" +
                "        arc = this.edge[i].arc;\n" +
                "        arc.SetArcOrientation();\n" +
                "        arc.Draw();\n" +
                "      }\n" +
                "    }\n" +
                "    return true;\n" +
                "  }\n" +
                "\n" +
                "  // Lastly, register event handler to redraw when a vertex moves.\n" +
                "  // The redraw is called in responding to only one vertex move when the move of a vertex notifies the listeners.  \n" +
                "  // Although the redraw is registered to all the vertices, it normally responds to only one vertex move as only one vertex can move at a time.\n" +
                "  for (var i = 0; i < this.order.length; i++) {\n" +
                "    var px = this.vector[this.order[i]].p;\n" +
                "    px.RegisterEventListener( this.Redraw, this );\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "var subjects = new Map();\n" +
                "\n" +
                "function Subject( name, pos, transform )\n" +
                "{\n" +
                "  this.person =  persons.get(name);\n" +
                "  this.name = name;\n" +
                "  this.pos = pos;\n" +
                "  this.selected = false;\n" +
                "  if( transform ) {\n" +
                "    this.transform = transform;\n" +
                "    this.pos.Add( this.transform );\n" +
                "  }\n" +
                "  this.dragging = {\n" +
                "    svgRoot: null,\n" +
                "    on: false,\n" +
                "    mouseOffset: null,\n" +
                "    newpos: new Point( pos.X, pos.Y )\n" +
                "  };\n" +
                "    \n" +
                "  this.Draw = function ( parent ) {\n" +
                "    this.dragging.svgRoot = parent;\n" +
                "    var label = document.createElementNS( svgNS, 'text' );\n" +
                "    label.setAttribute( \"class\", this.person.sex+this.person.live );\n" +
                "\t  label.setAttribute( \"x\", this.pos.X );\n" +
                "\t  label.setAttribute( \"y\", this.pos.Y );\n" +
                "\t  var tn = document.createTextNode(this.person ? this.person.name : '');\n" +
                "\t\tlabel.appendChild(tn);\n" +
                "    \n" +
                "    var me = this;\n" +
                "\t  parent.appendChild( label );\n" +
                "    this.label = label;\n" +
                "\n" +
                "    var bb = label.getBBox();\n" +
                "    var anchor = document.createElementNS( svgNS, 'rect' );\n" +
                "    anchor.setAttribute( \"class\", \"anchor\");\n" +
                "\t  anchor.setAttribute( \"x\", bb.x );\n" +
                "\t  anchor.setAttribute( \"y\", bb.y );\n" +
                "\t  anchor.setAttribute( \"width\", bb.width );\n" +
                "\t  anchor.setAttribute( \"height\", bb.height );\n" +
                "    //\t  anchor.setAttribute( \"style\", \"fill:yellow;opacity:0.5\" );\n" +
                "    //\t  parent.appendChild( anchor );\n" +
                "    /*\n" +
                "    var anchor = document.createElementNS( svgNS, 'circle' );\n" +
                "    anchor.setAttribute( \"class\", \"anchor\");\n" +
                "    anchor.setAttribute( \"cx\", this.pos.X );\n" +
                "    anchor.setAttribute( \"cy\", this.pos.Y );\n" +
                "    */\n" +
                "    this.anchor = anchor;\n" +
                "    /*\n" +
                "    anchor.addEventListener( \"mouseover\", (function () {\n" +
                "         if( ! me.dragging.on ) return;\n" +
                "            me.pos.X = me.dragging.newpos.X;\n" +
                "            me.pos.Y = me.dragging.newpos.Y;\n" +
                "            me.dragging.on = false;\n" +
                "         }));\n" +
                "    anchor.addEventListener( \"mouseout\", (function () {\n" +
                "         if( ! me.dragging.on ) return;\n" +
                "            me.pos.X = me.dragging.newpos.X;\n" +
                "            me.pos.Y = me.dragging.newpos.Y;\n" +
                "            me.dragging.on = false;\n" +
                "         }));\n" +
                "         */\n" +
                "    // http://www.codedread.com/dragtest.svg\n" +
                "    anchor.addEventListener( \"mousedown\", (function (evt) {\n" +
                "      if( opmode==1 ) {\n" +
                "        me.selected = !me.selected\n" +
                "        if( me.selected ) {\n" +
                "          me.anchor.setAttribute( \"style\", \"fill:yellow;opacity:0.5\" );\n" +
                "        } else {\n" +
                "          me.anchor.setAttribute( \"style\", \"\" );\n" +
                "        }\n" +
                "      } else {\n" +
                "            me.dragging.on = true;\n" +
                "            var p = svg.createSVGPoint();\n" +
                "            p.x = evt.clientX;\n" +
                "            p.y = evt.clientY;\n" +
                "            var m = me.anchor.getScreenCTM();\n" +
                "            p = p.matrixTransform(m.inverse());\n" +
                "            me.dragging.mouseOffset = new Point( p.x-me.pos.X, p.y-me.pos.Y );\n" +
                "      }\n" +
                "      }));\n" +
                "    anchor.addEventListener( \"mouseup\", (function () {\n" +
                "         if( ! me.dragging.on ) return;\n" +
                "            me.dragging.on = false;\n" +
                "            /*\n" +
                "            me.pos.X = me.dragging.newpos.X;\n" +
                "            me.pos.Y = me.dragging.newpos.Y;\n" +
                "            me.pos.Notify( me );\n" +
                "            */\n" +
                "         }));\n" +
                "    anchor.addEventListener( \"mousemove\", (function (evt) {\n" +
                "        if( ! me.dragging.on ) return;\n" +
                "        if( evt.which != 1) {\n" +
                "          // mouse is not in down position anymore\n" +
                "          me.dragging.on = false;\n" +
                "          return;\n" +
                "        }\n" +
                "        var p = svg.createSVGPoint();\n" +
                "        p.x = evt.clientX;\n" +
                "        p.y = evt.clientY;\n" +
                "        var m = me.anchor.getScreenCTM();\n" +
                "        p = p.matrixTransform(m.inverse());\n" +
                "        me.dragging.newpos.X = p.x - me.dragging.mouseOffset.X;\n" +
                "        me.dragging.newpos.Y = p.y - me.dragging.mouseOffset.Y;\n" +
                "        me.label.setAttribute(\"x\", me.dragging.newpos.X );\n" +
                "        me.label.setAttribute(\"y\", me.dragging.newpos.Y );\n" +
                "        bb = me.label.getBBox();\n" +
                "        me.anchor.setAttribute(\"x\", bb.x );\n" +
                "        me.anchor.setAttribute(\"y\", bb.y );\n" +
                "        \n" +
                "        var oldpos = new Point( me.pos.X, me.pos.Y );            \n" +
                "        me.pos.X = me.dragging.newpos.X;\n" +
                "        me.pos.Y = me.dragging.newpos.Y;\n" +
                "        if( ! me.pos.Notify( me ) ) {\n" +
                "          me.pos.X = oldpos.X;\n" +
                "          me.pos.Y = oldpos.Y;\n" +
                "          me.label.setAttribute(\"x\", me.pos.X );\n" +
                "          me.label.setAttribute(\"y\", me.pos.Y );\n" +
                "          bb = me.label.getBBox();\n" +
                "          me.anchor.setAttribute(\"x\", bb.x );\n" +
                "          me.anchor.setAttribute(\"y\", bb.y );\n" +
                "          me.pos.Notify( me );\n" +
                "          me.dragging.on = false;\n" +
                "        }\n" +
                "         }));\n" +
                "    /* This is not supported by SVG 2. It is proposed by SVG 3.\n" +
                "    anchor.addEventListener( \"drag\", (function (event) {\n" +
                "            var cX = event.clientX;     // Get the horizontal coordinate\n" +
                "            var cY = event.clientY;     // Get the vertical coordinate\n" +
                "            var coords1 = \"client - X: \" + cX + \", Y coords: \" + cY;\n" +
                "            var sX = event.screenX;\n" +
                "            var sY = event.screenY;\n" +
                "            var coords2 = \"screen - X: \" + sX + \", Y coords: \" + sY;\n" +
                "            alert( me.person.name + \" mouse drag @ \"+coords1+\" \"+coords2);\n" +
                "         }));\n" +
                "    */\n" +
                "\t  parent.appendChild( anchor );\n" +
                "  };\n" +
                "  this.Name = function () {\n" +
                "    return this.person ? this.person.name : '';\n" +
                "  }\n" +
                "  this.Role = function () {\n" +
                "      return undefined;\n" +
                "  }\n" +
                "  if( subjects.get(this.name) ) {\n" +
                "    alert( \"Duplicate subject name : \"+this.name );\n" +
                "  }\n" +
                "  subjects.set( this.name, this );\n" +
                "}\n" +
                "\n" +
                "function DisplayMembers( treeId )\n" +
                "{\n" +
                "  var tree = document.getElementById( treeId );\n" +
                "  subjects.forEach(function(s) {\n" +
                "    s.Draw(tree); \n" +
                "  }, this);\n" +
                "}\n" +
                "\n" +
                "var stars = new Map();\n" +
                "\n" +
                "function StarFamily( treeId, color )\n" +
                "{\n" +
                "  this.treeId = treeId; // first argument is the \n" +
                "  this.mode = 0;\n" +
                "  var subjectList = [];\n" +
                "  var posList = [];\n" +
                "  var name = ':';\n" +
                "  for (var i = 2; i < arguments.length; i++) {\n" +
                "      var p = subjects.get( arguments[i] );\n" +
                "      subjectList.push( p );\n" +
                "      name += arguments[i]+':';\n" +
                "      posList.push( p.pos );\n" +
                "  }\n" +
                "  this.polygon = new Polygon( color, posList );\n" +
                "  this.polygon.SetSubjects.apply( this.polygon, subjectList );\n" +
                "  this.polygon.SetAdjMode(this.mode);\n" +
                "  this.Draw = function () { this.polygon.Draw(this.treeId); }\n" +
                "  this.SetAdjMode = function(mode) {\n" +
                "    this.mode = mode;\n" +
                "    this.polygon.SetAdjMode( mode )\n" +
                "  }\n" +
                "  stars.set( name, this )\n" +
                "}\n" +
                "\n" +
                "function DisplayStarFamilies()\n" +
                "{\n" +
                "  //  var tree = document.getElementById( treeId );\n" +
                "  stars.forEach(function(s) {\n" +
                "    s.Draw(); \n" +
                "  }, this);\n" +
                "}\n" +
                "\n" +
                "function ReportPositions()\n" +
                "{\n" +
                "  //  alert( \"Reporting\" );\n" +
                "  if( document.getElementById( \"tree\" ).style.display == 'none') {\n" +
                "    document.getElementById( \"tree\" ).style.display = '';\n" +
                "    document.getElementById( \"reporting\" ).style.display = 'none';\n" +
                "    document.getElementById( 'button' ).innerHTML = 'Report adjusted positions';\n" +
                "  } else {\n" +
                "    document.getElementById( \"tree\" ).style.display = 'none';\n" +
                "    var text = '';\n" +
                "    var transformList = new Map();\n" +
                "    var tn = 1;\n" +
                "    subjects.forEach(function(s) {\n" +
                "      var p = new Point( s.pos, 'clone' );\n" +
                "      if( s.transform )\n" +
                "          p.Sub( s.transform );\n" +
                "      var t = \"new Subject('\"+s.name+\"', new Point( \"+Math.round(p.X*100)/100+', '+Math.round(p.Y*100)/100+' )';\n" +
                "      if( s.transform ) {\n" +
                "        var varname = transformList.get( s.transform );\n" +
                "        if( ! varname ) {\n" +
                "          varname = \"shift\"+(tn++);\n" +
                "          text += \"var \" +varname + \" = new Point(\"+Math.round(s.transform.X*100)/100+', '+Math.round(s.transform.Y*100)/100+' );\\n'\n" +
                "          transformList.set( s.transform, varname );\n" +
                "        }\n" +
                "        t += \", \" + varname;\n" +
                "      }\n" +
                "      t += ');\\n';\n" +
                "      text += t;\n" +
                "    }, this);\n" +
                "    document.getElementById( \"reporting\" ).innerHTML = text;\n" +
                "    document.getElementById( \"button\" ).innerHTML = 'Show Family Tree';\n" +
                "    document.getElementById( \"reporting\" ).style.display = '';\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "var opmode = 0 // normal\n" +
                "\n" +
                "function ToggleFamilyLayoutAdj()\n" +
                "{\n" +
                "  if( document.getElementById( 'button2' ).innerHTML == 'Adjust Family Layout') {\n" +
                "    document.getElementById( 'button2' ).innerHTML = 'Freeze Family Layout';\n" +
                "    opmode = 1\n" +
                "    stars.forEach(function(s) {\n" +
                "      s.SetAdjMode(opmode); \n" +
                "      s.Draw()\n" +
                "    }, this);\n" +
                "  } else {\n" +
                "    document.getElementById( 'button2' ).innerHTML = 'Adjust Family Layout';\n" +
                "    opmode = 0\n" +
                "    stars.forEach(function(s) {\n" +
                "      s.SetAdjMode(opmode); \n" +
                "      s.Draw()\n" +
                "    }, this);\n" +
                "  }\n" +
                "}    \n" +
                "</script>\n" +
                "\n" +
                "<!-- stonestory_unicode -->\n" +
                "<script charset=\"unicode\">\n" +
                "  \n" +
                "function SetPersons()\n" +
                "{\n" +
                "\tvar jz = new Person( '贾政', 'male');\n" +
                "\tvar wfr = new Person( '王夫人', 'female');\n" +
                "\tvar jby = new Person( '贾宝玉', 'male');\n" +
                "\tvar xym = new Person( '薛姨妈', 'female');\n" +
                "\tvar bcf = new Person( '宝钗父', 'male');\n" +
                "\tvar xbc = new Person( '薛宝钗', 'female');\n" +
                "\tvar jds = new Person( '贾代善', 'male');\n" +
                "\tvar jm = new Person( '贾母', 'female');\n" +
                "}\n" +
                "\n" +
                "function SetRelations()\n" +
                "{\n" +
                "\tSetRelation( '贾政', 'husband-wife', '王夫人' );\n" +
                "\tSetRelation( '贾政', 'father-child', '贾宝玉' );\t\n" +
                "\tSetRelation( '王夫人', 'mother-child', '贾宝玉' );\t\n" +
                "\tSetRelation( '薛姨妈', 'mother-child', '薛宝钗' );\n" +
                "\tSetRelation( '贾宝玉', 'husband-wife', '薛宝钗' );\n" +
                "\tSetRelation( '贾代善', 'husband-wife', '贾母' );\n" +
                "\tSetRelation( '贾代善', 'father-child', '贾政' );\t\n" +
                "\tSetRelation( '贾母', 'mother-child', '贾政' );\n" +
                "}\n" +
                "\n" +
                "SetPersons();\n" +
                "SetRelations();\n" +
                "\n" +
                "var family1 = new Family( persons.get('贾政'), persons.get('王夫人') );\n" +
                "var family2 = new Family( undefined, persons.get('薛姨妈') );\n" +
                "var family3 = new Family( persons.get('贾宝玉'), persons.get('薛宝钗') );\n" +
                "var family4 = new Family( persons.get('贾代善'), persons.get('贾母') );\n" +
                "\n" +
                "var jz = '贾政';\n" +
                "\n" +
                "new Subject('贾政', new Point( 30,100 ));\n" +
                "new Subject('王夫人', new Point( 70,100 ));\n" +
                "new Subject('贾宝玉', new Point( 30,160 ));\n" +
                "new Subject('宝钗父', new Point( 100,140 ));\n" +
                "new Subject('薛姨妈', new Point( 191.16,122.95 ));\n" +
                "new Subject('薛宝钗', new Point( 156.85,207.24 ));\n" +
                "new Subject('贾代善', new Point( 30,20 ));\n" +
                "new Subject('贾母', new Point( 143.9,18.97 ));\n" +
                "\n" +
                "var jzwfr = new StarFamily( 'tree', 2, '贾政', '王夫人', '贾宝玉' );\n" +
                "var xymfamily = new StarFamily( 'tree', 3, '宝钗父', '薛姨妈', '薛宝钗' );\n" +
                "var bybc = new StarFamily( 'tree', 4, '贾宝玉', '薛宝钗' );\n" +
                "var jdsjm = new StarFamily( 'tree', 5, '贾代善', '贾母', '贾政' );\n" +
                "\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "<p>\n" +
                "Family Tree of <b id=\"viewpoint\"></b>\n" +
                "</p>\n" +
                "\n" +
                "<button id='button' type=\"button\" onclick=\"ReportPositions()\">Report adjusted positions</button>\n" +
                "<button id='button2' type=\"button\" onclick=\"ToggleFamilyLayoutAdj()\">Adjust Family Layout</button>\n" +
                "\n" +
                "<textarea id=reporting rows=\"20\"\" cols=\"50\" style=\"display:none\">\n" +
                "</textarea>\n" +
                "\n" +
                "\n" +
                "<svg id=myroot width=\"100%\" height=\"100%\" version=\"1.2\" baseProfile=\"tiny\"\n" +
                "  viewBox=\"0 0 1000 1000\"\n" +
                "  xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "  <!--\n" +
                "  <g transform=\"translate(150, 0)\">\n" +
                "    <circle class=center cx=80 cy=60 /><circle cx=80 cy=60 r=30 style=\"fill:none;stroke-width:1;stroke:red\" /> \n" +
                "    <circle class=center cx=150 cy=80 /><circle cx=150 cy=80 r=40 style=\"fill:none;stroke-width:1;stroke:red\" /> \n" +
                "   \n" +
                "    <circle cx=80 cy=60 r=50 style=\"fill:none;stroke-width:1;stroke:yellow\" /> \n" +
                "    <circle cx=150 cy=80 r=60 style=\"fill:none;stroke-width:1;stroke:yellow\" /> \n" +
                "  \n" +
                "    <circle class=center cx=118.95 cy=28.65 /><circle cx=118.9 cy=28.65 r=20 style=\"fill:none;stroke-width:1;stroke:lightblue\" />\n" +
                "  </g> \n" +
                "-->\n" +
                "  <g id='tree' transform=\"translate(40, 20)\">\n" +
                "  </g>\n" +
                "\n" +
                "</svg>\n" +
                "\n" +
                "<script>\n" +
                "var svgNS = \"http://www.w3.org/2000/svg\";\n" +
                "var svg = document.getElementById( 'myroot' );\n" +
                "\n" +
                "DisplayMembers( 'tree' );\n" +
                "DisplayStarFamilies();\n" +
                "\n" +
                "document.getElementById(\"viewpoint\").innerHTML = jz;\n" +
                "\n" +
                "</script>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";
        return html;
    }
}
