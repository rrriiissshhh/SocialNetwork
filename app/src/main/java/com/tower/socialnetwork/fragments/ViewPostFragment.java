package com.tower.socialnetwork.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tower.socialnetwork.PostAdapter;
import com.tower.socialnetwork.R;
import com.tower.socialnetwork.utilities.Constants;
import com.tower.socialnetwork.utilities.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPostFragment extends Fragment {
    private View view;
    private OnViewPostListener mOnViewPostListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_fragment, container, false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnViewPostListener = (OnViewPostListener) context;
            Bundle bundle = this.getArguments();
            showPosts(bundle.getString("action"), bundle.getString("data"));
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCreatePostListener");
        }
    }

    private void showPosts(String action, final String data) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String loginUrl = Constants.SERVER_URL + action;
        Log.e("TAG", loginUrl);
        mOnViewPostListener.showProgress(true);
        // Request a json response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, loginUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("TAG--------D--", response);
                        List<Post> lPosts = new ArrayList<>();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("status")) {
                                JSONArray posts = jsonResponse.getJSONArray("data");
                                for (int i = 0; i < posts.length(); i++) {
                                    JSONObject post = (JSONObject) posts.get(i);
                                    Post uPost = new Post(post.getString("uid"), post.getInt("postid"), post.getString("text"), post.getString("timestamp"), post.getJSONArray("Comment"));
                                    lPosts.add(uPost);
                                }
                                addContentToList(lPosts);
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "Failed to load your posts", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("TAG--------JSON EX--", e.toString());
                        }
                        mOnViewPostListener.showProgress(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), "Didn't work", Toast.LENGTH_SHORT).show();
                        Log.e("TAG--------VOLLEY EX--", error.toString());
                        mOnViewPostListener.showProgress(false);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if(data != null) {
                    params.put("uid", data);
                }
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public interface OnViewPostListener {
        void showProgress(boolean is_visible);
    }

    private void addContentToList(List<Post> values) {
        ListView listView = view.findViewById(R.id.list);
        ArrayAdapter adapter = new PostAdapter(getActivity(), R.layout.item_post, new ArrayList<>(values));
        listView.setAdapter(adapter);

    }
}
