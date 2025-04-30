package edu.pucp.plg_back;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;
import edu.pucp.plg_back.service.impl.ACOPlanificador;
// import edu.pucp.plg_back.service.Planificador; // Keep if needed elsewhere
import edu.pucp.plg_back.service.impl.GAPlanificador;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PlgBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlgBackApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(GAPlanificador ag, ACOPlanificador aco) {

		return args -> {
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

			// Inicializador del año y mes de los pedidos
			int añoLectura = 2025;
			int mesLectura = 1;
			String nombreArchivo = String.format("ventas%d%02d.txt", añoLectura, mesLectura);
			String routeArchivo = "\\data\\pedidos\\" + nombreArchivo;
			String filePath = new File("").getAbsolutePath();
			filePath = filePath + routeArchivo;
			System.out.println("Ruta ecnontrada: " + filePath);
			String linea = "";

			// Expresión regular para parsear cada línea del archivo
			String regex = "(\\d+)d(\\d+)h(\\d+)m:(\\d+),(\\d+),c-(\\d+),(\\d+)m3,(\\d+)h";
			Pattern pattern = Pattern.compile(regex);
			try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
				while ((linea = br.readLine()) != null) {
					Matcher matcher = pattern.matcher(linea);
					if (pedidos.size() == 10) {
						if (mesLectura == 12) {
							añoLectura++;
							mesLectura = 1;
						} else {
							mesLectura++;
						}
						break;
					}
					if (matcher.matches()) {
						// Capturar los grupos de la expresión regular
						int dia = Integer.parseInt(matcher.group(1));
						int hora = Integer.parseInt(matcher.group(2));
						int minuto = Integer.parseInt(matcher.group(3));
						int posX = Integer.parseInt(matcher.group(4));
						int posY = Integer.parseInt(matcher.group(5));
						String idCliente = "c-" + matcher.group(6);
						int volumen = Integer.parseInt(matcher.group(7));
						int horasLimite = Integer.parseInt(matcher.group(8));

						// Se tranforma los dias, horas y minutos encontrados a LocalDate
						LocalDateTime fechaPedido = LocalDateTime.of(añoLectura, mesLectura, dia, hora, minuto);

						// Se suma la fecha del pedido con la hora limite
						LocalDateTime fechaLimiteEntrega = fechaPedido.plus(horasLimite, ChronoUnit.HOURS);

						// Añadir el pedido a la lista
						pedidos.add(new Pedido(dia, idCliente, volumen, posX, posY, fechaPedido, fechaLimiteEntrega));

					} else {
						System.err.println("Línea con formato incorrecto: " + linea);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error al leer el archivo: " + nombreArchivo);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("Error al parsear número en la línea: " + linea);
			}

			// ---------------------------------------------------
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

}
