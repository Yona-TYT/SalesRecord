package com.example.salesrecord;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.room.Room;

import com.example.salesrecord.db.AllDao;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.Deuda;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.db.GenericQueue;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.drive.SetWorkResult;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class GlobalData {

    private static GlobalData instance;
    private final Context context;

    // Variables globales

    // DB Config
//    public  Conf mConfigDB;
//    public  String mConfID = "confID0";
//    public  String mDateVersion = "0";

    //Nombre de data Base
//    private static final String nameDB = "Ventas";
//    public static String nameDBconf = "Config-RG";

    public Article currArt = null;
    public String currSalId = "";

    public List<String> unitList = Arrays.asList("", "kg", "L", "m", "cm", "?", "?", "?");

    public List<String> categ = Arrays.asList("Unidad", "Paquete", "Caja", "No Empacables");

    public List<String> saleType = Arrays.asList("Venta", "Fiado", "Perdida");

    public int optTasa = 0;
    public double tasaDolar = 0.0;
    public double sendValue = 0.0;
    public boolean isEsFormat = true;

    public Double[][] listCalc = {
            new Double[] {0.0, 0.0, 0.0},
            new Double[] {0.0, 0.0, 0.0},
            new Double[] {0.0, 0.0, 0.0}
    };

    public static String[] dataList = {"","","","","","",""};

    public static long[] longList = new long[0];

    public static double[] doubList = new double[0];

    public static String[] dataDbg = {""};

    public int optCalc = 0;

//    public static List<String[]> csvList = new ArrayList<>();

    //Todas las listas----------------------------------------------
//    public  List<Article> listacc =  new ArrayList<>();
//    public  List<Cliente> listclt =  new ArrayList<>();
//    public  List<Fecha> listfec =  new ArrayList<>();
//    public  List<Sale> listpay = new ArrayList<>();
//    public  List<Deuda> listdeb = new ArrayList<>();
//    // DB
//    public  AllDao appDBall;
//
//    public int sendDate = 0;

//    public static Activity mActivity;
//    public static Activity reloadActivity;
//    public GenericQueue genericQueue;
//    public SetWorkResult mWorkResult = null;
//    public LifecycleOwner mLifecycle = null;


    private GlobalData(Context context) {
        this.context = AppContextProvider.getContext(); // Garantizamos ApplicationContext
    }

    /**
     * Método seguro para obtener la instancia
     */
    public static GlobalData getInstance(Context context) {
        if (instance == null) {
            synchronized (GlobalData.class) {
                if (instance == null) {
                    if (context == null) {
                        throw new IllegalArgumentException("Context cannot be null when initializing AppData");
                    }
                    instance = new GlobalData(context);
                }
            }
        }
        return instance;
    }

    /**
     * Método recomendado para usar desde Application
     */
    public static void initialize(Context context) {
        if (instance == null) {
            instance = new GlobalData(context);
        }
    }

//    //------------------------------------------ Para guardar las LISTAS
//    public void setAllListDB(){
//        //Instancia de la base de datos
//        this.appDBall = Room.databaseBuilder( AppContextProvider.getContext(), AllDao.class, this.nameDB).allowMainThreadQueries().build();
//
//        this.listacc = this.appDBall.daoAtr().getUsers();
//        this.listclt = this.appDBall.daoClt().getUsers();
//        this.listdeb = this.appDBall.daoDeb().getUsers();
//        this.listfec = this.appDBall.daoDat().getUsers();
//        this.listpay = this.appDBall.daoSal().getUsers();
//
//        //Instancia de la base de datos para Config
//        this.mConfigDB = this.appDBall.daoCfg().getUsers(this.mConfID);
//
//        if(this.mConfigDB == null){
//            long currDate = 0;
//            long currTime = 0;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                currDate = java.time.Instant.now().toEpochMilli();
//                currTime = System.currentTimeMillis();
//            }
//
//            // Generar UUID
//            UUID uuid = UUID.randomUUID();
//            // Convertir UUID a bytes (16 bytes)
//            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
//            byteBuffer.putLong(uuid.getMostSignificantBits());
//            byteBuffer.putLong(uuid.getLeastSignificantBits());
//
//            // Codificar en Base64 (sin padding para ahorrar espacio)
//            String textID = "";
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                textID = Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
//            }
//
//            //configDatabase.daoConf().insertUser();
//            Conf obj = new Conf(this.mConfID, mDateVersion, textID, "",0d, currDate, currTime, 0, 0, 0, 0);
//            this.appDBall.daoCfg().insertUser(obj);
//        }
//    }
//
//    public void getConfigDB(){
//        //Instancia de la base de datos
//        this.mConfigDB =  this.appDBall.daoCfg().getUsers(this.mConfID);
//    }
//
//
//    public void getAccListDB(){
//        //Instancia para obtener Cuentas
//        this.listacc =  this.appDBall.daoAtr().getUsers();
//    }
//    //----------------------------------------------------------------------------------
//
//    public void getCltListDB(){
//        //Instancia para obtener Clientes
//        this.listclt =  this.appDBall.daoClt().getUsers();
//    }
//    //----------------------------------------------------------------------------------
//
//    public void getFecListDB(){
//        //Instancia para obtener Fechas
//        this.listfec =  this.appDBall.daoDat().getUsers();
//    }
//
//    public List<String> getImgList(){
//        ArrayList<String> list = new ArrayList<>();
//        List<Sale> payList =  this.appDBall.daoSal().getUsers();
//        for (Sale mU : payList) {
//            list.add(mU.imagen);
//        }
//        return list;
//    }
//
//    public void setCsvList(List<String[]> mList){
//        this.csvList.clear();
//        this.csvList = mList;
//    }
//
//    public void setmActivity(Activity activity){
//        this.mActivity = activity;
//    }


    public void setCurrArt(Article obj){
        this.currArt = obj;
    }
    public Article getCurrArt(){
        return this.currArt;
    }

    public void setCurrSalId(String obj){
        this.currSalId = obj;
    }
    public String getCurrSalId(){
        return this.currSalId;
    }

    public void setTasaDolar(double tasa) {
        this.tasaDolar = tasa;
    }
    public double getTasaDolar() {
        return this.tasaDolar;
    }

    public void setSendValue(double value){
        this.sendValue = value;
    }
    public double getSendValue() {
        return this.sendValue;
    }

    public boolean getIsEsFormat(){ return this.isEsFormat; }

    public void setIsEsFormat(boolean b){
        this.isEsFormat = b;
    }

    public void setOptTasa(int opt) {
        this.optTasa = opt;
    }

    public int getOptTasa() {
        return this.optTasa;
    }


    public List<String> getUnitList(){
        return this.unitList;
    }

    public void setOptCalc(int opt) {
        this.optCalc = opt;
    }

    public int getOptCalc() {
        return this.optCalc;
    }

    public void setListCalc(Double[] mList, int opt) {
        if (opt < this.listCalc.length){
            this.listCalc[opt] = mList;
        }
    }

    public Double[] getListCalc(int opt) {
        if (opt < this.listCalc.length){
            return this.listCalc[opt];
        }
        return new Double[] {(double)0, (double)0, (double)0};
    }
    public void cleanListCalc() {
        this.listCalc = new Double[][]{
                new Double[]{(double) 0, (double) 0, (double) 0},
                new Double[]{(double) 0, (double) 0, (double) 0},
                new Double[]{(double) 0, (double) 0, (double) 0}
        };
    }

    public void setDateList(int opt, String mDate) {
        if (opt < this.dataList.length){
            this.dataList[opt] = mDate;
        }
    }

    public void setLongList(long[] mDate) {
        this.longList = mDate;
    }

    public String getDate(int opt) {
        if (opt < this.dataList.length){
            return this.dataList[opt];
        }
        return "";
    }

    public long[] getLongList() {
        return this.longList;
    }

    public double[] getDoubList() {
        return this.doubList;
    }
}
