package com.dti.dronesimulator.model;
import com.dti.dronesimulator.enums.DroneState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Drone {
    private Long id;
    private double capacidadeKg;
    private double autonomiaKm;
    private double cargaAtualKg;
    private double bateriaPercentual;
    private Coordenada localizacao;
    private DroneState estado;
    private List<Pedido> pacotesEmRota;

    // Base do drone no ponto (0, 0)
    private static final Coordenada BASE = new Coordenada(0, 0);

    public Drone(Long id, double capacidadeKg, double autonomiaKm) {
        this.id = id;
        this.capacidadeKg = capacidadeKg;
        this.autonomiaKm = autonomiaKm;
        this.cargaAtualKg = 0;
        this.bateriaPercentual = 100.0;
        this.localizacao = BASE;
        this.estado = DroneState.IDLE;
        this.pacotesEmRota = new ArrayList<>();
    }
    
    public Coordenada getBase() {
        return BASE;
    }

    public boolean isNaBase() {
        return this.localizacao.equals(BASE);
    }
}