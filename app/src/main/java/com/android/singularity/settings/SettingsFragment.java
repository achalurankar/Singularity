package com.android.singularity.settings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.singularity.R;

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
        loadCredentials();
        view.findViewById(R.id.save_btn).setOnClickListener(view -> {
            saveCredentials();
        });
        return view;
    }

    private void saveCredentials() {
    }

    private void loadCredentials() {
    }
}