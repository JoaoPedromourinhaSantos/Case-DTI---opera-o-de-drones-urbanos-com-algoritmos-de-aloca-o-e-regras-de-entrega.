package com.dti.dronesimulator.controller;

import com.dti.dronesimulator.model.Coordenada;
import com.dti.dronesimulator.model.Drone;
import com.dti.dronesimulator.model.Pedido;
import com.dti.dronesimulator.repository.DroneRepository;
import com.dti.dronesimulator.repository.PedidoRepository;
import com.dti.dronesimulator.service.DroneAllocationService;
import com.dti.dronesimulator.enums.DroneState;
import com.dti.dronesimulator.enums.Prioridade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DroneController.class)
class DroneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private DroneRepository droneRepository;

    @MockBean
    private DroneAllocationService allocationService;

    // --- Testes do Endpoint POST /pedidos (Validações) ---

    @Test
    void testCreatePedido_Success() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .param("pesoKg", "5.0")
                        .param("prioridade", "MEDIA")
                        .param("x", "10.0")
                        .param("y", "20.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isCreated());

        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testCreatePedido_InvalidPeso() throws Exception {
        // Teste: Peso negativo
        mockMvc.perform(post("/api/pedidos")
                        .param("pesoKg", "-1.0").param("prioridade", "ALTA").param("x", "1.0").param("y", "1.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("O peso do pacote deve ser positivo."));
        
        // Teste: Peso máximo excedido (10.1kg)
        mockMvc.perform(post("/api/pedidos")
                        .param("pesoKg", "10.1").param("prioridade", "ALTA").param("x", "1.0").param("y", "1.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Pacote muito pesado. Limite máximo: 10kg."));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void testCreatePedido_InvalidPrioridade() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .param("pesoKg", "1.0")
                        .param("prioridade", "INVALIDA")
                        .param("x", "1.0")
                        .param("y", "1.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Prioridade inválida. Use: ALTA, MEDIA, ou BAIXA."));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void testCreatePedido_DestinoIsBase() throws Exception {
        // Validação contra a base (0,0)
        mockMvc.perform(post("/api/pedidos")
                        .param("pesoKg", "1.0")
                        .param("prioridade", "ALTA")
                        .param("x", "0.0")
                        .param("y", "0.0")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("A localização do cliente não pode ser a Base (0,0)."));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // --- Testes do Endpoint GET /entregas/status/{id} (Rastreamento) ---

    @Test
    void testGetEntregaStatus_NotFound() throws Exception {
        when(pedidoRepository.findById(100L)).thenReturn(null);

        mockMvc.perform(get("/api/entregas/status/100"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Pedido não encontrado."));
    }

    @Test
    void testGetEntregaStatus_InFila() throws Exception {
        Pedido pedido = new Pedido(100L, 1.0, Prioridade.ALTA, new Coordenada(1, 1));
        pedido.setAlocado(false);

        when(pedidoRepository.findById(100L)).thenReturn(pedido);

        mockMvc.perform(get("/api/entregas/status/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("O pedido #100 está na fila de espera. Prioridade: ALTA"));
    }

    @Test
    void testGetEntregaStatus_EmVoo() throws Exception {
        Pedido pedido = new Pedido(100L, 1.0, Prioridade.ALTA, new Coordenada(10, 0));
        pedido.setAlocado(true);

        Drone drone = new Drone(1L, 10.0, 50.0);
        drone.setEstado(DroneState.EM_VOO);
        drone.setLocalizacao(new Coordenada(5, 0)); 
        drone.setPacotesEmRota(Collections.singletonList(pedido));

        when(pedidoRepository.findById(100L)).thenReturn(pedido);
        when(droneRepository.findAll()).thenReturn(Collections.singletonList(drone));

        mockMvc.perform(get("/api/entregas/status/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("O pacote está alocado no Drone #1 e está em voo. Distância atual do drone para o destino: 5.00 km."));
    }

    @Test
    void testGetEntregaStatus_Entregue() throws Exception {
        Pedido pedido = new Pedido(100L, 1.0, Prioridade.ALTA, new Coordenada(1, 1));
        pedido.setAlocado(true); 
        
        // Simula o estado de entrega finalizada: alocado=true, mas não está mais em nenhum drone
        Drone drone = new Drone(1L, 10.0, 50.0);
        drone.setEstado(DroneState.IDLE); 
        drone.getPacotesEmRota().clear(); 

        when(pedidoRepository.findById(100L)).thenReturn(pedido);
        when(droneRepository.findAll()).thenReturn(Collections.singletonList(drone));

        mockMvc.perform(get("/api/entregas/status/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("O pedido #100 foi entregue e finalizado."));
    }
}