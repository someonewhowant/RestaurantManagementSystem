# 🏗️ Backend Integration Plan — Vanilla CRM

> **Стек:** NestJS + TypeORM + SQLite (better-sqlite3) + JWT  
> **Фронтенд:** Angular 18+ (Signals API)  
> **Цель:** Заменить все мок-данные из Angular-сервисов на реальный REST API с персистентным хранилищем

---

## Фаза 0 — Инициализация проекта

### 0.1 Scaffold NestJS-приложения
```
crm-ui/           # ← существующий Angular
crm-api/           # ← новый NestJS backend
├── src/
│   ├── app.module.ts
│   ├── main.ts
│   ├── common/           # Guards, Pipes, Interceptors, DTOs
│   ├── auth/             # Модуль аутентификации
│   ├── users/            # Пользователи (владельцы, менеджеры)
│   ├── menu/             # Блюда (меню)
│   ├── inventory/        # Склад (ингредиенты)
│   ├── staff/            # Персонал
│   ├── tables/           # Столики зала
│   ├── orders/           # Активные заказы
│   └── transactions/     # Бюджет (доходы/расходы)
├── data/
│   └── restaurant.db     # SQLite-файл
└── package.json
```

### 0.2 Зависимости
```bash
npm i @nestjs/typeorm typeorm better-sqlite3
npm i @nestjs/passport passport passport-jwt @nestjs/jwt
npm i class-validator class-transformer
npm i -D @types/better-sqlite3
```

### 0.3 Конфигурация TypeORM
```typescript
// app.module.ts
TypeOrmModule.forRoot({
  type: 'better-sqlite3',
  database: './data/restaurant.db',
  entities: [__dirname + '/**/*.entity{.ts,.js}'],
  synchronize: true, // Только для dev! В проде — миграции
})
```

---

## Фаза 1 — Аутентификация (`auth/`, `users/`)

> [!IMPORTANT]
> Это первый модуль, так как от него зависят все остальные (Guards, контекст пользователя).

### 1.1 Схема БД

| Таблица `users` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `email` | VARCHAR UNIQUE NOT NULL | Логин |
| `passwordHash` | VARCHAR NOT NULL | bcrypt-хеш |
| `firstName` | VARCHAR | |
| `lastName` | VARCHAR | |
| `restaurantName` | VARCHAR | Название заведения |
| `role` | ENUM('owner','manager','waiter') | Роль |
| `createdAt` | DATETIME | |

### 1.2 API Endpoints

| Метод | Endpoint | Описание | Auth |
|---|---|---|---|
| POST | `/auth/register` | Регистрация владельца | ❌ |
| POST | `/auth/login` | Вход (возвращает JWT) | ❌ |
| GET | `/auth/profile` | Текущий пользователь | ✅ |

### 1.3 Ключевые решения
- **JWT** с access-token (15 мин) + refresh-token (7 дней)
- Пароли хешируются через **bcrypt** (10 rounds)
- `JwtAuthGuard` как глобальный guard на все `/api/*` маршруты
- Декоратор `@CurrentUser()` для извлечения userId из request

---

## Фаза 2 — Меню (`menu/`)

### 2.1 Схема БД

| Таблица `dishes` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `name` | VARCHAR NOT NULL | Название блюда |
| `category` | VARCHAR NOT NULL | Горячее, Закуски, Напитки, Десерты |
| `price` | DECIMAL(10,2) NOT NULL | Цена |
| `weight` | VARCHAR | "350г" |
| `imageIcon` | VARCHAR | Эмодзи-иконка |
| `instructions` | TEXT | Инструкция по приготовлению |
| `allergens` | TEXT (JSON) | Аллергены |
| `macros` | TEXT (JSON) | КБЖУ |
| `userId` | INTEGER FK → users.id | Владелец |

| Таблица `dish_ingredients` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK | |
| `dishId` | INTEGER FK → dishes.id | |
| `ingredientId` | INTEGER FK → inventory_items.id | |
| `amount` | DECIMAL(10,3) | Кол-во ингредиента |

