package br.com.web.service;

import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ServicoConsultaCep {
	public static ResponseEntity<HashMap> consultaCep(String cep) throws Exception {
        if(!validcep(cep)) {
            throw new Exception();
        } else {
            String url = String.format("https://viacep.com.br/ws/%s/json/", cep);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<HashMap> response = restTemplate
                    .exchange(url, HttpMethod.GET, new HttpEntity<>(new String()), HashMap.class);
            if (response.getBody().containsKey("erro")) {
                throw new Exception();
            } else {
            	return response;
            }
        }
    }

    public static boolean validcep(String cep) {
        if (cep.length() == 9 && cep.matches("[0-9]{5}-[0-9]{3}")) {
            return true;
        } else if (cep.length() == 8 && cep.matches("[0-9]{8}")){
            return true;
        }
        return false;
    }
}