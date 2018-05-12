package it.univaq.casatracking.model;

public class Utente {

    private String nome;
    private String n_telefono;
    private String n_telefono_educatore;

    private boolean isTest;

    public Utente(){
        this.nome = "";
        this.n_telefono = "";
        this.n_telefono_educatore = "";
        this.isTest = false;
    }

    public Utente(String nome, String n_telefono, String n_telefono_educatore){
        this.nome = nome;
        this.n_telefono = n_telefono;
        this.n_telefono_educatore = n_telefono_educatore;
        this.isTest = false;
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

    public void setNome(String nome){
        this.nome = nome;
    }

    public void setNumeroTelefono(String n_telefono){
        this.n_telefono = n_telefono;
    }

    public void setNumeroTelefonoEducatore(String n_telefono_educatore){
        this.n_telefono_educatore = n_telefono_educatore;
    }

    public void createTest(){
        this.nome = "TEST";
        this.n_telefono = "TEST";
        this.n_telefono_educatore = "TEST";
        this.isTest = true;
    }

    public boolean isTest(){
        return this.isTest;
    }

}
