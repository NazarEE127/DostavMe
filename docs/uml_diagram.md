# UML Диаграмма классов проекта DostavMe

```mermaid
classDiagram
    class MainActivity {
        -Button btnLogin
        -Button btnRegister
        -SessionManager sessionManager
        +onCreate(Bundle)
        -redirectToAppropriateScreen(String)
    }

    class DatabaseHelper {
        -String DATABASE_NAME
        -int DATABASE_VERSION
        -Context context
        +onCreate(SQLiteDatabase)
        +onUpgrade(SQLiteDatabase, int, int)
        +addUser(User)
        +getUser(String)
        +addOrder(Order)
        +getOrder(String)
        +assignCourierToOrder(String, String)
        +completeOrder(String)
    }

    class NotificationHelper {
        -String CHANNEL_ID
        -Context context
        -NotificationManager notificationManager
        +createNotificationChannel()
        +showNewOrderNotification(String, String, String)
        +showOrderAcceptedNotification(String, String)
    }

    class Order {
        -String id
        -String clientId
        -String courierId
        -String fromAddress
        -String toAddress
        -String description
        -double weight
        -double price
        -String status
        -String createdAt
        +getters()
        +setters()
    }

    class User {
        -String id
        -String fullName
        -String email
        -String phone
        -String password
        -String role
        +getters()
        +setters()
    }

    class SessionManager {
        -SharedPreferences pref
        -Editor editor
        -Context context
        +createLoginSession(String, String)
        +isLoggedIn()
        +getUserId()
        +getUserRole()
        +logout()
    }

    class OrderDetailsActivity {
        -DatabaseHelper dbHelper
        -SessionManager sessionManager
        -GoogleMap mMap
        -Order currentOrder
        -User client
        -Button btnAcceptOrder
        -Button btnCompleteOrder
        +onCreate(Bundle)
        -loadOrderDetails(String)
        -updateUI()
        -setupButtons()
        -acceptOrder()
        -completeOrder()
    }

    MainActivity --> SessionManager : uses
    DatabaseHelper --> NotificationHelper : uses
    DatabaseHelper --> Order : manages
    DatabaseHelper --> User : manages
    OrderDetailsActivity --> DatabaseHelper : uses
    OrderDetailsActivity --> SessionManager : uses
    OrderDetailsActivity --> Order : displays
    OrderDetailsActivity --> User : displays
```

## Описание компонентов

### Активности
- **MainActivity** - главный экран приложения
- **OrderDetailsActivity** - экран деталей заказа

### Вспомогательные классы
- **DatabaseHelper** - работа с базой данных
- **NotificationHelper** - работа с уведомлениями
- **SessionManager** - управление сессией пользователя

### Модели данных
- **Order** - модель заказа
- **User** - модель пользователя

### Основные связи
- DatabaseHelper управляет объектами Order и User
- OrderDetailsActivity использует DatabaseHelper и SessionManager
- MainActivity использует SessionManager
- DatabaseHelper использует NotificationHelper

### Основные функции
- Управление пользователями (регистрация, авторизация)
- Управление заказами (создание, принятие, завершение)
- Отправка уведомлений
- Отображение деталей заказа
- Работа с картами 