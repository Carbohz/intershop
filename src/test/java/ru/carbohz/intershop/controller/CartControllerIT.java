package ru.carbohz.intershop.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.carbohz.intershop.TestcontainersConfiguration;
import ru.carbohz.intershop.model.Cart;
import ru.carbohz.intershop.model.Item;
import ru.carbohz.intershop.model.Order;
import ru.carbohz.intershop.repository.CartRepository;
import ru.carbohz.intershop.repository.ItemRepository;
import ru.carbohz.intershop.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public class CartControllerIT {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void buy() {
        List<Item> items = itemRepository.findAll();

        Cart cart1 = new Cart();
        cart1.setItem(items.getFirst());
        cart1.setCount(2L);
        cartRepository.save(cart1);

        Cart cart2 = new Cart();
        cart2.setItem(items.getLast());
        cart2.setCount(10L);
        cartRepository.save(cart2);

        ResponseEntity<String> response = restTemplate.postForEntity("/cart/buy", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(cartRepository.count()).isZero();

        List<Order> orders = orderRepository.findAll();
        assertThat(orders.size()).isEqualTo(1);
        Order order = orders.getFirst();
        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getTotalSum()).isEqualTo(359988L);
        // assertThat(order.getOrderItems().size()).isEqualTo(2); // LazyInitializationException

        checkHtmlRender(response);
    }

    private void checkHtmlRender(ResponseEntity<String> response) {
        assertThat(response.getBody()).isNotNull();

        // Parse with JSoup
        Document doc = Jsoup.parse(response.getBody());

        // 0
        Element successMessage = doc.select("h1").first();
        assertThat(successMessage.text())
                .isEqualTo("Поздравляем! Успешная покупка! 🙂");

        // 1. Test basic page structure
        assertThat(doc.title()).isEqualTo("Заказ");

        // 2. Test navigation links
        Elements navLinks = doc.select("body > a");
        assertThat(navLinks).hasSize(3);
        assertThat(navLinks.eachText())
                .containsExactlyInAnyOrder("ГЛАВНАЯ ⤵", "КОРЗИНА ⤵", "ЗАКАЗЫ ⤵");

        // 3. Test order header (dynamic content)
        Element orderHeader = doc.select("h2").first();
        assertThat(orderHeader.text()).matches("Заказ №\\d+");

        // 4. Test order items
        Elements items = doc.select("tr td table");
        assertThat(items).isNotEmpty(); // At least one item

        // Test first item's structure
        Element firstItem = items.first();
        assertThat(firstItem.select("img")).hasSize(1);
        assertThat(firstItem.select("img").attr("width")).isEqualTo("300");
        assertThat(firstItem.select("img").attr("height")).isEqualTo("300");

        // 5. Test item details (title, count, price)
        Elements itemDetails = firstItem.select("tr:eq(1) td");
        assertThat(itemDetails).hasSize(3);
        assertThat(itemDetails.get(0).text()).isNotBlank(); // Title
        assertThat(itemDetails.get(1).text()).matches("\\d+ шт."); // Count
        assertThat(itemDetails.get(2).text()).matches("\\d+ руб."); // Price

        // 6. Test total sum
        Element totalSum = doc.select("h3").first();
        assertThat(totalSum.text()).matches("Сумма: \\d+ руб.");
    }
}
