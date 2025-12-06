<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Редактирование записи</title>
    <link rel="stylesheet" href="css/day.css">
</head>
<body>

<div class="container">
    <a href="/day?date=${date}" class="back-link">← Отмена</a>

    <div class="add-form">
        <h3>✏️ Редактирование записи</h3>

        <form action="/edit-patient" method="post">
            <input type="hidden" name="id" value="${patient.id}">
            <input type="hidden" name="oldDate" value="${date}">

            <div class="form-row">
                <div class="form-group" style="flex: 2;">
                    <label>ФИО Пациента (не меняется):</label>
                    <input type="text" name="fullName" value="${patient.fullName}" readonly
                           style="background-color: #e9ecef; cursor: not-allowed;">
                </div>
                <div class="form-group" style="flex: 1;">
                    <label>Дата рождения:</label>
                    <input type="date" name="birthDate" value="${patient.birthDate}" required>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Дата приема:</label>
                    <input type="date" name="newDate" value="${date}" required>
                </div>
                <div class="form-group">
                    <label>Время приема:</label>
                    <input type="time" name="time" value="${patient.appointmentTime}" required>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Тип:</label>
                    <select name="type">
                        <option value="Первичный" <#if patient.type == "Первичный">selected</#if>>Первичный</option>
                        <option value="Вторичный" <#if patient.type == "Вторичный">selected</#if>>Вторичный</option>
                        <option value="Консультация" <#if patient.type == "Консультация">selected</#if>>Консультация</option>
                    </select>
                </div>
            </div>

            <div class="form-group" style="margin-bottom: 15px;">
                <label>Жалобы / Анамнез:</label>
                <input type="text" name="symptoms" value="${patient.symptoms}" required>
            </div>

            <button type="submit" class="btn-submit" style="background-color: #ffc107; color: #000;">Сохранить изменения</button>
        </form>
    </div>
</div>

</body>
</html>