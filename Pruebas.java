import java.time.LocalDate;

final class Pruebas {
    public static void main(String[] args) {
        Cooperative c = DemoData.create();
        Lot cafe = c.lots().stream().filter(l -> l.code().equals("CAF-1")).findFirst().orElseThrow();
        double pago = cafe.payment();
        if (Math.abs(pago - 5640.0) > 0.01) throw new AssertionError("Pago de cafe incorrecto: " + pago);
        if (cafe.expiration() != null) throw new AssertionError("El cafe no vence");
        boolean rechazado = false;
        try { new Vegetable().validate(new Lot("X","P",LocalDate.of(2026,1,1),10,Quality.A,new Vegetable(),0,0,2,0,null), LocalDate.of(2026,1,1)); }
        catch (IllegalArgumentException e) { rechazado = true; }
        if (!rechazado) throw new AssertionError("No se rechazo vida util invalida");
        System.out.println("Pruebas basicas aprobadas.");
    }
}
