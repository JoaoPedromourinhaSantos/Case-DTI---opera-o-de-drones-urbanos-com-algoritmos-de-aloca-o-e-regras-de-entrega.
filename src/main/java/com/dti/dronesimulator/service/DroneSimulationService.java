package com.dti.dronesimulator.service;
import com.dti.dronesimulator.model.Coordenada;
import com.dti.dronesimulator.model.Drone;
import com.dti.dronesimulator.enums.DroneState;
import com.dti.dronesimulator.model.Pedido;
import com.dti.dronesimulator.repository.DroneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DroneSimulationService {
    private final DroneRepository droneRepository;
    private static final double VELOCIDADE_KMH = 60.0; // Velocidade de voo simulada (60km/h)
    private static final double CUSTO_BATERIA_POR_KM = 1.5; // Ex: 1.5% de bateria por Km

    public void startFlight(Drone drone, List<Pedido> pacotes) {
        if (drone.getEstado() != DroneState.IDLE) {
            throw new IllegalStateException("Drone " + drone.getId() + " não está em estado IDLE.");
        }

        System.out.printf(Locale.ROOT, "\n[SIMULAÇÃO] Drone %d: Iniciando carregamento. Peso Total: %.2fkg\n", drone.getId(), drone.getCargaAtualKg());
        drone.setEstado(DroneState.CARREGANDO);

        new Thread(() -> {
            try {
                Thread.sleep(500); // Simula o tempo de carregamento
                simulateFlight(drone);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Simulação do Drone " + drone.getId() + " interrompida.");
            }
        }).start();
    }

    private void simulateFlight(Drone drone) throws InterruptedException {
        System.out.printf(Locale.ROOT, "[SIMULAÇÃO] Drone %d: EM VOO. Bateria: %.2f%%\n", drone.getId(), drone.getBateriaPercentual());
        drone.setEstado(DroneState.EM_VOO);

        double distanciaTotalPercorrida = 0;
        Coordenada localizacaoAtual = drone.getBase(); 
        
        List<Pedido> pacotesParaEntrega = List.copyOf(drone.getPacotesEmRota());

        for (Pedido pedido : pacotesParaEntrega) {
            
            // 1. Move para o cliente (inclui checagem e recarga de bateria se necessário)
            distanciaTotalPercorrida += moveDroneTo(drone, localizacaoAtual, pedido.getLocalizacaoCliente());
            
            // 2. Entrega
            System.out.println("-> Drone " + drone.getId() + ": Entregando pacote " + pedido.getId());
            drone.setEstado(DroneState.ENTREGANDO);
            Thread.sleep(500); // Simula o tempo de entrega
            
            // 3. Atualiza localização para o próximo trecho
            localizacaoAtual = pedido.getLocalizacaoCliente();
        }

        // 4. Retorno à Base (inclui checagem e recarga de bateria se necessário)
        System.out.println("-> Drone " + drone.getId() + ": Iniciando RETORNO à Base.");
        distanciaTotalPercorrida += moveDroneTo(drone, localizacaoAtual, drone.getBase());

        // 5. Finalização
        finishFlight(drone, distanciaTotalPercorrida);
    }
    
    private double moveDroneTo(Drone drone, Coordenada origem, Coordenada destino) throws InterruptedException {
        double distancia = origem.distanciaPara(destino);
        double custoBateria = distancia * CUSTO_BATERIA_POR_KM;
        
        // Bateria necessária para ir e, do destino, voltar à Base
        double bateriaNecessariaParaSeguranca = custoBateria + (destino.distanciaPara(drone.getBase()) * CUSTO_BATERIA_POR_KM);
        
        // Verifica a necessidade de Recarga antes de iniciar o trecho
        if (drone.getBateriaPercentual() < bateriaNecessariaParaSeguranca) { 
            recharge(drone);
        }

        // Simula o movimento do drone
        double tempoTotalSegundos = (distancia / VELOCIDADE_KMH) * 3600;
        int passos = 10;
        for(int i = 0; i < passos; i++) {
            Thread.sleep((long) (tempoTotalSegundos * 1000 / passos));
        }
        
        // Atualiza o estado
        drone.setBateriaPercentual(drone.getBateriaPercentual() - custoBateria);
        drone.setLocalizacao(destino);
        drone.setEstado(destino.isNaBase() ? DroneState.RETORNANDO : DroneState.EM_VOO);

        System.out.printf(Locale.ROOT, "   [TRECHO] %.2fkm concluído. Bateria: %.2f%%\n", distancia, drone.getBateriaPercentual());
        
        return distancia;
    }
    
    // Funcionalidade extra: Drones com recarga automática
    public void recharge(Drone drone) throws InterruptedException {
        
        // 1. Se não está na base, deve retornar
        if (!drone.isNaBase()) {
            System.out.println("[RECARGA] Drone " + drone.getId() + ": Bateria baixa! Retornando para Base antes de recarregar...");
            
            // Simula o retorno (assumindo que a alocação garante o voo de volta, sem consumir mais bateria)
            double distanciaRetorno = drone.getLocalizacao().distanciaPara(drone.getBase());
            double tempoTotalSegundos = (distanciaRetorno / VELOCIDADE_KMH) * 3600;
            Thread.sleep((long) (tempoTotalSegundos * 1000));
            
            drone.setLocalizacao(drone.getBase());
        }
        
        // 2. Inicia a recarga
        drone.setEstado(DroneState.RECARGA);
        System.out.println("[RECARGA] Drone " + drone.getId() + ": Na Base. Iniciando recarga (3s)...");
        Thread.sleep(3000); // Tempo de recarga
        
        // 3. Finaliza a recarga
        drone.setBateriaPercentual(100.0);
        drone.setLocalizacao(drone.getBase());
        drone.setEstado(DroneState.IDLE);
        System.out.println("[RECARGA] Drone " + drone.getId() + ": Recarga completa. IDLE.");
    }

    private void finishFlight(Drone drone, double distanciaTotalPercorrida) {
        System.out.printf(Locale.ROOT, "[SIMULAÇÃO] Drone %d: Retornou. Distância total: %.2fkm. Bateria final: %.2f%%\n", 
            drone.getId(), 
            distanciaTotalPercorrida, 
            drone.getBateriaPercentual()
        );
        
        // Finaliza o ciclo e limpa o drone
        drone.setCargaAtualKg(0);
        drone.getPacotesEmRota().clear();
        drone.setLocalizacao(drone.getBase());
        drone.setEstado(DroneState.IDLE); // Define como IDLE
        
        droneRepository.save(drone); // Atualiza o drone no repositório com o estado final
    }
}