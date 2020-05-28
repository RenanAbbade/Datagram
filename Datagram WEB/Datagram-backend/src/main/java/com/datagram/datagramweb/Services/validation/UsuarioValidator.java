package com.datagram.datagramweb.Services.validation;

import java.util.InputMismatchException;
import java.util.Calendar;

import org.springframework.stereotype.Service;

@Service
public class UsuarioValidator {

  // validacoes CPF
  public boolean isCPF(String CPF) {

    // considera-se erro CPF's formados por uma sequencia de numeros iguais
    if (CPF.length() > 11) {
      CPF = CPFnumeros(CPF);
    }

    if (CPF.equals("00000000000") || CPF.equals("11111111111") || CPF.equals("22222222222") || CPF.equals("33333333333")
        || CPF.equals("44444444444") || CPF.equals("55555555555") || CPF.equals("66666666666")
        || CPF.equals("77777777777") || CPF.equals("88888888888") || CPF.equals("99999999999") || (CPF.length() != 11))
      return (false);

    char dig10, dig11;
    int sm, i, r, num, peso;

    // "try" - protege o codigo para eventuais erros de conversao de tipo (int)
    try {
      // Calculo do 1o. Digito Verificador
      sm = 0;
      peso = 10;
      for (i = 0; i < 9; i++) {
        // converte o i-esimo caractere do CPF em um numero:
        // por exemplo, transforma o caractere '0' no inteiro 0
        // (48 eh a posicao de '0' na tabela ASCII)
        num = (int) (CPF.charAt(i) - 48);
        sm = sm + (num * peso);
        peso = peso - 1;
      }

      r = 11 - (sm % 11);
      if ((r == 10) || (r == 11))
        dig10 = '0';
      else
        dig10 = (char) (r + 48); // converte no respectivo caractere numerico

      // Calculo do 2o. Digito Verificador
      sm = 0;
      peso = 11;
      for (i = 0; i < 10; i++) {
        num = (int) (CPF.charAt(i) - 48);
        sm = sm + (num * peso);
        peso = peso - 1;
      }

      r = 11 - (sm % 11);
      if ((r == 10) || (r == 11))
        dig11 = '0';
      else
        dig11 = (char) (r + 48);

      // Verifica se os digitos calculados conferem com os digitos informados.
      if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
        return true;
      else
        return false;
    } catch (InputMismatchException erro) {
      return false;
    }
  }

  public static String imprimeCPF(String CPF) {
    CPF = CPFnumeros(CPF);
    return (CPF.substring(0, 3) + "." + CPF.substring(3, 6) + "." + CPF.substring(6, 9) + "-" + CPF.substring(9, 11));
  }

  public static String CPFnumeros(String CPF) {
    String novoCPF = CPF.replace("-", "");
    return novoCPF.replace(".", "");
  }

  // validacoes Date

  public boolean isGreatherThan18(String data) {//2020-05-16

    String novaData = formataDataPadraoBr(data);

    String[] array = novaData.split("/");

    int anoNasc = Integer.parseInt(array[2]);

    Calendar calendar = Calendar.getInstance();

    final int year = calendar.get(Calendar.YEAR);

    if (year - anoNasc >= 18)
      return true;
    else
      return false;
  }

  public boolean isGreatherThan18(String dataNasc, String dataEmp) {
    
    String novaDataNasc = formataDataPadraoBr(dataNasc);

    String novaDataEmp = formataDataPadraoBr(dataEmp);

    String[] array = novaDataNasc.split("/");

    String[] array2 = novaDataEmp.split("/");

    int anoNasc = Integer.parseInt(array[2]);

    int anoEmp = Integer.parseInt(array2[2]);

    Calendar calendar = Calendar.getInstance();

    final int year = calendar.get(Calendar.YEAR);

    if (anoEmp > year)
      return false;
    if (anoNasc > year)
      return false;

    if (anoEmp - anoNasc >= 18)
      return true;
    else
      return false;
  }

  public String formataDataPadraoBr(String data){
    String[] array = data.split("-");

    int ano = Integer.parseInt(array[0]);
    int mes = Integer.parseInt(array[1]);
    int dia = Integer.parseInt(array[2]);

    return dia+"/"+mes+"/"+ano;
  }
}
