package com.example.vikram.instajr;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView feeds_list_view;
    private List<InstaPost> insta_list;

    private DocumentSnapshot lastVisible;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    private InstaRecyclerAdapter instaRecyclerAdapter;

    private Boolean isFirstPageFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        insta_list = new ArrayList<>();
        feeds_list_view = view.findViewById(R.id.feeds_list_view);

        instaRecyclerAdapter = new InstaRecyclerAdapter(insta_list);
        feeds_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        feeds_list_view.setAdapter(instaRecyclerAdapter);
        feeds_list_view.setHasFixedSize(true);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            feeds_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom){

                        loadMorePost();

                    }
                }
            });


            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            insta_list.clear();
                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String instaPostId = doc.getDocument().getId(); //THIS ID NEEDED FOR LIKES

                                InstaPost instaPost = doc.getDocument().toObject(InstaPost.class).withId(instaPostId);

                                if (isFirstPageFirstLoad) {

                                    insta_list.add(instaPost);

                                } else {

                                    insta_list.add(0, instaPost);

                                }

                                instaRecyclerAdapter.notifyDataSetChanged();

                            }

                        }

                        isFirstPageFirstLoad = false;

                    }

                }
            });

        }

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost(){

        if (firebaseAuth.getCurrentUser() != null) {


            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String instaPostId = doc.getDocument().getId(); //THIS ID NEEDED FOR LIKES

                                InstaPost instaPost = doc.getDocument().toObject(InstaPost.class).withId(instaPostId);
                                insta_list.add(instaPost);

                                instaRecyclerAdapter.notifyDataSetChanged();

                            }

                        }

                    }

                }
            });

        }

    }


}
