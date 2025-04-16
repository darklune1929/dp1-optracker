package edu.pucp.plg_back;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// import javax.xml.crypto.Data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;
import edu.pucp.plg_back.service.Planificador;
import edu.pucp.plg_back.service.impl.AlgoGenPlanificador;
import edu.pucp.plg_back.service.impl.AntColonyPlanificador;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class PlgBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlgBackApplication.class, args);
		// System.out.println("Hello World");

		List<Camion> flota = List.of(
				new Camion("TA01", 25, 2.5),
				new Camion("TB01", 15, 2.0),
				new Camion("TC01", 10, 1.5));

		List<Pedido> pedidos = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			pedidos.add(
					new Pedido(i + 1, "C" + (100 + i), randInt(3, 8),
							randInt(0, 70),
							randInt(0, 50),
							LocalDateTime.now(), randInt(8, 48)));
		}

		// 2. Ejecutar Ant Colony ------------------------------
		Planificador aco = new AntColonyPlanificador(50, 20);
		List<Ruta> rutasACO = aco.planificarRutas(pedidos, flota);
		System.out.println("\n--- Resultados Ant Colony Optimization ---");
		rutasACO.forEach(System.out::println);

		Planificador ga = new AlgoGenPlanificador(40, 100, 0.8, 0.2);
		List<Ruta> rutasGA = ga.planificarRutas(pedidos, flota);
		System.out.println("\n--- Resultados Genetic Algorithm ---");
		rutasGA.forEach(System.out::println);
	}

	public static int randInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

}
