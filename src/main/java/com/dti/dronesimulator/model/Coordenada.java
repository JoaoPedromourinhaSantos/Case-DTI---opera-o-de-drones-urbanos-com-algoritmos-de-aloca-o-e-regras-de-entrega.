package com.dti.dronesimulator.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Objects;

@Data
@AllArgsConstructor
public class Coordenada {
    private double x;
    private double y;

    public double distanciaPara(Coordenada outra) {
        // Fórmula da distância euclidiana (2D)
        return Math.sqrt(Math.pow(this.x - outra.x, 2) + Math.pow(this.y - outra.y, 2));
    }
    
    // Adicionado método auxiliar para verificar se a coordenada é a base (0,0)
    public boolean isNaBase() {
        return x == 0.0 && y == 0.0;
    }

    // Sobrescrita do equals e hashCode para garantir que a comparação entre coordenadas funcione corretamente
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordenada that = (Coordenada) o;
        // Compara usando um pequeno epsilon para evitar erros de ponto flutuante
        return Math.abs(that.x - x) < 1e-6 && Math.abs(that.y - y) < 1e-6;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}