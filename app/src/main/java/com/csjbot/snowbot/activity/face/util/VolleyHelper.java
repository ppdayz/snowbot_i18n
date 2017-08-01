package com.csjbot.snowbot.activity.face.util;

/**
 * Created by mac on 15/10/19.
 */
public class VolleyHelper {
//
//    private static RequestQueue mQueue;
//    //dev
//
//    public static RequestQueue initQueue(Context mContext) {
//        if (mQueue == null) {
//            mQueue = Volley.newRequestQueue(mContext);
//        }
//        return mQueue;
//    }
//
//
//    /**
//     * 请求下一步
//     *
//     * @param url
//     * @param listener
//     */
//    public static void doGet(String url, final HelpListener listener) {
//        addRequest(new StringRequest(
//                url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        listener.onResponse(response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        error.printStackTrace();
//                        listener.onError(error);
//                    }
//                }) {
//        });
//    }
//
//
//
//    private static void addRequest(Request<?> mRequest) {
//        mRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        mRequest.setTag(1);
//
//        mQueue.add(mRequest);
//    }
//
//    public interface HelpListener {
//        public void onResponse(String response);
//
//        public void onError(VolleyError error);
//    }
//
//    public static void cancleAll() {
//        mQueue.cancelAll(1);
//    }
}
