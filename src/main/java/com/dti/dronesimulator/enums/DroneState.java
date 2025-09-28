package com.dti.dronesimulator.enums;
public enum DroneState {
    IDLE,        // Parado na base, pronto para voar
    CARREGANDO,  // Carregando pacotes
    EM_VOO,      // Voando para um cliente ou entre clientes
    ENTREGANDO,  // Entregando o pacote no local do cliente
    RETORNANDO,  // Voltando para a base
    RECARGA      // Recarregando bateria na base
}