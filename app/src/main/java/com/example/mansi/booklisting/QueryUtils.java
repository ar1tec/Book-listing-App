package com.example.mansi.booklisting;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class QueryUtils {
    private String LOG_TAG = "check";
    private String json = "";

    public ArrayList<Books> fetchBookData(String query) {
        URL url = createURL(query);
        if (url != null) {
            //make http request if url is not blank
            makeHttpRequest(url);
        }
        ArrayList<Books> booksArrayList = parseJsonResponse(json);
        return booksArrayList;
    }

    /**
     * Create the URL from passed string and return it
     */
    public URL createURL(String url1) {
        URL url = null;
        try {
            url = new URL(url1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public void makeHttpRequest(URL url) {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                //response code is successful
                inputStream = urlConnection.getInputStream();
                json = readFromStream(inputStream);
            } else {
                //Otherwise report the error response code
                Log.e(LOG_TAG, "Error response code " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
                //disconnect URL connection after our task is done and if it is not null
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                    //close input stream after our task is done and if it is not null to free system memory
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Convert the text read from inputstream to proper String object using InputStreamReader and BufferedReader
     */
    public String readFromStream(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        String temp = "";
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                temp = bufferedReader.readLine();
                while (temp != null) {
                    output.append(temp);
                    temp = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (output.toString());
    }

    public ArrayList<Books> parseJsonResponse(String json) {
        ArrayList<Books> booksList = new ArrayList<>();
        if (json != "" || !json.isEmpty())
            try {
                JSONObject root = new JSONObject(json);
                if (root.getInt("totalItems") == 0) {
                    return null;
                }
                JSONArray itemsArray = root.getJSONArray("items");
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemsObject = itemsArray.getJSONObject(i);
                    JSONObject volumeInfoObject = itemsObject.getJSONObject("volumeInfo");

                    String title = "";
                    if (volumeInfoObject.has("title")) {
                        title = volumeInfoObject.getString("title");
                    }

                    String authors = "";
                    if (volumeInfoObject.has("authors")) {
                        JSONArray authorsArray = volumeInfoObject.getJSONArray("authors");
                        for (int j = 0; j < authorsArray.length(); j++) {
                            authors = authors + authorsArray.getString(j) + "\n";
                        }
                    }
                    String previewLink = "";
                    if (volumeInfoObject.has("previewLink")) {
                        previewLink = volumeInfoObject.getString("previewLink");
                    }

                    JSONObject imageLinks;
                    String thumbnail = "";
                    if (volumeInfoObject.has("imageLinks")) {

                        imageLinks = volumeInfoObject.getJSONObject("imageLinks");
                        if (imageLinks.has("smallThumbnail")) {
                            thumbnail = imageLinks.getString("smallThumbnail");
                        }
                    }
                    booksList.add(new Books(title, authors, thumbnail, previewLink));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return booksList;
    }
}
