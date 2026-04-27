# Backendprogrammering – ForkMasters API

A RESTful backend application built with Java and Spring Boot, developed as part of the PGR209 course at Høyskolen Kristiania.

## About
ForkMasters is a backend API for managing customers, products, addresses, and orders. The application follows a layered architecture with clear separation between controller, service, and repository layers.

## Features
- Customer and address management
- Product inventory with stock validation
- Order placement with business rule enforcement
- Global exception handling with appropriate HTTP status codes
- Concurrency-safe stock updates

## Teknologi

* Java & Spring Boot
* Spring Data JPA
* PostgreSQL
* Flyway (database migrasjoner)
* Testcontainers (isolerte tester)
* JaCoCo (testdekning)

---

## Forutsetninger

* Java 17 installert
* Docker installert
* Maven wrapper brukes (`./mvnw`)

---

## Starte databasen

Applikasjonen bruker PostgreSQL via Docker.

Start databasen med:

```bash
docker-compose up -d
```

Dette starter en PostgreSQL-container med følgende konfigurasjon:

* Database: `appdb`
* Brukernavn: `appuser`
* Passord: `pirate`
* Port: `5432`

---

## Starte applikasjonen

Kjør applikasjonen med Maven wrapper:

```bash
./mvnw spring-boot:run
```

Applikasjonen starter på:

```
http://localhost:8080
```

Flyway vil automatisk kjøre database-migrasjoner ved oppstart.

---

## Kjøre tester

For å kjøre alle tester:

```bash
./mvnw clean test
```

Testprofilen bruker Testcontainers, som starter en isolert PostgreSQL-container automatisk under testkjøring.

---

## Se testdekning

Etter at tester er kjørt, kan JaCoCo-rapport åpnes her:

```
target/site/jacoco/index.html
```

Åpne filen i nettleser for å se dekning per pakke og klasse.

---

## Database migrasjoner

SQL-migrasjoner ligger i:

```
src/main/resources/db/migration
```

Flyway kjører disse automatisk ved oppstart av applikasjonen.

---

## Eksempel på endepunkter

* `POST /api/products`
* `GET /api/products/{id}`
* `POST /api/customers`
* `POST /api/orders`

---

## Manuell testing med curl

Forutsetter at databasen kjører og applikasjonen er startet på `http://localhost:8080`.

### 1. Opprett kunde

```bash
curl -i -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Kari Nordmann",
    "phoneNumber": "99999999",
    "email": "kari_test@example.com"
  }'
```

Noter `id` fra responsen (kalt `CUSTOMER_ID` under).

### 2. Opprett adresse for kunden

Bytt `CUSTOMER_ID` med id-en du fikk i steg 1.

```bash
curl -i -X POST http://localhost:8080/api/customers/CUSTOMER_ID/addresses \
  -H "Content-Type: application/json" \
  -d '{
    "street": "Karl Johans gate 1",
    "city": "Oslo",
    "zip": "0154",
    "country": "Norway"
  }'
```

Noter `id` fra responsen (kalt `ADDRESS_ID` under).

### 3. Hent adressen

```bash
curl -i http://localhost:8080/api/addresses/ADDRESS_ID
```

### 4. Opprett produkt

```bash
curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fork",
    "description": "Metal fork",
    "price": 49.00,
    "status": "ACTIVE",
    "quantityOnHand": 3
  }'
```

Noter `id` fra responsen (kalt `PRODUCT_ID` under).

### 5. Legg inn ordre

Bytt `CUSTOMER_ID`, `ADDRESS_ID` og `PRODUCT_ID` med riktige id-er.

```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": CUSTOMER_ID,
    "shippingAddressId": ADDRESS_ID,
    "shippingCharge": 100.00,
    "items": [
      { "productId": PRODUCT_ID, "quantity": 2 }
    ]
  }'
```

Noter `id` fra responsen (kalt `ORDER_ID` under).

### 6. Hent ordre

```bash
curl -i http://localhost:8080/api/orders/ORDER_ID
```

### 7. Verifiser at lagerbeholdning ble redusert

```bash
curl -i http://localhost:8080/api/products/PRODUCT_ID
```

`quantityOnHand` skal være redusert (fra 3 til 1 hvis du bestilte 2).

### 8. Out of stock-test (skal gi 409)

Forsøk å bestille mer enn det som er igjen på lager:

```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": CUSTOMER_ID,
    "shippingAddressId": ADDRESS_ID,
    "shippingCharge": 0.00,
    "items": [
      { "productId": PRODUCT_ID, "quantity": 999 }
    ]
  }'
```

Forventet respons: `409 Conflict`.

### 9. 404-test (kunde finnes ikke)

```bash
curl -i http://localhost:8080/api/customers/999999
```

Forventet respons: `404 Not Found`.
