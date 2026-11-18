# spy-backend

Backend gry Spy (Spring Boot 4, Java 25). Kontrakt API: [`todo-backend/00-kontrakt-api.md`](todo-backend/00-kontrakt-api.md).

## Uruchomienie

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Domyślny port **8080** (nadpisywany zmienną `PORT`). Sprawdzenie:

```bash
curl http://localhost:8080/api/health   # {"status":"ok"}
```

## Testy

```bash
./mvnw test
```

## Konfiguracja

| Właściwość | Zmienna środowiskowa | Domyślnie |
|---|---|---|
| `server.port` | `PORT` | `8080` |
| `app.cors.allowed-origins` | `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:4200,https://kacperkk2.github.io` |

Wszystkie endpointy są pod prefiksem `/api`. CORS dopuszcza metody `GET`, `POST`, `DELETE` i nagłówki `X-Player-Token`, `Content-Type`.
