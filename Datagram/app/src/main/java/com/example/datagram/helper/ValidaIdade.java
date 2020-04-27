package com.example.datagram.helper;

import java.util.Calendar;

public class ValidaIdade {

    public static boolean isGreatherThan18(String data){
        String[] array = data.split("/");

        int anoNasc = Integer.parseInt(array[2]);

        Calendar calendar = Calendar.getInstance();

        final int year = calendar.get(Calendar.YEAR);

        if(year-anoNasc>=18)
            return true;
        else return false;
    }

    public static boolean isGreatherThan18(String dataNasc, String dataEmp){
        String[] arrayDataNasc = dataNasc.split("/");

        String[] arrayDataEmp = dataEmp.split("/");

        int anoNasc = Integer.parseInt(arrayDataNasc[2]);

        int anoEmp = Integer.parseInt(arrayDataEmp[2]);

        Calendar calendar = Calendar.getInstance();

        final int year = calendar.get(Calendar.YEAR);

        if(anoEmp > year)
            return false;
        if(anoNasc > year)
            return false;

        if(anoEmp-anoNasc>=18)
            return true;
        else
            return false;
    }


}
