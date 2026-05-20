package com.example.salesrecord;

import android.app.Activity;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;
import androidx.room.Room;

import com.example.salesrecord.db.AllDao;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Deuda;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.GenericQueue;
import com.example.salesrecord.drive.SetWorkResult;
import com.example.salesrecord.utls.Basic;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Base64;

public class StartVar {

    //Mapa de arrays
    public static HashMap<String, ArrayList<String>> arrayMap = new HashMap<>();

    //Nombre de data Base
    private static final String nameDB = "Ventas";
    public static String nameDBconf = "Config-RG";

    //Worker tags
    public static final String WORK_TAG_DOWNLOAD = "DownloadWorkConfigDb"; // Define WORK_TAG para configdb
    //public static final String WORK_TAG_UPLOAD = "UploadWorkCowData"; // Define WORK_TAG para cowdatadb

    public static List<String[]> csvList = new ArrayList<>();

    //Todas las listas----------------------------------------------
    public static List<Article> listacc =  new ArrayList<>();
    public static List<Cliente> listclt =  new ArrayList<>();
    public static List<Fecha> listfec =  new ArrayList<>();
    public static List<Sale> listpay = new ArrayList<>();
    public static List<Deuda> listdeb = new ArrayList<>();
    // DB
    public static AllDao appDBall;

    //-------------------------------------------------------------------

    // DB Config
    public static Conf mConfigDB;
    public static String mConfID = "confID0";
    public static String mDateVersion = "0";

    // Var redundants
    public static boolean mPermiss = false;     //Permisos de gestion multimedia
    public static int accSelect = 0;      // Cuenta seleccionada
    public static int accCierre = 0;      // Cuenta seleccionada
    public static int mCurrency = 0;        //Moneda seleccionada
    public static int mCurrMes = 0;        //Mes seleccionado

    public static Double mDollar = 600d;       //Precio del dolar

    public static ArrayList<String> textList;
    public static ArrayList<String> dirList;
    public static ArrayList<String> typeList;
    public static ArrayList<String> morlist = new ArrayList<>();

    public static List<Object[]> bitList = new ArrayList<>();

    public static int currSel4 = 0;
    public static String currPayId = "";
    public static int cltIndex = 0;
    public static String cltBit = "0x0";

    public static final String dirAppName = ".salesrecord";
    public static final String csvAppName = "DataSave.csv";
    public static final String exportName = "DataSave.bin";

    //Root View
    public static View mRootView;

    //Preloder
    public static boolean mainStart = false;
    //Hacer upload cuando los datos esten disponibles.
    public static boolean makeUpdate = false;

    public static Activity mActivity;
    public static Activity reloadActivity;
    public static GenericQueue genericQueue;
    public static int sendDate = 0;

    public static SetWorkResult mWorkResult = null;
    public static LifecycleOwner mLifecycle = null;

    public StartVar(){}

    public static void getConfigDB(){
        //Instancia de la base de datos
        StartVar.mConfigDB =  StartVar.appDBall.daoCfg().getUsers(StartVar.mConfID);
    }

    //------------------------------------------ Para guardar las cuentas
    public void setAllListDB(){
        //Instancia de la base de datos
        StartVar.appDBall = Room.databaseBuilder( AppContextProvider.getContext(), AllDao.class, StartVar.nameDB).allowMainThreadQueries().build();

        StartVar.listacc = StartVar.appDBall.daoAtr().getUsers();
        StartVar.listclt = StartVar.appDBall.daoClt().getUsers();
        StartVar.listdeb = StartVar.appDBall.daoDeb().getUsers();
        StartVar.listfec = StartVar.appDBall.daoDat().getUsers();
        StartVar.listpay = StartVar.appDBall.daoSal().getUsers();

        //Instancia de la base de datos para Config
        StartVar.mConfigDB = StartVar.appDBall.daoCfg().getUsers(StartVar.mConfID);

        Basic.msg(""+StartVar.mConfigDB);
        if(StartVar.mConfigDB == null){
            String date = "";
            String time= "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                date = LocalDate.now().toString();
                time = LocalTime.now().toString();
            }

            // Generar UUID
            UUID uuid = UUID.randomUUID();
            // Convertir UUID a bytes (16 bytes)
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());

            // Codificar en Base64 (sin padding para ahorrar espacio)
            String textID = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                textID = Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
            }

            //configDatabase.daoConf().insertUser();
            Conf obj = new Conf(StartVar.mConfID, mDateVersion, textID, date, time, 0, 0d, 0, 0, 0);
            StartVar.appDBall.daoCfg().insertUser(obj);
        }
    }
    public void getAccListDB(){
        //Instancia para obtener Cuentas
        StartVar.listacc =  StartVar.appDBall.daoAtr().getUsers();
    }
    //----------------------------------------------------------------------------------

    public void getCltListDB(){
        //Instancia para obtener Clientes
        StartVar.listclt =  StartVar.appDBall.daoClt().getUsers();
    }
    //----------------------------------------------------------------------------------

    public void getFecListDB(){
        //Instancia para obtener Fechas
        StartVar.listfec =  StartVar.appDBall.daoDat().getUsers();
    }
    public void setmPermiss(boolean permiss){
        mPermiss = permiss;
    }
    public void setCurrentAcc(int idx){
        accSelect = idx;
    }
    public void setCurrentTyp(int idx){
        accCierre = idx;}
    public void setCurrency(int idx){
        mCurrency = idx;
    }
    public void setCurrentMes(int idx){
        mCurrMes = idx;
    }

    public void setDollar(Double value){
        StartVar.mDollar = value;
    }
    public void setRootView(View view){mRootView = view;}

    public void setArrayList(ArrayList<String> listA, ArrayList<String> listB, ArrayList<String> listC){
        StartVar.textList = listA;
        StartVar.dirList = listB;
        StartVar.typeList = listC;
    }

    public void setPayId(String value){
        StartVar.currPayId = value;
    }
    public void setCltIndex(int value){
        StartVar.cltIndex = value;
    }
    public static void setCltBit(String value){
        StartVar.cltBit = value;
    }

    public void setmActivity(Activity activity){
        StartVar.mActivity = activity;
    }

    public void setMorlist(ArrayList<String> list){
        StartVar.morlist.clear();
        StartVar.morlist = list;
    }

    public static List<String> getImgList(){
        ArrayList<String> list = new ArrayList<>();
        List<Sale> payList =  StartVar.appDBall.daoSal().getUsers();
        for (Sale mU : payList) {
            list.add(mU.imagen);
        }
        return list;
    }

    public static void setmMainStart(boolean mStart){mainStart = mStart;}

    public static void setCsvList(List<String[]> mList){
        StartVar.csvList.clear();
        StartVar.csvList = mList;
    }

}
