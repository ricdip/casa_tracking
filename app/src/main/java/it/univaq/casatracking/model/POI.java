package it.univaq.casatracking.model;

public class POI {

    private String id;
    private String nome;
    private String the_geom;
    private String id_percorso;
    private String foto;
    private String descrizione;

    public POI(String id, String nome, String id_percorso, String foto, String descrizione){
        this.id = id;
        this.nome = nome;
        this.id_percorso = id_percorso;
        this.foto = foto;
        this.descrizione = descrizione;

        this.the_geom = "";
    }

    public POI(){
        this.id = String.valueOf(0);
        this.nome = "";
        this.id_percorso = String.valueOf(0);
        this.foto = "";
        this.descrizione = "";

        this.the_geom = "";
    }

    public POI(String id, String the_geom, String nome, String id_percorso, String foto, String descrizione){
        this.id = id;
        this.nome = nome;
        this.id_percorso = id_percorso;
        this.foto = foto;
        this.descrizione = descrizione;

        this.the_geom = the_geom;
    }

    public int getId(){
        return Integer.parseInt(this.id);
    }

    public String getThe_geom(){
        return this.the_geom;
    }

    public String getNome(){
        return this.nome;
    }

    public int getId_percorso(){
        return Integer.parseInt(this.id_percorso);
    }

    public String getFoto(){
        return this.foto;
    }

    public String getDescrizione(){
        return this.descrizione;
    }


    public void setId(String id){
        this.id = id;
    }

    public void getThe_geom(String the_geom){
        this.the_geom = the_geom;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public void setId_percorso(String id_percorso){
        this.id_percorso = id_percorso;
    }

    public void setFoto(String foto){
        this.foto = foto;
    }

    public void setDescrizione(String descrizione){
        this.descrizione = descrizione;
    }

    @Override
    public String toString(){
        return this.id + " - " + this.nome;
    }

}
