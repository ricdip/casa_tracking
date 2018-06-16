package it.univaq.casatracking.model;

public class Percorso {

    private String id;
    private String the_geom;
    private String nome;
    private String tempo;

    public Percorso(String id, String nome, String tempo){
        this.id = id;
        this.nome = nome;
        this.tempo = tempo;

        this.the_geom = "";
    }

    public Percorso(){
        this.id = String.valueOf(0);
        this.nome = "";
        this.tempo = "";

        this.the_geom = "";
    }

    public Percorso(String id, String the_geom, String nome, String tempo){
        this.id = id;
        this.nome = nome;
        this.tempo = tempo;

        this.the_geom = the_geom;
    }

    public int getId(){
        return Integer.parseInt(this.id);
    }

    public String getThe_geom(String the_geom){
        return this.the_geom;
    }

    public String getNome(){
        return this.nome;
    }

    public int getTempo(){
        return Integer.parseInt(this.tempo);
    }

    public void setId(String id){
        this.id = id;
    }

    public void setThe_geom(String the_geom){
        this.the_geom = the_geom;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public void setTempo(String tempo){
        this.tempo = tempo;
    }

    @Override
    public String toString(){
        return this.nome + " - " + this.tempo;
    }

}
