package edu.pucp.plg_back;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;
// import edu.pucp.plg_back.service.Planificador;
import edu.pucp.plg_back.service.impl.AlgoGenPlanificador;
import edu.pucp.plg_back.service.impl.AntColonyPlanificador;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PlgBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlgBackApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(AlgoGenPlanificador ag, AntColonyPlanificador aco) {
		// return args -> {
		// // --- flota demo ---
		// Camion c1 = Camion.builder()
		// .codigo("TD01").capacidad(5).velocidad(50).build();
		// Camion c2 = Camion.builder()
		// .codigo("TC01").capacidad(10).velocidad(50).build();
		// List<Camion> flota = List.of(c1, c2);

		// // --- pedidos demo ---
		// List<Pedido> pedidos = new ArrayList<>();
		// pedidos.add(new Pedido(1, "c-101", 5, 32, 18, null, null));
		// pedidos.add(new Pedido(2, "c-102", 4, 45, 10, null, null));
		// pedidos.add(new Pedido(3, "c-103", 3, 20, 35, null, null));

		// // --- planificación ---
		// List<Ruta> rutas = planner.planificar(flota, pedidos);

		// rutas.forEach(r -> System.out.printf("Ruta %s → %s\n",
		// r.getStartTime(), r.getEndTime()));
		// };

		return args -> {
			// --- flota demo extendida ---
			List<Camion> flota = new ArrayList<>();
			flota.add(Camion.builder().codigo("TD01").capacidad(5).velocidad(50).build());
			flota.add(Camion.builder().codigo("TC01").capacidad(10).velocidad(50).build());
			flota.add(Camion.builder().codigo("TD02").capacidad(7).velocidad(45).build());
			flota.add(Camion.builder().codigo("TC02").capacidad(12).velocidad(40).build());
			flota.add(Camion.builder().codigo("TD03").capacidad(6).velocidad(55).build());
			flota.add(Camion.builder().codigo("TC03").capacidad(15).velocidad(35).build());
			flota.add(Camion.builder().codigo("TD04").capacidad(4).velocidad(60).build());
			flota.add(Camion.builder().codigo("TC04").capacidad(8).velocidad(55).build());

			// --- pedidos demo extendidos ---
			List<Pedido> pedidos = new ArrayList<>();
			// Grupo 1: Pedidos urbanos cercanos
			pedidos.add(new Pedido(1, "c-101", 5, 32, 18, null, null));
			pedidos.add(new Pedido(2, "c-102", 4, 45, 10, null, null));
			pedidos.add(new Pedido(3, "c-103", 3, 20, 35, null, null));
			pedidos.add(new Pedido(4, "c-104", 6, 15, 25, null, null));
			pedidos.add(new Pedido(5, "c-105", 2, 28, 30, null, null));

			// Grupo 2: Pedidos urbanos distantes
			pedidos.add(new Pedido(6, "c-201", 8, 60, 45, null, null));
			pedidos.add(new Pedido(7, "c-202", 5, 75, 38, null, null));
			pedidos.add(new Pedido(8, "c-203", 3, 50, 60, null, null));
			pedidos.add(new Pedido(9, "c-204", 4, 65, 55, null, null));
			pedidos.add(new Pedido(10, "c-205", 7, 55, 70, null, null));

			// Grupo 3: Pedidos pequeños (1-2 unidades)
			pedidos.add(new Pedido(11, "c-301", 1, 40, 22, null, null));
			pedidos.add(new Pedido(12, "c-302", 2, 35, 15, null, null));
			pedidos.add(new Pedido(13, "c-303", 1, 25, 42, null, null));
			pedidos.add(new Pedido(14, "c-304", 2, 48, 30, null, null));
			pedidos.add(new Pedido(15, "c-305", 1, 53, 25, null, null));

			// Grupo 4: Pedidos grandes (9-15 unidades)
			pedidos.add(new Pedido(16, "c-401", 9, 80, 65, null, null));
			pedidos.add(new Pedido(17, "c-402", 12, 70, 75, null, null));
			pedidos.add(new Pedido(18, "c-403", 10, 85, 60, null, null));
			pedidos.add(new Pedido(19, "c-404", 15, 90, 80, null, null));
			pedidos.add(new Pedido(20, "c-405", 11, 75, 70, null, null));

			// Grupo 5: Pedidos extremos (geográficamente apartados)
			pedidos.add(new Pedido(21, "c-501", 4, 100, 10, null, null));
			pedidos.add(new Pedido(22, "c-502", 3, 10, 100, null, null));
			pedidos.add(new Pedido(23, "c-503", 5, 100, 100, null, null));
			pedidos.add(new Pedido(24, "c-504", 2, 5, 8, null, null));
			pedidos.add(new Pedido(25, "c-505", 6, 95, 95, null, null));

			System.out.println(
					"Planificando rutas para " + flota.size() + " camiones y " + pedidos.size() +
							" pedidos...");

			// --- planificación con Ant Colony Optimization ---
			long startTime = System.currentTimeMillis();
			List<Ruta> rutasACO = aco.planificar(flota, pedidos);
			long endTime = System.currentTimeMillis();

			System.out.println("Planificación completada en " + (endTime - startTime) + "ms");
			System.out.println("Total de rutas generadas: " + rutasACO.size());

			rutasACO.forEach(r -> System.out.printf("Ruta %s → %s\n",
					r.getStartTime(), r.getEndTime()));

			// // --- planificación con Genetic Algorithm ---
			// startTime = System.currentTimeMillis();
			// List<Ruta> rutasGA = ag.planificar(flota, pedidos);
			// endTime = System.currentTimeMillis();
			// System.out.println("Planificación GA completada en " + (endTime - startTime)
			// + "ms");
			// System.out.println("Total de rutas generadas GA: " + rutasGA.size());
			// rutasGA.forEach(r -> System.out.printf("Ruta %s → %s\n",
			// r.getStartTime(), r.getEndTime()));
		};
	}

	// @Bean
	// CommandLineRunner pruebaGA(AlgoGenPlanificador ga,
	// AntColonyPlanificador aco) {
	// return args -> {
	// // flota demo
	// Camion td01 =
	// Camion.builder().codigo("TD01").capacidad(5).velocidad(50).build();
	// Camion tc01 =
	// Camion.builder().codigo("TC01").capacidad(10).velocidad(50).build();
	// List<Camion> flota = List.of(td01, tc01);

	// // pedidos demo (coordenadas arbitrarias)
	// List<Pedido> pedidos = List.of(
	// new Pedido(1, "c-201", 4, 40, 10, null, null),
	// new Pedido(2, "c-202", 3, 22, 34, null, null),
	// new Pedido(3, "c-203", 5, 17, 25, null, null),
	// new Pedido(4, "c-204", 2, 60, 5, null, null));

	// System.out.println("=== Ant Colony ===");
	// aco.planificar(flota, pedidos).forEach(r -> System.out.println(r.getRuta()));

	// System.out.println("\n=== Genetic Algorithm ===");
	// ga.planificar(flota, pedidos).forEach(r -> System.out.println(r.getRuta()));
	// };
	// }
}