### 2.2 API Endpoints

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/menu` | Список блюд |
| GET | `/api/menu/:id` | Одно блюдо с рецептурой |
| POST | `/api/menu` | Создать блюдо |
| PATCH | `/api/menu/:id` | Обновить блюдо |
| DELETE | `/api/menu/:id` | Удалить блюдо |

---

## Фаза 3 — Склад (`inventory/`)

### 3.1 Схема БД

| Таблица `inventory_items` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `name` | VARCHAR NOT NULL | "Лосось" |
| `category` | VARCHAR | "Морепродукты" |
| `currentStock` | DECIMAL(10,3) | Текущий остаток |
| `minStock` | DECIMAL(10,3) | Минимальный порог |
| `unit` | VARCHAR | "кг", "л" |
| `pricePerUnit` | DECIMAL(10,2) | Закупочная цена |
| `expiresAt` | DATETIME NULL | Дата истечения срока |
| `userId` | INTEGER FK → users.id | |

### 3.2 API Endpoints

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/inventory` | Все позиции склада |
| GET | `/api/inventory/alerts` | Дефицит + истекающие сроки |
| POST | `/api/inventory` | Добавить позицию |
| PATCH | `/api/inventory/:id` | Обновить позицию |
| POST | `/api/inventory/:id/restock` | Оприходование |
| POST | `/api/inventory/:id/consume` | Списание |
| POST | `/api/inventory/consume-batch` | Массовое списание по заказу |

---

## Фаза 4 — Персонал (`staff/`)

### 4.1 Схема БД

| Таблица `employees` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `name` | VARCHAR NOT NULL | ФИО |
| `role` | ENUM('Менеджер','Официант','Повар','Кассир') | |
| `status` | ENUM('Активен','В отпуске','Уволен') | |
| `hireDate` | DATE | Дата найма |
| `fireDate` | DATE NULL | |
| `vacationStart` | DATE NULL | |
| `vacationEnd` | DATE NULL | |
| `onShift` | BOOLEAN DEFAULT false | На смене? |
| `shiftStartTime` | DATETIME NULL | Начало текущей смены |
| `userId` | INTEGER FK → users.id | |

### 4.2 API Endpoints

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/staff` | Весь персонал |
| POST | `/api/staff` | Нанять сотрудника |
| PATCH | `/api/staff/:id` | Обновить данные |
| POST | `/api/staff/:id/toggle-shift` | Открыть/закрыть смену |

---

## Фаза 5 — Столики (`tables/`)

### 5.1 Схема БД

| Таблица `tables` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `number` | INTEGER NOT NULL | Номер стола |
| `capacity` | INTEGER | Вместимость |
| `status` | ENUM('Свободен','Занят','Ожидает блюда','Оплата') | |
| `waiterId` | INTEGER FK → employees.id NULL | Закрепленный официант |
| `statusUpdatedAt` | DATETIME | |
| `userId` | INTEGER FK → users.id | |

### 5.2 API Endpoints

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/tables` | Все столы |
| POST | `/api/tables` | Добавить стол |
| PATCH | `/api/tables/:id/status` | Сменить статус |
| PATCH | `/api/tables/:id/assign` | Назначить официанта |

---

## Фаза 6 — Заказы (`orders/`)

> [!NOTE]
> Самый сложный модуль. Заказы связывают Столы, Меню, Склад и Бюджет.

### 6.1 Схема БД

| Таблица `orders` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `tableId` | INTEGER FK → tables.id | |
| `waiterId` | INTEGER FK → employees.id | |
| `status` | ENUM('active','paid','cancelled') | |
| `totalAmount` | DECIMAL(10,2) | Итого к оплате |
| `createdAt` | DATETIME | |
| `paidAt` | DATETIME NULL | |
| `userId` | INTEGER FK → users.id | |

| Таблица `order_items` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK | |
| `orderId` | INTEGER FK → orders.id | |
| `dishId` | INTEGER FK → dishes.id | |
| `quantity` | INTEGER | |
| `priceAtOrder` | DECIMAL(10,2) | Цена на момент заказа |
| `status` | ENUM('new','cooking','ready','served') | |

### 6.2 API Endpoints

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/orders` | Активные заказы |
| GET | `/api/orders/:id` | Детали заказа |
| POST | `/api/orders` | Создать заказ (привязка к столу) |
| POST | `/api/orders/:id/items` | Добавить позицию в заказ |
| DELETE | `/api/orders/:id/items/:itemId` | Убрать позицию |
| POST | `/api/orders/:id/send-to-kitchen` | Отправить на кухню |
| PATCH | `/api/orders/:id/items/:itemId/status` | Статус блюда (Готовится → Готово) |
| POST | `/api/orders/:id/pay` | Оплатить → создать транзакцию + списать склад |

### 6.3 Бизнес-логика оплаты (транзакция)
```
POST /api/orders/:id/pay  →
  1. Пометить order.status = 'paid'
  2. Создать Transaction (type: 'Доход', category: 'Оплата заказа')
  3. Вызвать inventory.consumeBatch() для всех блюд с рецептурой
  4. Обновить table.status = 'Свободен'
