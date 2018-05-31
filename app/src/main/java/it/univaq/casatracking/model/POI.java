package it.univaq.casatracking.model;

public class POI {

    private int id;
    private String nome;
    //private String the_geom;
    private int id_percorso;
    private String foto;
    private String descrizione;

    public POI(int id, String nome, int id_percorso, String foto, String descrizione){
        this.id = id;
        this.nome = nome;
        this.id_percorso = id_percorso;
        this.foto = foto;
        this.descrizione = descrizione;
    }

    public POI(){
        this.id = 0;
        this.nome = "";
        this.id_percorso = 0;
        this.foto = "";
        this.descrizione = "";
    }

    public int getId(){
        return this.id;
    }

    public String getNome(){
        return this.nome;
    }

    public int getId_percorso(){
        return this.id_percorso;
    }

    public String getFoto(){
        return this.foto;
    }

    public String getDescrizione(){
        return this.descrizione;
    }


    public void setId(int id){
        this.id = id;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public void setId_percorso(int id_percorso){
        this.id_percorso = id_percorso;
    }

    public void setFoto(String foto){
        this.foto = foto;
    }

    public void setDescrizione(String descrizione){
        this.descrizione = descrizione;
    }

}
