package com.example.vikram.instajr;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InstaRecyclerAdapter extends RecyclerView.Adapter<InstaRecyclerAdapter.ViewHolder> {

    public List<InstaPost> insta_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private TextView instaDate;


    public InstaRecyclerAdapter(List<InstaPost> insta_list){

        this.insta_list = insta_list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        final String instaPostId = insta_list.get(position).InstaPostId;

        String desc_data = insta_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = insta_list.get(position).getImageUrl();
        String thumbUri = insta_list.get(position).getImageThumb();
        holder.setInstaImage(image_url, thumbUri);

         String userId = insta_list.get(position).getUserId();

         if (firebaseAuth.getCurrentUser().getUid() != null) {

             //USER DATA WILL BE RETRIEVED HERE
             firebaseFirestore.collection("Users").document(userId).get()
                     .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                             if (task.isSuccessful()) {

                                 String userName = task.getResult().getString("name");
                                 String userImage = task.getResult().getString("image");

                                 holder.setUserData(userName, userImage);

                             } else {

                                 String error = task.getException().getMessage();

                                 Toast.makeText(context, "Error : " + error, Toast.LENGTH_LONG).show();

                             }

                         }
                     });

             try {
                 long millisecond = insta_list.get(position).getTimestamp().getTime();
                 String dateString = android.text.format.DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
                 holder.setTime(dateString);
             } catch (Exception e) {

                 Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
             }

             if (firebaseAuth.getCurrentUser() != null) {

             //LIKES COUNT
             firebaseFirestore.collection("Posts/" + instaPostId + "/Likes")
                     .addSnapshotListener(new EventListener<QuerySnapshot>() {
                         @Override
                         public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                             if (!documentSnapshots.isEmpty()) {

                                 int count = documentSnapshots.size();

                                 holder.updateLikesCount(count);

                             } else {

                                 holder.updateLikesCount(0);

                             }

                         }
                     });

         }
             //GET LIKES

             if (firebaseAuth.getCurrentUser() != null) {
                 firebaseFirestore.collection("Posts/" + instaPostId + "/Likes").document(currentUserId)
                         .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                             @Override
                             public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                 if (documentSnapshot.exists()) {

                                     holder.instaLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.like_icon_red));

                                 } else {

                                     holder.instaLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.like_icon));

                                 }

                             }
                         });
             }


             //LIKES SECTION

             holder.instaLikeBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {

                     firebaseFirestore.collection("Posts/" + instaPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                             if (!task.getResult().exists()) {

                                 Map<String, Object> likeMap = new HashMap<>();
                                 likeMap.put("timestamp", FieldValue.serverTimestamp());

                                 firebaseFirestore.collection("Posts/" + instaPostId + "/Likes").document(currentUserId).set(likeMap);

                             } else {

                                 firebaseFirestore.collection("Posts/" + instaPostId + "/Likes").document(currentUserId).delete();

                             }

                         }
                     });

                 }
             });

             holder.instaCommentBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {

                     context.startActivity(new Intent(context, CommentsActivity.class).putExtra("insta_post_id", instaPostId));

                 }
             });
         }

    }

    @Override
    public int getItemCount() {
        return insta_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private ImageView instaImageView;

        private TextView instaUserName;
        private CircleImageView instaUserImage;

        private ImageView instaLikeBtn;
        private TextView instaLikeCount;

        private ImageView instaCommentBtn;
        private TextView instaCommentCount;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            instaLikeBtn = mView.findViewById(R.id.insta_like_btn);
            instaCommentBtn = mView.findViewById(R.id.insta_comment_btn);
        }


        public void setDescText(String descText){

            descView = mView.findViewById(R.id.instaDesc);
            descView.setText(descText);
        }


        public void setInstaImage(String downloadUri, String thumbUri){

            instaImageView = mView.findViewById(R.id.instaImage);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.rectangle_placeholder);
            requestOptions.error(R.drawable.profile);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(Glide.with(context).load(thumbUri)).into(instaImageView);

        }

        public void setTime(String date){

            instaDate = mView.findViewById(R.id.instaDate);
            instaDate.setText(date);

        }

        public void setUserData(String name, String image){

            instaUserName = mView.findViewById(R.id.instaUserName);
            instaUserImage  =mView.findViewById(R.id.instaUserImage);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile);

            instaUserName.setText(name);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(instaUserImage);

        }

        public void updateLikesCount(int count){

            instaLikeCount = mView.findViewById(R.id.insta_like_count);
            instaLikeCount.setText(count + " likes");

        }

    }

}
