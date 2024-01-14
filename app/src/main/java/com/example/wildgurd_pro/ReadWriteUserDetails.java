package com.example.wildgurd_pro;

import com.google.android.material.textfield.TextInputEditText;

public class ReadWriteUserDetails {

    public String telNo, email, password, confirmPassword;

    public ReadWriteUserDetails(String editTextTelephoneNo, String editTextEmail, String editTextPassword, String email, String password, String confirmPassword) {
        this.telNo = editTextTelephoneNo;
        this.email = editTextEmail;
        this.password = editTextPassword;
        this.password = editTextPassword;
    }

    public ReadWriteUserDetails(TextInputEditText editTextTelephoneNo, TextInputEditText editTextEmail, TextInputEditText editTextPassword) {
    }
}
