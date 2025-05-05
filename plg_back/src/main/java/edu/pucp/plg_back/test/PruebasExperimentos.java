package edu.pucp.plg_back.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;
import edu.pucp.plg_back.service.impl.ACOPlanificador;
import edu.pucp.plg_back.service.impl.GAPlanificador;

public class PruebasExperimentos {
        public void testExperimentos(GAPlanificador ag, ACOPlanificador aco) {
                System.out.println("======================================================");
                System.out.println("=          INICIANDO DISEÑO DE EXPERIMENTOS          =");
                System.out.println("======================================================");
                System.out.println("Hora actual: " + LocalDateTime.now());

                List<Pedido> pedidos = new ArrayList<>();

                // --- Flota de Camiones (basado en QA.md Pregunta 4) ---
                List<Camion> flota = new ArrayList<>();
                // Set a start time for trucks
                Calendar startCal = Calendar.getInstance();
                startCal.set(Calendar.YEAR, 2025);
                startCal.set(Calendar.MONTH, Calendar.JANUARY); // Enero es 0 en Calendar
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
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

                // --- Casos de prueba ---
                CasoPruebaACO casosPruebaACO = new CasoPruebaACO();
                casosPruebaACO = CasoPruebaACO.builder().nant1(20).nant2(40).niter1(120).niter2(180).alpha1(0.5)
                                .alpha2(1.5).beta1(2).beta2(4).rho1(0.3).rho2(0.7).build();
                casosPruebaACO.generarCombinaciones();

                // Variables para las iteraciones de pruebas
                int resultadoNumero = 0;
                int pedidosLeidos = 0;
                double GLPPedido = 0.0;
                int contadorACO = 0;
                int contadorGA = 0;

                // Eliminar archivos de resultados anteriores
                String filepathDeleteACO = new File("").getAbsolutePath() + "\\data\\resultados\\resultadosACO.txt";
                File archivoResultadosDeleteACO = new File(filepathDeleteACO);
                if (archivoResultadosDeleteACO.exists()) {
                        archivoResultadosDeleteACO.delete();
                }
                String filepathDeleteGA = new File("").getAbsolutePath() + "\\data\\resultados\\resultadosGA.txt";
                File archivoResultadosDeleteGA = new File(filepathDeleteGA);
                if (archivoResultadosDeleteGA.exists()) {
                        archivoResultadosDeleteGA.delete();
                }

                // Ruta del directorio donde están los archivos de pedidos
                String directorioPedidos = new File("").getAbsolutePath() +
                                "\\data\\pedidos";

                File carpeta = new File(directorioPedidos);
                File[] archivos = carpeta.listFiles((dir, name) -> name.startsWith("ventas")
                                && name.endsWith(".txt"));

                if (archivos == null || archivos.length == 0) {
                        System.err.println("No se encontraron archivos en el directorio: " +
                                        directorioPedidos);
                        return;
                }

                // --- Leer archivos de pedidos y ejecutar las pruebas---
                for (File archivo : archivos) {
                        System.out.println("Leyendo archivo: " + archivo.getName());
                        String linea = "";

                        // Expresión regular para parsear cada línea del archivo
                        String regex = "(\\d+)d(\\d+)h(\\d+)m:(\\d+),(\\d+),c-(\\d+),(\\d+)m3,(\\d+)h";
                        Pattern pattern = Pattern.compile(regex);

                        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                                while ((linea = br.readLine()) != null) {
                                        Matcher matcher = pattern.matcher(linea);
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

                                                // Transformar los días, horas y minutos encontrados a LocalDateTime
                                                LocalDateTime fechaPedido = LocalDateTime.of(2025, 1, dia, hora,
                                                                minuto);

                                                // Sumar la fecha del pedido con la hora límite
                                                LocalDateTime fechaLimiteEntrega = fechaPedido.plus(horasLimite,
                                                                ChronoUnit.HOURS);

                                                // Calcular el GLP total de los pedidos
                                                GLPPedido = GLPPedido + volumen;
                                                pedidosLeidos++;
                                                if (pedidosLeidos >= 5000)
                                                        break; // Limitar el número de pedidos a 500
                                                if (GLPPedido >= 200) { // Límite de GLP de nuestra flota. Aca detenmos
                                                                        // la lectura y
                                                                        // planificamos
                                                        GLPPedido = 0.0; // Reiniciar el GLP total
                                                        aco.setParametrosACO(
                                                                        casosPruebaACO.combinacionACO[contadorACO]
                                                                                        .getAlpha(),
                                                                        casosPruebaACO.combinacionACO[contadorACO]
                                                                                        .getBeta(),
                                                                        casosPruebaACO.combinacionACO[contadorACO]
                                                                                        .getRho(),
                                                                        casosPruebaACO.combinacionACO[contadorACO]
                                                                                        .getNAnts(),
                                                                        casosPruebaACO.combinacionACO[contadorACO]
                                                                                        .getNIter()); // Actualizar
                                                                                                      // parámetros ACO
                                                        // --- Planificación con Ant Colony Optimization ---
                                                        long startTimeACO = System.currentTimeMillis();
                                                        // Crear copias para evitar efectos secundarios entre algoritmos
                                                        List<Camion> flotaACO = new ArrayList<>();
                                                        flota.forEach(c -> flotaACO.add(Camion.builder()
                                                                        .codigo(c.getCodigo())
                                                                        .capacidad(c.getCapacidad())
                                                                        .velocidad(c.getVelocidad())
                                                                        .fechaInicio((Calendar) c.getFechaInicio()
                                                                                        .clone())
                                                                        .build()));
                                                        List<Pedido> pedidosACO = new ArrayList<>(pedidos);
                                                        List<Ruta> rutasACO = aco.planificar(flotaACO, pedidosACO);
                                                        long endTimeACO = System.currentTimeMillis();
                                                        pedidos.clear(); // Limpiar la lista de pedidos para la
                                                                         // siguiente iteración

                                                        guardarResultados("resultadosACO.txt",
                                                                        endTimeACO - startTimeACO,
                                                                        casosPruebaACO.combinacionACO[contadorACO]);
                                                        resultadoNumero++;
                                                        System.out.println(
                                                                        "Resultados guardados en ACO hasta el momento: "
                                                                                        + String.valueOf(
                                                                                                        resultadoNumero));

                                                        if (contadorACO < 31) {
                                                                contadorACO++;
                                                        } else {
                                                                contadorACO = 0;
                                                        }
                                                } else {
                                                        // Añadir el pedido a la lista
                                                        pedidos.add(new Pedido(pedidos.size() + 1, idCliente, volumen,
                                                                        posX, posY,
                                                                        fechaPedido,
                                                                        fechaLimiteEntrega));
                                                }
                                        } else {
                                                System.err.println("Línea con formato incorrecto: " + linea);
                                        }
                                }
                        } catch (IOException e) {
                                System.err.printf("Error al leer el archivo: %s\n", archivo.getName());
                        } catch (NumberFormatException e) {
                                System.err.println("Error al parsear número en la línea: " + linea);
                        }
                }

                System.out.printf("Total de pedidos leídos: %d.\n", pedidosLeidos);
        }

        public void guardarResultados(String ruta, Long resultados, CombinacionACO combinacion) {
                // --- Archivos donde escribir los resultados ---
                String filepath = new File("").getAbsolutePath() + "\\data\\resultados\\" + ruta;

                File archivoResultados = new File(filepath);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoResultados, true))) {
                        writer.write(String.valueOf(combinacion.getNAnts()) + "," +
                                        String.valueOf(combinacion.getNIter()) + "," +
                                        String.valueOf(combinacion.getAlpha()) + "," +
                                        String.valueOf(combinacion.getBeta()) + "," +
                                        String.valueOf(combinacion.getRho()) + "," +
                                        String.valueOf(resultados) + "\n");
                        System.out.println("File written successfully to " + archivoResultados.getAbsolutePath());
                } catch (java.io.IOException e) {
                        e.printStackTrace();
                }
        }
}
