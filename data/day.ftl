<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Расписание на ${date}</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 20px; background: #f4f4f4; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        h1 { margin-top: 0; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #007bff; color: white; }
        .back-link { display: inline-block; margin-bottom: 20px; color: #007bff; text-decoration: none; font-weight: bold;}
        .empty-msg { color: #777; font-style: italic; margin-top: 20px; }
    </style>
</head>
<body>
<div class="container">
    <a href="/calendar" class="back-link">← Вернуться к календарю</a>

    <h1>Расписание на: ${date}</h1>

    <#if patients?has_content>
        <table>
            <thead>
            <tr>
                <th>Время</th>
                <th>Пациент</th>
                <th>Тип</th>
                <th>Жалобы</th>
            </tr>
            </thead>
            <tbody>
            <#list patients as p>
                <tr>
                    <td>${p.appointmentTime}</td>
                    <td>${p.fullName}</td>
                    <td>${p.type}</td>
                    <td>${p.symptoms}</td>
                </tr>
            </#list>
            </tbody>
        </table>
    <#else>
        <p class="empty-msg">На этот день записей нет.</p>
    </#if>
</div>
</body>
</html>