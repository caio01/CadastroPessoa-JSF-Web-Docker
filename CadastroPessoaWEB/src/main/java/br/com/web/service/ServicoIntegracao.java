package br.com.web.service;

import java.util.HashMap;

import javax.inject.Inject;

import org.springframework.web.client.RestTemplate;

import br.com.web.dao.DAOPessoa;

public class ServicoIntegracao {
	
	@Inject
	private DAOPessoa daoPessoa;
	
	private static final String url = "http://localhost:8081/pessoa";
	
	public void atualizaIntegracao(String cpf, String situacao) {
		daoPessoa.atualizaIntegracao(cpf, situacao);		
	}

	public HashMap<?, ?> situacaoIntegracao(String cpf) {
		String urlCPF = String.format(url + "/" + cpf);
    	
		return new RestTemplate().getForObject(urlCPF, HashMap.class);
	}

}
