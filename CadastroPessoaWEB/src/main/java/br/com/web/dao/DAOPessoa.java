package br.com.web.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.web.model.Endereco;
import br.com.web.model.Pessoa;

public class DAOPessoa {
	
    @PersistenceContext(name = "banco-web")
    private EntityManager entityManager;
    
    public List<Pessoa> getAllPessoas() {
    	List<Pessoa> listPessoa = entityManager.createQuery("FROM Pessoa h", Pessoa.class)
    	.setMaxResults(100)
    	.getResultList();
    	
    	return listPessoa;
    }
    
    public Pessoa getPessoaByCPF(String cpf) {
    	try {
    		Pessoa pessoa = entityManager.createQuery("SELECT p FROM Pessoa p WHERE p.cpf LIKE :cpf", Pessoa.class)
        		    .setParameter("cpf", cpf)
        		    .setMaxResults(1)
        		    .getSingleResult();
        	
        	return pessoa;
    	} catch (Exception e) {
    		return null;
    	}    	
    }
    
	public Pessoa postPessoa(Pessoa pessoa) {
		pessoa.setSituacaoIntegracao("Pendente");
		entityManager.persist(pessoa);
    	return pessoa;
    }

	public Pessoa putPessoa(String cpf, Pessoa pessoa) {
		
		Pessoa oldPessoa = entityManager.createQuery("SELECT p FROM Pessoa p WHERE p.cpf LIKE :cpf", Pessoa.class)
    		    .setParameter("cpf", cpf)
    		    .setMaxResults(1)
    		    .getSingleResult();
		
		Endereco end = pessoa.getEndereco();
		Endereco oldEnd = oldPessoa.getEndereco();
		if (oldPessoa != null) {
			if(pessoa.getCpf() != "") oldPessoa.setCpf(pessoa.getCpf());
			if(pessoa.getEmail() != "") oldPessoa.setEmail(pessoa.getEmail());
			if(pessoa.getNascimento() != "") oldPessoa.setNascimento(pessoa.getNascimento());
			if(pessoa.getNome() != "") oldPessoa.setNome(pessoa.getNome());
			if(end.getCep() != "") oldEnd.setCep(end.getCep());
			if(end.getRua() != "") oldEnd.setRua(end.getRua());
			if(end.getNumero() != null) oldEnd.setNumero(end.getNumero());
			if(end.getCidade() != "") oldEnd.setCidade(end.getCidade());
			if(end.getEstado() != "") oldEnd.setEstado(end.getEstado());
			oldPessoa.setEndereco(oldEnd);
		}
		
		pessoa.setSituacaoIntegracao("Não integrado");
		return pessoa;
	}

	public void deletePessoa(String cpf) {
		Pessoa pessoa = entityManager.createQuery("SELECT p FROM Pessoa p WHERE p.cpf LIKE :cpf", Pessoa.class)
    		    .setParameter("cpf", cpf)
    		    .setMaxResults(1)
    		    .getSingleResult();
		if (pessoa != null) {
			entityManager.remove(pessoa);
		}
	}

	public void atualizaIntegracao(String cpf, String situacao) {
		Pessoa pessoa = entityManager.createQuery("SELECT p FROM Pessoa p WHERE p.cpf LIKE :cpf", Pessoa.class)
    		    .setParameter("cpf", cpf)
    		    .setMaxResults(1)
    		    .getSingleResult();
		pessoa.setSituacaoIntegracao(situacao);
	}
}
