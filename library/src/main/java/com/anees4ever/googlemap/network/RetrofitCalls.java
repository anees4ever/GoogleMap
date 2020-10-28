package com.anees4ever.googlemap.network;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitCalls {
    public static String LAST_ERROR= "";
    public static boolean ABORT_RUNNING_CALL= false;
    public interface RetrofitResponseHandler {
        void onSuccess(ResponseBody response);
        void onFailure(String errorMessage);
    }

    private static boolean checkIfAborted() {
        if(ABORT_RUNNING_CALL) {
            ABORT_RUNNING_CALL= false;
            return true;
        }
        return false;
    }

    public static boolean getString(final String url, final OnResponseString mOnResponse) {
        try {
            return get(url, new RetrofitResponseHandler() {
                @Override
                public void onSuccess(ResponseBody response) {
                    if (mOnResponse != null) {
                        try {
                            mOnResponse.onResponse(true, response.string());
                        } catch (Exception e) {
                            e.printStackTrace();
                            LAST_ERROR= "Invalid Response data";
                            mOnResponse.onResponse(false, null);
                        }
                    }
                }
                @Override
                public void onFailure(String errorMessage) {
                    if(mOnResponse != null) {
                        mOnResponse.onResponse(false, null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean getJSON(final String url, final OnResponseJSON mOnResponse) {
        try {
            return get(url, new RetrofitResponseHandler() {
                @Override
                public void onSuccess(ResponseBody response) {
                    if (mOnResponse != null) {
                        try {
                            mOnResponse.onResponse(true, new JSONObject(response.string()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            LAST_ERROR= "Invalid JSON Response: " + response;
                            mOnResponse.onResponse(false, null);
                        }
                    }
                }
                @Override
                public void onFailure(String errorMessage) {
                    if(mOnResponse != null) {
                        mOnResponse.onResponse(false, null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean downloadUrl(final String url, final File fileToSave, final OnResponseDownload mOnResponse) {
        try {
            return get(url, new RetrofitResponseHandler() {
                @Override
                public void onSuccess(ResponseBody response) {
                    try {
                        try(
                            InputStream input= new BufferedInputStream(response.byteStream());
                            OutputStream output= new FileOutputStream(fileToSave);
                        ) {
                            int count;
                            long readBytes= 0, totalBytes= input.available();
                            byte[] data = new byte[1024 * 1024];
                            while ((count = input.read(data)) != -1) {
                                readBytes+= count;
                                if (checkIfAborted()) {
                                    if(fileToSave.exists()) {
                                        fileToSave.delete();
                                    }
                                    if(mOnResponse != null) {
                                        mOnResponse.onResponse(false, fileToSave);
                                    }
                                    return;
                                }
                                if(mOnResponse != null) {
                                    mOnResponse.onProgress(readBytes, totalBytes);
                                }
                                output.write(data, 0, count);
                            }
                        }

                        if(mOnResponse != null) {
                            mOnResponse.onResponse(true, fileToSave);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LAST_ERROR= "Invalid Response data";
                        if(mOnResponse != null) {
                            mOnResponse.onResponse(false, fileToSave);
                        }
                    }
                }
                @Override
                public void onFailure(String errorMessage) {
                    if(mOnResponse != null) {
                        mOnResponse.onResponse(false, fileToSave);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean get(final String url, final RetrofitResponseHandler mResponseHandler) {
        try {
            String[] urlInfo= RetrofitApiClass.extractHostAndUrl(url);
            Call<ResponseBody> call = RetrofitApiClass.getWith(urlInfo[0]).create(Api_Calls_Custom.class).get(urlInfo[1]);
            return call(call, mResponseHandler);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean call(Call<ResponseBody> call, final RetrofitResponseHandler mResponseHandler) {
        try {
            ABORT_RUNNING_CALL= false;
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if(mResponseHandler != null) {
                            if (response.body() != null) {
                                mResponseHandler.onSuccess(response.body());
                            } else {
                                mResponseHandler.onFailure("Response: Invalid response...");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        LAST_ERROR= "Failure:" + t.getMessage();
                        if(mResponseHandler != null) {
                            mResponseHandler.onFailure(LAST_ERROR);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if(mResponseHandler != null) {
                mResponseHandler.onFailure("Call Failed: " + e.getMessage());
            }
        }
        return false;
    }
}
