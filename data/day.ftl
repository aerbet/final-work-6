<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Расписание на ${date}</title>
    <link rel="stylesheet"
          href="css/day.css">
</head>
<body>

<div class="container">
    <a href="/calendar" class="back-link">← Вернуться к календарю</a>
    <h1>Записи на: ${date}</h1>

    <#if patients?has_content>
        <table>
            <thead>
            <tr>
                <th style="width: 80px;">Время</th>
                <th>ФИО Пациента</th>
                <th>Тип приема</th>
                <th>Жалобы (Анамнез)</th>
                <th style="width: 50px;">Действие</th>
            </tr>
            </thead>
            <tbody>
            <#list patients as p>
                <tr>
                    <td><b>${p.appointmentTime}</b></td>
                    <td>${p.fullName}</td>
                    <td>
                    <span class="type-span ${p.type}">
                        ${p.type}
                     </span>
                    </td>
                    <td>${p.symptoms}</td>
                    <td style="text-align: center;">
                        <form action="/delete-patient" method="post" onsubmit="return confirm('Вы уверены?');">
                            <input type="hidden" name="date" value="${date}">
                            <input type="hidden" name="id" value="${p.id}">
                            <button type="submit" class="btn-delete">✕</button>
                        </form>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <div class="empty-msg">На этот день записей нет. Будьте первым!</div>
    </#if>

    <div class="add-form">
        <h3>➕ Записать нового пациента</h3>
        <form action="/add-patient" method="post">
            <input type="hidden" name="date" value="${date}">

            <div class="form-row">
                <div class="form-group" style="flex: 2;">
                    <label>ФИО Пациента:</label>
                    <input type="text" name="fullName" required placeholder="Иванов Иван Иванович">
                </div>
                <div class="form-group">
                    <label>Время:</label>
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