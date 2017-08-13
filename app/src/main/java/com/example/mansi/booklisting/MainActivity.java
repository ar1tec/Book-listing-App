package com.example.mansi.booklisting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

   private String preQuery = "https://www.googleapis.com/books/v1/volumes?q=";
    private String midQuery = "&orderBy=relevance&startIndex=";
    private String postQuery = "&printType=books";
   private ArrayList<Books> booksArrayList = new ArrayList<Books>();
    private ArrayList<Books> booksArrayList1 = new ArrayList<Books>();
   private String userEnteredText = "";
    private ImageView previous_button;
    private ImageView next_button;
    private int mStartIndex = 0;
    private ProgressBar progressBar;
    //original visibility of these buttons are invisible
    private int next_button_visibility = View.INVISIBLE;
    private int previous_button_visibility = View.INVISIBLE;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //saving array list to continue showing showing that array list on rotation
        outState.putParcelableArrayList("arraylist", booksArrayList);
        outState.putInt("next_button_visibility", next_button.getVisibility());
        outState.putInt("previous_button_visibility", previous_button.getVisibility());
        outState.putInt("startIndex", mStartIndex);
        outState.putString("userText", userEnteredText);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button search_button = (Button) findViewById(R.id.search_button);
        previous_button = (ImageView) findViewById(R.id.previous_button);
        next_button = (ImageView) findViewById(R.id.next_button);

        /**to hide focus from edittext everytime the activity is created
         and disabling the keypad to pop up because of that on rotation*/
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (savedInstanceState != null && savedInstanceState.containsKey("arraylist")) {
            /**retrieving the previously saved array list and image buttons visibility
             /and updatating the UI with that list*/
            booksArrayList = savedInstanceState.getParcelableArrayList("arraylist");
            next_button_visibility = savedInstanceState.getInt("next_button_visibility");
            previous_button_visibility = savedInstanceState.getInt("previous_button_visibility");
            userEnteredText = savedInstanceState.getString("userText");
            mStartIndex = savedInstanceState.getInt("startIndex");
            updateUI();
        }

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hiding the keypad after user enters search button
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                //read text from edittext
                EditText readText = (EditText) findViewById(R.id.search_viewer);
                userEnteredText = readText.getText().toString().trim();

                //setting startindex again to 0 for every new search
                mStartIndex = 0;
                //setting previous button to be invisible if it was set visible by previous search
                previous_button.setVisibility(View.INVISIBLE);

                //check for internet connection
                if (!isInternetConnected()) { //if internet is not connected-display message noconnection to user
                    TextView textView = (TextView) findViewById(R.id.failure_message);
                    textView.setText(R.string.noconnection);
                    textView.setVisibility(View.VISIBLE);
                    return;
                }

                //if the user doesn't enter any text and tries tpo click search button
                if (userEnteredText == null || userEnteredText.isEmpty() || userEnteredText == "") {
                    TextView textView = (TextView) findViewById(R.id.failure_message);
                    textView.setText(R.string.blanksearch);
                    textView.setVisibility(View.VISIBLE);
                    return;
                }

                String query = formQuery(userEnteredText, mStartIndex);
                BookAsyncTask bookAsyncTask = new BookAsyncTask();
                bookAsyncTask.execute(query);
            }
        });

        previous_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check for internet connection
                if (!isInternetConnected()) { //if internet is not connected-display message noconnection to user
                    TextView textView = (TextView) findViewById(R.id.failure_message);
                    textView.setText(R.string.noconnection);
                    textView.setVisibility(View.VISIBLE);
                    return;
                }
                //decrement startIndex by 10
                mStartIndex = mStartIndex - 10;
                if (mStartIndex <= 0) { // if startIndex =0 then previous icon is hidden
                    previous_button.setVisibility(View.INVISIBLE);
                }
                String query = formQuery(userEnteredText, mStartIndex);
                BookAsyncTask bookAsyncTask = new BookAsyncTask();
                bookAsyncTask.execute(query);
            }
        });

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check for internet connection
                if (!isInternetConnected()) { //if internet is not connected-display message noconnection to user
                    TextView textView = (TextView) findViewById(R.id.failure_message);
                    textView.setText(R.string.noconnection);
                    textView.setVisibility(View.VISIBLE);
                    return;
                }
                //increment startIndex by 10
                mStartIndex = mStartIndex + 10;
                String query = formQuery(userEnteredText, mStartIndex);
                BookAsyncTask bookAsyncTask = new BookAsyncTask();
                bookAsyncTask.execute(query);
                //previous button is made visible after next is clicked
                previous_button.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Check for internet connection on device and set
     * progress spinner visible if query or userEnteredText is valid
     */
    public boolean isInternetConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    /**
     * Form URL query for Google API website
     */
    public String formQuery(String userEnteredText, int startIndex) {
        if (userEnteredText.contains(" ")) {
            //replace  space with + operator to form URL query only if oit conatins space
            userEnteredText = userEnteredText.replace(' ', '+');
        }
        String queryURL = preQuery + userEnteredText + midQuery + startIndex + postQuery;
        return queryURL;
    }

    /**
     * Update the listview with contents of custom adapter-BookAdapter and
     * if a book is clicked open the associated URL in web browser
     */
    public void updateUI() {
        next_button.setVisibility(next_button_visibility);
        previous_button.setVisibility(previous_button_visibility);
        ListView listView = (ListView) findViewById(R.id.listview);
        final BooksAdapter adapter = new BooksAdapter(getBaseContext(), booksArrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Books clickedBook = adapter.getItem(position);
                //get particular book which was clicked by user
                if (clickedBook.getmLink() != null || clickedBook.getmLink() != "") {
                    //if URL or link is not blank then open it in web browser
                    Uri uri = Uri.parse(clickedBook.getmLink());
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(webIntent);
                }
            }
        });
    }

    public class BookAsyncTask extends AsyncTask<String, Void, ArrayList<Books>> {

        @Override
        protected void onPreExecute() {
            if (userEnteredText != "" && userEnteredText != null && !userEnteredText.isEmpty()) {
                //is userEnteredText is not blank set loading spinner to be visible
                progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected ArrayList<Books> doInBackground(String... strings) {
            if (strings.length >= 1 && !strings[0].isEmpty()) {
                QueryUtils utils = new QueryUtils();
                booksArrayList1 = utils.fetchBookData(strings[0]);
                return booksArrayList1;
            } else
                return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Books> booksArrayList2) {
            TextView textView = (TextView) findViewById(R.id.failure_message);
            progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
            progressBar.setVisibility(View.GONE);
            if (booksArrayList2 == null) {
                //Google API has no books for the qiven query
                textView.setText(R.string.nobooks);
                textView.setVisibility(View.VISIBLE);
            } else {
                booksArrayList.clear();
                textView.setVisibility(View.GONE);
                booksArrayList.addAll(booksArrayList2);
                next_button.setVisibility(View.VISIBLE);
            }
            //store visibility of both buttons to set back same visibility on rotation
            next_button_visibility = next_button.getVisibility();
            previous_button_visibility = previous_button.getVisibility();
            updateUI();
        }
    }
}
