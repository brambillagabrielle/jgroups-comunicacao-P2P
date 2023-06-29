package br.edu.ifsul.cc.sdi.comunicacaop2p;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import java.io.*;
import br.edu.ifsul.cc.sdi.comunicacaop2p.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.jgroups.Address;
import org.jgroups.View;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe Peer representa os peers que se comunicam para no Sistema de Arquivos
 * P2P
 *
 * @author gabrielle
 */
public class Peer extends ReceiverAdapter {

    private JChannel channel;
    private View view;
    private Mensagem mensagem;
    private boolean autenticado;
    private boolean terminou;

    // lista que contém os arquivos locais do peer, com nome e o conteúdo do arquivo
    private HashMap<String, String> arquivosLocais;

    // lista que contém os arquivos globais do cluster de peers, com nome e endereço
    // do peer dono do arquivo
    private HashMap<String, Address> arquivosGlobais;

    // lista de usuários utilizado pelo coordenador para autenticação do novo peer
    private final ArrayList<Usuario> usuarios = new ArrayList<>();

    /**
     * Função start de Thread que inicia o canal de comunicação dos peers,
     * colocando-os no cluster "Sistema_Arquivos_P2P"
     */
    private void start() throws Exception {

        arquivosLocais = new HashMap<>();
        arquivosGlobais = new HashMap<>();

        channel = new JChannel().setReceiver(this);
        channel.connect("Sistema_Arquivos_P2P");

        enviaMensagem();

        channel.close();

    }

    /**
     * Função que atualiza o View da comunicação, no caso de entrada ou saída de
     * peers da comunicação
     *
     * @param novaView
     */
    @Override
    public void viewAccepted(View novaView) {
        this.view = novaView;
    }

