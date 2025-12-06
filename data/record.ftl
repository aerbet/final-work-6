<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Новая запись</title>
    <link rel="stylesheet"
          href="css/day.css">
</head>
<body>

<div class="container">
    <a href="/day?date=${date}" class="back-link">← Назад в список</a>
    <div class="add-form">
        <h3>➕ Записать нового пациента на ${date}</h3>
        <form action="/add-patient" method="post">
            <input type="hidden" name="date" value="${date}">

            <div class="form-row">
                <div class="form-group" style="flex: 2;">
                    <label>ФИО Пациента:</label>
                    <input type="text" name="fullName" required placeholder="Иванов Иван Иванович">
                </div>
                <div class="form-group" style="flex: 1;">
                    <label>Дата рождения:</label>
                    <input type="date" name="birthDate" required>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Время приема:</label>
                    <input type="time" name="time" required>
                </div>
                <div class="form-group">
                    <label>Тип:</label>
                    <select name="type">
                        <option value="Первичный">Первичный</option>
                        <option value="Вторичный">Вторичный</option>
                        <option value="Консультация">Консультация</option>
                    </select>
                </div>
            </div>

            <div class="form-group" style="margin-bottom: 15px;">
                <label>Жалобы / Анамнез:</label>
                <input type="text" name="symptoms" required placeholder="Опишите симптомы...">
            </div>

            <button type="submit" class="btn-submit">Записать пациента</button>
        </form>
    </div>
</div>

</body>
</html>