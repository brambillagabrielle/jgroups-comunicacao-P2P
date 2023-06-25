package br.edu.ifsul.cc.sdi.comunicacaop2p.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Mensagem implements Serializable {

    private String operacao;
    private Status status;

    Map<String, String> params;

    public Mensagem(String operacao) {
        this.operacao = operacao;
        params = new HashMap<>();
    }

    public Mensagem() {
        params = new HashMap<>();
    }

    public String getOperacao() {
        return operacao;
    }

    public void setStatus(Status s) {
        this.status = s;
    }

    public Status getStatus() {
        return status;
    }

    public void setParam(String chave, String valor) {
        params.put(chave, valor);
    }

    public String getParam(String chave) {
        return params.get(chave);
    }

}
