package edu.pucp.plg_back.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;
import edu.pucp.plg_back.service.impl.ACOPlanificador;
import edu.pucp.plg_back.service.impl.GAPlanificador;

public class PruebasHardcodeadas {
    public void testHardcodeadas(GAPlanificador ag, ACOPlanificador aco) {
        LocalDateTime now = LocalDateTime.now();
			System.out.println("======================================================");
			System.out.println("=          INICIANDO DEMO PLANIFICACIÓN            =");
			System.out.println("======================================================");
			System.out.println("Hora actual: " + now);

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
			// Add more TD trucks if needed for testing, up to TD10

			System.out.println("\n--- Flota Definida ---");
			System.out.println("Total camiones: " + flota.size());
			System.out.println("Hora de inicio de turno: " + startCal.getTime());

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
			pedidos.add(new Pedido(4, "C004", 20, 30, 10, now.minusHours(2), now.plusHours(4).plusMinutes(30))); // Pedido
																													// hace
																													// 2h,
																													// vence
																													// en
																													// 4.5h
			// Pedido 5: Volumen medio, otra zona
			pedidos.add(new Pedido(5, "C005", 10, 50, 20, now, now.plusHours(7))); // Pedido ahora, vence en 7h
			// Pedido 6: Pequeño, esquina lejana
			pedidos.add(new Pedido(6, "C006", 3, 68, 48, now.minusDays(1), now.plusHours(8))); // Pedido ayer, vence en
																								// 8h
			// Pedido 7: Grande, otra esquina
			pedidos.add(new Pedido(7, "C007", 22, 5, 45, now, now.plusHours(8))); // Pedido ahora, vence en 8h
			// Pedido 8: Cerca del Almacén Norte (42, 42)
			pedidos.add(new Pedido(8, "C008", 7, 40, 40, now.minusHours(3), now.plusHours(5))); // Pedido hace 3h, vence
																								// en 5h
			// Pedido 9: Cerca del Almacén Este (63, 3)
			pedidos.add(new Pedido(9, "C009", 9, 60, 5, now, now.plusHours(6))); // Pedido ahora, vence en 6h
			// Pedido 10: Otro pedido
			pedidos.add(new Pedido(10, "C010", 14, 25, 25, now.minusMinutes(15), now.plusHours(4))); // Pedido hace 15m,
																										// vence en 4h

			System.out.println("\n--- Pedidos Definidos ---");
			System.out.println("Total pedidos: " + pedidos.size());
			pedidos.forEach(p -> System.out.printf(
					"  Pedido %d: (%d,%d), Vol: %d, Pedido: %s, Límite: %s\n",
					p.getId(), p.getX(), p.getY(), p.getVolumen(),
					p.getFechaPedido() != null ? p.getFechaPedido().toLocalTime() : "N/A",
					p.getFechaLimiteEntrega() != null ? p.getFechaLimiteEntrega().toLocalTime() : "N/A"));

			// --- Planificación con Ant Colony Optimization ---
			System.out.println("\n======================================================");
			System.out.println("=      EJECUTANDO ANT COLONY OPTIMIZATION (ACO)    =");
			System.out.println("======================================================");
			long startTimeACO = System.currentTimeMillis();
			// Crear copias para evitar efectos secundarios entre algoritmos
			List<Camion> flotaACO = new ArrayList<>();
			flota.forEach(c -> flotaACO.add(Camion.builder()
					.codigo(c.getCodigo())
					.capacidad(c.getCapacidad())
					.velocidad(c.getVelocidad())
					.fechaInicio((Calendar) c.getFechaInicio().clone())
					.build()));
			List<Pedido> pedidosACO = new ArrayList<>(pedidos);

			List<Ruta> rutasACO = aco.planificar(flotaACO, pedidosACO);
			long endTimeACO = System.currentTimeMillis();

			System.out.println("\n--- Resultados ACO ---");
			System.out.println("Tiempo de ejecución: " + (endTimeACO - startTimeACO) + " ms");
			System.out.println("Total de rutas generadas: " + rutasACO.size());
			if (rutasACO.isEmpty() && !pedidos.isEmpty()) {
				System.out.println("ACO: No se encontraron rutas factibles.");
			} else {
				rutasACO.forEach(r -> System.out.printf("  Ruta ACO: %s -> %s (%d paradas)\n",
						r.getStartTime(), r.getEndTime(), r.getRuta() != null ? r.getRuta().size() : 0));
			}

			// --- Planificación con Genetic Algorithm ---
			System.out.println("\n======================================================");
			System.out.println("=        EJECUTANDO GENETIC ALGORITHM (GA)         =");
			System.out.println("======================================================");
			long startTimeGA = System.currentTimeMillis();
			// Crear copias nuevas
			List<Camion> flotaGA = new ArrayList<>();
			flota.forEach(c -> flotaGA.add(Camion.builder()
					.codigo(c.getCodigo())
					.capacidad(c.getCapacidad())
					.velocidad(c.getVelocidad())
					.fechaInicio((Calendar) c.getFechaInicio().clone())
					.build()));
			List<Pedido> pedidosGA = new ArrayList<>(pedidos);

			List<Ruta> rutasGA = ag.planificar(flotaGA, pedidosGA);
			long endTimeGA = System.currentTimeMillis();

			System.out.println("\n--- Resultados GA ---");
			System.out.println("Tiempo de ejecución: " + (endTimeGA - startTimeGA) + " ms");
			System.out.println("Total de rutas generadas: " + rutasGA.size());
			if (rutasGA.isEmpty() && !pedidos.isEmpty()) {
				System.out.println("GA: No se encontraron rutas factibles.");
			} else {
				rutasGA.forEach(r -> System.out.printf("  Ruta GA: %s -> %s (%d paradas)\n",
						r.getStartTime(), r.getEndTime(), r.getRuta() != null ? r.getRuta().size() : 0));
			}

			System.out.println("\n======================================================");
			System.out.println("=            DEMO PLANIFICACIÓN FINALIZADO           =");
			System.out.println("======================================================");
		};
    }   
