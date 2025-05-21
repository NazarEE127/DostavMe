package com.example.dostavme.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;
import com.example.dostavme.models.Order;
import com.example.dostavme.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import com.example.dostavme.utils.NotificationHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DostavMe.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";

    // Таблицы
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ORDERS = "orders";

    // Общие колонки
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Колонки таблицы users
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";

    // Колонки таблицы orders
    private static final String COLUMN_CLIENT_ID = "client_id";
    private static final String COLUMN_COURIER_ID = "courier_id";
    private static final String COLUMN_FROM_ADDRESS = "from_address";
    private static final String COLUMN_TO_ADDRESS = "to_address";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_STATUS = "status";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_FULL_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PHONE + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ROLE + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        
        String CREATE_ORDERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_CLIENT_ID + " TEXT,"
                + COLUMN_COURIER_ID + " TEXT,"
                + COLUMN_FROM_ADDRESS + " TEXT,"
                + COLUMN_TO_ADDRESS + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_PRICE + " INTEGER,"
                + COLUMN_STATUS + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_CLIENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(" + COLUMN_COURIER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_ORDERS_TABLE);
        
        Log.d(TAG, "База данных создана");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        db.beginTransaction();
        try {
            
            db.execSQL("CREATE TABLE IF NOT EXISTS temp_users AS SELECT * FROM " + TABLE_USERS);
            db.execSQL("CREATE TABLE IF NOT EXISTS temp_orders AS SELECT * FROM " + TABLE_ORDERS);
            
            
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            
            onCreate(db);
            
            db.execSQL("INSERT INTO " + TABLE_USERS + " SELECT * FROM temp_users");
            db.execSQL("INSERT INTO " + TABLE_ORDERS + " SELECT * FROM temp_orders");
            
            db.execSQL("DROP TABLE IF EXISTS temp_users");
            db.execSQL("DROP TABLE IF EXISTS temp_orders");
            
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Методы для работы с пользователями
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID, user.getId());
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PHONE, user.getPhone());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_ROLE, user.getRole());

        try {
            return db.insertOrThrow(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении пользователя: " + e.getMessage());
            return -1;
        }
    }

    public boolean isUserExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_PHONE + " = ?",
                new String[]{phone},
                null, null, null);
        
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public User getUserByPhone(String phone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Log.d(TAG, "Поиск пользователя по телефону: " + phone);

        Cursor cursor = db.query(TABLE_USERS,
                null,
                COLUMN_PHONE + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{phone, password},
                null, null, null);

        if (cursor != null) {
            Log.d(TAG, "Найдено записей: " + cursor.getCount());
            
            if (cursor.moveToFirst()) {
                user = new User();
                
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_FULL_NAME);
                int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                int phoneIndex = cursor.getColumnIndex(COLUMN_PHONE);
                int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
                int roleIndex = cursor.getColumnIndex(COLUMN_ROLE);

                if (idIndex != -1) user.setId(cursor.getString(idIndex));
                if (nameIndex != -1) user.setFullName(cursor.getString(nameIndex));
                if (emailIndex != -1) user.setEmail(cursor.getString(emailIndex));
                if (phoneIndex != -1) user.setPhone(cursor.getString(phoneIndex));
                if (passwordIndex != -1) user.setPassword(cursor.getString(passwordIndex));
                if (roleIndex != -1) user.setRole(cursor.getString(roleIndex));

                Log.d(TAG, "Пользователь найден: " + user.getFullName() + ", роль: " + user.getRole());
            } else {
                Log.d(TAG, "Пользователь не найден");
            }
            cursor.close();
        } else {
            Log.e(TAG, "Ошибка при выполнении запроса");
        }

        return user;
    }

    public User getUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(TABLE_USERS,
                null,
                COLUMN_ID + " = ?",
                new String[]{userId},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            user.setFullName(cursor.getString(cursor.getColumnIndex(COLUMN_FULL_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
            user.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
            user.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
            user.setRole(cursor.getString(cursor.getColumnIndex(COLUMN_ROLE)));
            cursor.close();
        }

        return user;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PHONE, user.getPhone());
        values.put(COLUMN_PASSWORD, user.getPassword());

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_ID + " = ?",
                new String[]{user.getId()});

        return rowsAffected > 0;
    }

    public boolean deleteUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_USERS,
                COLUMN_ID + " = ?",
                new String[]{userId});
        return rowsAffected > 0;
    }

    // Методы для работы с заказами
    public boolean addOrder(Order order) {
        if (order == null) {
            Log.e("DatabaseHelper", "Попытка добавить null заказ");
            return false;
        }
        
        if (order.getId() == null) {
            Log.e("DatabaseHelper", "Попытка добавить заказ с null ID");
            return false;
        }
        
        Log.d("DatabaseHelper", "Добавление заказа с ID: " + order.getId());
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("id", order.getId());
        values.put("client_id", order.getClientId());
        values.put("courier_id", order.getCourierId());
        values.put("from_address", order.getFromAddress());
        values.put("to_address", order.getToAddress());
        values.put("description", order.getDescription());
        values.put("weight", order.getWeight());
        values.put("price", order.getPrice());
        values.put("status", order.getStatus());
        values.put("created_at", order.getCreatedAt());
        
        Log.d("DatabaseHelper", "Данные заказа для вставки: " + values.toString());
        
        try {
            long result = db.insertOrThrow(TABLE_ORDERS, null, values);
            if (result != -1) {
                Log.d("DatabaseHelper", "Заказ успешно добавлен с ID: " + order.getId());
                
                List<User> couriers = getAllCouriers();
                NotificationHelper notificationHelper = new NotificationHelper(context);
                for (User courier : couriers) {
                    notificationHelper.showNewOrderNotification(
                        order.getId(),
                        order.getFromAddress(),
                        order.getToAddress()
                    );
                }
                
                return true;
            } else {
                Log.e("DatabaseHelper", "Ошибка при добавлении заказа с ID: " + order.getId());
                return false;
            }
        } catch (SQLiteConstraintException e) {
            Log.e("DatabaseHelper", "Ошибка уникального ограничения при добавлении заказа: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Неизвестная ошибка при добавлении заказа: " + e.getMessage());
            return false;
        }
    }

    public Order getOrder(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Order order = null;

        Cursor cursor = db.query(TABLE_ORDERS,
                null,
                COLUMN_ID + " = ?",
                new String[]{orderId},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            order = new Order();
            order.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            order.setClientId(cursor.getString(cursor.getColumnIndex(COLUMN_CLIENT_ID)));
            order.setCourierId(cursor.getString(cursor.getColumnIndex(COLUMN_COURIER_ID)));
            order.setFromAddress(cursor.getString(cursor.getColumnIndex(COLUMN_FROM_ADDRESS)));
            order.setToAddress(cursor.getString(cursor.getColumnIndex(COLUMN_TO_ADDRESS)));
            order.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
            order.setWeight(cursor.getDouble(cursor.getColumnIndex(COLUMN_WEIGHT)));
            order.setPrice(cursor.getInt(cursor.getColumnIndex(COLUMN_PRICE)));
            order.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
            cursor.close();
        }

        return order;
    }

    public List<Order> getNewOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Log.d("DatabaseHelper", "Начинаем получение новых заказов");
        
        Cursor cursor = db.query(
            TABLE_ORDERS,
            null,
            "status = ?",
            new String[]{"new"},
            null,
            null,
            "created_at DESC"
        );

        if (cursor != null) {
            Log.d("DatabaseHelper", "Получено заказов из базы: " + cursor.getCount());
            
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                Log.d("DatabaseHelper", "Обработка заказа с ID: " + id);
                
                Order order = new Order(
                    id,
                    cursor.getString(cursor.getColumnIndexOrThrow("client_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("courier_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("from_address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("to_address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("weight")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                );
                
                if (order.getId() == null) {
                    Log.e("DatabaseHelper", "Заказ создан с null ID! Данные из базы: " + 
                        "id=" + id + 
                        ", client_id=" + cursor.getString(cursor.getColumnIndexOrThrow("client_id")) +
                        ", status=" + cursor.getString(cursor.getColumnIndexOrThrow("status")));
                }
                
                orders.add(order);
            }
            cursor.close();
        }
        
        Log.d("DatabaseHelper", "Всего получено заказов: " + orders.size());
        return orders;
    }

    public List<Order> getClientOrders(String clientId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ORDERS,
                null,
                COLUMN_CLIENT_ID + " = ?",
                new String[]{clientId},
                null, null,
                COLUMN_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                order.setClientId(cursor.getString(cursor.getColumnIndex(COLUMN_CLIENT_ID)));
                order.setCourierId(cursor.getString(cursor.getColumnIndex(COLUMN_COURIER_ID)));
                order.setFromAddress(cursor.getString(cursor.getColumnIndex(COLUMN_FROM_ADDRESS)));
                order.setToAddress(cursor.getString(cursor.getColumnIndex(COLUMN_TO_ADDRESS)));
                order.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                order.setWeight(cursor.getDouble(cursor.getColumnIndex(COLUMN_WEIGHT)));
                order.setPrice(cursor.getInt(cursor.getColumnIndex(COLUMN_PRICE)));
                order.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
                orders.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    public List<Order> getCourierOrders(String courierId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ORDERS,
                null,
                COLUMN_COURIER_ID + " = ? AND " + COLUMN_STATUS + " IN (?, ?)",
                new String[]{courierId, "in_progress", "pending"},
                null, null,
                COLUMN_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                order.setClientId(cursor.getString(cursor.getColumnIndex(COLUMN_CLIENT_ID)));
                order.setCourierId(cursor.getString(cursor.getColumnIndex(COLUMN_COURIER_ID)));
                order.setFromAddress(cursor.getString(cursor.getColumnIndex(COLUMN_FROM_ADDRESS)));
                order.setToAddress(cursor.getString(cursor.getColumnIndex(COLUMN_TO_ADDRESS)));
                order.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                order.setWeight(cursor.getDouble(cursor.getColumnIndex(COLUMN_WEIGHT)));
                order.setPrice(cursor.getInt(cursor.getColumnIndex(COLUMN_PRICE)));
                order.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
                orders.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    public boolean updateOrderStatus(String orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);

        int rowsAffected = db.update(TABLE_ORDERS, values,
                COLUMN_ID + " = ?",
                new String[]{orderId});

        return rowsAffected > 0;
    }

    public boolean assignCourierToOrder(String orderId, String courierId) {
        Log.d(TAG, "assignCourierToOrder: orderId=" + orderId + ", courierId=" + courierId);
        
        if (orderId == null || courierId == null) {
            Log.e(TAG, "assignCourierToOrder: orderId или courierId равен null");
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            Cursor cursor = db.query(TABLE_ORDERS,
                    new String[]{COLUMN_STATUS, COLUMN_CLIENT_ID},
                    COLUMN_ID + " = ?",
                    new String[]{orderId},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String currentStatus = cursor.getString(cursor.getColumnIndex(COLUMN_STATUS));
                String clientId = cursor.getString(cursor.getColumnIndex(COLUMN_CLIENT_ID));
                cursor.close();
                Log.d(TAG, "assignCourierToOrder: текущий статус заказа=" + currentStatus);

                if (!"new".equals(currentStatus)) {
                    Log.d(TAG, "assignCourierToOrder: заказ уже не в статусе 'new'");
                    return false;
                }

                ContentValues values = new ContentValues();
                values.put(COLUMN_COURIER_ID, courierId);
                values.put(COLUMN_STATUS, "in_progress");

                int rowsAffected = db.update(TABLE_ORDERS, values,
                        COLUMN_ID + " = ?",
                        new String[]{orderId});

                Log.d(TAG, "assignCourierToOrder: обновлено строк=" + rowsAffected);
                
                if (rowsAffected > 0) {
                    // Отправляем уведомление клиенту
                    User courier = getUser(courierId);
                    if (courier != null) {
                        NotificationHelper notificationHelper = new NotificationHelper(context);
                        notificationHelper.showOrderAcceptedNotification(orderId, courier.getFullName());
                    }
                    return true;
                }
            }

            if (cursor != null) {
                cursor.close();
            }
            Log.e(TAG, "assignCourierToOrder: заказ не найден");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "assignCourierToOrder: ошибка при обновлении заказа", e);
            return false;
        }
    }

    public boolean completeOrder(String orderId) {
        Log.d(TAG, "completeOrder: начало выполнения для заказа " + orderId);
        SQLiteDatabase db = this.getWritableDatabase();
        
        Cursor cursor = db.query(TABLE_ORDERS,
                new String[]{COLUMN_STATUS, COLUMN_COURIER_ID},
                COLUMN_ID + " = ?",
                new String[]{orderId},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String currentStatus = cursor.getString(cursor.getColumnIndex(COLUMN_STATUS));
            String courierId = cursor.getString(cursor.getColumnIndex(COLUMN_COURIER_ID));
            cursor.close();
            
            Log.d(TAG, "completeOrder: текущий статус=" + currentStatus + ", courierId=" + courierId);

            if (!"in_progress".equals(currentStatus)) {
                Log.e(TAG, "completeOrder: заказ не в статусе 'in_progress'");
                return false;
            }

            if (courierId == null) {
                Log.e(TAG, "completeOrder: у заказа нет назначенного курьера");
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_STATUS, "completed");
            values.put(COLUMN_CREATED_AT, new Date().toString());

            int rowsAffected = db.update(TABLE_ORDERS, values,
                    COLUMN_ID + " = ?",
                    new String[]{orderId});

            Log.d(TAG, "completeOrder: обновлено строк=" + rowsAffected);
            
            if (rowsAffected > 0) {
                Order order = getOrder(orderId);
                if (order != null) {
                    double courierPayment = order.getPrice();
                    updateCourierBalance(courierId, 120.0);
                    Log.d(TAG, "completeOrder: начислено курьеру " + courierPayment + " руб.");
                }
                return true;
            }
        }
        
        Log.e(TAG, "completeOrder: заказ не найден");
        return false;
    }

    public boolean updateCourierBalance(String courierId, double amount) {
        Log.d(TAG, "updateCourierBalance: обновляем баланс курьера " + courierId + " на " + amount);
        
        if (courierId == null) {
            Log.e(TAG, "updateCourierBalance: courierId равен null");
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{"balance"},
                    COLUMN_ID + " = ?",
                    new String[]{courierId},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                double currentBalance = cursor.getDouble(0);
                cursor.close();
                
                ContentValues values = new ContentValues();
                values.put("balance", currentBalance + amount);
                
                int result = db.update(TABLE_USERS, values,
                        COLUMN_ID + " = ?",
                        new String[]{courierId});
                
                Log.d(TAG, "updateCourierBalance: результат обновления=" + result);
                return result > 0;
            }
            
            if (cursor != null) {
                cursor.close();
            }
            Log.e(TAG, "updateCourierBalance: курьер не найден");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "updateCourierBalance: ошибка при обновлении баланса", e);
            return false;
        }
    }

    public int getCompletedOrdersCount(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;

        Cursor cursor = db.query(TABLE_ORDERS,
                new String[]{"COUNT(*)"},
                COLUMN_COURIER_ID + " = ? AND " + COLUMN_STATUS + " = ?",
                new String[]{userId, "completed"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    public double getCourierRating(String courierId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double rating = 0.0;
        return 4.5;
    }

    public void updateOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM_ADDRESS, order.getFromAddress());
        values.put(COLUMN_TO_ADDRESS, order.getToAddress());
        values.put(COLUMN_DESCRIPTION, order.getDescription());
        values.put(COLUMN_PRICE, order.getPrice());
        values.put(COLUMN_STATUS, order.getStatus());
        values.put(COLUMN_COURIER_ID, order.getCourierId());
        values.put(COLUMN_CLIENT_ID, order.getClientId());
        values.put(COLUMN_CREATED_AT, order.getCreatedAt());

        db.update(TABLE_ORDERS, values, COLUMN_ID + " = ?", new String[]{order.getId()});
    }

    private List<User> getAllCouriers() {
        List<User> couriers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                null,
                COLUMN_ROLE + " = ?",
                new String[]{"courier"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                user.setFullName(cursor.getString(cursor.getColumnIndex(COLUMN_FULL_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                user.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                user.setRole(cursor.getString(cursor.getColumnIndex(COLUMN_ROLE)));
                couriers.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return couriers;
    }
} 