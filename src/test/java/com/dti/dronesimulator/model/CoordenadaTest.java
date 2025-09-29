package com.dti.dronesimulator.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoordenadaTest {

    @Test
    void testDistanciaPara() {
        Coordenada p1 = new Coordenada(0, 0);
        Coordenada p2 = new Coordenada(3, 4); // Distância esperada: 5
        Coordenada p3 = new Coordenada(1, 1);
        Coordenada p4 = new Coordenada(1, 1);

        assertEquals(5.0, p1.distanciaPara(p2), 0.001, "Distância entre (0,0) e (3,4) deve ser 5.0");
        assertEquals(0.0, p3.distanciaPara(p4), "Distância entre pontos iguais deve ser 0");
    }

    @Test
    void testIsNaBase() {
        Coordenada base = new Coordenada(0, 0);
        Coordenada notBase = new Coordenada(0.0001, 0);

        assertTrue(base.isNaBase(), "Ponto (0,0) deve ser a base");
        assertFalse(notBase.isNaBase(), "Ponto fora de (0,0) não deve ser a base");
    }

    @Test
    void testEqualsAndHashCode() {
        Coordenada c1 = new Coordenada(1.2, 3.4);
        Coordenada c2 = new Coordenada(1.2, 3.4);
        Coordenada c3 = new Coordenada(5.6, 7.8);

        assertEquals(c1, c2, "Coordenadas com mesmos valores devem ser iguais");
        assertNotEquals(c1, c3, "Coordenadas com valores diferentes devem ser desiguais");
        assertEquals(c1.hashCode(), c2.hashCode(), "HashCodes devem ser iguais para objetos iguais");
    }
}