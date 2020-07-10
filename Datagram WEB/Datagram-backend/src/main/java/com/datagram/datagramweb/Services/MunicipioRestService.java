package com.datagram.datagramweb.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.springframework.stereotype.Service;

@Service
public class MunicipioRestService implements Serializable {

	private static final long serialVersionUID = 1L;

	private String URL_WS = "http://ibge.herokuapp.com/municipio/?val=";

	public ArrayList<String> listarMunicipios(String Uf) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		StringBuffer stringBuffer = null;
		ArrayList<String> MunicipioList = new ArrayList<String>();

		try {
			URL url = new URL(URL_WS.concat(Uf));// Cast de String para URL
			HttpURLConnection conexao = (HttpURLConnection) url.openConnection();// Cria a requisicao //O tipo
																																						// HttpUrlConnection permite que recuperemos
																																						// os

			inputStream = conexao.getInputStream();// Recuperando os dados devolvidos pelo servidor em bytes[]

			inputStreamReader = new InputStreamReader(inputStream);// Le os dados em bytes e decodifica para caracteres

			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);// Leitura dos caracteres decodificados pelo
																																						// InputStreamReader

			String linha = "";

			stringBuffer = new StringBuffer();

			while ((linha = bufferedReader.readLine()) != null) {// readLine lerá todas as linhas do bufferedReader, quando
																														// acabar retorna null
				stringBuffer.append(linha);
			}

			Map<String, String> retMap = new Gson().fromJson(stringBuffer.toString(),
					new TypeToken<HashMap<String, String>>() {
					}.getType());

			for (String sigla_key : retMap.keySet()) {
				// String codigo_value = retMap.get(sigla_key);
				// Adiciona o municipio a um list
				MunicipioList.add(sigla_key);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// return ufList;
		Collections.sort(MunicipioList);
		return MunicipioList;
	}

}