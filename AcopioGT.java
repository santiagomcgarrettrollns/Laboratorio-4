import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

final class AcopioGT {
    static void iniciar() { Cooperative acopio = DemoData.create(); acopio.runMenu(); }
}

enum Quality { A, B, C }
enum LotState { AVAILABLE, EXHAUSTED, DISCARDED }

record Settlement(double amount, LocalDate expiration) { }

interface Perishable {
    LocalDate expirationDate();
}

interface ColdStorageRequired {
    boolean requiresColdStorage();
}

abstract class Category {
    private final String name;
    protected Category(String name) { this.name = name; }
    public String name() { return name; }
    public abstract double calculatePayment(Lot lot);
    public abstract LocalDate expirationDate(Lot lot);
    public abstract boolean requiresColdStorage();
    public abstract void validate(Lot lot, LocalDate operationDate);
}

class Coffee extends Category {
    Coffee() { super("Cafe pergamino"); }
    public double calculatePayment(Lot l) {
        double base = l.receivedKg() * switch (l.quality()) { case A -> 12; case B -> 9; case C -> 6; };
        long points = (long) Math.ceil(Math.max(0, l.moisture() - 12));
        return base * (1 - points * .03);
    }
    public LocalDate expirationDate(Lot l) { return null; }
    public boolean requiresColdStorage() { return false; }
    public void validate(Lot l, LocalDate d) { if (l.moisture() < 8 || l.moisture() > 14) throw new IllegalArgumentException("La humedad debe estar entre 8 y 14."); }
}

final class OrganicCoffee extends Coffee {
    OrganicCoffee() { super(); }
    public String toString() { return "Cafe organico"; }
    public double calculatePayment(Lot l) { return super.calculatePayment(l) + l.receivedKg() * .75; }
    public void validate(Lot l, LocalDate d) { super.validate(l, d); if (l.certificationValidUntil() == null || d.isAfter(l.certificationValidUntil())) throw new IllegalArgumentException("El sello organico no es valido en la fecha de ingreso."); }
}

final class Cardamom extends Category {
    Cardamom() { super("Cardamomo"); }
    public double calculatePayment(Lot l) { double base=l.receivedKg()*switch(l.quality()){case A->60;case B->48;case C->35;}; return l.greenGrainPercent()<85?base*1.10:base; }
    public LocalDate expirationDate(Lot l) { return l.entryDate().plusDays(180); }
    public boolean requiresColdStorage() { return false; }
    public void validate(Lot l, LocalDate d) { if(l.greenGrainPercent()<0||l.greenGrainPercent()>100) throw new IllegalArgumentException("El grano verde debe estar entre 0 y 100."); }
}

final class Vegetable extends Category {
    Vegetable() { super("Hortaliza"); }
    public double calculatePayment(Lot l) { double base=l.receivedKg()*switch(l.quality()){case A->4;case B->3;case C->2;}; return base*.95; }
    public LocalDate expirationDate(Lot l) { return l.entryDate().plusDays(l.shelfLifeDays()); }
    public boolean requiresColdStorage() { return true; }
    public void validate(Lot l, LocalDate d) { if(l.shelfLifeDays()<3||l.shelfLifeDays()>14) throw new IllegalArgumentException("La vida util debe estar entre 3 y 14 dias."); }
}

final class FreshCheese extends Category {
    FreshCheese() { super("Queso fresco"); }
    public double calculatePayment(Lot l) { if(l.quality()==Quality.C) throw new IllegalArgumentException("No se acepta queso de calidad C."); double base=l.receivedKg()*(l.quality()==Quality.A?38:30); return base-l.receivedKg()*1.50; }
    public LocalDate expirationDate(Lot l) { return l.entryDate().plusDays(21); }
    public boolean requiresColdStorage() { return true; }
    public void validate(Lot l, LocalDate d) { if(l.fatPercent()<10||l.fatPercent()>40) throw new IllegalArgumentException("La grasa debe estar entre 10 y 40."); if(l.quality()==Quality.C) throw new IllegalArgumentException("No se acepta queso de calidad C."); }
}

