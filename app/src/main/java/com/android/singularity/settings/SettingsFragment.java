package com.android.singularity.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.singularity.R;
import com.android.singularity.util.Constants;
import com.android.singularity.util.Credentials;
import com.andromeda.calloutmanager.Session;

public class SettingsFragment extends Fragment {

    View view;
    EditText UsernameET, PasswordET, ClientIdET, ClientKeyET, SecurityTokenET;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        UsernameET = view.findViewById(R.id.username_et);
        PasswordET = view.findViewById(R.id.password_et);
        ClientIdET = view.findViewById(R.id.client_id_et);
        ClientKeyET = view.findViewById(R.id.client_key_et);
        SecurityTokenET = view.findViewById(R.id.security_token_et);
        assert getActivity() != null;
        Context context = getActivity().getApplicationContext();
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        loadCredentials();
        view.findViewById(R.id.save_btn).setOnClickListener(view -> {
            saveCredentials();
        });
        return view;
    }

    private void saveCredentials() {
        sharedPref.edit()
                .putString("CLIENT_ID", ClientIdET.getText().toString())
                .putString("CLIENT_SECRET", ClientKeyET.getText().toString())
                .putString("PASSWORD", PasswordET.getText().toString())
                .putString("SECURITY_TOKEN", SecurityTokenET.getText().toString())
                .putString("USERNAME", UsernameET.getText().toString())
                .commit(); //immediate write
        Toast.makeText(getActivity(), "Creds Saved", Toast.LENGTH_SHORT).show();
        loadCredentials();
        Session.storeAccessToken(Constants.getAccessTokenEndpoint());
    }

    SharedPreferences sharedPref;

    private void loadCredentials() {
        Credentials.CLIENT_ID =  sharedPref.getString("CLIENT_ID", null);
        Credentials.CLIENT_SECRET =  sharedPref.getString("CLIENT_SECRET", null);
        Credentials.PASSWORD =  sharedPref.getString("PASSWORD", null);
        Credentials.SECURITY_TOKEN =  sharedPref.getString("SECURITY_TOKEN", null);
        Credentials.USERNAME =  sharedPref.getString("USERNAME", null);
        ClientIdET.setText(Credentials.CLIENT_ID);
        ClientKeyET.setText(Credentials.CLIENT_SECRET);
        PasswordET.setText(Credentials.PASSWORD);
        SecurityTokenET.setText(Credentials.SECURITY_TOKEN);
        UsernameET.setText(Credentials.USERNAME);
    }
}