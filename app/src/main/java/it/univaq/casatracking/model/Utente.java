package it.univaq.casatracking.model;

public class Utente {

    private String nome;
    private String n_telefono;
    private String n_telefono_educatore;
    private String n_numero_emergenza;

    public Utente(){
        this.nome = "";
        this.n_telefono = "";
        this.n_telefono_educatore = "";
        this.n_numero_emergenza = "";
    }

    public Utente(String nome, String n_telefono, String n_telefono_educatore, String n_numero_emergenza){
        this.nome = nome;
        this.n_telefono = n_telefono;
        this.n_telefono_educatore = n_telefono_educatore;
        this.n_numero_emergenza = n_numero_emergenza;
    }

    public String getNome(){
        return this.nome;
    }

    public String getNumeroTelefono(){
        return this.n_telefono;
    }

    public String getNumeroTelefonoEducatore(){
        return this.n_telefono_educatore;
    }

    public String getNumeroEmergenza(){ return this.n_numero_emergenza; }


    public void setNome(String nome){
        this.nome = nome;
    }

    public void setNumeroTelefono(String n_telefono){
        this.n_telefono = n_telefono;
    }

    public void setNumeroTelefonoEducatore(String n_telefono_educatore){
        this.n_telefono_educatore = n_telefono_educatore;
    }

    public void setNumeroEmergenza(String n_numero_emergenza){
        this.n_numero_emergenza = n_numero_emergenza;
    }

}
