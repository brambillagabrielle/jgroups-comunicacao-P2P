package br.edu.ifsul.cc.sdi.comunicacaop2p.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa as mensagens trocadas pelos peers no cluster do Sistema
 * de Arquivos P2P
 * @author gabrielle
 */
public class Mensagem implements Serializable {

    private String operacao;
    private Status status;

    Map<String, String> params;

     /**
     * Construtor que cria uma nova mensagem, iniciando os valores da operação e
     * dos paramêtros.
     * @param operacao - String
     */
    public Mensagem(String operacao) {
        this.operacao = operacao;
        params = new HashMap<>();
    }
    
    /**
     * Método para retornar o valor do atributo operação
     * @return - String
     */
    public String getOperacao() {
        return operacao;
    }

    /**
     * Método para atribuir um valor para o atributo status
     * @param status - Status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Método para retornar o valor do atributo status
     * @return - Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Método para atribuir valor para o atributo param
     * @param chave - String
     * @param valor - String
     */
    public void setParam(String chave, String valor) {
        params.put(chave, valor);
    }

    /**
     * Método para retornar o valor do atributo param
     * @param chave - String
     * @return - String
     */
    public String getParam(String chave) {
        return params.get(chave);
    }

}
