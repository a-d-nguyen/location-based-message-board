package com.example.cs160_sp18.prog3;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// Displays a list of comments for a particular landmark.
public class CommentFeedActivity extends AppCompatActivity {

    private static final String TAG = CommentFeedActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Comment> mComments = new ArrayList<Comment>();

    public String username;
    public String MessageBoardTitle;

    // UI elements
    EditText commentInputBox;
    RelativeLayout layout;
    Button sendButton;
    Toolbar mToolbar;

    /* TODO: right now mRecyclerView is using hard coded comments.
     * You'll need to add functionality for pulling and posting comments from Firebase
     */
    private FirebaseDatabase mDatabase;
    private DatabaseReference landmarkRef;
//    private FirebaseRecyclerAdapter populate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_feed);

        mDatabase = FirebaseDatabase.getInstance();

        Intent landmarkIntent = getIntent();
        Bundle landmarkIntentExtras = landmarkIntent.getExtras();
        username = (String) landmarkIntentExtras.get("username");
        MessageBoardTitle = (String) landmarkIntentExtras.get("Message Board Title");

        String landmarkName = MessageBoardTitle;

        // sets the app bar's title
        setTitle(landmarkName + ": Posts");

        // hook up UI elements
        layout = (RelativeLayout) findViewById(R.id.comment_layout);
        commentInputBox = (EditText) layout.findViewById(R.id.comment_input_edit_text);
        sendButton = (Button) layout.findViewById(R.id.send_button);

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(landmarkName + ": Posts");

        mRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // create an onclick for the send button
        setOnClickForSendButton();

        // make some test comment objects that we add to the recycler view
        makeTestComments();

        // use the comments in mComments to create an adapter. This will populate mRecyclerView
        // with a custom cell (with comment_cell_layout) for each comment in mComments
//        setAdapterAndUpdateData();
    }

    // TODO: delete me
    private void makeTestComments() {
//        String randomString = "hello world hello world ";
//        Comment newComment = new Comment(randomString, "test_user1", new Date());
//        Comment hourAgoComment = new Comment(randomString + randomString, "test_user2", new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
//        Comment overHourComment = new Comment(randomString, "test_user3", new Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000)));
//        Comment dayAgoComment = new Comment(randomString, "test_user4", new Date(System.currentTimeMillis() - (25 * 60 * 60 * 1000)));
//        Comment daysAgoComment = new Comment(randomString + randomString + randomString, "test_user5", new Date(System.currentTimeMillis() - (48 * 60 * 60 * 1000)));
//        mComments.add(newComment);mComments.add(hourAgoComment); mComments.add(overHourComment);mComments.add(dayAgoComment); mComments.add(daysAgoComment);

        landmarkRef = mDatabase.getReference(MessageBoardTitle);
        landmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                HashMap<String, ArrayList<Comment>> hashy = (HashMap<String, ArrayList<Comment>>) dataSnapshot.getValue();
//                mComments.addAll(hashy.get("feed"));
                HashMap<String, Object> data = new HashMap<>();
                Long date = null;
                ArrayList<HashMap<String, Object>> hullo = (ArrayList<HashMap<String, Object>>) dataSnapshot.getValue();
//                HashMap<String, HashMap<String, Object>> hashy = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();
//                if (dataSnapshot.getValue() != null) {
//                    for (int i =0; i<hashy.size(); i++) {
//                        data = hashy.get(Integer.toString(i));
//                        date = (Long) data.get("date");
//                        Date mDate = new Date(date);
//                        mComments.add(new Comment((String) data.get("msg"), (String) data.get("user"), mDate));
//                    }
//                }
                if (hullo != null) {
                    for (int i = 0; i <hullo.size(); i++) {
                        data = hullo.get(i);
                        date = (Long) data.get("date");
                        Date mDate = new Date(date);
                        mComments.add(new Comment((String) data.get("msg"), (String) data.get("user"), mDate));
                    }
                    setAdapterAndUpdateData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to retrieve database.", databaseError.toException());
            }
        });
    }

    private void setOnClickForSendButton() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentInputBox.getText().toString();
                if (TextUtils.isEmpty(comment)) {
                    // don't do anything if nothing was added
                    commentInputBox.requestFocus();
                } else {
                    // clear edit text, post comment
                    commentInputBox.setText("");
                    postNewComment(comment);
                }
            }
        });
    }

    private void setAdapterAndUpdateData() {
        // create a new adapter with the updated mComments array
        // this will "refresh" our recycler view
        mAdapter = new CommentAdapter(this, mComments);
        mRecyclerView.setAdapter(mAdapter);

        // scroll to the last comment
        mRecyclerView.smoothScrollToPosition(mComments.size());
    }

    private void postNewComment(String commentText) {
        landmarkRef = mDatabase.getReference(MessageBoardTitle);

        DatabaseReference temp = landmarkRef.child(String.valueOf(mComments.size()));
        temp.child("msg").setValue(commentText);
        temp.child("user").setValue(username);
        temp.child("date").setValue(ServerValue.TIMESTAMP);

        Comment newComment = new Comment(commentText, username, new Date());
        mComments.add(newComment);

        setAdapterAndUpdateData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
