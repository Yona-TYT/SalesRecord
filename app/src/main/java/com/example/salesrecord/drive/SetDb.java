package com.example.salesrecord.drive;

import android.content.Context;
import android.net.Uri;

import androidx.work.Data;

import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class SetDb {
    public static void set(Context context, Data outputData, Uri uri, DriveManager manager) throws IOException {

        boolean preloader = outputData.getBoolean("preloader", false);
        boolean newObj = outputData.getBoolean("newobj", false);
        boolean isCheck = outputData.getBoolean("check", false);
        boolean isId = outputData.getBoolean("isId", false);

        // Se ha seleccionado un respaldo y remplazara todos los datos locales
        if (isId){
            String mMsg = "Restaurando respaldo...";
            DBListCreator.cvsToDB(StartVar.mActivity, uri, 1, mMsg);
            return;
        }

        // call this to persist permission across decice reboots
        StringBuilder stringBuilder = new StringBuilder();

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader( new InputStreamReader(Objects.requireNonNull(inputStream)));

        String line;

        String hexID = "";
        String date = "";
        String time = "";

        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("\"", "");
            String[] spl = line.split(",");

            if (spl[0].equals("confID0")){
                //spl[0]; //Obj id
                //spl[1]; //Version
                hexID = spl[2]; //Hexa ID
                date = spl[3]; //Date
                time = spl[4]; //Time
                //spl[5]; //Save1
                //spl[6]; //Save2
                //spl[7]; //Save3
                stringBuilder.append(line);
                break;
            }
        }

        Conf mConf = StartVar.appDBall.daoCfg().getUsers(StartVar.mConfID);
        List<Article> mAccList = StartVar.appDBall.daoAtr().getUsers();
        if(!mConf.hexid.equals(hexID)){
            if(mAccList.isEmpty()){
                String mMsg = "Los datos locales están vacios";
                DBListCreator.cvsToDB(StartVar.mActivity, uri, 1, mMsg);
                return;
            }
            else {
                Basic.msg("Error: Los IDs de las DB no coinciden: "+hexID+" , "+mConf.hexid, true);

                //Si es desde el preloder se reinicia la actividad
                SetWorkResult.resetPreloader(preloader);
                return;
            }
        }
        else if(mAccList.isEmpty()){
            String mMsg = "Los datos locales están vacios";
            DBListCreator.cvsToDB(StartVar.mActivity, uri, 1, mMsg);
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Validar datos de entrada
            if (mConf.date == null || date.isEmpty() || mConf.time == null || time.isEmpty()) {
                Basic.msg("Error: Datos de fecha/hora incompletos");
                return;
            }

            // Combinar fecha y hora en LocalDateTime
            LocalDateTime dateTimeA = CalendUtls.DTformat(mConf.date + "T" + mConf.time);
            LocalDateTime dateTimeB = CalendUtls.DTformat(date + "T" + time);

            // Comparar fechas y horas
            int result = dateTimeA.compareTo(dateTimeB);
            if (result > 0) {
                //uploadDataBase();
                if (newObj) {
                    //Basic.msg("Enviando Actualizacion...");
                    manager.uploadDataBase();
                }
                else{
                    //Basic.msg("Los datos locales están más actualizados (" + dateTimeA + " > " + dateTimeB + ")");
                    if(isCheck) {
                        StartVar.genericQueue.startUsuarioQueue(1);
                    }
                }
            }
            else if (result < 0) {

                String mMsg = "Los datos en línea están más actualizados (" + dateTimeA + " < " + dateTimeB + ")";

                if (newObj){
                    mMsg = "Error los cambios no se sincronizaron";
                }
                if(isCheck) {
                    DBListCreator.cvsToDbNotFinish(StartVar.mActivity, uri, 1, "");
                    StartVar.genericQueue.startUsuarioQueue(2);
                }
                else {
                    DBListCreator.cvsToDB(StartVar.mActivity, uri, 1, "");

                }
                return;
            }
            else {
                if (newObj){
                    //Basic.msg("Enviando Actualizacion...");
                    String currDate = LocalDate.now().toString();
                    String currTime = LocalTime.now().toString();
                    StartVar.appDBall.daoCfg().updateDateTime(StartVar.mConfID, currDate, currTime);
                    StartVar.getConfigDB();
                    manager.uploadDataBase();
                }
                else {
                    if(!isCheck) {
                        Basic.msg("La base de datos está actualizada (" + dateTimeA + ")");
                    }
                }
                if(isCheck) {
                    StartVar.genericQueue.startUsuarioQueue(1);
                }
            }
            //Si es desde el preloder se reinicia la actividad
            SetWorkResult.resetPreloader(preloader);
        }
    }
}
