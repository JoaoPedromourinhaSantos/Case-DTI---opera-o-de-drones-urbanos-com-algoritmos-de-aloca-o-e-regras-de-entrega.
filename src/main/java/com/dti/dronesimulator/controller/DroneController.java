package com.dti.dronesimulator.controller;
import com.dti.dronesimulator.model.Coordenada;
import com.dti.dronesimulator.model.Drone;
import com.dti.dronesimulator.model.Pedido;
import com.dti.dronesimulator.enums.Prioridade;
import com.dti.dronesimulator.enums.DroneState;
import com.dti.dronesimulator.repository.DroneRepository;
import com.dti.dronesimulator.repository.PedidoRepository;
import com.dti.dronesimulator.service.DroneAllocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class DroneController {
    private final PedidoRepository pedidoRepository;
    private final DroneRepository droneRepository;
    private final DroneAllocationService allocationService;

    public DroneController(PedidoRepository pedidoRepository, DroneRepository droneRepository, DroneAllocationService allocationService) {
        this.pedidoRepository = pedidoRepository;
        this.droneRepository = droneRepository;
        this.allocationService = allocationService;
    }


    // Endpoint: POST /pedidos
    @PostMapping("/pedidos")
    public ResponseEntity<Pedido> createPedido(
            @RequestParam double pesoKg,
            @RequestParam String prioridade,
            @RequestParam double x,
            @RequestParam double y) {

        // 1. Validação de Peso
        if (pesoKg <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O peso do pacote deve ser positivo.");
        }
        
        // 2. Validação de Prioridade
        Prioridade p;
        try {
            p = Prioridade.valueOf(prioridade.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prioridade inválida. Use: ALTA, MEDIA, ou BAIXA.");
        }

        // 3. Validação de Peso Máximo (Regra do Drone mais forte)
        if (pesoKg > 10.0) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pacote muito pesado. Limite máximo: 10kg.");
        }

        // 4. Validação de Localização (não pode ser a base)
        Coordenada localizacaoCliente = new Coordenada(x, y);
        if (localizacaoCliente.isNaBase()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A localização do cliente não pode ser a Base (0,0).");
        }

        Pedido novoPedido = new Pedido(null, pesoKg, p, localizacaoCliente);
        pedidoRepository.save(novoPedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);
    }

    // Endpoint: GET /drones/status
    @GetMapping("/drones/status")
    public Collection<Drone> getDroneStatus() {
        return droneRepository.findAll();
    }
    
    // Endpoint para iniciar o processo de alocação
    @PostMapping("/simulacao/alocar")
    public ResponseEntity<String> triggerAllocation() {
        allocationService.allocateOrders();
        return ResponseEntity.ok("Processo de alocação de drones iniciado. Verifique o console para a simulação de voo.");
    }
    
    // Endpoint Adicional: Rastreamento de Entrega
    @GetMapping("/entregas/status/{id}")
    public ResponseEntity<String> getEntregaStatus(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id);

        if (pedido == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.");
        }
        
        if (!pedido.isAlocado()) {
            return ResponseEntity.ok("O pedido #" + id + " está na fila de espera. Prioridade: " + pedido.getPrioridade());
        }
        
        // Encontra o drone que está com este pacote
        Drone droneEmVoo = droneRepository.findAll().stream()
                .filter(d -> d.getPacotesEmRota() != null && d.getPacotesEmRota().contains(pedido))
                .findFirst()
                .orElse(null);
                
        if (droneEmVoo == null) {
             return ResponseEntity.ok("O pedido #" + id + " foi entregue e finalizado.");
        }

        // Rastreamento detalhado:
        if (droneEmVoo.getEstado() == DroneState.EM_VOO || droneEmVoo.getEstado() == DroneState.ENTREGANDO) {
            Coordenada localizacaoDrone = droneEmVoo.getLocalizacao();
            double distancia = localizacaoDrone.distanciaPara(pedido.getLocalizacaoCliente());
            String status = String.format(Locale.ROOT, 
                "O pacote está alocado no Drone #%d e está em voo. Distância atual do drone para o destino: %.2f km.", 
                droneEmVoo.getId(), 
                distancia
            );
            return ResponseEntity.ok(status);
        }
        
        return ResponseEntity.ok("O pedido #" + id + " está alocado no Drone #" + droneEmVoo.getId() + " e está com status: " + droneEmVoo.getEstado());
    }
}