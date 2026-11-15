import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

final class Reportes {
    private Reportes() { }

    static void existencias(List<Lot> lotes) {
        System.out.println("\n=== EXISTENCIAS POR CATEGORIA ===");
        Map<String, Double> total = lotes.stream().filter(l -> l.state() == LotState.AVAILABLE)
                .collect(Collectors.groupingBy(l -> l.category().name(), TreeMap::new, Collectors.summingDouble(Lot::availableKg)));
        total.forEach((categoria, kg) -> System.out.printf(Locale.US, "%s: %.2f kg%n", categoria, kg));
    }

    static void proximosVencimientos(List<Lot> lotes, LocalDate fecha) {
        System.out.println("\n=== VENCIMIENTOS EN LOS PROXIMOS 3 DIAS ===");
        lotes.stream().filter(l -> l.state() == LotState.AVAILABLE && l.expiration() != null)
                .filter(l -> !l.expiration().isBefore(fecha) && !l.expiration().isAfter(fecha.plusDays(3)))
                .sorted(Comparator.comparing(Lot::expiration)).forEach(l -> System.out.println(l.code() + " | " + l.category().name() + " | " + l.expiration()));
    }

    static void lotesPorBodega(List<Warehouse> bodegas, LocalDate fecha) {
        System.out.println("\n=== LOTES POR BODEGA EN ORDEN DE SALIDA ===");
        for (Warehouse b : bodegas) { System.out.println("Bodega " + b.code()); b.ordered(fecha).forEach(l -> System.out.println("  " + l)); }
    }
}