```

---

## Фаза 7 — Бюджет/Транзакции (`transactions/`)

### 7.1 Схема БД

| Таблица `transactions` | Тип | Описание |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `date` | DATETIME NOT NULL | |
| `amount` | DECIMAL(10,2) NOT NULL | |
| `type` | ENUM('Доход','Расход') | |
| `category` | VARCHAR | "Оплата заказа", "Закупки", "Коммуналка" |
| `description` | TEXT | |
| `orderId` | INTEGER FK → orders.id NULL | Связь с заказом |
| `userId` | INTEGER FK → users.id | |

### 7.2 API Endpoints

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/transactions` | Все транзакции (с пагинацией) |
| GET | `/api/transactions/summary` | Итоги: доход, расход, баланс |
| POST | `/api/transactions` | Добавить ручную транзакцию |

---

## Фаза 8 — Dashboard Analytics (`dashboard/`)

> [!TIP]
> Этот модуль не хранит своих данных — он агрегирует данные из других модулей.

| Метод | Endpoint | Описание |
|---|---|---|
| GET | `/api/dashboard/kpi` | Выручка, средний чек, гости, фудкост |
| GET | `/api/dashboard/weekly-chart` | Данные для графика доходов/расходов |
| GET | `/api/dashboard/peak-hours` | Прогноз загрузки по часам |
| GET | `/api/dashboard/abc-analysis` | Хиты и аутсайдеры меню |
| GET | `/api/dashboard/top-waiters` | Лидерборд официантов |

---

## Фаза 9 — Интеграция Angular ↔ NestJS

### 9.1 Паттерн миграции сервисов

Каждый Angular-сервис будет переведён с локального `signal()` на `HttpClient`:

```typescript
// БЫЛО (мок):
private itemsSignal = signal<InventoryItem[]>([/* хардкод */]);

// СТАЛО (API):
private itemsSignal = signal<InventoryItem[]>([]);

constructor() {
  this.loadItems();
}

private loadItems() {
  this.http.get<InventoryItem[]>('/api/inventory')
    .subscribe(items => this.itemsSignal.set(items));
}
```

### 9.2 Порядок миграции сервисов

| # | Сервис | Зависит от |
|---|---|---|
| 1 | `AuthService` | — |
| 2 | `MenuService` | Auth |
| 3 | `InventoryService` | Auth |
| 4 | `StaffService` | Auth |
| 5 | `TablesService` | Auth, Staff |
| 6 | `OrderService` | Auth, Menu, Tables, Inventory |
| 7 | `BudgetService` | Auth, Orders |

### 9.3 HTTP Interceptor для JWT
```typescript
// Автоматически прикрепляет Bearer token ко всем /api/* запросам
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('access_token');
  if (token && req.url.includes('/api/')) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
```

### 9.4 Proxy Config (Dev)
```json
// proxy.conf.json
{
  "/api": {
    "target": "http://localhost:3000",
    "secure": false
  }
}
```

---

## Порядок реализации (Roadmap)

| Этап | Модули | Результат |
|---|---|---|
| **Sprint 1** | Фаза 0 + 1 | Scaffold + Auth (JWT login/register) |
| **Sprint 2** | Фаза 2 + 3 | Меню + Склад (CRUD с БД) |
| **Sprint 3** | Фаза 4 + 5 | Персонал + Столы |
| **Sprint 4** | Фаза 6 + 7 | Заказы + Транзакции (ядро бизнес-логики) |
| **Sprint 5** | Фаза 8 + 9 | Dashboard API + Angular-интеграция |

---

## Лучшие практики NestJS

- **Validation Pipe** — глобальный `ValidationPipe` с `class-validator` DTOs
- **Модульность** — каждый домен в отдельном `Module` со своими `Service`, `Controller`, `Entity`
- **Repository Pattern** — TypeORM `@InjectRepository()` для работы с БД
- **Guards** — `JwtAuthGuard` + `RolesGuard` для RBAC
- **Exception Filters** — единообразная обработка ошибок
- **Serialization** — `ClassSerializerInterceptor` для скрытия `passwordHash`
- **Logging** — встроенный `Logger` NestJS для аудита операций
