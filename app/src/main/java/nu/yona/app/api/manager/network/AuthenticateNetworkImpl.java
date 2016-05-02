/*
 *  Copyright (c) 2016 Stichting Yona Foundation
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *
 */

package nu.yona.app.api.manager.network;

import java.io.IOException;
import java.lang.annotation.Annotation;

import nu.yona.app.YonaApplication;
import nu.yona.app.api.model.ErrorMessage;
import nu.yona.app.api.model.OTPVerficationCode;
import nu.yona.app.api.model.PinResetDelay;
import nu.yona.app.api.model.RegisterUser;
import nu.yona.app.api.model.User;
import nu.yona.app.listener.DataLoadListener;
import nu.yona.app.utils.AppUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by kinnarvasa on 28/03/16.
 */
public class AuthenticateNetworkImpl extends BaseImpl {

    /**
     * Register user.
     *
     * @param password the password
     * @param object   the object
     * @param listener the listener
     */
    public void registerUser(String password, RegisterUser object, final DataLoadListener listener) {
        try {
            getRestApi().registerUser(password, object).enqueue(getUserCallBack(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Register user override.
     *
     * @param password Yona Password
     * @param object   RegisterUser object
     * @param otp      SMS
     * @param listener the listener
     */
    public void registerUserOverride(String password, RegisterUser object, String otp, final DataLoadListener listener) {
        try {
            getRestApi().overrideRegisterUser(password, otp, object).enqueue(getUserCallBack(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Gets user.
     *
     * @param url          url from user object to get/update user
     * @param yonaPassword yona password
     * @param listener     the listener
     */
    public void getUser(String url, String yonaPassword, DataLoadListener listener) {
        try {
            getRestApi().getUser(url, yonaPassword).enqueue(getUserCallBack(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Verify mobile number.
     *
     * @param password yona password
     * @param url      url for verify mobile number
     * @param otp      sms verification code
     * @param listener the listener
     */
    public void verifyMobileNumber(String password, String url, OTPVerficationCode otp, final DataLoadListener listener) {
        try {
            getRestApi().verifyMobileNumber(url, password, otp).enqueue(getUserCallBack(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Resend otp.
     *
     * @param url      url to resend sms
     * @param password yona password
     * @param listener the listener
     */
    public void resendOTP(String url, String password, final DataLoadListener listener) {
        try {
            getRestApi().resendOTP(url, password).enqueue(getCall(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Request user override.
     *
     * @param mobileNumber Registering mobile number
     * @param listener     the listener
     */
    public void requestUserOverride(String mobileNumber, DataLoadListener listener) {
        try {
            getRestApi().requestUserOverride(mobileNumber).enqueue(getCall(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Delete user.
     *
     * @param url          the url
     * @param yonaPassword the yona password
     * @param listener     the listener
     */
    public void deleteUser(String url, String yonaPassword, DataLoadListener listener) {
        try {
            getRestApi().deleteUser(url, yonaPassword).enqueue(getCall(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Do passcode reset.
     *
     * @param url          : URL for passcode reset
     * @param yonaPassword : Yona password
     * @param listener     the listener
     */
    public void doPasscodeReset(String url, String yonaPassword, final DataLoadListener listener) {
        try {
            getRestApi().requestPinReset(url, yonaPassword).enqueue(new Callback<PinResetDelay>() {
                @Override
                public void onResponse(Call<PinResetDelay> call, Response<PinResetDelay> response) {
                    if (response.code() < NetworkConstant.RESPONSE_STATUS) {
                        listener.onDataLoad(response.body());
                    } else {
                        try {
                            Converter<ResponseBody, ErrorMessage> errorConverter =
                                    getRetrofit().responseBodyConverter(ErrorMessage.class, new Annotation[0]);
                            listener.onError(errorConverter.convert(response.errorBody()));
                        } catch (IOException e) {
                            listener.onError(new ErrorMessage(e.getMessage()));
                        }
                    }
                }

                @Override
                public void onFailure(Call<PinResetDelay> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Do verify pin.
     *
     * @param url      URL for verify pin
     * @param otp      SMS received value
     * @param listener the listener
     */
    public void doVerifyPin(String url, String otp, final DataLoadListener listener) {
        try {
            getRestApi().verifyPin(url, YonaApplication.getYonaPassword(), new OTPVerficationCode(otp)).enqueue(getCall(listener));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    /**
     * Do clear pin.
     *
     * @param url URL for Verify Pin Reset
     */
    public void doClearPin(String url) {
        try {
            getRestApi().clearPin(url, YonaApplication.getYonaPassword()).enqueue(getCall(new DataLoadListener() {
                @Override
                public void onDataLoad(Object result) {
                    // Do nothing as we don't worry about this response.
                }

                @Override
                public void onError(Object errorMessage) {
                    // Do nothing as we don't worry about this response.
                }
            }));
        } catch (Exception e) {
            AppUtils.throwException(AuthenticateNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), null);
        }
    }


    private Callback<User> getUserCallBack(final DataLoadListener listener) {
        return new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() < NetworkConstant.RESPONSE_STATUS) {
                    listener.onDataLoad(response.body());
                } else {
                    try {
                        Converter<ResponseBody, ErrorMessage> errorConverter =
                                getRetrofit().responseBodyConverter(ErrorMessage.class, new Annotation[0]);
                        ErrorMessage errorMessage = errorConverter.convert(response.errorBody());
                        listener.onError(errorMessage);
                    } catch (IOException e) {
                        listener.onError(new ErrorMessage(e.getMessage()));
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                listener.onError(new ErrorMessage(t.getMessage()));
            }
        };
    }

}
