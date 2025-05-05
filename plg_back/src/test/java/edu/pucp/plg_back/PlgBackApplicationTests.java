package edu.pucp.plg_back;
/* 
import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;
import edu.pucp.plg_back.service.fileReader.BloqueoService;
import edu.pucp.plg_back.service.impl.GAPlanificador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
*/
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PlgBackApplicationTests {

	/* 
	@Autowired
	private GAPlanificador gaPlanificador;*/

	@Test
    void testAlgoritmoGenetico() throws IOException {

		LocalDateTime now = LocalDateTime.now();
		System.out.println("Fecha y hora actual: " + now);
		/* 
        // Cargar bloqueos desde un archivo
        BloqueoService bloqueoService = new BloqueoService();
        bloqueoService.cargarBloqueos("C:\\Repositorios\\DP1\\dp1-optracker\\docs\\bloqueos\\202501.bloqueos.txt", "202504");
        List<Bloqueo> bloqueos = bloqueoService.getBloqueos();
		System.out.println("Total bloqueos: " + bloqueos.size());


		// --- Flota de Camiones (basado en QA.md Pregunta 4) ---
		List<Camion> flota = new ArrayList<>();
		// Set a start time for trucks (e.g., beginning of shift 8:00 AM)
		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.HOUR_OF_DAY, 8);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);

		// Velocidad promedio 50 km/h según QA.md
		double velocidadPromedio = 50.0;
		// Añadir camiones según tipos y unidades en QA.md
		// 2 x TA (25 m³)
		flota.add(Camion.builder().codigo("TA01").capacidad(25).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TA02").capacidad(25).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		// 4 x TB (15 m³)
		flota.add(Camion.builder().codigo("TB01").capacidad(15).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TB02").capacidad(15).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TB03").capacidad(15).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TB04").capacidad(15).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		// 4 x TC (10 m³)
		flota.add(Camion.builder().codigo("TC01").capacidad(10).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TC02").capacidad(10).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TC03").capacidad(10).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TC04").capacidad(10).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		// 10 x TD (5 m³)
		flota.add(Camion.builder().codigo("TD01").capacidad(5).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TD02").capacidad(5).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TD03").capacidad(5).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
		flota.add(Camion.builder().codigo("TD04").capacidad(5).velocidad(velocidadPromedio)
				.fechaInicio((Calendar) startCal.clone()).build());
				
    

        // --- Pedidos (Coordenadas dentro 70x50, Plazo >= 4 horas) ---
		List<Pedido> pedidos = new ArrayList<>();
		// Pedidos con diferentes urgencias y ubicaciones
		// Plazo mínimo 4 horas (240 minutos)

		// Pedido 1: Urgente, cerca del depot (12,8)
		pedidos.add(new Pedido(1, "C001", 8, 20, 15, now.minusHours(1), now.plusHours(4))); // Pedido hace 1h, vence
																							// en 4h
		// Pedido 2: Volumen medio, zona intermedia
		pedidos.add(new Pedido(2, "C002", 12, 40, 30, now.minusMinutes(30), now.plusHours(5))); // Pedido hace 30m,
																								// vence en 5h
		// Pedido 3: Pequeño, lejos
		pedidos.add(new Pedido(3, "C003", 4, 65, 45, now, now.plusHours(6))); // Pedido ahora, vence en 6h
		// Pedido 4: Grande, relativamente cerca
		pedidos.add(new Pedido(4, "C004", 20, 30, 10, now.minusHours(2), now.plusHours(4).plusMinutes(30)));
		*/
        // Ejecutar el algoritmo genético
        //List<Ruta> rutas = gaPlanificador.planificar(flota, pedidos, bloqueos);

        // Validar los resultados
		/* 
        assertNotNull(rutas, "Las rutas no deben ser nulas");
        assertFalse(rutas.isEmpty(), "Debe generarse al menos una ruta");
        rutas.forEach(ruta -> {
            assertNotNull(ruta.getRuta(), "La ruta debe contener nodos");
            assertTrue(ruta.getRuta().size() > 1, "La ruta debe incluir al menos el depósito y un destino");
        });

        // Imprimir resultados para inspección manual
        rutas.forEach(ruta -> System.out.printf("Ruta: %s -> %s (%d paradas)\n",
                ruta.getStartTime(), ruta.getEndTime(), ruta.getRuta().size()));*/
    }


}
