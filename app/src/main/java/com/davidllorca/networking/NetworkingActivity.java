package com.davidllorca.networking;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class NetworkingActivity extends Activity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networking);

        imageView = (ImageView) findViewById(R.id.image);

        new DownloadImageTask().execute(
                "http://www.mundodeportivo.com/img/888cb276-5ad0-11e5-9d07-fd52c238baac/lowres/bundesligafuaballkingsley-coman.jpg",
                "http://www.mundodeportivo.com/r/GODO/MD/p3/Futbol/Imagenes/2015/09/11/Recortada/MD_20150126_FOTOS_D_54425135462-kFQF--572x385@MundoDeportivo-Web.jpg",
                "http://www.mundodeportivo.com/r/GODO/MD/p3/Futbol/Imagenes/2015/09/11/Recortada/img_cordula_20150911-103846_imagenes_md_otras_fuentes_vangiii-kFQF--572x222@MundoDeportivo-Web.JPG",
                "http://www.mundodeportivo.com/rsc/images/ico/apple-touch-icon-120x120.png");

        //new DownloadTextTask().execute("https://theysaidso.p.mashape.com/authors");
        //new DownloadTextTask()
        //.execute("http://iheartquotes.com/api/v1/random?max_characters=256&max_lines=10");

        //---access a Web Service using GET---
        new AccessWebServiceTask().execute("apple");
    }

    private InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            if (!(conn instanceof HttpURLConnection)) {
                throw new IOException("Not an HTTP connection");
            }

            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    private Bitmap downloadImage(String URL) {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e) {
            Log.d("NetworkingActivity", e.getLocalizedMessage());
        }
        return bitmap;
    }

    private String downloadText(String URL) {
        int BUFFER_SIZE = 2000;
        InputStream in = null;
        String str = "";
        try {
            in = OpenHttpConnection(URL);

            InputStreamReader isr = new InputStreamReader(in);
            int charRead;
            char[] inputBuffer = new char[BUFFER_SIZE];
            while ((charRead = isr.read(inputBuffer)) > 0) {
                // Convert the chars to a String
                String readString =
                        String.copyValueOf(inputBuffer, 0, charRead);
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
        } catch (IOException e) {
            Log.d("NetworkingActivity", e.getLocalizedMessage());
            return "";
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    /**
     * Download a image.
     */
    private class DownloadImageTask extends AsyncTask<String, Bitmap, Long> {

        // Get a list of url images
        @Override
        protected Long doInBackground(String... params) {
            long imagesCount = 0;
            for (int i = 0; i < params.length; i++) {
                // Download image
                Bitmap imageDownloaded = downloadImage(params[i]);
                if (imageDownloaded != null) {
                    imagesCount++;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // return image downloaded
                    publishProgress(imageDownloaded);
                }
            }
            return imagesCount;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            imageView.setImageBitmap(values[0]);
        }

        protected void onPostExecute(Long imagesDownloaded) {
            Toast.makeText(getBaseContext(), "Total " + imagesDownloaded + " imagesDownloaded", Toast.LENGTH_LONG).show();
        }
    }

    private class DownloadTextTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return downloadText(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
        }
    }

    private String wordDefinition(String word) {
        InputStream in = null;
        String strDefinition = "";
        try {
            in = OpenHttpConnection(
                    "http://services.aonaware.com/DictService/DictService.asmx/Define?word=" + word);
            Document doc = null;
            DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            try {
                db = dbf.newDocumentBuilder();
                doc = db.parse(in);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            doc.getDocumentElement().normalize();

            // Retrieve all the <Definition> elements
            NodeList definitionElements =
                    doc.getElementsByTagName("Definition");

            // Iterate through each <Definition> elements
            for (int i = 0; i < definitionElements.getLength(); i++) {
                Node itemNode = definitionElements.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    // Convert the Definition node into an Element
                    Element definitionElement = (Element) itemNode;

                    // Get all the <WordDefinition> elements underthe <Definition> element
                    NodeList wordDefinitionElements =
                            (definitionElement).getElementsByTagName(
                                    "WordDefinition");

                    strDefinition = "";
                    // Iterate through each <WordDefinition> elements
                    for (int j = 0; j < wordDefinitionElements.getLength(); j++) {
                        // Convert a <WordDefinition> node into an Element
                        Element wordDefinitionElement =
                                (Element) wordDefinitionElements.item(j);

                        // Get all the child nodes under the <WordDefinition> element
                        NodeList textNodes =
                                ((Node) wordDefinitionElement).getChildNodes();
                        strDefinition +=
                                ((Node) textNodes.item(0)).getNodeValue() + ". \n";
                    }
                }
            }
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }
        //---return the definitions of the word---
        return strDefinition;
    }

    private class AccessWebServiceTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return wordDefinition(urls[0]);
        }

        protected void onPostExecute(String result) {
            Log.d("NetworkingActivity", "result: " + result);
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
        }
    }


}