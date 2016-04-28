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

import nu.yona.app.api.model.AddBuddy;
import nu.yona.app.api.model.Buddy;
import nu.yona.app.api.model.ErrorMessage;
import nu.yona.app.listener.DataLoadListener;
import nu.yona.app.utils.AppUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by kinnarvasa on 28/04/16.
 */
public class BuddyNetworkImpl extends BaseImpl {

    /**
     * Add Buddy
     *
     * @param buddy
     * @param listener
     */
    public void addBuddy(String url, String yonaPassowrd, AddBuddy buddy, DataLoadListener listener) {
        try {
            getRestApi().addBuddy(url, yonaPassowrd, buddy).enqueue(getCall(listener));
        } catch (Exception e) {
            AppUtils.throwException(BuddyNetworkImpl.class.getSimpleName(), e, Thread.currentThread(), listener);
        }
    }

    public void getBuddies(String url, String password, final DataLoadListener listener) {
        getRestApi().getBuddy(url, password).enqueue(new Callback<Buddy>() {
            @Override
            public void onResponse(Call<Buddy> call, Response<Buddy> response) {
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
            public void onFailure(Call<Buddy> call, Throwable t) {
                listener.onError(new ErrorMessage(t.getMessage()));
            }
        });
    }

    public void deleteBuddy(String url, String passwrod, DataLoadListener listener) {
        getRestApi().deleteBuddy(url, passwrod).enqueue(getCall(listener));
    }
}
