package com.example.salesrecord.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.GalleryAdapter;
import com.example.salesrecord.databinding.FragmentHomeBinding;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.Obj;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // DB
    private DaoArt daoArt;
    private List<Article> mArtList =  new ArrayList<>();

    private GridView gridView;
    private Long currGrid = 0L;

    private Context contex;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);


        contex = AppContextProvider.getContext();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
        gridView = binding.gcImg;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        setViwes();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setViwes(){

        daoArt = StartVar.appDBall.daoAtr();
        mArtList = daoArt.getUsers();

        Basic.msg("Siz: "+mArtList.size());
        List<Obj> objList = new ArrayList<>();
        for (Article obj : mArtList) {
            objList.add(setGalleryArray(obj));
        }
        gridView.setAdapter(new GalleryAdapter(contex, objList));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //nextViewActivity((int)id);
            }
        });

    }

//    public void nextViewActivity(int pos){
//        Intent mIntent = new Intent(this, ViewActivity.class);
//        Bundle mBundle = new Bundle();
//        //Log.d("PhotoPicker", "11100------------------------: " + dirList.size());
//        mBundle.put("index", pos);
//        mIntent.putExtras(mBundle);
//        //Save gallery petition
//        int firstVisiblePosition = gridView.getFirstVisiblePosition();
//        Basic.msg("Primera posición visible: " + firstVisiblePosition);
//
//        // Opcional: Para más precisión, obtén el offset (píxeles desde el top del primer ítem)
//        if (gridView.getChildCount() > 0) {
//            int offset = gridView.getChildAt(0).getTop();
//            Basic.msg("Offset: " + offset);
//            // Guarda ambos: posición + offset
//            myPrefernce.setGalleryPosition(firstVisiblePosition, offset);
//        }
//        else {
//            myPrefernce.setGalleryPosition(firstVisiblePosition, 0);
//        }
//        startActivity(mIntent);
//    }

    private Obj setGalleryArray(Article art){

        Obj mObj = new Obj(art.nombre, art.descr, art.image, art.uid);

        return mObj;

    }
}