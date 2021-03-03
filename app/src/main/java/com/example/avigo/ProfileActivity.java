package com.example.avigo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
{
    private String receiveUserId="", receiveUserImage="", receiveUserName="";
    private ImageView backgroud_profile_view;
    private TextView name_profile;
    private Button add_friend,decline_friend_request;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState= "new" ;
    private DatabaseReference friendRequestRef, contactRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiveUserId = getIntent().getExtras().get("visit_user_id").toString();
        receiveUserImage= getIntent().getExtras().get("profile_image").toString();
        receiveUserName= getIntent().getExtras().get("profile_name").toString();

        backgroud_profile_view = findViewById(R.id. backgroud_profile_view);
        name_profile = findViewById(R.id. name_profile);
        add_friend = findViewById(R.id. add_friend);
        decline_friend_request =findViewById(R.id. decline_friend_request);

        Picasso.get().load(receiveUserImage).into(backgroud_profile_view);
        name_profile.setText(receiveUserName);

        manageClickEvents();
    }

    private void manageClickEvents()
    {
        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiveUserId))
                        {
                            String requestType = dataSnapshot.child(receiveUserId).child("request_type").getValue().toString();

                            if (requestType.equals("sent"))
                            {
                                currentState= "request_sent";
                                add_friend.setText("Cancel Friend Request");
                            }
                            else if (requestType.equals("received"))
                            {
                                currentState= "request_received";
                                add_friend.setText("Accept Friend Request");

                                decline_friend_request.setVisibility(View.VISIBLE);
                                decline_friend_request.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiveUserId))
                                            {
                                                currentState = "friends";
                                                add_friend.setText("Delete Contact");
                                            }
                                            else
                                            {
                                                currentState= "new";
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

        if(senderUserId.equals(receiveUserId))
        {
            add_friend.setVisibility(View.GONE);
        }
        else
        {
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(currentState.equals("new"))
                    {
                        SendFriendRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                }
            });
        }
    }



    private void AcceptFriendRequest()
    {
        contactRef.child(senderUserId).child(receiveUserId).child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactRef.child(receiveUserId).child(senderUserId).child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                friendRequestRef.child(senderUserId).child(receiveUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(receiveUserId).child(senderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        currentState = "friends";
                                                                                        add_friend.setText("Delete Contact");

                                                                                        decline_friend_request.setVisibility(View.GONE);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void CancelFriendRequest()
    {
        friendRequestRef.child(senderUserId).child(receiveUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            friendRequestRef.child(receiveUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                currentState = "new";
                                                add_friend.setText("Add Friend");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendFriendRequest()
    {
        friendRequestRef.child(senderUserId).child(receiveUserId).child("request_type").setValue("sent")
        .addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
               if (task.isSuccessful())
               {
                   friendRequestRef.child(receiveUserId).child(senderUserId).child("request_type").setValue("received")
                           .addOnCompleteListener(new OnCompleteListener<Void>()
                           {
                               @Override
                               public void onComplete(@NonNull Task<Void> task)
                               {
                                    if (task.isSuccessful())
                                    {
                                        currentState= "request_sent";
                                        add_friend.setText("Cancel Friend Request");
                                        Toast.makeText(ProfileActivity.this,"Friend Request Sent", Toast.LENGTH_SHORT).show();
                                    }
                               }
                           });
               }
            }
        });
    }
}
