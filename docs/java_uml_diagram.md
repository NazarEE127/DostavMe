# UML Диаграмма Java-классов проекта DostavMe

```mermaid
classDiagram
    %% Основные активности
    class MainActivity {
        -Button btnLogin
        -Button btnRegister
        -SessionManager sessionManager
        +onCreate(Bundle)
        -redirectToAppropriateScreen(String)
    }

    class LoginActivity {
        -EditText etPhone
        -EditText etPassword
        -Button btnLogin
        -TextView tvRegister
        -SessionManager sessionManager
        +onCreate(Bundle)
        -validateInput()
        -login()
    }

    class RegisterActivity {
        -EditText etFullName
        -EditText etEmail
        -EditText etPhone
        -EditText etPassword
        -Button btnRegister
        -TextView tvLogin
        +onCreate(Bundle)
        -validateInput()
        -register()
    }

    class ClientActivity {
        -RecyclerView rvOrders
        -FloatingActionButton fabNewOrder
        -SessionManager sessionManager
        -DatabaseHelper dbHelper
        +onCreate(Bundle)
        -loadOrders()
        -setupRecyclerView()
    }

    class CourierActivity {
        -RecyclerView rvOrders
        -SessionManager sessionManager
        -DatabaseHelper dbHelper
        +onCreate(Bundle)
        -loadOrders()
        -setupRecyclerView()
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

    %% Вспомогательные классы
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

    class GeocodingUtils {
        +getLocationFromAddress(Context, String)
        +getAddressFromLocation(Context, LatLng)
    }

    %% Модели данных
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

    %% Адаптеры
    class OrderAdapter {
        -List<Order> orders
        -Context context
        -OnOrderClickListener listener
        +onCreateViewHolder()
        +onBindViewHolder()
        +getItemCount()
    }

    %% Связи между классами
    MainActivity --> SessionManager : uses
    LoginActivity --> SessionManager : uses
    RegisterActivity --> DatabaseHelper : uses
    ClientActivity --> DatabaseHelper : uses
    ClientActivity --> OrderAdapter : uses
    CourierActivity --> DatabaseHelper : uses
    CourierActivity --> OrderAdapter : uses
    OrderDetailsActivity --> DatabaseHelper : uses
    OrderDetailsActivity --> SessionManager : uses
    OrderDetailsActivity --> GeocodingUtils : uses
    DatabaseHelper --> NotificationHelper : uses
    DatabaseHelper --> Order : manages
    DatabaseHelper --> User : manages
    OrderAdapter --> Order : displays
```

## Описание компонентов

### Активности
- **MainActivity** - главный экран приложения
- **LoginActivity** - экран входа
- **RegisterActivity** - экран регистрации
- **ClientActivity** - главный экран клиента
- **CourierActivity** - главный экран курьера
- **OrderDetailsActivity** - экран деталей заказа

### Вспомогательные классы
- **DatabaseHelper** - работа с базой данных
- **NotificationHelper** - работа с уведомлениями
- **SessionManager** - управление сессией пользователя
- **GeocodingUtils** - работа с геокодированием

### Модели данных
- **Order** - модель заказа
- **User** - модель пользователя

### Адаптеры
- **OrderAdapter** - адаптер для отображения списка заказов

### Основные связи
- Активности используют SessionManager для управления сессией
- Активности используют DatabaseHelper для работы с данными
- DatabaseHelper управляет объектами Order и User
- OrderAdapter отображает данные Order
- OrderDetailsActivity использует GeocodingUtils для работы с адресами
- DatabaseHelper использует NotificationHelper для отправки уведомлений

### Основные функции
- Аутентификация и авторизация пользователей
- Управление заказами (создание, принятие, завершение)
- Отображение списков заказов
- Работа с картами и геолокацией
- Отправка уведомлений
- Управление сессией пользователя 