    /**
     * Função que interage com o usuaŕio e que é responsável pelo envio de
     * mensagem para os outros peers. Para o peer entrar na comunicação
     * oficialmente e poder manipular os arquivos, precisa antes se autenticar
     * com o coordenador ou criar um novo cadastro
     */
    private void enviaMensagem() {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String entrada, usuario, senha, nomeArquivo, conteudo;
        boolean sair = false;
        autenticado = false;

        while (!sair) {

            try {

                // entra na área de login para peers não autenticados apenas quando
                // o novo peer não é o coordenador, pois o coordenador que vai
                // autenticar os novos peers
                if (!autenticado && !this.channel.address().equals(view.getCoord())) {

                    System.out.println("\n  _____OPÇÕES_______");
                    System.out.println(" |                  |");
                    System.out.println(" | 1 - LOGIN        |");
                    System.out.println(" | 2 - CADASTRAR    |");
                    System.out.println(" | 3 - SAIR         |");
                    System.out.println(" |__________________|\n");

                    System.out.println("Que ação deseja realizar?");
                    System.out.print(" > ");
                    entrada = in.readLine().toUpperCase();

                    switch (entrada) {

                        case "1":
                        case "LOGIN":

                            System.out.println("\nLOGIN:\n");

                            System.out.println("Digite o seu usuário:");
                            System.out.print(" > ");
                            usuario = in.readLine();

                            System.out.println("Digite a sua senha:");
                            System.out.print(" > ");
                            senha = in.readLine();

                            mensagem = new Mensagem("AUTENTICAR_PEER");
                            mensagem.setParam("usuario", usuario);
                            mensagem.setParam("senha", senha);

                            channel.send(null, mensagem);

                            terminou = false;

                            System.out.println("\nEsperando confirmação do coordenador...");
                            while (true) {

                                if (terminou) {
                                    break;
                                }

                            }

                            if (autenticado) {

                                entrandoReceberArquivos();
                                System.out.println("\nLogin realizado com sucesso!");

                            } else {
                                System.out.println("\nErro na autenticação: usuário ou"
                                        + " senha inválidos!");
                            }

                            break;

                        case "2":
                        case "CADASTRAR":

                            System.out.println("\nCADASTRAR NOVO USUÁRIO:\n");

                            System.out.println("Digite o novo usuário:");
                            System.out.print(" > ");
                            usuario = in.readLine();

                            System.out.println("Digite a nova senha: ");
                            System.out.print(" > ");
                            senha = in.readLine();

                            mensagem = new Mensagem("CADASTRAR_PEER");
                            mensagem.setParam("usuario", usuario);
                            mensagem.setParam("senha", senha);

                            channel.send(null, mensagem);

                            terminou = false;

                            while (true) {

                                if (terminou) {
                                    break;
                                }

                            }

                            break;

                        case "3":
                        case "SAIR":

                            sair = true;
                            break;

                        default:
                            System.out.println("\nOpção inválida!");

                    }

                } else {

                    System.out.println("\n* SISTEMA DE ARQUIVOS P2P *");
                    System.out.println("\n ____________OPÇÕES____________");
                    System.out.println(" |                              |");
                    System.out.println(" | 1 - MOSTRAR_PEERS            |");
                    System.out.println(" | 2 - ADICIONAR_ARQUIVO        |");
                    System.out.println(" | 3 - LISTAR_MEUS_ARQUIVOS     |");
                    System.out.println(" | 4 - LISTAR_ARQUIVOS_GLOBAIS  |");
                    System.out.println(" | 5 - DELETAR_ARQUIVO          |");
                    System.out.println(" | 6 - VISUALIZAR_CONTEUDO      |");
                    System.out.println(" | 7 - SAIR                     |");
                    System.out.println(" |______________________________|\n");

                    System.out.println("Que ação deseja realizar?");
                    System.out.print(" > ");
                    entrada = in.readLine().toUpperCase();

                    switch (entrada) {

                        case "1":
                        case "MOSTRAR_PEERS":

                            System.out.println("\nPEERS: ");
                            System.out.println(view);

                            break;

                        case "2":
                        case "ADICIONAR_ARQUIVO":

                            System.out.println("\nADICIONAR ARQUIVO:\n");

                            System.out.println("Insira o nome para o novo arquivo: ");
                            System.out.print(" > ");
                            nomeArquivo = in.readLine().toLowerCase();

                            System.out.println("Insira o conteúdo para o novo arquivo: ");
                            System.out.print(" > ");
                            conteudo = in.readLine().toLowerCase();

                            // caso o arquivo não exista na lista de arquivos globais,
                            // procede em adicionar o arquivo nas listas do peer que
                            // está adicionando-o e enviando o aviso do novo arquivo
                            // para outros peers atualizarem suas listas
                            if (arquivosGlobais.get(nomeArquivo) == null) {

                                adicionarArquivo(nomeArquivo, conteudo);
                                System.out.println("\nArquivo adicionado com sucesso!");

                            } else {
                                System.out.println("\nArquivo já existe! Dono: " + 
                                        arquivosGlobais.get(nomeArquivo));
                            }

                            break;

                        case "3":
                        case "LISTAR_MEUS_ARQUIVOS":

                            System.out.println("\nMEUS ARQUIVOS:\n");

                            if (!arquivosLocais.isEmpty()) {
                                System.out.println(listaArquivosLocais());
                            } else {
                                System.out.println("Nenhum arquivo para mostrar!");
                            }

                            break;

                        case "4":
                        case "LISTAR_ARQUIVOS_GLOBAIS":

                            System.out.println("\nARQUIVOS DE TODOS OS PEERS:\n");

                            if (!arquivosGlobais.isEmpty()) {
                                System.out.println(listaArquivosGlobais());
                            } else {
                                System.out.println("Nenhum arquivo para mostrar!");
                            }

                            break;

                        case "5":
                        case "DELETAR_ARQUIVO":

                            System.out.println("DELETAR ARQUIVO: \n");

                            System.out.print("Insira o nome do arquivo para deletar:");
                            System.out.print(" > ");
                            nomeArquivo = in.readLine().toLowerCase();

                            // verifica se o arquivo que está querendo ser deletado
                            // está na lista de arquivos locais do peer, se sim pode
                            // ser excluído e será enviado um aviso para os outros
                            // peers atualizarem suas listas
                            if (arquivosLocais.get(nomeArquivo) != null) {

                                deletarArquivo(nomeArquivo);
                                System.out.println("\nArquivo deletado!");

                            } else {
                                System.out.println("\nArquivo não pode ser deletado pois"
                                        + " não existe localmente!");
                            }

                            break;

                        case "6":
                        case "VISUALIZAR_CONTEUDO":

                            System.out.println("\nVISUALIZAR CONTEÚDO DE UM ARQUIVO: \n");

                            System.out.print("Insira o nome do arquivo para visualizar:");
                            System.out.print(" > ");
                            nomeArquivo = in.readLine().toLowerCase();

                            // verifica primeiro a lista local dos arquivos do peer
                            // e caso não esteja nessa lista envia uma solicitação
                            // do conteúdo para o peer que é dono do arquivo
                            if (arquivosLocais.get(nomeArquivo) != null) {
                                System.out.println("\nConteúdo: " + arquivosLocais.get(
                                        nomeArquivo));
                            } else {

                                if (arquivosGlobais.get(nomeArquivo) != null) {
                                    visualizaConteudoArquivo(nomeArquivo);
                                } else {
                                    System.out.println("\nArquivo não foi encontrado"
                                            + " em nenhum dos peers!");
                                }

                            }

                            break;

                        case "7":
                        case "SAIR":

                            if (!arquivosLocais.isEmpty()) {
                                saindoDeletarArquivos();
                            }

                            sair = true;

                            break;

                        default:
                            System.out.println("\nOpção inválida!");

                    }

                }

            } catch (Exception e) {

                System.out.println("\nErro na entrada de dados! ");
                System.out.println("ERRO: " + e);

            }

        }

    }

