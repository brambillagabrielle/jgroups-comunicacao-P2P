package br.edu.ifsul.cc.sdi.comunicacaop2p.util;

/**
 * Classe que representa um usuário do Sistema de Arquivos P2P
 * @author gabrielle
 */
public class Usuario {
    
    private String usuario;
    private String senha;
    
    /**
     * Usuário que inicia os valores de usuário e senha
     * @param usuario
     * @param senha
     */
    public Usuario(String usuario, String senha) {
        this.usuario = usuario;
        this.senha = senha;
    }

    /**
     * Método para retornar o valor do atributo usuário
     * @return - String
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Método para atribuir valor ao atributo usuário
     * @param usuario - String
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    /**
     * Método para retornar o valor do atributo senha
     * @return String
     */
    public String getSenha() {
        return senha;
    }

    /**
     * Método para atribuir valor ao atributo senha
     * @param senha - String
     */
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
}