final class Lot {
    private final String code, producer; private final LocalDate entryDate; private final double receivedKg; private double availableKg; private final Quality quality; private final Category category; private LotState state=LotState.AVAILABLE;
    private final double moisture, greenGrainPercent, fatPercent; private final int shelfLifeDays; private final LocalDate certificationValidUntil; private Warehouse warehouse;
    Lot(String code,String producer,LocalDate date,double kg,Quality q,Category c,double moisture,double green,int shelf, double fat,LocalDate cert){this.code=code;this.producer=producer;entryDate=date;receivedKg=kg;availableKg=kg;quality=q;category=c;this.moisture=moisture;greenGrainPercent=green;shelfLifeDays=shelf;fatPercent=fat;certificationValidUntil=cert;}
    String code(){return code;} String producer(){return producer;} LocalDate entryDate(){return entryDate;} double receivedKg(){return receivedKg;} double availableKg(){return availableKg;} Quality quality(){return quality;} Category category(){return category;} double moisture(){return moisture;} double greenGrainPercent(){return greenGrainPercent;} double fatPercent(){return fatPercent;} int shelfLifeDays(){return shelfLifeDays;} LocalDate certificationValidUntil(){return certificationValidUntil;} LotState state(){return state;} Warehouse warehouse(){return warehouse;}
    void assign(Warehouse w){warehouse=w;} double payment(){return category.calculatePayment(this);} LocalDate expiration(){return category.expirationDate(this);}
    void consume(double kg){ if(kg<=0||kg>availableKg) throw new IllegalArgumentException("Cantidad invalida."); availableKg-=kg; if(availableKg==0)state=LotState.EXHAUSTED; }
    void discard(){if(state==LotState.AVAILABLE)state=LotState.DISCARDED;}
    boolean isExpired(LocalDate d){return expiration()!=null && d.isAfter(expiration());}
    public String toString(){return code+" | "+category.name()+" | disponible: "+String.format("%.2f",availableKg)+" kg | "+state;}
}

final class Warehouse {
    private final String code; private final double capacity; private final boolean cold; private final List<Lot> lots=new ArrayList<>();
    Warehouse(String code,double capacity,boolean cold){if(capacity<=0||code.isBlank())throw new IllegalArgumentException("Datos de bodega invalidos.");this.code=code;this.capacity=capacity;this.cold=cold;}
    double used(){return lots.stream().filter(l->l.state()==LotState.AVAILABLE).mapToDouble(Lot::availableKg).sum();} boolean canReceive(Lot l){return used()+l.receivedKg()<=capacity && (!l.category().requiresColdStorage()||cold);}
    void receive(Lot l){if(!canReceive(l))throw new IllegalArgumentException("La bodega no tiene capacidad o no es compatible.");l.assign(this);lots.add(l);} List<Lot> ordered(LocalDate d){return lots.stream().filter(l->l.state()==LotState.AVAILABLE).sorted(Comparator.comparing((Lot l)->l.expiration()==null?LocalDate.MAX:l.expiration()).thenComparing(Lot::entryDate).thenComparing(l->l.quality().ordinal()).thenComparing(Lot::code)).toList();}
    String code(){return code;} boolean cold(){return cold;} List<Lot> lots(){return lots;}
}

