# Проект intershop

## Используемый стек

- Spring Boot
- Spring WebFlux + Netty
- Spring Data R2DBC
- r2dbc-postgresql для PostgreSQL
- Spring Data Redis для кеширования товаров
- OpenAPI дляразработки RESTful-сервиса платежей на реактивном стеке (клиент и сервер)
- Spring Boot Test, @DataR2dbcTest, @WebFluxTest и Testcontainers для тестов
- gradle как система сборки
- jib для сборки docker-образов и docker compose для развертывания приложения
- spring-boot-docker-compose для локальной разработки

## Использование приложения
- склонировать репозиторий
```shell
git clone https://github.com/Carbohz/intershop.git
```
- перейти в директорию с репозиторием
```shell
cd intershop
```
- переключится на тег v3.0
```shell
git checkout tags/v3.0
```
### Запуск локально (Intellij Idea)
- открыть проект в Intellij Idea
- запустить Docker Server
- запустить сервис платежей через класс `PaymentApplication`
- запустить сервис "Витрина интернет-магазина" через класс `ShopApplication`, выбрав профиль local
- перейти по ссылке http://localhost:8080/

### Запуск в Docker
- собрать образ
```shell
./gradlew jibDockerBuild
```
- поднять контейнеры через docker-compose
```shell
docker-compose up -d 
```
- перейти по ссылке http://localhost:8080/

### Запуск тестов
- запустить Docker Server
- запустить тесты командой
```shell
./gradlew test
```

## Основной функционал (сервис "Витрина интернет-магазина")

В приложении используются следующие эндпоинты/страницы приложения:

а) GET "/" - редирект на "/main/items"

б) GET "/main/items" - список всех товаров плиткой на главной странице

Параметры:
- search - строка с поисков по названию/описанию товара (по умолчанию, пустая строка - все товары)
- sort - сортировка перечисление NO, ALPHA, PRICE (по умолчанию, NO - не использовать сортировку)
- pageSize - максимальное число товаров на странице (по умолчанию, 10)
- pageNumber - номер текущей страницы (по умолчанию, 1)

Возвращает:
- шаблон "main.html"

в) POST "/main/items/{id}" - изменить количество товара в корзине

Параметры:
- action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)

Возвращает:
- редирект на "/main/items"

г) GET "/cart/items" - список товаров в корзине

Возвращает:
- шаблон "cart.html"

д) POST "/cart/items/{id}" - изменить количество товара в корзине

Параметры:
- action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)

Возвращает:
- редирект на "/cart/items"

е) GET "/items/{id}" - карточка товара

Возвращает:
- шаблон "item.html"

ж) POST "/items/{id}" - изменить количество товара в корзине

Параметры:
- action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)

Возвращает:
- редирект на "/items/{id}"

з) POST "/buy" - купить товары в корзине (выполняет покупку товаров в корзине и очищает ее)

Возвращает:
- редирект на "/orders/{id}?newOrder=true"

и) GET "/orders" - список заказов

Возвращает:
- шаблон "orders.html"

к) GET "/orders/{id}" - карточка заказа

Параметры:
- newOrder - true, если переход со страницы оформления заказа (по умолчанию, false)

Возвращает:
- шаблон "order.html"
