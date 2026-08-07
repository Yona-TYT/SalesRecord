package com.example.salesrecord;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.Deuda;
import com.example.salesrecord.db.dao.DaoDat;
import com.example.salesrecord.db.dao.DaoDeb;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.FilesManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DBListCreator extends AppCompatActivity {

    // Classs para la gestion de archivos
    private FilesManager fmang = new FilesManager();



    private GlobalData glData ;

    private final Context context;

    // Constructor para forzar el uso del contexto correcto
    public DBListCreator(Context context) {
        this.context = context.getApplicationContext();
        this.glData = GlobalData.getInstance(this.context);
    }

    public static HashMap<String, HashMap<String, ArrayList<Object>>> createDbLists(){

        DaoCfg daoConf = StartVar.appDBall.daoCfg();
        DaoArt daoArt = StartVar.appDBall.daoAtr();
        DaoClt daoCliente = StartVar.appDBall.daoClt();
        DaoDeb daoDeuda = StartVar.appDBall.daoDeb();
        DaoDat daoFecha = StartVar.appDBall.daoDat();
        DaoSal daoSales = StartVar.appDBall.daoSal();

        //Lista general de todos los objetos db
        List<String[]> mList = new ArrayList<>();


        //=================================== Config DB Lista =====================================================
        mList.add(new String[]{"<0>"});// Etiqueta para config
        //Instancia de la base de datos
        Conf mConf =  daoConf.getUsers(StartVar.mConfID);
        mList.add(new String[]{mConf.config, mConf.version, mConf.hexid, mConf.datetasa,
                String.valueOf(mConf.dolar), String.valueOf(mConf.date),
                String.valueOf(mConf.time), mConf.curr.toString(), mConf.moneda.toString(),
                mConf.mes.toString(), mConf.show.toString(), mConf.dbg
        });

        //=================================== Cuenta DB Lista =====================================================

        mList.add(new String[]{"<1>"});// Etiqueta para cuenta

        HashMap<String, ArrayList<Object>> mapAtr = new HashMap<>();
        mapAtr.put("mA", new ArrayList<>());
        mapAtr.put("mB", new ArrayList<>());
        mapAtr.put("mC", new ArrayList<>());
        mapAtr.put("mD", new ArrayList<>());
        mapAtr.put("mE", new ArrayList<>());
        mapAtr.put("mF", new ArrayList<>());
        mapAtr.put("mG", new ArrayList<>());
        mapAtr.put("mH", new ArrayList<>());
        mapAtr.put("mI", new ArrayList<>());
        mapAtr.put("mJ", new ArrayList<>());

        ArrayList<Object> atrLmA = mapAtr.get("mA");
        ArrayList<Object> atrLmB = mapAtr.get("mB");
        ArrayList<Object> atrLmC = mapAtr.get("mC");
        ArrayList<Object> atrLmD = mapAtr.get("mD");
        ArrayList<Object> atrLmE = mapAtr.get("mE");
        ArrayList<Object> atrLmF = mapAtr.get("mF");
        ArrayList<Object> atrLmG = mapAtr.get("mG");
        ArrayList<Object> atrLmH = mapAtr.get("mH");
        ArrayList<Object> atrLmI = mapAtr.get("mI");
        ArrayList<Object> atrLmJ = mapAtr.get("mJ");

        // Instancia de la base de datos
        List<Article> listAtr = daoArt.getUsers();

        for (Article myAtr : listAtr) {

            // 1. Lista para exportar a CSV (Ajustada a 14 campos según tu clase)
            String[] txList = new String[18];

            txList[0] = myAtr.article;
            txList[1] = myAtr.nombre;
            txList[2] = myAtr.descr;
            txList[3] = (myAtr.iddesde != null) ? myAtr.iddesde : "";
            txList[4] = myAtr.image;

            txList[5] = String.valueOf(myAtr.precund);
            txList[6] = String.valueOf(myAtr.precpq);
            txList[7] = String.valueOf(myAtr.preccj);
            txList[8] = String.valueOf(myAtr.margen);

            txList[9] = String.valueOf(myAtr.totalcount);
            txList[10] = String.valueOf(myAtr.currcount);
            txList[11] = String.valueOf(myAtr.isopen);
            txList[12] = String.valueOf(myAtr.artipo);
            txList[13] = String.valueOf(myAtr.metrica);
            txList[14] = String.valueOf(myAtr.caduca);
            txList[15] = String.valueOf(myAtr.staus);
            txList[16] = String.valueOf(myAtr.ultfec); // Se guarda como Long (Timestamp)
            txList[17] = String.valueOf(myAtr.fecha);

            mList.add(txList);

            // 2. Listas individuales (Asegúrate de haber declarado atrLmK, atrLmL, etc.)
            atrLmA.add(myAtr.article);
            atrLmB.add(myAtr.nombre);
            atrLmC.add(myAtr.descr);
            atrLmD.add(String.valueOf(myAtr.precund));
            atrLmE.add(String.valueOf(myAtr.totalcount));
            atrLmF.add(String.valueOf(myAtr.currcount));
            atrLmG.add(String.valueOf(myAtr.artipo));
            atrLmH.add(String.valueOf(myAtr.metrica));
            atrLmI.add(String.valueOf(myAtr.ultfec));
            atrLmJ.add(String.valueOf(myAtr.fecha));
        }

        //=================================== Cliente DB Lista =====================================================

        mList.add(new String[]{"<2>"});// Etiqueta para cliente

        HashMap<String, ArrayList<Object>> mapClt = new HashMap<>();
        mapClt.put("mA", new ArrayList<>());
        mapClt.put("mB", new ArrayList<>());
        mapClt.put("mC", new ArrayList<>());
        mapClt.put("mD", new ArrayList<>());
        mapClt.put("mE", new ArrayList<>());
        mapClt.put("mF", new ArrayList<>());
        mapClt.put("mG", new ArrayList<>());
        mapClt.put("mH", new ArrayList<>());
        mapClt.put("mI", new ArrayList<>());
        mapClt.put("mJ", new ArrayList<>());

        ArrayList<Object> cltLmA = mapClt.get("mA");
        ArrayList<Object> cltLmB = mapClt.get("mB");
        ArrayList<Object> cltLmC = mapClt.get("mC");
        ArrayList<Object> cltLmD = mapClt.get("mD");
        ArrayList<Object> cltLmE = mapClt.get("mE");
        ArrayList<Object> cltLmF = mapClt.get("mF");
        ArrayList<Object> cltLmG = mapClt.get("mG");
        ArrayList<Object> cltLmH = mapClt.get("mH");
        ArrayList<Object> cltLmI = mapClt.get("mI");
        ArrayList<Object> cltLmJ = mapClt.get("mJ");

        //Instancia de la base de datos
        List<Cliente> listClt =  daoCliente.getUsers();

        for (Cliente myClt : listClt) {

            //------------------------------------------------------
            // Se crea la lista para exportar a csv  ---------------
            String[] txList = new String[10];

            txList[0] = myClt.cliente;
            txList[1] = myClt.nombre;
            txList[2] = myClt.alias;
            txList[3] = myClt.defaulacc;
            txList[4] = myClt.priority.toString();
            txList[5] = myClt.fecha;
            txList[6] = myClt.level.toString();
            txList[7] = myClt.ulfech;
            txList[8] = myClt.oper.toString();
            txList[9] = myClt.bits;

            mList.add(txList);

            //--------------------------------------------------------
            cltLmA.add(myClt.cliente);
            cltLmB.add(myClt.nombre);
            cltLmC.add(myClt.alias);
            cltLmD.add(myClt.defaulacc);
            cltLmE.add(myClt.priority);
            cltLmF.add(myClt.fecha);
            cltLmG.add(myClt.level);
            cltLmH.add(myClt.ulfech);
            cltLmI.add(myClt.oper);
            cltLmJ.add(myClt.bits);
            //------------------------------------------
        }

        //=================================== Deuda DB Lista =====================================================

        mList.add(new String[]{"<3>"});// Etiqueta para deuda

        HashMap<String, ArrayList<Object>> mapDeb = new HashMap<>();
        mapDeb.put("mA", new ArrayList<>());
        mapDeb.put("mB", new ArrayList<>());
        mapDeb.put("mC", new ArrayList<>());
        mapDeb.put("mD", new ArrayList<>());
        mapDeb.put("mE", new ArrayList<>());
        mapDeb.put("mF", new ArrayList<>());
        mapDeb.put("mG", new ArrayList<>());
        mapDeb.put("mH", new ArrayList<>());
        mapDeb.put("mI", new ArrayList<>());
        mapDeb.put("mJ", new ArrayList<>());
        mapDeb.put("mK", new ArrayList<>());

        ArrayList<Object> debLmA = mapDeb.get("mA");
        ArrayList<Object> debLmB = mapDeb.get("mB");
        ArrayList<Object> debLmC = mapDeb.get("mC");
        ArrayList<Object> debLmD = mapDeb.get("mD");
        ArrayList<Object> debLmE = mapDeb.get("mE");
        ArrayList<Object> debLmF = mapDeb.get("mF");
        ArrayList<Object> debLmG = mapDeb.get("mG");
        ArrayList<Object> debLmH = mapDeb.get("mH");
        ArrayList<Object> debLmI = mapDeb.get("mI");
        ArrayList<Object> debLmJ = mapDeb.get("mJ");
        ArrayList<Object> debLmK = mapDeb.get("mK");

        //Instancia de la base de datos
        List<Deuda> listDeb =  daoDeuda.getUsers();

        for (Deuda myDeb : listDeb) {

            //------------------------------------------------------
            // Se crea la lista para esportar a csv  ---------------
            String[] txList = new String[12];

            txList[0] = myDeb.deuda;
            txList[1] = myDeb.accid;
            txList[2] = myDeb.cltid;
            txList[3] = myDeb.rent.toString();
            txList[4] = myDeb.porc.toString();
            txList[5] = myDeb.fecha;
            txList[6] = myDeb.estat.toString();
            txList[7] = myDeb.pagado.toString();
            txList[8] = myDeb.ulfech;
            txList[9] = myDeb.oper.toString();
            txList[10] = myDeb.remnant.toString();
            txList[11] = myDeb.disabfec;

            mList.add(txList);

            //--------------------------------------------------------
            debLmA.add(myDeb.deuda);
            debLmB.add(myDeb.accid);
            debLmC.add(myDeb.cltid);
            debLmD.add(myDeb.rent);
            debLmE.add(myDeb.porc);
            debLmF.add(myDeb.fecha);
            debLmG.add(myDeb.estat);
            debLmH.add(myDeb.pagado);
            debLmI.add(myDeb.ulfech);
            debLmJ.add(myDeb.oper);
            debLmK.add(myDeb.remnant);
            //------------------------------------------
        }

        //=================================== Date DB Lista =====================================================

        mList.add(new String[]{"<4>"});// Etiqueta para date

        HashMap<String, ArrayList<Object>> mapDat = new HashMap<>();
        mapDat.put("mA", new ArrayList<>());
        mapDat.put("mB", new ArrayList<>());
        mapDat.put("mC", new ArrayList<>());
        mapDat.put("mD", new ArrayList<>());
        mapDat.put("mE", new ArrayList<>());
        mapDat.put("mF", new ArrayList<>());

        ArrayList<Object> datLmA = mapDat.get("mA");
        ArrayList<Object> datLmB = mapDat.get("mB");
        ArrayList<Object> datLmC = mapDat.get("mC");
        ArrayList<Object> datLmD = mapDat.get("mD");
        ArrayList<Object> datLmE = mapDat.get("mE");
        ArrayList<Object> datLmF = mapDat.get("mF");

        //Instancia de la base de datos
        List<Fecha> listDat =  daoFecha.getUsers();

        for (Fecha myDat : listDat) {

            //------------------------------------------------------
            // Se crea la lista para esportar a csv  ---------------
            String[] txList = new String[4];

            txList[0] = myDat.fecha;
            txList[1] = myDat.strdate;
            txList[2] = String.valueOf(myDat.date);
            txList[3] = String.valueOf(myDat.time);
            mList.add(txList);

            //--------------------------------------------------------
            datLmA.add(myDat.fecha);
            datLmB.add(myDat.strdate);
            datLmC.add(String.valueOf(myDat.date));
            datLmD.add(String.valueOf(myDat.time));

            //------------------------------------------
        }

        //=================================== Sales DB Lista =====================================================

        mList.add(new String[]{"<5>"});// Etiqueta para cliente

        HashMap<String, ArrayList<Object>> mapSal = new HashMap<>();
        mapSal.put("mA", new ArrayList<>());
        mapSal.put("mB", new ArrayList<>());
        mapSal.put("mC", new ArrayList<>());
        mapSal.put("mD", new ArrayList<>());
        mapSal.put("mE", new ArrayList<>());
        mapSal.put("mF", new ArrayList<>());
        mapSal.put("mG", new ArrayList<>());
        mapSal.put("mH", new ArrayList<>());
        mapSal.put("mI", new ArrayList<>());
        mapSal.put("mJ", new ArrayList<>());
        mapSal.put("mK", new ArrayList<>());
        mapSal.put("mL", new ArrayList<>());
        mapSal.put("mM", new ArrayList<>());
        mapSal.put("mN", new ArrayList<>());
        mapSal.put("mO", new ArrayList<>());


        ArrayList<Object> salLmA = mapSal.get("mA");
        ArrayList<Object> salLmB = mapSal.get("mB");
        ArrayList<Object> salLmC = mapSal.get("mC");
        ArrayList<Object> salLmD = mapSal.get("mD");
        ArrayList<Object> salLmE = mapSal.get("mE");
        ArrayList<Object> salLmF = mapSal.get("mF");
        ArrayList<Object> salLmG = mapSal.get("mG");
        ArrayList<Object> salLmH = mapSal.get("mH");
        ArrayList<Object> salLmI = mapSal.get("mI");
        ArrayList<Object> salLmJ = mapSal.get("mJ");
        ArrayList<Object> salLmK = mapSal.get("mK");
        ArrayList<Object> salLmL = mapSal.get("mL");
        ArrayList<Object> salLmM = mapSal.get("mM");
        ArrayList<Object> salLmN = mapSal.get("mN");
        ArrayList<Object> salLmO = mapSal.get("mO");

        //Instancia de la base de datos
        List<Sale> listSal = daoSales.getUsers();

        for (Sale mySal : listSal) {
            if(mySal == null) {
                continue;
            }
            //------------------------------------------------------
            // Se crea la lista para esportar a csv  ---------------
            String[] txList = new String[15];

            txList[0] = mySal.sale;
            txList[1] = mySal.cliente;
            txList[2] = mySal.artclist;
            txList[3] = mySal.countlist;
            txList[4] = mySal.pricelist;
            txList[5] = mySal.marglist;

            txList[6] = String.valueOf(mySal.monto);
            txList[7] = String.valueOf(mySal.tasa);
            txList[8] = String.valueOf(mySal.status);

            txList[9] = mySal.imagen;
            txList[10] = String.valueOf(mySal.time);
            txList[11] = mySal.cltid;
            txList[12] = String.valueOf(mySal.more4);
            txList[13] = mySal.more5;
            txList[14] = String.valueOf(mySal.fecha);

            mList.add(txList);

            //--------------------------------------------------------
            salLmA.add(mySal.sale);
            salLmB.add(mySal.cliente);
            salLmC.add(mySal.artclist);
            salLmD.add(mySal.countlist);
            salLmE.add(mySal.pricelist);
            salLmF.add(mySal.marglist);

            salLmG.add(String.valueOf(mySal.monto));
            salLmH.add(String.valueOf(mySal.tasa));
            salLmI.add(String.valueOf(mySal.status));

            salLmJ.add(mySal.imagen);
            salLmK.add(String.valueOf(mySal.time));
            salLmM.add(String.valueOf(mySal.more4));
            salLmN.add(mySal.more5);
            salLmL.add(String.valueOf(mySal.fecha));

            //------------------------------------------
        }

        //Log.d("DB_INSTANCE", "Hash: " + System.identityHashCode(StartVar.appDBall));
        //Basic.msg("uploadDataBase.. " +mList.get(5)[1], true);

        StartVar.setCsvList(mList);

        HashMap<String, HashMap<String, ArrayList<Object>>> allMaps = new HashMap<>();
        allMaps.put("acc", mapAtr);
        allMaps.put("clt", mapClt);
        allMaps.put("deb", mapDeb);
        allMaps.put("dat", mapDat);
        allMaps.put("pay", mapSal);

        //Basic.msg("csvlist "+mList.size(), true);

        return allMaps;
    }

    public void cvsToDB(Activity mActivity, Uri uri, int importType, String mMsg) {
        cvsToDBInternal(mActivity, uri, importType, mMsg, true);
    }

    public void cvsToDbNotFinish(Activity mActivity, Uri uri, int importType, String mMsg) {
        cvsToDBInternal(mActivity, uri, importType, mMsg,false);
    }

    public void cvsToDBInternal(Activity mActivity, Uri uri, int importType, String mMsg, boolean finish){
        StringBuilder stringBuilder = new StringBuilder();

        DaoCfg daoConf = StartVar.appDBall.daoCfg();
        DaoArt daoArt = StartVar.appDBall.daoAtr();
        DaoClt daoCliente = StartVar.appDBall.daoClt();
        DaoDeb daoDeuda = StartVar.appDBall.daoDeb();
        DaoDat daoFecha = StartVar.appDBall.daoDat();
        DaoSal daoSales = StartVar.appDBall.daoSal();

        try {

            //Limpia todas las DB
            for (Article obj : daoArt.getUsers()){
                daoArt.removerUser(obj.article);
            }
            for (Cliente obj : daoCliente.getUsers()){
                daoCliente.removerUser(obj.cliente);
            }
            for (Deuda obj : daoDeuda.getUsers()){
                daoDeuda.removerUser(obj.deuda);
            }
            for (Fecha obj : daoFecha.getUsers()){
                daoFecha.removerUser(obj.fecha);
            }
            for (Sale obj : daoSales.getUsers()){
                daoSales.removerUser(obj.sale);
            }

            InputStream inputStream = AppContextProvider.getContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader( new InputStreamReader(Objects.requireNonNull(inputStream)));
            String line;
            String version = "0";
            int opt = 0;
            String[] t = {"<0>", "<1>", "<2>", "<3>", "<4>", "<5>"};
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\"", "");
                String[] spl = line.split(",");
                //Log.d("PhotoPicker", " Aquiiiiiiiiii Hayyyyyy ------------------------: "+ line);
                int f = spl.length;
                if(f==1){
                    String tx = spl[0];
                    if(tx.equals(t[0])){
                        t[0] = "";
                        stringBuilder.append(line);
                        continue;
                    }
                    else if(tx.equals(t[1])){
                        t[1] = "";
                        opt = 1;
                        stringBuilder.append(line);
                        continue;
                    }
                    else if(tx.equals(t[2])){
                        t[2] = "";
                        opt = 2;
                        stringBuilder.append(line);
                        continue;
                    }
                    else if(tx.equals(t[3])){
                        t[3] = "";
                        opt = 3;
                        stringBuilder.append(line);
                        continue;
                    }
                    else if(tx.equals(t[4])){
                        t[4] = "";
                        opt = 4;
                        stringBuilder.append(line);
                        continue;
                    }
                    else if(tx.equals(t[5])){
                        t[5] = "";
                        opt = 5;
                        stringBuilder.append(line);
                        continue;
                    }
                    stringBuilder.append(line);
                    continue;
                }
                if(opt==0) {
                    version = spl[1];
                    daoConf.updateUser("confID0", StartVar.mDateVersion, spl[2], spl[3], Double.parseDouble(spl[4]), Long.parseLong(spl[5]), Long.parseLong(spl[6]), Integer.parseInt(spl[7]), Integer.parseInt(spl[8]), Integer.parseInt(spl[9]), Integer.parseInt(spl[10]), version.equals("1")? spl[11]:"csv Version Vieja");
                }
                else if(opt==1){
                    Article obj = new Article(
                            spl[0], spl[1], spl[2], spl[3], spl[4],
                            Double.parseDouble(spl[5]), Double.parseDouble(spl[6]), Double.parseDouble(spl[7]),
                            Double.parseDouble(spl[8]), Double.parseDouble(spl[9]), Double.parseDouble(spl[10]), Integer.parseInt(spl[11]),
                            Integer.parseInt(spl[12]),  Integer.parseInt(spl[13]), Integer.parseInt(spl[14]), Integer.parseInt(spl[15]),
                            Long.parseLong(spl[16]), Long.parseLong(spl[17])
                    );
                    daoArt.insertUser(obj);
                }
                else if(opt==2) {
                    Cliente obj = new Cliente(
                            spl[0], spl[1], spl[2], spl[3], Integer.parseInt(spl[4]), spl[5], Float.parseFloat(spl[6]),
                            spl[7], Integer.parseInt(spl[8]), spl[9]
                    );
                    daoCliente.insertUser(obj);
                }
                else if(opt==3){
                    Deuda obj = new Deuda(
                            spl[0], spl[1], spl[2], Double.parseDouble(spl[3]), 0, Integer.parseInt(spl[4]), spl[5],
                            Integer.parseInt(spl[6]), Integer.parseInt(spl[7]), spl[8], Integer.parseInt(spl[9]), Double.parseDouble(spl[10]), "@null"
                    );
                    daoDeuda.insertUser(obj);
                }

                else if(opt==4){
                    Fecha obj = new Fecha(
                            spl[0], spl[1], Long.parseLong(spl[2]), Long.parseLong(spl[3])
                    );
                    daoFecha.insertUser(obj);
                }
                else {
                    Sale obj = new Sale(
                            spl[0], spl[1], spl[2], spl[3], spl[4], spl[5], Double.parseDouble(spl[6]), Double.parseDouble(spl[7]),
                            Integer.parseInt(spl[8]), spl[9], Long.parseLong(spl[10]), spl[11], Integer.parseInt(spl[12]), spl[13], Long.parseLong(spl[14])
                    );
                    daoSales.insertUser(obj);
                }
                stringBuilder.append(line);
            }
        }
        catch (FileNotFoundException e) {
            Basic.msg("ErrorA: "+ e.getMessage());
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            Basic.msg("ErrorB: "+ e.getMessage());
            throw new RuntimeException(e);
        }

        if(finish) {
            if( StartVar.reloadActivity != null){
                mActivity = StartVar.reloadActivity;
            }
            Intent mIntent = new Intent(AppContextProvider.getContext(), mActivity.getClass());
            mActivity.startActivity(mIntent);
            Basic.msg(mMsg);
            mActivity.finish();
        }
    }

    private static String getUserId(DaoClt mDao){
        //Configura el nuevo index-------------------------------------------------------------------
        int mSiz = mDao.getUsers().size();
        String mIdx = "userID0";
        if(mSiz > 0) {
            mIdx = "userID" + mSiz;
        }
//        for(int i = 0; i < mSiz; i++){
//            Deuda mUser = mDao.getUsers("userID"+i);
//            if(mUser == null){
//                mIdx =  "userID"+i;
//                break;
//            }
//        }
        return mIdx;
        //-------------------------------------------------------------------------------------------
    }
}
