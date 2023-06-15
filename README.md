# CadastroPessoa-JSF-Web-Docker

Esse projeto consiste na criação de:

    • Um CRUD de pessoa
    • Consulta em api externa (ViaCep), para os dados do endereço
    • Salvamento na base de dados
    • Envio da pessoa para uma fila interna (JMS)
    • Envio da pessoa para outra API (criada em outro projeto)
    • Criação de um endpoint de consumo de informações da pessoa (pelo CPF)

## Demonstração de Funcionamento
![chrome_fZa1vDXDfm](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/ff8a8321-842c-4cd1-be5f-c2c4c912338e)


## Repositórios

[ Ambiente 1 - Aplicação Web (JSF) ](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/tree/main/CadastroPessoaWEB) 

[ Servidor WildFly da Aplicação Web ](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/tree/main/wildfly-19.1-WEB) 

[ Banco de dados da Aplicação WEB (Docker) ](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/tree/main/banco-web) 

Foram implementadas duas aplicações:  
Essa aplicação WEB, usado no cadastro e consulta da pessoa;  
E uma [API](https://github.com/caio01/CadastroPessoa-JSF-API-Docker) que apenas disponibilizará endpoints REST para registro das pessoas. 


## Regras de Negócio

A aplicação web terá apenas uma tela, contendo todas as seguintes funcionalidades:

    1. Uma área de cadastro, e busca de CEP
    2. Uma lista das pessoas cadastradas
    3. Uma área para a consulta da integração da pessoa

### Cadastro de pessoa

- R1 - Todos os campos são obrigatórios 
  - R1.2 - Validar o nome com as regras:
	- Ter mais de 1 nome
	- A primeira letra de cada nome deve ser maiúscula, e as demais minúsculas
- R2 - A data de nascimento não pode ser maior que o dia atual
- R3 - O CPF deve ser válido, deve usar máscara no campo e salvar apenas os números no banco de dados
- R4 - O e-mail deve ser válido (Exemplo email@teste.com)
- R5 - O CEP do Endereço deve consultar o serviço https://viacep.com.br/ e atualizar os campos conforme o retorno
	- R5.1 - Se não encontrado, o usuário pode cadastrar qualquer CEP (Exemplo 00000-000)
	- R5.2 - Campo número, deve aceitar apenas número
- R6 - Salvar a pessoa
	- R6.1 - O botão de salvar vai criar ou atualizar os dados da pessoa
	- R6.2 - Validar se o CPF já está cadastrado, atualizando as informações, em caso positivo
	- R6.3 - Integrar a pessoa com a API
		- R6.3.1 - A pessoa só pode ser enviada para a API se todos os campos forem preenchidos e foi possível cadastrar a pessoa

### Listagem de pessoas
	
- R7 - A tabela deve listar todas as pessoas cadastradas no banco de dados da aplicação Web
- R8 - A coluna nascimento deve ser no formato dd/mm/yyyy
- R9 - A coluna CPF deve ter máscara, no formato 000.000.000-00
- R10 - A coluna cidade deve informar também o estado, no formato Cidade/Estado
- R11 - A situação da integração deve consultar no banco de dados
	- R11.1 - Será “Não enviado”, se a pessoa não foi enviada para a API
	- R11.2 - Será “Pendente”, se foi integrado e ainda não teve resposta da API
	- R11.3 - Será “Sucesso” se a API retornou com sucesso
	- R11.4 - Será “Erro” se der erro no envio (por problema no request) ou no retorno (por alguma validação da API)
- R11 - Botões de Ação
	- R11.1 - Editar, vai carregar os dados da pessoa no formulário
	- R11.2 - Integrar, vai enviar a pessoa para a integração
		- R11.2.1 - Só vai mostrar esse botão se a situação for diferente de Sucesso
	- R11.3 - Remover, vai remover a pessoa da API, via requisição REST, e do Banco de dados da aplicação Web
		- R11.3.1 - Se der erro na remoção da API, não deve ser removido da base de dados

### Integração da pessoa

A integração deve ser feita com REST, entre a aplicação WEB (cadastro de pessoa) e a API.  
As seguintes regras devem ser desenvolvidas:

- R12 - A integração deve ocorrer ao salvar a pessoa no cadastro de pessoa apenas se:
	- R12.1 - Todos os campos forem preenchidos
	- R12.2 - O salvamento da pessoa ocorreu com sucesso
- R13 - A integração deve ser feita numa fila interna (JMS)
	- R13.1 - A fila deve ser implementada na aplicação WEB
	- R13.2 - A fila deve enviar a pessoa para a API
- R13.3 - A fila deve atualizar a situação da integração (erro ou sucesso) e mensagem, ou objeto da pessoa, retornado pela API
- R14 - A API vai validar se todos campos estão preenchidos
	- R14.1 - Se algum campo não for preenchido, deve retornar com erro
- R15 - A API deve ter um endpoint para consulta da pessoa (sugestão GET /pessoa/cpf/{cpf})
- R16 - A API deve ter um endpoint para a remoção da pessoa (sugestão DELETE /pessoa/cpf/{cpf}), que vai ser usado pela aplicação WEB

### Consultar pessoa integrada
Na aplicação WEB deve ter a possibilidade de se consultar uma pessoa integrada, retornando dados da API

- R17 - Consultar os dados da pessoa pelo CPF 
- R18 - Mostrar os dados da integração na tela, incluindo a data da primeira integração e ultima alteração

### Processo de integração
O processo de integração pode ser avaliado no seguinte diagrama de sequência:  
![image](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/000ed0ab-3253-4efc-80b6-ddbff266d054)


Seguem as etapas do processo de integração:

Processo 1 - Cadastro/Alteração de Pessoa

    1. No formulario de pessoa, o usuario preenche os dados
    2. O usuario consulta os dados do endereço pelo cep na API do site https://viacep.com.br/
    3. O usuario confirma o cadastro no botão “Salvar”
    4. O sistema registra o cliente no banco de dados
    5. O sistema envia a pessoa para a fila interna, enquanto devolve a confirmação para o usuario.
    6. A fila envia a pessoa para a API desenvolvida pelo candidato
    7. A API valida os dados da pessoa (todos campos devem ser obrigatórios)
    8. A API salva no banco
    9. A API retorna a resposta para a fila da aplicação Web com a situação de erro ou sucesso
    10. A fila registra no banco de dados a situação da integração

Processo 2 - Consulta de Pessoa

    1. O usuario, na aplicação WEB, vai informar o CPF da pessoa
    2. A aplicação WEB vai consultar na API, pelo CPF, os dados da pessoa

Processo 3 - Remoção de Pessoa

    1. O usuario remove a pessoa na lista de pessoas
    2. A Aplicação WEB envia uma requisição de DELETE para API indicando a remoção
    3. A API retorna se foi removido
    4. Se foi removido da API, a aplicação WEB deve remover da base de dados
    5. O usuario é informado da remoção


## API
A API a ser desenvolvida deve conter alguns serviços REST que serão consumidos pela aplicação WEB.
Seguem os serviços que devem ser criados:

### POST /pessoa  
![image](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/19d058ca-6121-4993-8740-94e3fd5aa687)

### PUT /pessoa/cpf/{cpf}  
![image](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/684b7167-19c3-43fa-984a-47fd90b2ab68)

### GET /pessoa/cpf/{cpf}  
![image](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/e921fd8e-fe76-4caa-815a-4850d77a7417)

### DELETE /pessoa/cpf/{cpf}  
![image](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/96581638-d3f0-410f-b3cb-20701a92b92a)


## Banco de Dados
As tabelas do banco de dados são criadas automaticamente.  
As seguintes entidades devem existir:  
![image](https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/31099735-4c3c-4806-854f-f87350770252)  



#### Créditos pelo Desenvolvimento do case: Senai/FIESC
