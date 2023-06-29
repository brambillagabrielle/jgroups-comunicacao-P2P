package br.edu.ifsul.cc.sdi.comunicacaop2p.util;

import org.jgroups.Address;

public class Usuario {
    
    private String usuario;
    private String senha;
    
    public Usuario(String usuario, String senha) {
        this.usuario = usuario;
        this.senha = senha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
    
}