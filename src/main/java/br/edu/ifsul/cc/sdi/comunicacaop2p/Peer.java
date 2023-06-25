package br.edu.ifsul.cc.sdi.comunicacaop2p;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import java.io.*;
import br.edu.ifsul.cc.sdi.comunicacaop2p.util.*;
import java.util.HashMap;
import org.jgroups.Address;
import org.jgroups.View;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer extends ReceiverAdapter {

    private JChannel channel;
    private View view;

    private HashMap<String, String> arquivosLocais;
    private HashMap<String, Address> arquivosGlobais;

    private void start() throws Exception {
        
        arquivosLocais = new HashMap<>();
        arquivosGlobais = new HashMap<>();

        channel = new JChannel().setReceiver(this);
        channel.connect("Sistema_Arquivos_P2P");
        
        enviaMensagem();
        
        channel.close();

    }

    @Override
    public void viewAccepted(View novaView) {
        this.view = novaView;
    }

    private void enviaMensagem() {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Mensagem mensagem;
        String entrada, nomeArquivo, conteudoArquivo;

        try {

            mensagem = new Mensagem("NOVO_PEER");
            channel.send(null, mensagem);

            System.out.println("* SISTEMA DE ARQUIVOS P2P *");
            while (true) {

                System.out.println("\n ____________OPÇÕES____________");
                System.out.println(" |                              |");
                System.out.println(" | 1 - MOSTRAR_PEERS            |");
                System.out.println(" | 2 - ADICIONAR_ARQUIVO        |");
                System.out.println(" | 3 - LISTAR_MEUS_ARQUIVOS     |");
                System.out.println(" | 4 - LISTAR_ARQUIVOS_GLOBAIS  |");
                System.out.println(" | 5 - DELETAR_ARQUIVO          |");
                System.out.println(" | 6 - VISUALIZAR_CONTEUDO      |");
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
                        conteudoArquivo = in.readLine().toLowerCase();
                        
                        if (arquivosGlobais.get(nomeArquivo) == null) {
                            
                            arquivosLocais.put(nomeArquivo, conteudoArquivo);
                            arquivosGlobais.put(nomeArquivo, this.channel.address());
                            
                            mensagem = new Mensagem("ARQUIVO_ADICIONADO");
                            mensagem.setParam("nome_arquivo", nomeArquivo);
                                    
                            channel.send(null, mensagem);
                            
                            System.out.println("\nArquivo adicionado com sucesso!");
                            
                        } else
                            System.out.println("\nArquivo já existe! Dono: " + arquivosLocais.get(nomeArquivo));
                        
                        break;

                    case "3":
                    case "LISTAR_MEUS_ARQUIVOS":
                        
                        System.out.println("\nMEUS ARQUIVOS:\n");
                        
                        if (!arquivosLocais.isEmpty())
                            System.out.println(listaArquivosLocais());
                        else
                            System.out.println("Nenhum arquivo para mostrar!");

                        break;

                    case "4":
                    case "LISTAR_ARQUIVOS_GLOBAIS":
                        
                        System.out.println("\nARQUIVOS DE TODOS OS PEERS:\n");
                        
                        if (!arquivosGlobais.isEmpty())
                            System.out.println(listaArquivosGlobais());
                        else
                            System.out.println("Nenhum arquivo para mostrar!");

                        break;

                    case "5":
                    case "DELETAR_ARQUIVO":
                        
                        System.out.println("DELETAR ARQUIVO: \n");

                        System.out.print("Insira o nome do arquivo para deletar:");
                        System.out.print(" > ");
                        nomeArquivo = in.readLine().toLowerCase();
                        
                        if (arquivosLocais.get(nomeArquivo) != null) {
                            
                            arquivosLocais.remove(nomeArquivo);
                            arquivosGlobais.remove(nomeArquivo);
                            
                            mensagem = new Mensagem("ARQUIVO_DELETADO");
                            mensagem.setParam("nome_arquivo", nomeArquivo);
                                    
                            channel.send(null, mensagem);
                            
                            System.out.println("\nArquivo deletado!");
                            
                        } else
                            System.out.println("\nArquivo não pode ser deletado pois não existe localmente!");
                        
                        break;

                    case "6":
                    case "VISUALIZAR_CONTEUDO":
                        
                        System.out.println("\nVISUALIZAR CONTEÚDO DE UM ARQUIVO: \n");

                        System.out.print("Insira o nome do arquivo para visualizar:");
                        System.out.print(" > ");
                        nomeArquivo = in.readLine().toLowerCase();
                        
                        if (arquivosLocais.get(nomeArquivo) != null) {
                            
                            System.out.println("\nConteúdo: "+ arquivosLocais.get(nomeArquivo));
                            
                        } else {
                            
                            if (arquivosGlobais.get(nomeArquivo) != null) {
                                
                                mensagem = new Mensagem("SOLICITA_CONTEUDO_ARQUIVO");
                                mensagem.setParam("nome_arquivo", nomeArquivo);
                                channel.send(null, mensagem);
                                
                            } else
                                System.out.println("\nArquivo não foi encontrado em nenhum dos peers!");
                            
                        }
                        
                        break;

                    default:
                        System.out.println("\nOpção inválida!");

                }

            }

        } catch (Exception e) {

            System.out.println("\nErro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }
    
    public String listaArquivosLocais() {
        
        String lista = "";
        
        for (String nome : arquivosLocais.keySet()) {
            
            lista += "Nome: " + nome;
            lista += "\nConteúdo: " + arquivosLocais.get(nome);
            lista += "\n";
            
        }
        
        return lista;
        
    }
    
    public String listaArquivosGlobais() {
        
        String lista = "";
        
        for (String nome : arquivosGlobais.keySet()) {
            
            lista += "Nome: " + nome;
            lista += "\nDono: " + arquivosGlobais.get(nome);
            lista += "\n";
            
        }
        
        return lista;
        
    }

    @Override
    public void receive(Message m) {

        Mensagem mensagem = (Mensagem) m.getObject();
        String nomeArquivo;

        try {

            if (m.getSrc() != this.channel.address()) {

                switch (mensagem.getOperacao()) {

                    case "NOVO_PEER":

                        mensagem = new Mensagem("RESPOSTA_" + mensagem.getOperacao());
                        mensagem.setStatus(Status.OK);

                        String listaPeers = "";

                        for (String nome : arquivosGlobais.keySet()) {
                            listaPeers += nome + "=" + arquivosGlobais.get(nome) + ";";
                        }

                        mensagem.setParam("lista_arquivos", listaPeers);
                        
                        channel.send(m.getSrc(), mensagem);
                        
                        break;

                    case "RESPOSTA_NOVO_PEER":
                        
                        String listaArquivos[] = mensagem.getParam("lista_arquivos").split(";");
                        
                        for (String a : listaArquivos) {
                            
                            String dadosArquivo[] = a.split("=");
                            arquivosGlobais.put(dadosArquivo[0], stringToAddress(dadosArquivo[1]));
                            
                        }
                        
                        break;
                        
                    case "ARQUIVO_ADICIONADO":
                        
                        nomeArquivo = mensagem.getParam("nome_arquivo");
                        arquivosGlobais.put(nomeArquivo, m.getSrc());
                        
                        break;
                        
                    case "ARQUIVO_DELETADO":
                        
                        nomeArquivo = mensagem.getParam("nome_arquivo");
                        arquivosGlobais.remove(nomeArquivo);
                        
                        break;
                        
                    case "SOLICITA_CONTEUDO_ARQUIVO":
                        
                        nomeArquivo = mensagem.getParam("nome_arquivo");
                        
                        mensagem = new Mensagem("RESPOSTA_" + mensagem.getOperacao());
                        mensagem.setStatus(Status.OK);
                        mensagem.setParam("conteudo_arquivo", arquivosLocais.get(nomeArquivo));
                        channel.send(m.getSrc(), mensagem);
                        
                        break;
                        
                    case "RESPOSTA_SOLICITA_CONTEUDO_ARQUIVO":
                        
                        System.out.println("\nConteúdo: " + mensagem.getParam("conteudo_arquivo"));

                }

            }

        } catch (Exception e) {

            System.out.println("Erro na comunicação com os peers! ");
            System.out.println("ERRO: " + e);

        }

    }
    
    public Address stringToAddress(String stringAddress) {
        
        for (Address a : view.getMembersRaw()) {
            
            if (stringAddress.equals(a.toString()))
                return a;
            
        }
        
        return null;
        
    }

    public static void main(String[] args) throws Exception {

        Logger jgroupsLogger = Logger.getLogger("org.jgroups");
        jgroupsLogger.setLevel(Level.SEVERE);

        new Peer().start();

    }

    public JChannel getChannel() {
        return channel;
    }

    public void setChannel(JChannel channel) {
        this.channel = channel;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public HashMap<String, String> getArquivosLocais() {
        return arquivosLocais;
    }

    public void setArquivosLocais(HashMap<String, String> arquivosLocais) {
        this.arquivosLocais = arquivosLocais;
    }

    public HashMap<String, Address> getArquivosGlobais() {
        return arquivosGlobais;
    }

    public void setArquivosGlobais(HashMap<String, Address> arquivosGlobais) {
        this.arquivosGlobais = arquivosGlobais;
    }

}
