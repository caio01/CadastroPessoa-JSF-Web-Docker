# CadastroPessoa-JSF-Web-Docker

Esse projeto consiste na criação de:

    • Um CRUD de pessoa
    • Consulta em api externa (ViaCep), para os dados do endereço
    • Salvamento na base de dados
    • Envio da pessoa para uma fila interna (JMS)
    • Envio da pessoa para outra API (criada em outro projeto)
    • Criação de um endpoint de consumo de informações da pessoa (pelo CPF)

## Demonstração de Funcionamento
![chrome_fZa1vDXDfm][demo-funcionamento]


## Repositórios

[ Ambiente 1 - Aplicação Web (JSF) ][aplicacao-web] 

[ Servidor WildFly da Aplicação Web ][server-aplicacao-web] 

[ Banco de dados da Aplicação WEB (Docker) ][banco-aplicacao-web] 

Foram implementadas duas aplicações:  
Essa aplicação WEB, usado no cadastro e consulta da pessoa;  
E uma [API][link-api] que apenas disponibilizará endpoints REST para registro das pessoas. 


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
![image][img-processo-integracao]


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
![image][img-post]

### PUT /pessoa/cpf/{cpf}  
![image][img-put]

### GET /pessoa/cpf/{cpf}  
![image][img-get]

### DELETE /pessoa/cpf/{cpf}  
![image][img-delete]


## Banco de Dados
As tabelas do banco de dados são criadas automaticamente.  
As seguintes entidades devem existir:  
![image][img-esquema-banco]  


# Instruções para Execução do Projeto

## Versão JDK
Utilizar a versão mais recente.

## Download da IDE
Baixar e descompactar o Eclipse:

- [Windows][eclipse-windows]
- [Linux][eclipse-linux]
- [Mac][eclipse-mac]

Iniciar o Eclipse escolhendo uma workspace de sua preferência.

*Fique a vontade para usar outras IDE, com o IntelliJ ou VSCode.*

## Download do Servidor de aplicação 
Fazer o download do servidor de aplicação (Wildfly) pré-configurado [aqui][server-aplicacao-web] e extrair em um local apropriado.

## Clone do repositório do projeto

Clonar esse repositório dentro da pasta do workspace utilizado no Eclipse.

## Clone do repositório do banco de dados

Instalar o docker: https://www.docker.com/get-started > Docker Desktop

O banco de dados é criado via docker e o fonte está disponível [aqui][banco-aplicacao-web]

Apos o clone, ir via terminal na pasta extraida e executar o banco pelo docker: `docker-compose up -d`

OBS: Se atentar que não pode haver nenhum servidor postgres rodando na máquina, se tiver, necessário fechar todos os serviços, assim como
qualquer outro serviço que esteja utilizando a porta 5432 (ou trocar a porta no arquivo docker-compose.yaml da aplicação).  
Para verificar se o docker subiu corretamente, realizar a conexão com o banco:
- host: localhost
- port: 5432
- database: ist
- user: ist
- password: ist

## Configuração da IDE

### Eclipse
- Ir em "File > Import..." e escolher a opção conforme segue:

  ![image][inst-1]

- Selecionar a pasta do projeto e confirmar como segue:

  ![image][inst-2]

- Em *Select root repository* escolher a pasta do projeto e clicar em *Finish*.
- Ir no menu "Window > Preferences" e Adicionar um novo *Runtime Environment* conforme segue:

  ![image][inst-3]

  - Nesse momento será feito o download dos arquivos necessários para que o *Ecplise* consiga usar o servidor de aplicação Wildfly.
  - A instalação será feita em segundo plano (verificar barra de status).
  - Confirmar, caso haja, alguma tela de confirmação e reiniciar o *Eclipse ao final do processo*.

- Na aba "Servers" clicar em *No server are available. Click this link to create a new server...* como segue:

  ![image][inst-4]

- Na etapa de criação do servidor, selecionar a pasta do servidor baixado anteriormente. Como por exemplo:

  ![image][inst-5]

