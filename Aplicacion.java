import java.time.LocalDate;
import java.util.*;

final class Aplicacion {
    private Aplicacion() { }
    static void ejecutar(Cooperative c) {
        Scanner in = new Scanner(System.in);
        List<Pedido> pedidos = new ArrayList<>();
        while (true) {
            System.out.println("\n=== ACOPIOGT | " + c.date() + " ===");
            System.out.println("1 Pre-liquidar/recibir lote | 2 Registrar bodega | 3 Registrar pedido y despachar");
            System.out.println("4 Avanzar fecha | 5 Consultar | 6 Reportes | 0 Salir");
            try {
                int op = Integer.parseInt(in.nextLine());
                switch (op) {
                    case 1 -> recibir(c, in);
                    case 2 -> { System.out.print("Codigo: "); String code=in.nextLine(); System.out.print("Capacidad kg: "); double cap=Double.parseDouble(in.nextLine()); System.out.print("Camara fria (s/n): "); c.addWarehouse(new Warehouse(code,cap,in.nextLine().equalsIgnoreCase("s"))); }
                    case 3 -> despachar(c, in, pedidos);
                    case 4 -> { System.out.print("Dias: "); c.advance(Integer.parseInt(in.nextLine())); }
                    case 5 -> Reportes.lotesPorBodega(c.warehouses(), c.date());
                    case 6 -> { Reportes.existencias(c.lots()); Reportes.proximosVencimientos(c.lots(), c.date()); Reportes.lotesPorBodega(c.warehouses(), c.date()); c.reporteFinanciero(); }
                    case 0 -> { return; }
                    default -> System.out.println("Opcion invalida.");
                }
            } catch (Exception e) { System.out.println("Operacion rechazada: " + e.getMessage()); }
        }
    }
    private static void recibir(Cooperative c, Scanner in) {
        System.out.print("Codigo de lote: "); String code=in.nextLine();
        System.out.print("Productor: "); String producer=in.nextLine();
        System.out.print("Categoria (cafe/cafe organico/cardamomo/hortaliza/queso): "); String n=in.nextLine().toLowerCase();
        Category cat=switch(n){case "cafe"->new Coffee();case "cafe organico"->new OrganicCoffee();case "cardamomo"->new Cardamom();case "hortaliza"->new Vegetable();case "queso"->new FreshCheese();default->throw new IllegalArgumentException("Categoria invalida.");};
        System.out.print("Kg: "); double kg=Double.parseDouble(in.nextLine()); System.out.print("Calidad (A/B/C): "); Quality q=Quality.valueOf(in.nextLine().toUpperCase());
        System.out.print("Bodega: "); String b=in.nextLine(); Warehouse w=c.warehouses().stream().filter(x->x.code().equals(b)).findFirst().orElseThrow(()->new IllegalArgumentException("Bodega no encontrada."));
        double moisture=0, green=0, fat=0; int shelf=0; LocalDate cert=null;
        if(cat instanceof Coffee){System.out.print("Humedad: "); moisture=Double.parseDouble(in.nextLine());}
        if(cat instanceof OrganicCoffee){System.out.print("Sello valido hasta (AAAA-MM-DD): "); cert=LocalDate.parse(in.nextLine());}
        if(cat instanceof Cardamom){System.out.print("Porcentaje grano verde: "); green=Double.parseDouble(in.nextLine());}
        if(cat instanceof Vegetable){System.out.print("Vida util en dias: "); shelf=Integer.parseInt(in.nextLine());}
        if(cat instanceof FreshCheese){System.out.print("Porcentaje de grasa: "); fat=Double.parseDouble(in.nextLine());}
        Lot lot=new Lot(code,producer,c.date(),kg,q,cat,moisture,green,shelf,fat,cert); c.addLot(lot,w); c.preLiquidate(lot); System.out.println("Lote recibido correctamente.");
    }
    private static void despachar(Cooperative c, Scanner in, List<Pedido> pedidos) {
        System.out.print("Codigo del pedido: "); String code=in.nextLine(); System.out.print("Categoria exacta: "); String cat=in.nextLine(); System.out.print("Kg: "); double kg=Double.parseDouble(in.nextLine());
        Pedido p=new Pedido(code,cat,kg); c.dispatch(c.lots().stream().map(Lot::category).filter(x->x.name().equals(cat)).findFirst().orElseThrow(()->new IllegalArgumentException("Categoria sin existencia.")),kg); p.marcarDespachado(); pedidos.add(p); System.out.println("Pedido despachado: "+p);
    }
}
