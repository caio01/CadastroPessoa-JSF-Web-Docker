package br.com.web.jms;

import java.text.ParseException;
import java.util.HashMap;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import br.com.web.controller.ControllerPessoa;
import br.com.web.model.Endereco;
import br.com.web.model.Pessoa;
import br.com.web.service.ServicoIntegracao;
import br.com.web.util.JMS;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JMS.VALUE_QUEUE),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = QueueConsumer.QUEUE_NAME),
        @ActivationConfigProperty(propertyName = "clientFailureCheckPeriod", propertyValue = "600000"),
        @ActivationConfigProperty(propertyName = "connectionTTL", propertyValue = "-1"),
        @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "5"),
}, mappedName = QueueConsumer.QUEUE_NAME)

public class QueueConsumer implements MessageListener {
	@Inject
	ControllerPessoa controllerPessoa;
	
	@Inject
	ServicoIntegracao servicoIntegracao;

    public static final String QUEUE_NAME = "java:/jms/queue/queueIntegracao";
    
    private static final String url = "http://localhost:8081/pessoa";

    @Override
    public void onMessage(Message message) {
        try {
        	System.out.println("Mensagem na fila -> Método: " + message.getStringProperty("metodo"));
        	postPessoa(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    

    public void postPessoa(Message message) throws Exception {    	
    	if(message.getStringProperty("metodo").equalsIgnoreCase("POST")) {
    		RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Pessoa> request = new HttpEntity<Pessoa>(xformMsgParaPessoa(message));
            ResponseEntity<HashMap> response = restTemplate.exchange(url, HttpMethod.POST, request, HashMap.class);
            
            if(response.getBody().containsKey("erro")) 
            	servicoIntegracao.atualizaIntegracao(message.getStringProperty("cpf"), "Erro");
            
            if(response.getBody().containsKey("criacaoRegistro")) 
            	servicoIntegracao.atualizaIntegracao((String) response.getBody().get("cpf"), "Sucesso");
            
    	} else if (message.getStringProperty("metodo").equalsIgnoreCase("PUT")) {
    		String urlPut = url + "/" + message.getStringProperty("cpf");

    		RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Pessoa> request = new HttpEntity<Pessoa>(xformMsgParaPessoa(message));
            ResponseEntity<HashMap> response = restTemplate.exchange(urlPut, HttpMethod.PUT, request, HashMap.class);
            
            if(response.getBody().containsKey("erro")) 
            	servicoIntegracao.atualizaIntegracao(message.getStringProperty("cpf"), "Erro");
            
            if(response.getBody().containsKey("alteracaoRegistro")) {
            	servicoIntegracao.atualizaIntegracao((String) response.getBody().get("cpf"), "Sucesso");
            }
    	} else if (message.getStringProperty("metodo").equalsIgnoreCase("DELETE")) {
    		String urlDel = url + "/" + message.getStringProperty("cpf");

    		RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Pessoa> request = new HttpEntity<Pessoa>(new Pessoa());
            ResponseEntity<HashMap> response = restTemplate.exchange(urlDel, HttpMethod.DELETE, request, HashMap.class);            
            
            if(response.getBody().containsKey("erro")) 
            	servicoIntegracao.atualizaIntegracao(message.getStringProperty("cpf"), "Erro");
            
            if(response.getBody().containsKey("mensagem")) {
            	controllerPessoa.deletePessoa(message.getStringProperty("cpf"));
            }
    	}
        
    }

	public Pessoa xformMsgParaPessoa(Message message) throws JMSException, ParseException {
		Pessoa pessoa = new Pessoa();
		Endereco endereco = new Endereco();
		
		pessoa.setNome(message.getStringProperty("nome"));
		pessoa.setNascimento(message.getStringProperty("nascimento"));
		
        pessoa.setCpf(message.getStringProperty("cpf"));
        pessoa.setEmail(message.getStringProperty("email"));
        
        endereco.setCep(message.getStringProperty("cep"));
        endereco.setRua(message.getStringProperty("rua"));
        endereco.setNumero(Integer.parseInt(message.getStringProperty("numero")));
        endereco.setCidade(message.getStringProperty("cidade"));
        endereco.setEstado(message.getStringProperty("estado"));
        pessoa.setEndereco(endereco);        
		
		return pessoa;
	}
}