    /**
     * Função que envia uma requisição para envio da lista de arquivos globais
     * dos outros peers para o novo peer
     */
    public void entrandoReceberArquivos() {

        try {

            mensagem = new Mensagem("NOVO_PEER");
            channel.send(null, mensagem);

        } catch (Exception e) {

            System.out.println("\nErro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }

    /**
     * Função que envia um aviso para um novo arquivo adicionado por um peer
     * para os outros peers, adicionado-o na lista de arquivos locais e globais
     * caso o nome do arquivo não seja repetido
     *
     * @param nome
     * @param conteudo
     */
    public void adicionarArquivo(String nome, String conteudo) {

        try {

            arquivosLocais.put(nome, conteudo);
            arquivosGlobais.put(nome, this.channel.address());

            mensagem = new Mensagem("ARQUIVO_ADICIONADO");
            mensagem.setParam("nome_arquivo", nome);

            channel.send(null, mensagem);

        } catch (Exception e) {

            System.out.println("\nErro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }

    /**
     * Função para visualizar os arquivos locais do peer, mostrando o nome do
     * arquivo e conteúdo
     *
     * @return
     */
    public String listaArquivosLocais() {

        String lista = "";

        for (String nome : arquivosLocais.keySet()) {

            lista += "Nome: " + nome;
            lista += "\nConteúdo: " + arquivosLocais.get(nome);
            lista += "\n";

        }

        return lista;

    }

    /**
     * Função para mostrar os arquivos globais de todos os peers do cluster,
     * mostrando o nome do arquivo e quem é o dono
     *
     * @return
     */
    public String listaArquivosGlobais() {

        String lista = "";

        for (String nome : arquivosGlobais.keySet()) {

            lista += "Nome: " + nome;
            lista += "\nDono: " + arquivosGlobais.get(nome);
            lista += "\n";

        }

        return lista;

    }

    /**
     * Função para enviar um aviso para todos os peers do cluster que um arquivo
     * foi deletado por um peer, e deve ser atualizado nas listas de arquivos
     * globais
     *
     * @param nome
     */
    public void deletarArquivo(String nome) {

        try {

            arquivosLocais.remove(nome);
            arquivosGlobais.remove(nome);

            mensagem = new Mensagem("ARQUIVO_DELETADO");
            mensagem.setParam("nome_arquivo", nome);

            channel.send(null, mensagem);

        } catch (Exception e) {

            System.out.println("\nErro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }

    /**
     * Função para mostrar o conteúdo de um arquivo. No caso do arquivo não
     * estar presente na lista local, vai fazer uma requisição para o peer que o
     * contém através dos endereços da lista global de arquivos
     *
     * @param nome
     */
    public void visualizaConteudoArquivo(String nome) {

        try {

            mensagem = new Mensagem("SOLICITA_CONTEUDO_ARQUIVO");
            mensagem.setParam("nome_arquivo", nome);
            channel.send(null, mensagem);

        } catch (Exception e) {

            System.out.println("\nErro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }

    /**
     * Função para avisar os peers do cluster que um peer está saindo, fazendo
     * com que deletem o arquivo desse peer nas suas respectivas listas globais
     * de arquivos
     */
    public void saindoDeletarArquivos() {

        try {

            mensagem = new Mensagem("PEER_SAINDO");
            channel.send(null, mensagem);

        } catch (Exception e) {

            System.out.println("\nErro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }

    /**
     * Função que recebe mensagens e respostas dos peers dentro do cluster,
     * tratando as mensagens através das operações recebidas no protocolo
     *
     * @param m
     */
    @Override
    public void receive(Message m) {

        mensagem = (Mensagem) m.getObject();
        String nomeArquivo, usuario, senha;

        try {

            if (!m.getSrc().equals(this.channel.address())) {

                switch (mensagem.getOperacao()) {

                    case "AUTENTICAR_PEER":

                        // peer coordenador recebe a solicatação para autentificação,
                        // passando a verificar se as informações fornecidas
                        // autenticam o peer que solicitou o login
                        if (this.channel.address().equals(view.getCoord())) {

                            usuario = mensagem.getParam("usuario");
                            senha = mensagem.getParam("senha");

                            mensagem = new Mensagem("RESPOSTA_" + mensagem.getOperacao());
                            mensagem.setStatus(Status.ERROR);

                            if (!usuarios.isEmpty()) {

                                for (Usuario u : usuarios) {

                                    if (u.getUsuario().equals(usuario) && 
                                            u.getSenha().equals(senha)) {
                                        mensagem.setStatus(Status.OK);
                                    }

                                }

                            }

                            channel.send(m.getSrc(), mensagem);

                        }

                        break;

                    case "RESPOSTA_AUTENTICAR_PEER":

                        // resposta recebida pelo peer confirmando se pode se autenticar
                        if (mensagem.getStatus() == Status.OK) {
                            autenticado = true;
                        }

                        terminou = true;

                        break;

                    case "CADASTRAR_PEER":

                        // peer coordenador recebe a solicitação e tenta cadastrar
                        // o novo peer com os dados informados, caso o usuário não exista 
                        // entre os usuários que já foram cadastrados
                        if (this.channel.address().equals(view.getCoord())) {

                            usuario = mensagem.getParam("usuario");
                            senha = mensagem.getParam("senha");

                            mensagem = new Mensagem("RESPOSTA_" + mensagem.getOperacao());
                            mensagem.setStatus(Status.OK);

                            for (Usuario u : usuarios) {

                                if (u.getUsuario().equals(usuario)) {
                                    mensagem.setStatus(Status.ERROR);
                                }

                            }

                            if (mensagem.getStatus().equals(Status.OK)) {
                                usuarios.add(new Usuario(usuario, senha));
                            }

                            channel.send(m.getSrc(), mensagem);

                        }

                        break;

                    case "RESPOSTA_CADASTRAR_PEER":

                        // resposta recebida pelo peer confirmando se pode se cadastrar
                        if (mensagem.getStatus().equals(Status.OK)) {
                            System.out.println("Cadastro realizado com sucesso!");
                        } else {
                            System.out.println("Erro ao cadastrar: usuário já existe!");
                        }

                        terminou = true;

                        break;

                    case "NOVO_PEER":

                        // peer coordenador recebe a requisição da lista de arquivos
                        // globais para o novo peer
                        if (this.channel.address().equals(view.getCoord()) && 
                                !arquivosGlobais.isEmpty()) {

                            mensagem = new Mensagem("RESPOSTA_" + mensagem.getOperacao());
                            mensagem.setStatus(Status.OK);

                            String listaPeers = "";

                            for (String nome : arquivosGlobais.keySet()) {
                                listaPeers += nome + "=" + arquivosGlobais.get(nome) + ";";
                            }

                            mensagem.setParam("lista_arquivos", listaPeers);

                            channel.send(m.getSrc(), mensagem);

                        }

                        break;

                    case "RESPOSTA_NOVO_PEER":

                        // resposta recebida pelo peer com a lista de arquivos globais
                        String listaArquivos[] = mensagem.getParam(
                                "lista_arquivos").split(";");

                        for (String a : listaArquivos) {

                            String dadosArquivo[] = a.split("=");
                            arquivosGlobais.put(dadosArquivo[0], 
                                    stringToAddress(dadosArquivo[1]));

                        }

                        break;

                    case "ARQUIVO_ADICIONADO":

                        // mensagem que informa todos os peers que um novo arquivo
                        // foi adicionado e atualiza a sua lista de arquivos globais
                        nomeArquivo = mensagem.getParam("nome_arquivo");
                        arquivosGlobais.put(nomeArquivo, m.getSrc());

                        break;

                    case "ARQUIVO_DELETADO":

                        // mensagem que informa todos os peers que um arquivo foi
                        // deletado e atualiza a sua lista de arquivos globais
                        nomeArquivo = mensagem.getParam("nome_arquivo");
                        arquivosGlobais.remove(nomeArquivo);

                        break;

                    case "SOLICITA_CONTEUDO_ARQUIVO":

                        // solicitação para um peer enviar o conteúdo de um dos seus
                        // arquivos para outro peer 
                        nomeArquivo = mensagem.getParam("nome_arquivo");

                        mensagem = new Mensagem("RESPOSTA_" + mensagem.getOperacao());
                        mensagem.setStatus(Status.OK);
                        mensagem.setParam("conteudo_arquivo", 
                                arquivosLocais.get(nomeArquivo));
                        channel.send(m.getSrc(), mensagem);

                        break;

                    case "RESPOSTA_SOLICITA_CONTEUDO_ARQUIVO":

                        // resposta recebida pelo peer que requisitou conteúdo de um
                        // arquivo
                        System.out.println("\nConteúdo: " + mensagem.getParam(
                                "conteudo_arquivo"));

                        break;

                    case "PEER_SAINDO":

                        // mensagem que informa todos os peers que um peer está
                        // saindo da comunicação, atualizando a lista de arquivos
                        // globais para não conter os arquivos que o peer saindo
                        // era dono  
                        
                        HashMap<String, Address> novoArquivosGlobais = new HashMap<>();

                        for (String nome : arquivosGlobais.keySet()) {

                            if (!arquivosGlobais.get(nome).equals(m.getSrc())) {
                                novoArquivosGlobais.put(nome, 
                                        arquivosGlobais.get(nome));
                            }

                        }

                        arquivosGlobais.clear();
                        arquivosGlobais.putAll(novoArquivosGlobais);

                        break;

                }

            }

        } catch (Exception e) {

            System.out.println("Erro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }

    /**
     * Função auxiliar para transformar um endereço de um peer em formato de
     * String e retornar o objeto endereço correspondente
     *
     * @param stringAddress
     * @return
     */
    public Address stringToAddress(String stringAddress) {

        for (Address a : view.getMembersRaw()) {

            if (stringAddress.equals(a.toString())) {
                return a;
            }

        }

        return null;

    }

    /**
     * Função main para execução do processo do peer. O Logger foi utilizado
     * para evitar mensagens do JGroups
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Logger jgroupsLogger = Logger.getLogger("org.jgroups");
        jgroupsLogger.setLevel(Level.SEVERE);

        new Peer().start();

    }

}
