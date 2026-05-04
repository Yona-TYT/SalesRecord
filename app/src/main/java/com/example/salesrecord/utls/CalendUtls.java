package com.example.salesrecord.utls;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Fecha;
import com.example.salesrecord.db.dao.DaoDat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendUtls {
    public CalendUtls() {
    }

    public static String dataConverted(String text, int selec) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //Convierte Sting  a forrmato de fecha
            LocalDate date = LocalDate.parse(text);
            //Inicia la fecha actual
            LocalDate currdate = LocalDate.now();

            long vlresult = 0;
            //Para años
            if (selec == 0) {
                vlresult = ChronoUnit.YEARS.between(date, currdate);
            }
            //Para meses
            else if (selec == 1) {
                vlresult = ChronoUnit.MONTHS.between(date, currdate);
            }
            //Para Dias
            else if (selec == 2) {
                vlresult = ChronoUnit.DAYS.between(date, currdate);
            }
            //Para Formato de fecha
            else if (selec == 3) {
                Period result = date.until(currdate);
                return result.getDays() + "-" + result.getMonths() + "-" + result.getYears();
            }
            return "" + (vlresult < 0 ? 1 : vlresult);
        }
        return "1";
    }

    public static String[] dataValidate(String text) {
        Pattern patt = Pattern.compile("(^(\\d{1,2})(/)(\\d{1,2})(/)(\\d{1,3})$)|(^(\\d{1,2})(-)(\\d{1,2})(-)(\\d{1,3})$)|(^(\\d{1,2})(\\.)(\\d{1,2})(\\.)(\\d{1,3})$)");
        Matcher matcher = patt.matcher(text);
        if (matcher.find()) {
            if (text.contains("-")) {
                return text.split("-");
            } else if (text.contains("/")) {
                return text.split("/");
            } else if (text.contains(".")) {
                return text.split("\\.");
            } else {
                return null;
            }
        }
        return null;
    }

    public static String getTime(String value) {
        String text = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalTime mTime = LocalTime.parse(value);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return formatter.format(mTime);
        }
        return text;
    }

    public static void addCurrentMonthIfAbsent(Context mContext) {
        DaoDat daoFecha = StartVar.appDBall.daoDat();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Inicia la fecha actual
            LocalDate currdate = LocalDate.now();
            List<Fecha> listFecha = daoFecha.getUsers();
            boolean exists = false;
            for (Fecha d : listFecha) {
                String f = d.date;
                LocalDate date = LocalDate.parse(f);
                if (currdate.getMonth().equals(date.getMonth()) && currdate.getYear() == date.getYear()) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                return;
            }
            LocalTime currtime = LocalTime.now();
            Fecha obj = new Fecha("dateID" + listFecha.size(), "" + currdate.getYear(),
                    currdate.getMonth().toString(), "" + currdate.getDayOfMonth(),
                    CalendUtls.getTime(currtime.toString()), currdate.toString());
            daoFecha.insertUser(obj);
            // Recarga la lista de la DB ----------------------------
            StartVar var = new StartVar();
            var.getFecListDB();
            // -------------------------------------------------------
        }
    }

    public static int getRangeMultiple(String txDate, int selec) {
        long num = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!txDate.isEmpty()) {
                // Convierte String a formato de fecha
                LocalDate date = LocalDate.parse(txDate);
                // Fecha actual
                LocalDate currdate = LocalDate.now();

                if (selec == 1) {
                    // Para días: diferencia directa (incluye día actual)
                    num = ChronoUnit.DAYS.between(date, currdate) + 1;
                } else if (selec == 2) {
                    // Para meses: desde 1 del mes de txDate hasta 1 del mes actual +1 si día actual >1
                    LocalDate mDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
                    LocalDate currMonthStart = currdate.withDayOfMonth(1);
                    num = ChronoUnit.MONTHS.between(mDate, currMonthStart);
                    if (currdate.getDayOfMonth() > 1) {
                        num += 1;
                    }
                } else if (selec == 3) {
                    // Para años: desde 1/1 del año de txDate hasta 1/1 del año actual +1 si día del año >1
                    LocalDate yDate = LocalDate.of(date.getYear(), 1, 1);
                    LocalDate currYearStart = currdate.withDayOfYear(1);
                    num = ChronoUnit.YEARS.between(yDate, currYearStart);
                    if (currdate.getDayOfYear() > 1) {
                        num += 1;
                    }
                }
            }
        }
        return (int) num;
    }


    public static Object[] dateToMoney(String startDate, int select, Double rent, Double paid) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (rent <= 0) {
                return null;
            }
            if (!startDate.isEmpty()) {
                LocalDate originalDate = LocalDate.parse(startDate);
                LocalDate currdate = LocalDate.now();  // O usa LocalDate.of(2025, 10, 26) para testing fijo

                int result = currdate.compareTo(originalDate);
                if (result < 0) {
                    return null;
                }

                // Ajustar originalDate según select
                LocalDate adjustedOriginal = originalDate;
                if (select == 2) {
                    adjustedOriginal = LocalDate.of(originalDate.getYear(), originalDate.getMonth(), 1);
                } else if (select == 3) {
                    adjustedOriginal = LocalDate.of(originalDate.getYear(), 1, 1);
                }

                // Calcular numOwed (períodos completos)
                long numOwed = 0;
                int daysPassed = 0;
                if (select == 1) {
                    numOwed = ChronoUnit.DAYS.between(adjustedOriginal, currdate)+1;
//                    if (numOwed == 0) {
//                        numOwed = 1;
//                    }
                } else if (select == 2) {
                    LocalDate currMonthStart = currdate.withDayOfMonth(1);
                    numOwed = ChronoUnit.MONTHS.between(adjustedOriginal, currMonthStart);
                    daysPassed = currdate.getDayOfMonth() - 1;
                    if (daysPassed > 0) {
                        numOwed += 1;
                    }
                } else if (select == 3) {
                    LocalDate currYearStart = currdate.withDayOfYear(1);
                    numOwed = ChronoUnit.YEARS.between(adjustedOriginal, currYearStart);
                    daysPassed = currdate.getDayOfYear() - 1;
                    if (daysPassed > 0) {
                        numOwed += 1;
                    }
                } else {
                    return new Object[]{0f, 0f, "", 1};
                }
                if (numOwed < 1) {
                    numOwed = 0;
                }

                double debt = 0.0;
                double currentPaid = 0.0;
                LocalDate date = adjustedOriginal;
                long covered = 0;

                // Calcular covered
                if (paid >= 0) {
                    covered = (long) Math.floor(paid / rent);
                } else {
                    covered = (long) Math.ceil(paid / rent);
                }

                long unpaidFull = numOwed - covered;
                double remainder = paid - (double) covered * rent;
                if (unpaidFull > 0) {
                    debt = (double) unpaidFull * rent;
                    currentPaid = remainder;
                } else {
                    debt = 0.0;
                    currentPaid = paid - (double) numOwed * rent;
                }

                // Calcular fecha: clamp superior para overpay positivo, permite negativo
                long periodsToAdd;
                if (covered > numOwed) {
                    periodsToAdd = numOwed;
                } else {
                    periodsToAdd = covered;
                }
                if (select == 1) {
                    date = adjustedOriginal.plusDays(periodsToAdd);
                } else if (select == 2) {
                    date = adjustedOriginal.plusMonths(periodsToAdd);
                } else {  // select == 3
                    date = adjustedOriginal.plusYears(periodsToAdd);
                }
                // Para overpay, opcional: si periodsToAdd == numOwed && currentPaid > 0, mantener en el siguiente inicio

                return new Object[]{debt, currentPaid, date.toString(), 0};
            }
        }
        return null;
    }

    public static String getDatePlus(String txDate, int sum, int selec) {
        String newDate = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!txDate.isEmpty()) {
                //Convierte Sting  a forrmato de fecha
                LocalDate date = LocalDate.parse(txDate);
                //Para Dias
                if (selec == 1) {
                    newDate = date.plusDays(sum).toString();
                }
                //Para meses
                else if (selec == 2) {
                    newDate = date.plusMonths(sum).toString();
                }
                //Para años
                else if (selec == 3) {
                    newDate = date.plusYears(sum).toString();
                }
            }
        }
        return newDate;
    }

    public static String getCorrectDate(String txDate, int selec) {
        String newDate = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!txDate.isEmpty()) {
                //Convierte Sting  a forrmato de fecha
                LocalDate date = LocalDate.parse(txDate);
                //Basic.msg("selec"+selec+" txDate "+txDate);
                //Para Dias
                if (selec == 1) {
                    return txDate;
                }
                //Para meses
                else if (selec == 2) {
                    newDate = LocalDate.of(date.getYear(), date.getMonth(), 1).toString();

                }
                //Para años
                else if (selec == 3) {
                    newDate = LocalDate.of(date.getYear(), 1, 1).toString();
                }
            }
        }
        return newDate;
    }

    public static LocalDateTime DTformat(String dt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (dt == null || dt.trim().isEmpty()) {
                Log.e("DTformat", "Fecha recibida es null o vacía");
                return LocalDateTime.now();
            }

            // Intentamos varios formatos comunes de Google Drive
            String[] patterns = {
                    "yyyy-MM-dd'T'HH:mm:ss",           // formato normal
                    "yyyy-MM-dd'T'H:mm:ss",            // hora con 1 dígito
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",       // con milisegundos
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",    // con microsegundos
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"  // con nanosegundos
            };

            for (String pattern : patterns) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault());
                    LocalDateTime result = LocalDateTime.parse(dt, formatter);

                    Log.d("DTformat", "✅ Parseado con éxito usando: " + pattern + " → " + result);
                    return result;

                } catch (Exception ignored) {
                    // Probamos el siguiente patrón
                }
            }

            // Si ninguno funcionó
            Log.e("DTformat", "❌ No se pudo parsear la fecha: " + dt);
            return LocalDateTime.now(); // fallback seguro
        }
        return null;
    }
}
