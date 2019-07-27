package com.example.cs160_sp18.prog3;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class Login extends AppCompatActivity {

    // UI elements
    RelativeLayout layout;
    EditText usernameInput;
    Button loginButton;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // hook up UI elements
        layout = (RelativeLayout) findViewById(R.id.loginLayout);
        usernameInput = (EditText) layout.findViewById(R.id.usernameInput);
        loginButton = (Button) layout.findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, LandmarkListView.class);
                intent.putExtra("username", usernameInput.getText().toString());
                username = usernameInput.getText().toString();
                startActivity(intent);
            }
        });
    }
}
