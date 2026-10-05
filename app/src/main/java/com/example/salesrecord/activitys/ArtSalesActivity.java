package com.example.salesrecord.activitys;

import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.adapters.PayAdapter;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtSalesActivity extends AppCompatActivity {


    private DaoArt daoArt;
    private DaoSal daoSal;

    private Article mArt;

    private TextView mText1;
    private ListView mListView;
    private PayAdapter mAdapter1;

    private Context contex;
    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_art_sales);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mText1 = findViewById(R.id.txview_art_sal1);
        mListView = findViewById(R.id.art_sal_viewList);

        setViwes();
    }

    @Override
    public void onResume() {
        super.onResume();

        setViwes();
    }

    private void setViwes() {
        contex = AppContextProvider.getContext();

        if (StartVar.appDBall == null) {
            StartVar.setAllListDB();
        }

        daoArt = StartVar.appDBall.daoAtr();
        daoSal = StartVar.appDBall.daoSal();

        DaoCfg daoCfg = StartVar.appDBall.daoCfg();

        Conf conf = daoCfg.getUsers(StartVar.mConfID);

        mArt = glData.getCurrArt();

        if(mArt != null){

            String metrTag = mArt.metrica == 0 ? "" : "Por "+glData.unitList.get(mArt.metrica);
            String desc = (mArt.descr.isEmpty() ? " ("+metrTag+")" : " (" + mArt.descr + " "+ metrTag +")");
            if(metrTag.isEmpty() && mArt.descr.isEmpty()){
                desc = "";
            }

            mText1.setText(mArt.nombre+desc);

            List<Sale> mSalList = daoSal.getUsers();

            List<Object[]> mPayList = new ArrayList<>();

            for (Sale mSale : mSalList){
                if (mSale != null){
                    String[] artcList = mSale.artclist.split("\\|");
                    String[] countList = mSale.countlist.split("\\|");
                    String[] priceList = mSale.pricelist.split("\\|");
                    String[] margList = mSale.marglist.split("\\|");

                    for (int i = 0; i < artcList.length; i++) {

                        if (mArt.article.equals(artcList[i])) {
                            double count = Double.parseDouble(countList[i]);
//                            double price = Double.parseDouble(priceList[i]);
//                            double marge = Double.parseDouble(margList[i]);
//
//                            double calc = MathUtls.addPercentage(price, marge);

                            double clcPrice = MathUtls.addPercentage(mArt.precund, mArt.margen+conf.margen);
                            String date = CalendUtls.getShortDate(mSale.fecha);
                            String time = CalendUtls.getTime(mSale.time);

                            String txAlias = mSale.cliente;

                            if (txAlias.startsWith("cltID")) {
                                DaoClt daoClt = StartVar.appDBall.daoClt();
                                Cliente mClt = daoClt.getUsers(txAlias);
                                if (mClt != null) {
                                    txAlias = mClt.nombre;
                                }
                            }

                            Object[] strList = new Object[8];
                            strList[0] = mSale.sale;
                            strList[1] = "Cantidad: "+ MoneyUtls.formatDecimal(count);
                            strList[2] = clcPrice * count;
                            strList[3] = date;
                            strList[4] = mSale.status;
                            strList[5] = time;
                            strList[6] = mSale.tasa;
                            strList[7] = txAlias;

                            mPayList.add(strList);
                        }
                    }
                }
            }

            Collections.reverse(mPayList);
            mAdapter1 = new PayAdapter(contex, mPayList);
            mListView.setAdapter(mAdapter1);
            mAdapter1.getFilter().filter("");

        }
    }
}