- Na próxima etapa, adicionar o projeto ficando dessa forma:

  ![image][inst-6]

- Finalizar em *Finish*.

- Subir o servidor em modo *Debug* no botão à seguir:

  ![image][inst-7]

- No navegador digitar http://localhost:8080/cadastro/, irá mostrar a página.

  ![image][inst-8]


### IntelliJ

- Ir em File -> New -> Project from Existing Sources...

- Selecionar o arquivo pom.xml, na raiz do projeto

- Adicionar uma nova configuração

  ![image][inst-9]

- Selecionar a pasta do servidor

  ![image][inst-10]

  *Note que essa configuração pode conflitar com o repositorio API. Assim, basta mudar o nome do Server.*

  *O caminho do servidor muda de acordo com o local baixado, e nome da pasta. Nesse caso, deveria ser ..../wildfly-19.1.0.Final-ProvaJavaPleno-Web*

- Adicionar o artefato

  ![image][inst-11]

- Selecionar Exploded

  ![image][inst-12]

- Executar o servidor

  ![image][inst-13]

- No navegador digitar http://localhost:8080/cadastro/, irá mostrar página.

  ![image][inst-8]



[demo-funcionamento]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/e220c0ae-9824-46d7-a929-2d48bd36af9a
[aplicacao-web]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/tree/main/CadastroPessoaWEB
[server-aplicacao-web]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/tree/main/wildfly-19.1-WEB
[banco-aplicacao-web]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/tree/main/banco-web
[link-api]: https://github.com/caio01/CadastroPessoa-JSF-API-Docker
[img-processo-integracao]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/9596f8ca-bfde-4bf0-98d6-2cb0c7cf3fd5

[img-post]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/736794da-1ab7-482d-8f29-c77962a968a5
[img-put]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/f3a9b4ee-af3e-4a02-b3ef-cf717f25c6f1
[img-get]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/27b12596-ff02-4d72-aa09-fb28e4d5663e
[img-delete]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/b6e757ac-0ee4-4092-8b6b-2f3346eff873
[img-esquema-banco]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/e4fab17e-1a59-45de-85ad-7a391622023b

[eclipse-windows]: https://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/2020-03/R/eclipse-jee-2020-03-R-incubation-win32-x86_64.zip
[eclipse-linux]: https://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/2020-03/R/eclipse-jee-2020-03-R-incubation-linux-gtk-x86_64.tar.gz
[eclipse-mac]: https://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/2020-03/R/eclipse-jee-2020-03-R-incubation-macosx-cocoa-x86_64.dmg

[inst-1]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/fb536041-faf4-4e90-98d3-3d6b4dcefa2c
[inst-2]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/8bb8fcef-db13-4a05-be9e-551a3fd650c9
[inst-3]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/4cdaf159-c8ba-4246-900e-dafa75dbd05d
[inst-4]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/ebda7813-116f-42f7-a68c-307eaa528e86
[inst-5]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/a2c2cfd7-1dee-453d-ae29-f4115c09b360
[inst-6]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/fc9a17e2-5150-4a3c-ad08-57230aab7c17
[inst-7]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/8477701b-7c9a-41aa-8b25-64fdcb4dc627
[inst-8]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/71a7ad37-e1e2-4932-a67b-b6234b616989
[inst-9]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/aeca69dc-65c2-4e28-8c45-bd6189a9567e
[inst-10]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/b760067c-b621-49bd-b06f-61728fa92182
[inst-11]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/ae26c309-4c48-468b-ae3a-94bf4d649e6a
[inst-12]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/de59049b-debd-4de4-8115-a7ddbda2ad51
[inst-13]: https://github.com/caio01/CadastroPessoa-JSF-Web-Docker/assets/49879702/1d2354f9-5486-4aa0-8a5a-bdf402d59dd8





#### Créditos pelo Desenvolvimento do case: Senai SC / FIESC
