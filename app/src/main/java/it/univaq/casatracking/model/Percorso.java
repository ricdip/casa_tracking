package it.univaq.casatracking.model;

public class Percorso {

    private int id;
    private String nome;
    private String tempo;

    public Percorso(int id, String nome, String tempo){
        this.id = id;
        this.nome = nome;
        this.tempo = tempo;
    }

    public Percorso(){
        this.id = 0;
        this.nome = "";
        this.tempo = "";
    }

    public int getId(){
        return this.id;
    }

    public String getNome(){
        return this.nome;
    }

    public String getTempo(){
        return this.tempo;
    }

    public void setId(int id){
        this.id = id;
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
