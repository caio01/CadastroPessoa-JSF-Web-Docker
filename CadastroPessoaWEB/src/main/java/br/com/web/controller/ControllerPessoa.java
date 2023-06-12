package br.com.web.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Queue;

import br.com.web.dao.DAOPessoa;
import br.com.web.jms.QueueConsumer;
import br.com.web.model.Pessoa;
import br.com.web.service.ServicoIntegracao;
import br.com.web.util.JMS;

@Stateless
public class ControllerPessoa {
	
	@Inject
	private DAOPessoa daoPessoa;

	@Inject
	private JMS jms;

	@Resource(lookup = QueueConsumer.QUEUE_NAME)
	private Queue queueIntegracao;
	
	@Inject
	ServicoIntegracao servicoIntegracao;

	public List<Pessoa> getAllPessoas() {
		return daoPessoa.getAllPessoas();
	}
	
	public Pessoa getPessoaByCPF(String cpf) {
		if(daoPessoa.getPessoaByCPF(cpf) == null) {
			return null;
		} else {
			return daoPessoa.getPessoaByCPF(cpf);
		}
	}
	
	public Pessoa postPessoa(Pessoa pessoa) {
		jms.sendMessageQueue(queueIntegracao, pessoa, "POST");
		return daoPessoa.postPessoa(pessoa);
	}

	public Pessoa putPessoa(String cpf, Pessoa pessoa) {
		jms.sendMessageQueue(queueIntegracao, pessoa, "PUT");
		servicoIntegracao.atualizaIntegracao(cpf, "Pendente");
		return daoPessoa.putPessoa(cpf, pessoa);		
	}

	public void deletePessoa(String cpf) {
		daoPessoa.deletePessoa(cpf);
	}

	public void remover(Pessoa pessoa) {
		jms.sendMessageQueue(queueIntegracao, pessoa, "DELETE");
		servicoIntegracao.atualizaIntegracao(pessoa.getCpf(), "Pendente");
	}

	public boolean validaCPF(String cpf) {
		if(cpf == "") {
			return false;
		}
		
		char[] digitos = cpf.toCharArray();
		int soma = 0;
        int verificador1, verificador2;

        //CALCULA O PRIMEIRO DÍGITO VERIFICADOR
        for(int i = 0; i < 9; i++) {
            soma += (i+1) * Character.digit(digitos[i], 10);
        }
        
		if(soma % 11 == 10) verificador1 = 0;
		else verificador1 = soma % 11;

        //CALCULA O SEGUNDO DÍGITO VERIFICADOR
        soma = 0;
        for(int i = 0; i < 9; i++) {
            soma += i * Character.digit(digitos[i], 10);
        }
        soma += verificador1 * 9;
        
		if(soma % 11 == 10) verificador2 = 0;
		else verificador2 = soma % 11;

		//VALIDA OS DÍGITOS CALCULADOS
        if(verificador1 == Character.digit(digitos[9], 10) && 
           verificador2 == Character.digit(digitos[10], 10)) 
            return true;
        else 
            return false;
	}

	public boolean validaNome(String nome) {
        //VALIDA PELO MENOS UM NOME E SOBRENOME 
        //INICANDO COM MAIUSCULA E DEMAIS MINUSCULAS
        //E APENAS LETRAS. EX: José Oliveira
		return nome.matches("[A-ZÁ-Ú][a-zá-ú]{1,}([ ][A-ZÁ-Ú][a-zá-ú]{1,})+");
    }
	
	public boolean validaEmail(String email) {
		return email.matches("[a-z0-9!#$%&*-_]{1,}[@][a-z0-9]{1,}([.][a-z]{1,}){1,2}");
	}

	public void reintegrar(Pessoa pessoa) {
		jms.sendMessageQueue(queueIntegracao, pessoa, "POST");
	}
}
