package com.inventory.config;

import com.inventory.entity.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Seeds demo data on first startup so the dashboard looks populated
 * immediately (see blueprint section 42, "Demo Mode"). Disable by setting
 * app.demo.seed=false in application.properties.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.seed:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || businessRepository.count() > 0) {
            return;
        }
        log.info("Seeding demo data...");

        Business business = businessRepository.save(Business.builder()
                .name("Demo Retail Co.")
                .currency("INR")
                .email("demo@business.com")
                .build());

        userRepository.save(User.builder()
                .name("Demo Admin")
                .email("admin@demo.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.BUSINESS_ADMIN)
                .business(business)
                .active(true)
                .build());

        Category electronics = categoryRepository.save(Category.builder()
                .name("Electronics").description("Electronic accessories").business(business).build());
        Category stationery = categoryRepository.save(Category.builder()
                .name("Stationery").description("Office supplies").business(business).build());

        Supplier supplier1 = supplierRepository.save(Supplier.builder()
                .name("TechSource Traders").company("TechSource Pvt Ltd").email("sales@techsource.com")
                .phone("9876543210").leadTimeDays(5).rating(4.2).status("ACTIVE").business(business).build());
        Supplier supplier2 = supplierRepository.save(Supplier.builder()
                .name("OfficeMart Supplies").company("OfficeMart").email("orders@officemart.com")
                .phone("9876500000").leadTimeDays(3).rating(4.7).status("ACTIVE").business(business).build());

        customerRepository.save(Customer.builder()
                .name("Walk-in Customer").customerType("REGULAR").business(business).build());
        Customer regular = customerRepository.save(Customer.builder()
                .name("Priya Sharma").email("priya@example.com").phone("9000011111")
                .customerType("REGULAR").business(business).build());

        Product mouse = productRepository.save(Product.builder()
                .sku("ELEC-MOUSE-01").name("Wireless Mouse").description("2.4GHz wireless mouse")
                .category(electronics).supplier(supplier1).unit("pcs")
                .purchasePrice(350.0).sellingPrice(599.0).tax(18.0)
                .minStock(10).maxStock(200).reorderPoint(30).leadTimeDays(5)
                .currentStock(45).status("ACTIVE").business(business).build());

        Product keyboard = productRepository.save(Product.builder()
                .sku("ELEC-KEYB-01").name("Mechanical Keyboard").description("RGB mechanical keyboard")
                .category(electronics).supplier(supplier1).unit("pcs")
                .purchasePrice(1200.0).sellingPrice(1999.0).tax(18.0)
                .minStock(5).maxStock(100).reorderPoint(15).leadTimeDays(7)
                .currentStock(120).status("ACTIVE").business(business).build());

        Product monitor = productRepository.save(Product.builder()
                .sku("ELEC-MON-01").name("24-inch Monitor").description("Full HD LED monitor")
                .category(electronics).supplier(supplier1).unit("pcs")
                .purchasePrice(6500.0).sellingPrice(8999.0).tax(18.0)
                .minStock(3).maxStock(50).reorderPoint(10).leadTimeDays(10)
                .currentStock(8).status("ACTIVE").business(business).build());

        Product notebook = productRepository.save(Product.builder()
                .sku("STAT-NB-01").name("Ruled Notebook (200pg)").description("A5 ruled notebook")
                .category(stationery).supplier(supplier2).unit("pcs")
                .purchasePrice(35.0).sellingPrice(70.0).tax(5.0)
                .minStock(50).maxStock(1000).reorderPoint(100).leadTimeDays(3)
                .currentStock(850).status("ACTIVE").business(business).build());

        Product pen = productRepository.save(Product.builder()
                .sku("STAT-PEN-01").name("Gel Pen (Blue)").description("Smooth gel pen")
                .category(stationery).supplier(supplier2).unit("pcs")
                .purchasePrice(8.0).sellingPrice(15.0).tax(5.0)
                .minStock(100).maxStock(2000).reorderPoint(200).leadTimeDays(3)
                .currentStock(60).status("ACTIVE").business(business).build());

        // Generate ~28 days of synthetic historical sales so the demand
        // prediction engine has real data to compute averages/trends from.
        List<Product> demoProducts = List.of(mouse, keyboard, monitor, notebook, pen);
        Random random = new Random(42);

        for (int daysAgo = 28; daysAgo >= 1; daysAgo--) {
            LocalDateTime saleDate = LocalDateTime.now().minusDays(daysAgo).withHour(11).withMinute(0);
            for (Product product : demoProducts) {
                // Skew notebook/pen and mouse to trend upward recently; monitor stays low volume.
                int baseQty = switch (product.getSku()) {
                    case "ELEC-MOUSE-01" -> 2 + random.nextInt(3);
                    case "ELEC-KEYB-01" -> random.nextInt(2);
                    case "ELEC-MON-01" -> random.nextInt(2) == 0 ? 0 : 1;
                    case "STAT-NB-01" -> 5 + random.nextInt(6);
                    case "STAT-PEN-01" -> 8 + random.nextInt(10);
                    default -> 1;
                };
                // Trend boost: more recent days sell a bit more, to create a RISING trend signal.
                int recencyBoost = daysAgo <= 14 ? 1 : 0;
                int qty = baseQty + recencyBoost;
                if (qty <= 0) continue;

                Sale sale = Sale.builder()
                        .customer(daysAgo % 3 == 0 ? regular : null)
                        .saleDate(saleDate)
                        .discount(0.0)
                        .tax(0.0)
                        .paymentStatus("PAID")
                        .business(business)
                        .build();

                double price = product.getSellingPrice();
                SaleItem item = SaleItem.builder().sale(sale).product(product).quantity(qty).price(price).build();
                sale.getItems().add(item);
                sale.setTotalAmount(price * qty);
                saleRepository.save(sale);
            }
        }

        log.info("Demo data seeded. Login with admin@demo.com / Admin@123");
    }
}
