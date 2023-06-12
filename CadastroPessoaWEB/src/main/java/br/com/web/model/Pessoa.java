package br.com.web.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table
public class Pessoa {
	
	@Id
    @GeneratedValue
    private Integer idPessoa;

    private String nome;
    private String nascimento;
    private String cpf;
    private String email;
    private String situacaoIntegracao;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fidEndereco", referencedColumnName = "idEndereco")
    private Endereco endereco;

	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNascimento() {
		return nascimento;
	}

	public void setNascimento(String nascimento) {
		this.nascimento = nascimento;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSituacaoIntegracao() {
		return situacaoIntegracao;
	}

	public void setSituacaoIntegracao(String situacaoIntegracao) {
		this.situacaoIntegracao = situacaoIntegracao;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	@Override
	public String toString() {
		return "Pessoa [idPessoa=" + idPessoa + ", nome=" + nome + ", nascimento=" + nascimento.toString() + ", cpf=" + cpf
				+ ", email=" + email + ", situacaoIntegracao=" + situacaoIntegracao + ", endereco=" + endereco + "]";
	}
}
