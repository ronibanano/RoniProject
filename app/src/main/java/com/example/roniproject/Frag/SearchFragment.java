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

/**
 * Fragment that provides a user interface for searching items based on a selected category.
 * <p>
 * This fragment allows users to input a search term into an {@link EditText} field
 * and select a search category from a {@link Spinner}. Upon clicking the search button,
 * it validates the input and launches the {@link com.example.roniproject.Activities.SearchResultsActivity}
 * with the search term and selected category passed as Intent extras.
 * </p>
 * <p>
 * The Spinner is populated with search options defined in an array resource ({@code R.array.choice_array}).
 * The fragment implements {@link AdapterView.OnItemSelectedListener} to react to selections
 * made in the Spinner, primarily to display a Toast message indicating the selected item, though
 * the actual selected value for search is retrieved directly when the search button is clicked.
 * </p>
 *
 * @see com.example.roniproject.Activities.SearchResultsActivity
 * @see android.widget.Spinner
 * @see android.widget.EditText
 * @see android.widget.Button
 */
public class SearchFragment extends Fragment implements AdapterView.OnItemSelectedListener  {

    private Spinner spinner;
    private EditText etSearch;
    private Button btnSearch;

    /**
     * Default constructor.
     * <p>
     * Required empty public constructor for Fragment instantiation.
     * </p>
     */
    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * <p>
     * This method inflates the layout for the fragment's UI ({@code R.layout.fragment_search}).
     * </p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * <p>
     * This method initializes the UI components: the {@link Spinner} for search categories,
     * the {@link EditText} for the search term, and the {@link Button} to trigger the search.
     * An {@link ArrayAdapter} is created and set for the Spinner using items from
     * {@code R.array.choice_array}. The fragment itself is set as the
     * {@link AdapterView.OnItemSelectedListener} for the Spinner.
     * A click listener is set on the search button to call {@link #performSearch()}.
     * </p>
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinner = view.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adpSpin = ArrayAdapter.createFromResource(requireContext(),
                R.array.choice_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adpSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adpSpin);

        spinner.setOnItemSelectedListener(this);

        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the search button has been clicked.
             * <p>Invokes the method.</p>
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
    }

    /**
     * Validates the search input and starts the {@link SearchResultsActivity}.
     * <p>
     * Retrieves the search term from {@code etSearch} and the selected option from {@code spinner}.
     * If the search term is empty, an error is set on the {@code etSearch} field.
     * If no option is selected in the spinner (though by default an item is usually selected,
     * this checks if the retrieved string is empty, which is unlikely for a standard Spinner setup),
     * a Toast message is shown.
     * If both inputs are valid, an {@link Intent} is created for
     * {@link com.example.roniproject.Activities.SearchResultsActivity}, and the search term
     * (as "searchUser") and selected option (as "selectedOption") are passed as extras.
     * The {@link com.example.roniproject.Activities.SearchResultsActivity} is then started.
     * </p>
     */
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

    }

    /**
     * Callback method to be invoked when an item in this view has been selected.
     * <p>
     * This listener is invoked whenever an item in the {@link Spinner} is selected.
     * It displays a {@link Toast} message confirming the selection.
     * The actual selected value for initiating a search is retrieved in {@link #performSearch()}.
     * </p>
     * @param parent The AdapterView where the selection happened.
     * @param view The view within the AdapterView that was clicked.
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was selected.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Display a toast with the selected item.
        // This is for immediate user feedback on selection;
        // the actual value is retrieved in performSearch().
        Toast.makeText(requireContext(), "You selected: " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback method to be invoked when the selection disappears from this view.
     * <p>
     * This method is part of the {@link AdapterView.OnItemSelectedListener} interface.
     * It is called when the previously selected item is no longer available.
     * Currently, no specific action is taken in this implementation.
     * </p>
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Interface callback method. No action needed here for this fragment's current functionality.
    }
}
