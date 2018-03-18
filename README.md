# DegreeProject (Дипломний проект)
#### Тема дипломного проекту: 

"Розробка програмної системи для автоматизації складання розкладу занять ВСП Тальнівський будівельно-економічний коледж"

#### Керівник:
Викладач Тальніського будівельно-економічного коледжу з предмету "Бази даних"

Харченко О.О.

#### Розробник:
студент IV курсу групи ПС-46 фаху "Розробка програмного забезпечення" 5.05010301
 
Поліщук Володимир

### Версія 
0.1.3.14

# Для користувача

## Вимоги до системи
#### Програмна система
* Windows 10 / Ubuntu 13+ / MacOS
* JRE 1.8+
#### Серверна частина (додатково)
* MySQl Server 5.2+
* .NET Framework 4.5.2
* Visual C++ 

## Інструкція користувачеві

* Встановлення програмної системи
    * Встановлення Java
    * Налаштування бази даних MySQL
        * Встановлення
        * Створення користувача
    * Встановлення програмної системи
* Налаштування програмної системи
* Створення графіку навчання
* Створення розкладу занять

### Встановлення програмної системи
#### Встановлення Java

1. Спочатку виконайте завантаження _Java_ для вашої системи. 
Зробити це можна на офіційному сайті 
[Завантажити Java](https://java.com/ru/download/)

2. Після завантаження запускаємо виконавчий файл. 
Після того, як відкриється наступне вікно потрібно натиснути **Install**

![Встановлення Java 1](https://www.java.com/ga/images/ru/java_welcome.jpg)

3. Потім потрібно зачекати поки не відбудеться завантаження

![Завантаження Java](https://hwork.net/vova/2.png)

4. Дочекайтесь завершення виконання встановлення і натисніть **Close**

![Встановлення Java 2](https://www.java.com/ga/images/ru/java_finish.jpg)

#### Налаштування бази даних MySQL

1. Перейдіть на офіційний веб-сайт для завантаження останньої версії програмного продукту _MySQL_
[Завантажити MySQL](https://dev.mysql.com/downloads/windows/installer/5.7.html)

2. Знайдіть і виконате завантаження виконавчого файлу для вашої операційної системи.

3. Після виконання завантаження виконайте розпакування завантаженого файлу

![Встановлення MySQL](https://hwork.net/vova/5.png)

4. Запустість виконавчий файл і встановість пункт **I accept the licence terms** і натисніть **Next**
![Встановлення MySQL](http://info-comp.ru/images/stories/kartinki7/install_mysql_5_6_5.jpg)

5. Оберіть пункт **Server Only** і натисніть **Next**

![Встановлення MySQL 2](https://hwork.net/vova/7.png)

6. У випадку, якщо у вас не встановлено **Microsoft Visual C++ 2013** вам буде запропоновано його встановити.
Обераєте пункт меню **Execute** і після встановлення нажимаєте **Next**

![Встановлення MySQl 3](https://hwork.net/vova/8.png)

7. В наступному вікні вам буде запропоновано виконати встановлення сервера **MySQL**. Нажимаєте **Execute** і після встановлення 
нажимаєте **Next**

![](https://hwork.net/vova/11.png)

8. В наступному вікні нажимаєте **Next** і переходете до конфігурування системи

![](https://hwork.net/vova/13.png)

9. В наступному вікні переконуємося, що все так як на рисунку і нажимаєте **Next**

![](https://hwork.net/vova/14.png)

10. В наступному вікні переконуємося, що все так як на рисунку і нажимаєте **Next** 

![](https://hwork.net/vova/15.png)

11. Далі потрібно встановити пароль користувача root. Варто обережно встановлювати пароль і записати його десь, тому
що після втрати відновити доступ до бази даних буде не можливо. Після введення пароля нажимаємо **Next**

![](https://hwork.net/vova/16.png)

12. В наступному вікні переконуємося, що все так як на рисунку і нажимаєте **Next** 

![](https://hwork.net/vova/17.png)

13. В наступному вікні переконуємося, що все так як на рисунку і нажимаєте **Next** 

![](https://hwork.net/vova/18.png)

14. Нажимоємо **Execute** і програма почне конфігурувати систему. Чекаємо до тих пір поки всі пунки 
не будуть виконані

![](https://hwork.net/vova/19.png)

15. В наступному вікні нажимаємо **Next**

16. В наступному вікні нажимаємо **Finish**

![](https://hwork.net/vova/22.png)

17. **MySQL Server** успішно встановлено

#### Встановлення програмної системи

1. Виконайте завантеження програмної системи за наступним посиланням

[Програмна система DegreeProject](#)

2. Розташуйте файл **DegreeProject.jar** у зручному для вас місці

3. Виконайте запуск файлу

4. Після того, як перед вами відкриється наступне вікно введіть адресу _localhost_, порт _3306_, користувача _root_, а пароль, той що ви
задали при створенні бази даних і натисніть **З'єднатися**

![](https://hwork.net/vova/23.png)

5. Перед вами відкриється вікно роботи з програмою


