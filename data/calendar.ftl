<!DOCTYPE html>
<html lang="ru">
<head>
	<meta charset="UTF-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	<title>Календарь</title>
	<style>
      body {
          font-family: Arial, sans-serif;
          background: #ffffff;
          color: #333;
          margin: 0;
          padding: 40px;
          display: flex;
          justify-content: center;
      }

      .calendar-container {
          width: 90%;
          max-width: 1200px;
      }

      .calendar-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          font-size: 28px;
          font-weight: bold;
      }

      .calendar-grid {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 1px;
          background: #dcdcdc;
          border: 1px solid #dcdcdc;
      }

      .calendar-cell-header {
          background: #f7f7f7;
          text-align: center;
          padding: 14px 0;
          font-size: 16px;
          font-weight: bold;
      }

      .calendar-cell {
          background: #ffffff;
          min-height: 140px;
          padding: 10px;
          font-size: 16px;
          display: flex;
          flex-direction: column;
          border: 1px solid #eaeaea;
      }

      .day-number {
          font-weight: bold;
          margin-bottom: 8px;
      }

      .event {
          margin-top: 4px;
          padding: 3px 6px;
          border-radius: 6px;
          font-size: 14px;
          line-height: 1.2;
          display: inline-block;
      }

      .green { background: #e6f7e6; color: #1d7d1d; }
      .orange { background: #fff2e0; color: #b87333; }
      .blue { background: #e7f1ff; color: #2c6ed5; }
      .purple { background: #f5e9ff; color: #7d4bcc; }
      .red { background: #ffe5e5; color: #c62828; }

      .today {
          background: #e8f4ff;
          border: 2px solid #57a6ff !important;
      }

	</style>
</head>
<body>
<div class="calendar-container">

	<div class="calendar-header">
		<div>${monthName?cap_first}, ${currentYear}</div>

		<div style="font-size:16px; font-weight: normal;">
			<a href="/calendar?monthYear=${prevMonthYear}">←</a>
			Месяц
			<a href="/calendar?monthYear=${nextMonthYear}">→</a>
		</div>
	</div>

	<div class="calendar-grid">
		<div class="calendar-cell-header">пн</div>
		<div class="calendar-cell-header">вт</div>
		<div class="calendar-cell-header">ср</div>
		<div class="calendar-cell-header">чт</div>
		<div class="calendar-cell-header">пт</div>
		<div class="calendar-cell-header">сб</div>
		<div class="calendar-cell-header">вс</div>

		<#list calendar as day>

		<#if day.empty>
		<div class="calendar-cell"></div>

		<#else>

		<div class="calendar-cell ${day.today?string('today', '')}">

            <div class="day-number">
                <a href="/day?date=${currentYear?c}-${currentMonth?string['00']}-${day.dayNumber?string['00']}"
                   style="text-decoration: none; color: inherit; display:block; width: 100%;">
                    ${day.dayNumber}
                </a>
            </div>

          <#list day.appointments as p>
             <div class="event blue">
                ${p.fullName}<br>
                ${p.appointmentTime}
             </div>
          </#list>

          <#if day.appointmentCount gt 2>
             <div class="event red">
                +${day.appointmentCount - 2} еще
             </div>
        </#if>
</div>
</#if>
</#list>
</div>

</div>
</body>
</html>
