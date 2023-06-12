package br.com.web.managedbean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.springframework.http.ResponseEntity;

import br.com.web.controller.ControllerPessoa;
import br.com.web.model.Endereco;
import br.com.web.model.Pessoa;
import br.com.web.service.ServicoConsultaCep;
import br.com.web.service.ServicoIntegracao;
import br.com.web.view.GrowlView;

@ManagedBean(name = "MBPessoa")
public class ManagedBeanPessoa {

	@Inject
	ControllerPessoa controllerPessoa;

	@Inject
	private ServicoIntegracao servicoIntegracao;

	@Inject
	private GrowlView growlView;

	private Pessoa pessoa = new Pessoa();
	private Endereco endereco = new Endereco();

	private String cpfAPI;
	private String nomeAPI;
	private String nascimentoAPI;
	private String integracaoAPI;
	private String inclusaoAPI;
	private String alteracaoAPI;
	private boolean situacaoIntegracaoAPI;

	public void postPessoa() {
		pessoa.setCpf(pessoa.getCpf().replace(".", "").replace("-", ""));
		boolean valid = true;
		
		try {
			if (!controllerPessoa.validaCPF(pessoa.getCpf())) {
				growlView.showError("Erro", "Verifique o CPF informado");
				valid = false;
			}
			
			if (!controllerPessoa.validaNome(pessoa.getNome())) {
				growlView.showError("Erro", "Verifique o nome informado");
				valid = false;
			}
			
			if (!controllerPessoa.validaEmail(pessoa.getEmail())) {
				growlView.showError("Erro", "Verifique o email informado");
				valid = false;
			}
			
			if(pessoa.getNascimento() != "") {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			 	Date data = (Date) sdf.parse(pessoa.getNascimento());
			 	Calendar dataNascimento = Calendar.getInstance();
			 	dataNascimento.setTime(data);
			 	
			 	Calendar dataAtual = Calendar.getInstance();
			 	
			 	if(dataNascimento.after(dataAtual)) {
			 		valid = false;
			 		growlView.showError("Erro", "Verifique a data de nascimento informada");
			 	}
			} else {
				valid = false;
				growlView.showError("Erro", "Verifique a data de nascimento informada");
			}
			
		 	
			
			if (endereco != null && endereco.getNumero() != null && endereco.getCep() != null &&
				endereco.getRua() != "" && endereco.getCidade() != "" && endereco.getEstado() != "") 
				pessoa.setEndereco(endereco);
			else {
				valid = false;
				growlView.showError("Erro", "Verifique o endereço informado");
			}
		} catch (Exception e) {
			valid = false;
			growlView.showError("Erro", "Preencha todos os campos");
		}
		
		if (controllerPessoa.getPessoaByCPF(pessoa.getCpf()) == null && valid) {
			controllerPessoa.postPessoa(pessoa);
			growlView.showInfo("", "Cadastro realizado com sucesso");
		} else if (valid) {
			servicoIntegracao.atualizaIntegracao(pessoa.getCpf(), "Não enviado");
			controllerPessoa.putPessoa(pessoa.getCpf(), pessoa);
			growlView.showInfo("", "Cadastro atualizado com sucesso");
		}
	}

	public List<Pessoa> getPessoas() {
		return controllerPessoa.getAllPessoas();
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public void buscaCep() {
		try {
			ResponseEntity<HashMap> response = ServicoConsultaCep.consultaCep(endereco.getCep());
			endereco.setRua(response.getBody().get("logradouro").toString());
			endereco.setCidade(response.getBody().get("localidade").toString());
			endereco.setEstado(response.getBody().get("uf").toString());
		} catch (Exception e) {
			if (endereco.getCep() == "") growlView.showError("Erro", "Informe um CEP");
			else growlView.showError("Erro", "CEP informado não foi encontrado");
		}
	}

	public void consultaIntegracao() {
		String cpf = this.getCpfAPI().replace(".", "").replace("-", "");
		if(cpf == "") {
			growlView.showError("Erro", "Por favor informe um CPF para ser consultado");
		}
		else {
			try {
				HashMap<?, ?> response = servicoIntegracao.situacaoIntegracao(cpf);
				this.nomeAPI = (String) response.get("nome");
				this.nascimentoAPI = (String) response.get("nascimento");
				
				
				this.inclusaoAPI = formatStringDate((String) response.get("criacaoRegistro"));
				if(response.get("alteracaoRegistro") != null) {
					this.alteracaoAPI = formatStringDate((String) response.get("alteracaoRegistro"));
				}
				
				if(controllerPessoa.getPessoaByCPF(cpf) != null) {
					Pessoa p = controllerPessoa.getPessoaByCPF(cpf);
					this.integracaoAPI = p.getSituacaoIntegracao();
				} else {
					growlView.showInfo("Info", "CPF cadastrado apenas na API");
				}
			} catch (Exception e) {
				growlView.showError("Erro", "Erro na consulta à API de Integração");
			}
		}
	}
	
	public String formatStringDate(String date) {
		String dateString = date.substring(8, 10) + "/" +
							date.substring(5, 7) + "/" +
							date.substring(0, 4) + " " +
							date.substring(11, 19);
		
		return dateString;
	}

	public void editar(Pessoa pessoa) {
		this.pessoa = pessoa;
		this.endereco = pessoa.getEndereco();
	}

	public void reintegrar(Pessoa pessoa) {
		controllerPessoa.reintegrar(pessoa);
	}
	
	public void remover(Pessoa pessoa) {
		controllerPessoa.remover(pessoa);
	}

	public String getCpfAPI() {
		return cpfAPI;
	}

	public void setCpfAPI(String cpfAPI) {
		this.cpfAPI = cpfAPI;
	}

	public String getNomeAPI() {
		return nomeAPI;
	}

	public void setNomeAPI(String nomeAPI) {
		this.nomeAPI = nomeAPI;
	}

	public String getNascimentoAPI() {
		return nascimentoAPI;
	}

	public void setNascimentoAPI(String nascimentoAPI) {
		this.nascimentoAPI = nascimentoAPI;
	}

	public String getIntegracaoAPI() {
		return integracaoAPI;
	}

	public void setIntegracaoAPI(String integracaoAPI) {
		this.integracaoAPI = integracaoAPI;
	}

	public String getInclusaoAPI() {
		return inclusaoAPI;
	}

	public void setInclusaoAPI(String inclusaoAPI) {
		this.inclusaoAPI = inclusaoAPI;
	}

	public String getAlteracaoAPI() {
		return alteracaoAPI;
	}

	public void setAlteracaoAPI(String alteracaoAPI) {
		this.alteracaoAPI = alteracaoAPI;
	}
	
	public boolean getSituacaoIntegracaoAPI() {
		return situacaoIntegracaoAPI;
	}

	public void setSituacaoIntegracaoAPI(Pessoa pessoa) {
		if(pessoa.getSituacaoIntegracao() == "Sucesso") {
			this.situacaoIntegracaoAPI = false;
		} else {
			this.situacaoIntegracaoAPI = true;
		}
	}
}