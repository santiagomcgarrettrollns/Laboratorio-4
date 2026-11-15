import java.util.*;

final class Finanzas {
    private final Map<String, Double> pagos = new TreeMap<>();
    private final Map<String, Double> cobros = new TreeMap<>();
    private final Map<String, Double> perdidas = new TreeMap<>();
    void pago(String categoria, double monto) { pagos.merge(categoria, monto, Double::sum); }
    void cobro(String categoria, double monto) { cobros.merge(categoria, monto, Double::sum); }
    void perdida(String categoria, double monto) { perdidas.merge(categoria, monto, Double::sum); }
    void imprimir() {
        Set<String> categorias = new TreeSet<>(); categorias.addAll(pagos.keySet()); categorias.addAll(cobros.keySet()); categorias.addAll(perdidas.keySet());
        System.out.println("\n=== REPORTE FINANCIERO POR CATEGORIA ===");
        System.out.printf("%-20s %12s %12s %12s%n", "Categoria", "Pagado", "Cobrado", "Perdida");
        for (String c : categorias) System.out.printf(Locale.US, "%-20s Q%10.2f Q%10.2f Q%10.2f%n", c, pagos.getOrDefault(c,0.0), cobros.getOrDefault(c,0.0), perdidas.getOrDefault(c,0.0));
        System.out.printf(Locale.US, "%-20s Q%10.2f Q%10.2f Q%10.2f%n", "TOTAL", pagos.values().stream().mapToDouble(Double::doubleValue).sum(), cobros.values().stream().mapToDouble(Double::doubleValue).sum(), perdidas.values().stream().mapToDouble(Double::doubleValue).sum());
    }
}
