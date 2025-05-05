package edu.pucp.plg_back;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

// import edu.pucp.plg_back.service.Planificador; // Keep if needed elsewhere
import edu.pucp.plg_back.service.impl.GAPlanificador;
import edu.pucp.plg_back.service.impl.ACOPlanificador;
import edu.pucp.plg_back.test.PruebasExperimentos;
import edu.pucp.plg_back.test.PruebasHardcodeadas;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PlgBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlgBackApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(GAPlanificador ag, ACOPlanificador aco) {

		return args -> {
			PruebasHardcodeadas ph = new PruebasHardcodeadas();
			//ph.testHardcodeadas(ag, aco); // Uncomment to run hardcoded tests

			PruebasExperimentos pe = new PruebasExperimentos();
			pe.testExperimentos(ag, aco);
		};
	}

}
