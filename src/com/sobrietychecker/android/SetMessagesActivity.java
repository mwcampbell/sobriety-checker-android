package com.sobrietychecker.android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.sobrietychecker.Status;

public final class SetMessagesActivity extends Activity {
    private final Handler mHandler = new Handler();
    private MessageStore mMessageStore = null;

    private void setupEditText(final int status, EditText editText) {
        String message = mMessageStore.getMessage(status);
        if (message != null) {
            editText.setText(message);
        }
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            public void afterTextChanged(Editable s) {
                mMessageStore.setMessage(status, s.toString());
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageStore = MessageStore.getInstance(this);
        setContentView(R.layout.set_messages);
        EditText helpNowMessageEdit = (EditText)findViewById(R.id.helpNowMessageEdit);
        setupEditText(Status.HELP_NOW, helpNowMessageEdit);
        EditText slippingMessageEdit = (EditText)findViewById(R.id.slippingMessageEdit);
        setupEditText(Status.SLIPPING, slippingMessageEdit);
        EditText goodMessageEdit = (EditText)findViewById(R.id.goodMessageEdit);
        setupEditText(Status.GOOD, goodMessageEdit);
        helpNowMessageEdit.requestFocus();
        showSoftInput(helpNowMessageEdit);
    }

    private void showSoftInput(final EditText editText) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                } 
            }
        }, 100);         
    }
}
