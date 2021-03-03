package com.example.avigo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class ContactActivity extends AppCompatActivity
{
    BottomNavigationView navView;
    RecyclerView my_ContactList;
    ImageView findPeoplebtn;
    private DatabaseReference contactRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String username="", profileImg="";
    private String calledBy="";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_activity);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");


        navView = findViewById(R.id. nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        findPeoplebtn = findViewById(R.id. find_people_btn);
        my_ContactList= findViewById(R.id. contact_list);
        my_ContactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeoplebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent findPeopleIntent = new Intent(ContactActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);

            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.navigation_home:
                    Intent mainIntent = new Intent(ContactActivity.this, ContactActivity.class);
                    startActivity(mainIntent);
                    break;

                case R.id. navigation_setting:
                    Intent settingIntent = new Intent(ContactActivity.this, SettingActivity.class);
                    startActivity(settingIntent);
                    break;

                case R.id.navigation_notifications:
                    Intent notificationIntent = new Intent(ContactActivity.this, NotificationActivity.class);
                    startActivity(notificationIntent);
                    break;

                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(ContactActivity.this, RegistrationActivity2.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
            }

            return false;
        }
    };


    @Override
    protected void onStart()
    {
        super.onStart();

        checkForReceivingCall();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts contacts)
            {
                final String listUserId = getRef(i).getKey();

                userRef.child(listUserId).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            username= dataSnapshot.child("name").getValue().toString();
                            profileImg= dataSnapshot.child("image").getValue().toString();

                            holder.userNameTxt.setText(username);
                            Picasso.get().load(profileImg).into(holder.profileImageView);
                        }

                        holder.callBtn.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent callingIntent= new Intent(ContactActivity.this, CallingActivity.class);
                                callingIntent.putExtra("visit_user_id", listUserId);
                                startActivity(callingIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent, false );
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder ;
            }
        };
        my_ContactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }



    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        Button callBtn;
        ImageView profileImageView;

        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userNameTxt = itemView.findViewById(R.id. name_contact);
            callBtn = itemView.findViewById(R.id. call_btn);
            profileImageView = itemView.findViewById(R.id. image_contact);
        }
    }


    private void  validateUser()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists())
                {
                    Intent settingIntent= new Intent(ContactActivity.this, SettingActivity.class);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    private void checkForReceivingCall()
    {
        userRef.child(currentUserId).child("Ringing")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("ringing"))
                        {
                            calledBy=  dataSnapshot.child("ringing").getValue().toString();

                            Intent callingIntent= new Intent(ContactActivity.this, CallingActivity.class);
                            callingIntent.putExtra("visit_user_id", calledBy);
                            startActivity(callingIntent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
    }


}
