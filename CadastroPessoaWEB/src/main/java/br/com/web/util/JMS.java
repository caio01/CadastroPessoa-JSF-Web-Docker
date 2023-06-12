package br.com.web.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import br.com.web.model.Pessoa;

@ApplicationScoped
public class JMS {

    public static final String VALUE_QUEUE = "javax.jms.Queue";

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    private JMSContext jmsContext;

    public void sendMessageQueue(Queue queue, Pessoa pessoa, String metodo) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            ObjectMessage message = jmsContext.createObjectMessage();
            
            message.setStringProperty("metodo", metodo);
            message.setStringProperty("nome", pessoa.getNome());
            message.setStringProperty("nascimento", pessoa.getNascimento());
    		message.setStringProperty("cpf", pessoa.getCpf());
    		message.setStringProperty("email", pessoa.getEmail());
    		message.setStringProperty("cep", pessoa.getEndereco().getCep());
    		message.setStringProperty("rua", pessoa.getEndereco().getRua());
    		message.setStringProperty("numero", pessoa.getEndereco().getNumero().toString());
    		message.setStringProperty("cidade", pessoa.getEndereco().getCidade());
    		message.setStringProperty("estado", pessoa.getEndereco().getEstado());
    		
            producer.send(queue, message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
