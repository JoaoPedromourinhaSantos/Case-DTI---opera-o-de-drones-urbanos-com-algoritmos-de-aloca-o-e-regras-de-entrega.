package com.dti.dronesimulator.service;

import com.dti.dronesimulator.enums.DroneState;
import com.dti.dronesimulator.enums.Prioridade;
import com.dti.dronesimulator.model.Coordenada;
import com.dti.dronesimulator.model.Drone;
import com.dti.dronesimulator.model.Pedido;
import com.dti.dronesimulator.repository.DroneRepository;
import com.dti.dronesimulator.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DroneAllocationServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private DroneSimulationService simulationService;

    @InjectMocks
    private DroneAllocationService allocationService;

    private Drone droneForte;

    @BeforeEach
    void setUp() {
        droneForte = new Drone(1L, 10.0, 50.0);
        droneForte.setEstado(DroneState.IDLE);
    }

    @Test
    void testPriorizacaoPorNivelEData() {
        // Teste de priorização ALTA > MEDIA > BAIXA
        Pedido p1Alta = new Pedido(1001L, 1.0, Prioridade.ALTA, new Coordenada(1.0, 1.0));
        p1Alta.setDataChegada(LocalDateTime.now().minusMinutes(5));

        Pedido p2Media = new Pedido(1002L, 2.0, Prioridade.MEDIA, new Coordenada(1.0, 1.0));
        p2Media.setDataChegada(LocalDateTime.now().minusMinutes(3));

        Pedido p3Baixa = new Pedido(1003L, 3.0, Prioridade.BAIXA, new Coordenada(1.0, 1.0));
        p3Baixa.setDataChegada(LocalDateTime.now().minusMinutes(1));

        List<Pedido> pedidosNaoOrdenados = Arrays.asList(p3Baixa, p1Alta, p2Media);

        when(pedidoRepository.findPendingOrders()).thenReturn(pedidosNaoOrdenados);
        when(droneRepository.findAvailableDrones()).thenReturn(Collections.singletonList(droneForte));

        allocationService.allocateOrders();

        // O pacote ALTA (p1) deve ser o primeiro, seguido pelo MEDIA (p2)
        assertEquals(3, droneForte.getPacotesEmRota().size());
        assertEquals(p1Alta, droneForte.getPacotesEmRota().get(0), "O pacote ALTA deve ser o primeiro na rota.");
        assertEquals(p2Media, droneForte.getPacotesEmRota().get(1), "O pacote MEDIA deve ser o segundo na rota.");
    }
    
    @Test
    void testValidacaoCapacidade() {
        // Drone: 10kg. Pacotes: 3kg, 3kg, 3kg, 3kg (Total 12kg)
        Pedido pA = new Pedido(1L, 3.0, Prioridade.ALTA, new Coordenada(1, 1));
        Pedido pB = new Pedido(2L, 3.0, Prioridade.ALTA, new Coordenada(1, 1));
        Pedido pC = new Pedido(3L, 3.0, Prioridade.ALTA, new Coordenada(1, 1));
        Pedido pD = new Pedido(4L, 3.0, Prioridade.ALTA, new Coordenada(1, 1)); 

        List<Pedido> pedidos = Arrays.asList(pA, pB, pC, pD); 
        
        when(pedidoRepository.findPendingOrders()).thenReturn(pedidos);
        when(droneRepository.findAvailableDrones()).thenReturn(Collections.singletonList(droneForte)); 

        allocationService.allocateOrders();

        // Esperado: Apenas 3 pacotes (9kg total) devem ser alocados
        assertEquals(3, droneForte.getPacotesEmRota().size(), "Apenas 3 pacotes (9kg) devem caber no drone de 10kg.");
        assertFalse(pD.isAlocado(), "O quarto pacote deve ser rejeitado por exceder a capacidade.");
    }

    @Test
    void testValidacaoAutonomia() {
        // Drone Forte: 50km autonomia
        // Pedido que exige 30km de ida e 30km de volta (Rota total = 60km)
        Pedido pLonge = new Pedido(1L, 1.0, Prioridade.ALTA, new Coordenada(30.0, 0.0)); 
        
        when(pedidoRepository.findPendingOrders()).thenReturn(Collections.singletonList(pLonge));
        when(droneRepository.findAvailableDrones()).thenReturn(Collections.singletonList(droneForte)); 

        allocationService.allocateOrders();

        // Esperado: Rota de 60km excede a autonomia de 50km.
        assertEquals(0, droneForte.getPacotesEmRota().size(), "Drone de 50km não deve alocar pacote que exige 60km de rota total.");
    }

    @Test
    void testOtimizacaoMultiplosPedidosDentroDaAutonomia() {
        // Drone Forte (50km autonomia)
        // Rota A -> B -> C -> Volta é 40km (OK)
        // Rota A -> B -> C -> D -> Volta é 60km (NOT OK)
        Pedido pA = new Pedido(1L, 1.0, Prioridade.ALTA, new Coordenada(5.0, 0.0));
        Pedido pB = new Pedido(2L, 1.0, Prioridade.ALTA, new Coordenada(10.0, 0.0));
        Pedido pC = new Pedido(3L, 1.0, Prioridade.ALTA, new Coordenada(20.0, 0.0));
        Pedido pD = new Pedido(4L, 1.0, Prioridade.ALTA, new Coordenada(30.0, 0.0));
        
        List<Pedido> pedidos = Arrays.asList(pA, pB, pC, pD);
        
        when(pedidoRepository.findPendingOrders()).thenReturn(pedidos);
        when(droneRepository.findAvailableDrones()).thenReturn(Collections.singletonList(droneForte));

        allocationService.allocateOrders();

        // Esperado: Apenas pA, pB, pC devem ser alocados
        assertEquals(3, droneForte.getPacotesEmRota().size(), "Deve caber apenas 3 pacotes na rota de 50km.");
        assertFalse(pD.isAlocado(), "O pacote D deve ser rejeitado por exceder a autonomia de 50km.");
    }
}