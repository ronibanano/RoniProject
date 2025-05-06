package com.example.roniproject.Frag;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.roniproject.Activities.SearchResultsActivity;
import com.example.roniproject.R;

public class SearchFragment extends Fragment {

    private EditText etBookName, etAuthor, etGenre, etLocation;
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

        etBookName = view.findViewById(R.id.etBookName);
        etAuthor = view.findViewById(R.id.etAuthor);
        etGenre = view.findViewById(R.id.etGenre);
        etLocation = view.findViewById(R.id.etLocation);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
    }

    private void performSearch() {
        String bookName = etBookName.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
        intent.putExtra("bookName", bookName);
        intent.putExtra("author", author);
        intent.putExtra("genre", genre);
        intent.putExtra("location", location);
        startActivity(intent);
    }
}