final class Cooperative {
    private final Finanzas finanzas=new Finanzas();
    private final List<Warehouse> warehouses=new ArrayList<>(); private final List<Lot> lots=new ArrayList<>(); private final List<Order> orders=new ArrayList<>(); private LocalDate date=LocalDate.of(2026,1,1);
    void addWarehouse(Warehouse w){if(warehouses.stream().anyMatch(x->x.code().equals(w.code())))throw new IllegalArgumentException("Codigo repetido.");warehouses.add(w);} void addLot(Lot l,Warehouse w){if(lots.stream().anyMatch(x->x.code().equals(l.code())))throw new IllegalArgumentException("Codigo repetido.");l.category().validate(l,date);w.receive(l);lots.add(l);finanzas.pago(l.category().name(),l.payment());}
    void preLiquidate(Lot l){System.out.printf("Pago: Q%.2f | Vencimiento: %s%n",l.payment(),l.expiration()==null?"No vence":l.expiration());} void advance(int days){if(days<0)throw new IllegalArgumentException("La fecha no puede retroceder.");date=date.plusDays(days);lots.stream().filter(l->l.isExpired(date)&&l.state()==LotState.AVAILABLE).forEach(l->{finanzas.perdida(l.category().name(),l.availableKg()*l.payment()/l.receivedKg());l.discard();});}
    void dispatch(Category c,double kg){if(kg<=0)throw new IllegalArgumentException("Cantidad invalida.");List<Lot> candidates=warehouses.stream().flatMap(w->w.ordered(date).stream()).filter(l->l.category().name().equals(c.name())).toList();if(candidates.stream().mapToDouble(Lot::availableKg).sum()<kg)throw new IllegalArgumentException("El pedido no puede cubrirse completo.");double remaining=kg,total=0;for(Lot l:candidates){double take=Math.min(remaining,l.availableKg());l.consume(take);total+=take*l.payment()/l.receivedKg()*1.35;remaining-=take;if(remaining==0)break;}finanzas.cobro(c.name(),total);System.out.printf("Despacho cobrado: Q%.2f%n",total);}
    void reporteFinanciero(){finanzas.imprimir();}
    void runMenu(){java.util.Scanner s=new java.util.Scanner(System.in);boolean salir=false;while(!salir){System.out.println("\\n=== ACOPIOGT ===");System.out.println("Fecha: "+date);System.out.println("1. Pre-liquidar lote");System.out.println("2. Consultar lotes y bodegas");System.out.println("3. Avanzar fecha");System.out.println("4. Despachar pedido");System.out.println("5. Reportes");System.out.println("0. Salir");try{int op=Integer.parseInt(s.nextLine());switch(op){case 1-> {System.out.print("Codigo: ");String c=s.nextLine();lots.stream().filter(l->l.code().equals(c)).findFirst().ifPresentOrElse(this::preLiquidate,()->System.out.println("Lote no encontrado."));}case 2->{for(Warehouse w:warehouses){System.out.println("Bodega "+w.code());w.ordered(date).forEach(System.out::println);}}case 3->{System.out.print("Dias: ");advance(Integer.parseInt(s.nextLine()));}case 4->{System.out.print("Categoria exacta: ");String n=s.nextLine();System.out.print("Kg: ");double kg=Double.parseDouble(s.nextLine());Category cat=lots.stream().map(Lot::category).filter(x->x.name().equals(n)).findFirst().orElseThrow(()->new IllegalArgumentException("Categoria no encontrada."));dispatch(cat,kg);}case 5->{for(Category cat:lots.stream().map(Lot::category).distinct().toList())System.out.printf("%s: %.2f kg%n",cat.name(),lots.stream().filter(l->l.category().name().equals(cat.name())&&l.state()==LotState.AVAILABLE).mapToDouble(Lot::availableKg).sum());}case 0->salir=true;default->System.out.println("Opcion invalida.");}}catch(Exception e){System.out.println("Operacion rechazada: "+e.getMessage());}}}
    List<Lot> lots(){return lots;} List<Warehouse> warehouses(){return Collections.unmodifiableList(warehouses);} LocalDate date(){return date;}
}

final class Order { }
final class DemoData {
    static Cooperative create(){Cooperative c=new Cooperative();Warehouse dry=new Warehouse("S-01",3000,false),cold=new Warehouse("F-01",2000,true),dry2=new Warehouse("S-02",2500,false);c.addWarehouse(dry);c.addWarehouse(cold);c.addWarehouse(dry2);LocalDate d=LocalDate.of(2025,12,25);for(int i=1;i<=2;i++){c.addLot(new Lot("CAF-"+i,"Productor "+i,d,500,Quality.A,new Coffee(),13.4,0,0,0,null),dry);c.addLot(new Lot("ORG-"+i,"Organico "+i,d,500,Quality.B,new OrganicCoffee(),11,0,0,0,LocalDate.of(2026,12,31)),dry);c.addLot(new Lot("CAR-"+i,"Cardamomo "+i,d,80,Quality.B,new Cardamom(),0,90,0,0,null),dry);c.addLot(new Lot("HOR-"+i,"Hortaliza "+i,d,200,Quality.A,new Vegetable(),0,0,7,0,null),cold);c.addLot(new Lot("QUE-"+i,"Quesero "+i,d,40,Quality.A,new FreshCheese(),0,0,0,20,null),cold);}return c;}
}



