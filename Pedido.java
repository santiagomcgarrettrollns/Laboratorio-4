import java.time.LocalDateTime;

final class Pedido {
    private final String codigo;
    private final String categoria;
    private final double kilogramos;
    private final LocalDateTime fecha;
    private boolean despachado;

    Pedido(String codigo, String categoria, double kilogramos) {
        if (codigo == null || codigo.isBlank() || categoria == null || categoria.isBlank() || kilogramos <= 0)
            throw new IllegalArgumentException("Codigo, categoria y kilogramos deben ser validos.");
        this.codigo = codigo; this.categoria = categoria; this.kilogramos = kilogramos;
        this.fecha = LocalDateTime.now();
    }
    String codigo() { return codigo; }
    String categoria() { return categoria; }
    double kilogramos() { return kilogramos; }
    boolean despachado() { return despachado; }
    void marcarDespachado() { despachado = true; }
    @Override public String toString() { return codigo + " | " + categoria + " | " + kilogramos + " kg | " + (despachado ? "DESPACHADO" : "PENDIENTE"); }
}
