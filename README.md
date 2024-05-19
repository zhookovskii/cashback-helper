# Cashback Helper

### Консольное приложение для хранения банковской информации и оперативного выбора карты с наилучшим кэшбеком

Пример работы приложения. В терминале Идеи выглядит лучше!

```
add card Alpha Only

Added card Only for bank Alpha

add cashback current Only Health 6

Added category Health with 6.0% cashback for card Only in current month 

add transaction Only Health 5000

Added 5000 transaction from Only (Health)

estimate

Cashbacks for this month:
Card: MIR    Cashback: 63
Card: Only    Cashback: 300

add cashback current -p MIR Health 2

Added category Health with 2.0% cashback for card MIR in current month permanently

choose Health

Use card Only for this purchase, it's the best one

add transaction Only Health 10000

Added 10000 transaction from Only (Health)

estimate

Cashbacks for this month:
Card: MIR    Cashback: 63
Card: Only    Cashback: 900

choose Health -v=10000

Use card MIR for this purchase, it's the best one

add transaction Only Health 5000

Added 5000 transaction from Only (Health)

estimate
Cashbacks for this month:
Card: MIR    Cashback: 63
Card: Only    Cashback: 1000
```

Использованные технологии:
* **Picocli** - библиотека для написания Command-line interface приложений,
предоставляет декларативное описание командного интерфейса, автоматическую
генерацию usage, парсинг аргументов командной строки
* **Kolor** - небольшая библиотека, позволяющаяя добавить цвет в консольный вывод
* **KotlinX Datetime** - небольшая библиотека для работы с датами в Kotlin
* **Exposed** - фреймворк для работы с базами данных, предоставляет достаточно
удобный DAO и расширение для работы с **KotlinX Datetime**

Структура проекта:
* [Database](src/main/kotlin/app/Database.kt) - файл, содержащий определение
таблиц и сущностей
* [CashbackService](src/main/kotlin/app/CashbackService.kt) - сервис, осуществляющий
коммуникацию между UI и базой данных
* [TransactionResult](src/main/kotlin/app/TransactionResult.kt) - вспомогательный класс,
представляющий собой результат некоторых операций сервиса
* [Util](src/main/kotlin/app/Util.kt) - файл, содержащий различные вспомогательные функции и константы
* [Main](src/main/kotlin/Main.kt) - входная точка приложения, консольный интерфейс

**ВАЖНО**: при запуске тестов база данных создается в оперативной памяти
и уничтожается после завершения процеесса. Однако при запуске приложения
будет создана база данных с названием `cashback-helper` в домашней 
директории пользователя, так что не забудьте почистить