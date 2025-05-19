package com.example.roniproject.Frag;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roniproject.Activities.SearchResultsActivity;
import com.example.roniproject.R;

public class SearchFragment extends Fragment implements AdapterView.OnItemSelectedListener  {

    private Spinner spinner;

    private EditText etSearch;
    private Button btnSearch;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinner=view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adpSpin = ArrayAdapter.createFromResource(requireContext(), R.array.choice_array, android.R.layout.simple_spinner_item);
        adpSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adpSpin);

        spinner.setOnItemSelectedListener(this);

        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);

//        // יוצרים אדפטר עם Override לפונקציות למנוע בחירה בפריט הראשון ולהציג אותו באפור
//        ArrayAdapter<String> adpSpin = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, options) {
//            @Override
//            public boolean isEnabled(int position) {
//                // הפריט הראשון לא ניתן לבחירה
//                return position != 0;
//            }
//
//            @Override
//            public View getDropDownView(int position, View convertView, ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView tv = (TextView) view;
//                if (position == 0) {
//                    // טקסט אפור לפריט הראשון
//                    tv.setTextColor(Color.GRAY);
//                } else {
//                    tv.setTextColor(Color.BLACK);
//                }
//                return view;
//            }
//        };

//        adpSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adpSpin);
//        spinner.setSelection(0);  // מציג את הפריט הראשון כברירת מחדל


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
    }


    private void performSearch() {
        String searchUser = etSearch.getText().toString().trim();//מה שהמשתמש כתב
        if (searchUser.isEmpty()) {
            etSearch.setError("Please enter a search term");
            return;
        }
        String selectedOption = spinner.getSelectedItem().toString();//הפריט שנבחר
        if (selectedOption.isEmpty()) {
            Toast.makeText(requireContext(), "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("SearchDebug", "selectedOption: " + selectedOption);
        Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
        intent.putExtra("searchUser", searchUser);
        intent.putExtra("selectedOption", selectedOption);
        startActivity(intent);
//        String bookName = etBookName.getText().toString().trim();
//        String author = etAuthor.getText().toString().trim();
//        String genre = etGenre.getText().toString().trim();
//        String location = etLocation.getText().toString().trim();
//
//        Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
//        intent.putExtra("bookName", bookName);
//        intent.putExtra("author", author);
//        intent.putExtra("genre", genre);
//        intent.putExtra("location", location);
//        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Toast toast = Toast.makeText(requireContext(), "You selected " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
