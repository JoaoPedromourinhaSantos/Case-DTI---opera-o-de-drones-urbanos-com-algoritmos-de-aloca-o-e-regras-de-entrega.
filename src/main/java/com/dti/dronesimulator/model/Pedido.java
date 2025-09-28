package com.dti.dronesimulator.model;
import com.dti.dronesimulator.enums.Prioridade;
import lombok.Data; 
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Pedido {
    private Long id;
    private double pesoKg;
    private Prioridade prioridade;
    private Coordenada localizacaoCliente;
    private LocalDateTime dataChegada; 
    private boolean alocado;

    public Pedido(Long id, double pesoKg, Prioridade prioridade, Coordenada localizacaoCliente) {
        this.id = id;
        this.pesoKg = pesoKg;
        this.prioridade = prioridade;
        this.localizacaoCliente = localizacaoCliente;
        this.dataChegada = LocalDateTime.now();
        this.alocado = false;
    }
}