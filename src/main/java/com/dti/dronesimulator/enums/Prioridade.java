package com.dti.dronesimulator.enums;
public enum Prioridade {
    ALTA(3),
    MEDIA(2),
    BAIXA(1);

    private final int nivel;

    Prioridade(int nivel) {
        this.nivel = nivel;
    }

    public int getNivel() {
        return nivel;
    }
}