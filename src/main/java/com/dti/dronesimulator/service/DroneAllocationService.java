package com.dti.dronesimulator.service;
import com.dti.dronesimulator.model.Coordenada;
import com.dti.dronesimulator.model.Drone;
import com.dti.dronesimulator.model.Pedido;
import com.dti.dronesimulator.repository.DroneRepository;
import com.dti.dronesimulator.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DroneAllocationService {
    private final PedidoRepository pedidoRepository;
    private final DroneRepository droneRepository;
    private final DroneSimulationService simulationService;

    public void allocateOrders() {
        // Fila de entrega: Priorizar (ALTA > MEDIA > BAIXA) e, em caso de empate, pelo mais antigo
        List<Pedido> pedidosPendentes = pedidoRepository.findPendingOrders().stream()
                .sorted(Comparator
                        // CORREÇÃO: Usando Lambda explícito para comparar pelo NÍVEL numérico (3, 2, 1)
                        // em ordem DECRESCENTE (ALTA > MEDIA > BAIXA).
                        .comparing((Pedido p) -> p.getPrioridade().getNivel(), Comparator.reverseOrder())
                        // Em caso de empate, o mais antigo primeiro (data crescente).
                        .thenComparing(Pedido::getDataChegada)) 
                .collect(Collectors.toList());

        System.out.println("\n--- Iniciando Alocação. Pedidos Pendentes: " + pedidosPendentes.size() + " ---");

        // Prioriza drones com maior capacidade/autonomia para otimizar o uso
        List<Drone> dronesDisponiveis = droneRepository.findAvailableDrones().stream()
            .sorted(Comparator
                    .comparing(Drone::getCapacidadeKg, Comparator.reverseOrder())
                    .thenComparing(Drone::getAutonomiaKm, Comparator.reverseOrder()))
            .toList();

        if (pedidosPendentes.isEmpty() || dronesDisponiveis.isEmpty()) {
            System.out.println("Nenhum pedido pendente ou drone disponível.");
            return;
        }

        for (Drone drone : dronesDisponiveis) {
            // Busca a combinação de pacotes que maximiza o uso do drone
            List<Pedido> pacotesParaVoo = findOptimalPackageCombination(drone, pedidosPendentes);

            if (!pacotesParaVoo.isEmpty()) {
                double pesoTotal = pacotesParaVoo.stream().mapToDouble(Pedido::getPesoKg).sum();
                
                // Configura o drone
                drone.setCargaAtualKg(pesoTotal);
                drone.setPacotesEmRota(new ArrayList<>(pacotesParaVoo)); 
                
                // Marca os pedidos como alocados e remove da fila pendente
                pacotesParaVoo.forEach(p -> p.setAlocado(true));
                pedidosPendentes.removeAll(pacotesParaVoo);
                
                // Inicia a simulação do voo em thread separada
                simulationService.startFlight(drone, pacotesParaVoo);

                if (pedidosPendentes.isEmpty()) break;
            }
        }
        System.out.println("--- Alocação Concluída. Pedidos restantes: " + pedidosPendentes.size() + " ---\n");
    }
    /**
     * Encontra a combinação ótima de pacotes para o drone, maximizando o uso
     * dentro das limitações de capacidade e autonomia.
     */
    private List<Pedido> findOptimalPackageCombination(Drone drone, List<Pedido> pedidos) {
        List<Pedido> pacotesSelecionados = new ArrayList<>();
        double pesoAtual = 0;
        
        Coordenada pontoAnterior = drone.getBase();
        double distanciaAcumuladaTrechos = 0; // Distância total dos trechos Base->C1->C2...
        
        // Tenta alocar os pedidos na ordem de prioridade
        for (Pedido pedido : pedidos) {
            
            // 1. Validação de Capacidade
            double novoPeso = pesoAtual + pedido.getPesoKg();
            if (novoPeso > drone.getCapacidadeKg()) {
                continue; 
            }
            
            // Distância para ir do ponto anterior (Base ou Cliente N-1) até o novo Cliente N
            double distParaNovoCliente = pontoAnterior.distanciaPara(pedido.getLocalizacaoCliente());
            
            // Calcula a ROTA TOTAL (distância de entrega + distância do último cliente para a Base)
            double novaDistanciaTrechos = distanciaAcumuladaTrechos + distParaNovoCliente;
            double novaDistanciaTotal = novaDistanciaTrechos + pedido.getLocalizacaoCliente().distanciaPara(drone.getBase());
            
            // 2. Validação de Autonomia (Alcance)
            if (novaDistanciaTotal <= drone.getAutonomiaKm()) {
                // Combinação válida! Adiciona e atualiza os parâmetros.
                pacotesSelecionados.add(pedido);
                pesoAtual = novoPeso;
                distanciaAcumuladaTrechos = novaDistanciaTrechos;
                pontoAnterior = pedido.getLocalizacaoCliente(); 
            }
        }
        return pacotesSelecionados;
    }
}
