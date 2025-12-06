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
                <th>Дата рождения</th>
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
                    <td>${p.birthDate}</td>
                    <td>
                    <span class="type-span ${p.type}">
                        ${p.type}
                     </span>
                    </td>
                    <td>${p.symptoms}</td>
                    <td style="text-align: center; display: flex; gap: 5px; justify-content: center;">
                        <a href="/edit?date=${date}&id=${p.id}" class="btn-edit"
                           style="text-decoration: none; background: #ffc107; color: black; padding: 6px 10px; border-radius: 4px; font-size: 12px;">
                            ✎
                        </a>

                        <form action="/delete-patient" method="post" onsubmit="return confirm('Вы уверены?');" style="margin: 0;">
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
        <div class="empty-msg">На этот день записей нет. Запишите пациента!</div>
    </#if>

    <a href="/record?date=${date}" class="btn-submit" style="text-decoration: none; display: inline-block; text-align: center;">Добавить новую запись</a>

</div>

</body>
</html>