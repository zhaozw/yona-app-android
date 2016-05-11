/*
 *  Copyright (c) 2016 Stichting Yona Foundation
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *
 */

package nu.yona.app.ui.frinends;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import nu.yona.app.R;
import nu.yona.app.YonaApplication;
import nu.yona.app.api.manager.APIManager;
import nu.yona.app.api.model.ErrorMessage;
import nu.yona.app.api.model.RegisterUser;
import nu.yona.app.customview.CustomAlertDialog;
import nu.yona.app.customview.YonaFontButton;
import nu.yona.app.customview.YonaFontEditTextView;
import nu.yona.app.customview.YonaPhoneWatcher;
import nu.yona.app.listener.DataLoadListener;
import nu.yona.app.state.EventChangeListener;
import nu.yona.app.state.EventChangeManager;
import nu.yona.app.ui.BaseFragment;
import nu.yona.app.ui.YonaActivity;
import nu.yona.app.utils.AppUtils;

/**
 * Created by kinnarvasa on 27/04/16.
 */
public class AddFriendManually extends BaseFragment implements EventChangeListener {
    private YonaFontEditTextView firstName, lastName, email, mobileNumber;
    private TextInputLayout firstNameLayout, lastNameLayout, emailLayout, mobileNumberLayout;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            hideErrorMessage();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private YonaFontButton addFriendButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_friend_manually_fragment, null);

        getView(view);
        addButtonListener();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        YonaApplication.getEventChangeManager().registerListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        YonaApplication.getEventChangeManager().unRegisterListener(this);
    }

    private void getView(View view) {
        firstName = (YonaFontEditTextView) view.findViewById(R.id.first_name);
        lastName = (YonaFontEditTextView) view.findViewById(R.id.last_name);
        email = (YonaFontEditTextView) view.findViewById(R.id.email);
        mobileNumber = (YonaFontEditTextView) view.findViewById(R.id.mobile_number);

        firstName.addTextChangedListener(textWatcher);
        lastName.addTextChangedListener(textWatcher);
        email.addTextChangedListener(textWatcher);

        firstNameLayout = (TextInputLayout) view.findViewById(R.id.first_name_layout);
        lastNameLayout = (TextInputLayout) view.findViewById(R.id.last_name_layout);
        emailLayout = (TextInputLayout) view.findViewById(R.id.email_layout);
        mobileNumberLayout = (TextInputLayout) view.findViewById(R.id.mobile_number_layout);

        addFriendButton = (YonaFontButton) view.findViewById(R.id.addFriendButton);

        firstName.setFilters(new InputFilter[]{AppUtils.getFilter()});
        lastName.setFilters(new InputFilter[]{AppUtils.getFilter()});

        mobileNumber.setNotEditableLength(getString(R.string.country_code_with_zero).length());
        mobileNumber.addTextChangedListener(new YonaPhoneWatcher(mobileNumber, getString(R.string.country_code_with_zero), getActivity(), mobileNumberLayout));

        mobileNumber.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    goToNext();
                }
                return false;
            }
        });

        mobileNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && TextUtils.isEmpty(mobileNumber.getText())) {
                    mobileNumber.setText(R.string.country_code_with_zero);
                }
            }
        });

        firstName.requestFocus();
    }

    private void goToNext() {
        if (validateFields()) {
            addFriend();
        }
    }

    private void addButtonListener() {
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNext();
            }
        });
    }

    private boolean validateFields() {
        if (!APIManager.getInstance().getBuddyManager().validateText(firstName.getText().toString())) {
            firstNameLayout.setErrorEnabled(true);
            firstNameLayout.setError(getString(R.string.enterfirstnamevalidation));
            showKeyboard(firstName);
            return false;
        } else if (!APIManager.getInstance().getBuddyManager().validateText(lastName.getText().toString())) {
            lastNameLayout.setErrorEnabled(true);
            lastNameLayout.setError(getString(R.string.enterlastnamevalidation));
            showKeyboard(lastName);
            return false;
        } else if (!APIManager.getInstance().getBuddyManager().validateEmail(email.getText().toString())) {
            emailLayout.setErrorEnabled(true);
            emailLayout.setError(getString(R.string.enteremailvalidation));
            showKeyboard(email);
            return false;
        } else if (!APIManager.getInstance().getBuddyManager().validateMobileNumber(mobileNumber.getText().toString())) {
            mobileNumberLayout.setErrorEnabled(true);
            mobileNumberLayout.setError(getString(R.string.enternumbervalidation));
            showKeyboard(mobileNumber);
            return false;
        }
        return true;
    }

    private void addFriend() {
        ((YonaActivity) getActivity()).showLoadingView(true, null);
        APIManager.getInstance().getBuddyManager().addBuddy(firstName.getText().toString(), lastName.getText().toString(), email.getText().toString(), mobileNumber.getText().toString(), new DataLoadListener() {
            @Override
            public void onDataLoad(Object result) {
                ((YonaActivity) getActivity()).showLoadingView(false, null);
                getActivity().onBackPressed();
            }

            @Override
            public void onError(Object errorMessage) {
                ErrorMessage message = (ErrorMessage) errorMessage;
                ((YonaActivity) getActivity()).showLoadingView(false, null);

                CustomAlertDialog.show(getActivity(), message.getMessage(), getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void hideErrorMessage() {
        firstNameLayout.setError(null);
        lastNameLayout.setError(null);
        emailLayout.setError(null);
        mobileNumberLayout.setError(null);
    }

    private void showKeyboard(EditText editText) {
        ((InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE)).showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void onStateChange(int eventType, Object object) {
        int NUMBER_LENGTH = 9;
        switch (eventType) {
            case EventChangeManager.EVENT_CONTAT_CHOOSED:
                if (object instanceof RegisterUser) {
                    RegisterUser user = (RegisterUser) object;
                    firstName.setText(user.getFirstName());
                    lastName.setText(user.getLastName());
                    email.setText(user.getEmailAddress());
                    if (!TextUtils.isEmpty(user.getMobileNumber())) {
                        String number = user.getMobileNumber().replace(getString(R.string.space), getString(R.string.blank));
                        if (number.length() > NUMBER_LENGTH) {
                            number = number.substring(number.length() - NUMBER_LENGTH);
                            number = number.substring(0, 3) + getString(R.string.space) + number.substring(3, 6) + getString(R.string.space) + number.substring(6, 9);
                        }
                        mobileNumber.setText(getString(R.string.country_code_with_zero) + number);
                    } else {
                        mobileNumber.setText(getString(R.string.country_code_with_zero));
                    }
                }
                break;
            default:
                break;
        }
    }
}