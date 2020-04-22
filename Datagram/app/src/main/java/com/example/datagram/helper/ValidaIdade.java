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

}
