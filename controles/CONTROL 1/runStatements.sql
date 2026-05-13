-- consulta 1- Horario con menos citas durante el dia por peluqueria, identifificando la comuna

WITH ConteoCitas AS (
    SELECT 
        p.Nombre AS Peluqueria,
        h.Dia_Semana,
        h.Hora_Inicio,
        h.Hora_Fin,
        co.Nombre_Comuna,
        COUNT(c.Id_Cita) AS Total_Citas,
        RANK() OVER (
            PARTITION BY p.Id_Peluqueria 
            ORDER BY COUNT(c.Id_Cita) ASC
        ) as Ranking
    FROM horario h
    JOIN citas c ON h.Id_Horario = c.Id_Horario
    JOIN peluqueria p ON p.Id_Peluqueria = c.Id_Peluqueria
    JOIN comuna co ON co.Id_Comuna = p.Id_Comuna
    GROUP BY 
        p.Id_Peluqueria, 
        p.Nombre, 
        h.Dia_Semana, 
        h.Hora_Inicio, 
        h.Hora_Fin,
        co.Nombre_Comuna
)
SELECT 
    Peluqueria,
    Dia_Semana,
    Hora_Inicio,
    Hora_Fin,
    Nombre_Comuna,
    Total_Citas
FROM ConteoCitas
WHERE Ranking = 1;

-- consulta 2- Lista de clientes que gastan mas dinero mensual por peluqueria, indicando la comuna del cliente y de la peluqueria, ademas de cuanto gasto el cliente

SELECT * FROM ( 
    SELECT 
        c.Nombres, 
        c.Apellido_Paterno, 
        com_c.Nombre_Comuna as Comuna_Cliente, 
        p.Nombre as Peluqueria,
        com_p.Nombre_Comuna as Comuna_Peluqueria, 
        EXTRACT(MONTH FROM pa.Fecha_pago) as Mes, 
        SUM(pa.Monto_Total) as Gasto_Total,
        RANK() OVER (
            PARTITION BY p.Id_Peluqueria, EXTRACT(MONTH FROM pa.Fecha_pago) 
            ORDER BY SUM(pa.Monto_Total) DESC
        ) as Ranking
    FROM cliente c
    JOIN comuna com_c ON c.Id_Comuna = com_c.Id_Comuna
    JOIN citas ci ON c.Id_Cliente = ci.Id_Cliente
    JOIN peluqueria p ON ci.Id_Peluqueria = p.Id_Peluqueria
    JOIN Comuna com_p ON p.Id_Comuna = com_p.Id_Comuna
    JOIN Pago pa ON ci.Id_Cita = pa.Id_Cita
    GROUP BY 
        c.Id_Cliente, 
        c.Nombres, 
        c.Apellido_Paterno, 
        com_c.Nombre_Comuna, 
        p.Id_Peluqueria, 
        p.Nombre, 
        com_p.Nombre_Comuna, 
        EXTRACT(MONTH FROM pa.Fecha_pago)
) AS Resultado
WHERE Ranking = 1;

-- consulta 3- Lista de pluqueros por peluqueria que han ganado mas por mes los ultimos 3 años

WITH GananciasPorEmpleado AS (
  -- Filtrar, unir y AGREGAR ganancias por peluquero y mes
  SELECT 
    c.Id_Peluqueria,
    c.Id_Empleado,
    TO_CHAR(c.Fecha_Cita, 'YYYY-MM') AS Mes, -- PostgreSQL: TO_CHAR para formatear
    SUM(p.Monto_Total) AS Total_Ganado 
  FROM citas c
  JOIN pago p ON c.Id_Cita = p.Id_Cita
  WHERE c.Fecha_Cita >= CURRENT_DATE - INTERVAL '3 years' -- PostgreSQL: Resta de fechas directa
    AND p.Estado_pago = 'Pagado' 
  GROUP BY c.Id_Peluqueria, c.Id_Empleado, TO_CHAR(c.Fecha_Cita, 'YYYY-MM') -- Agrupamos por la función completa
),
RankingGanancias AS (
  -- Ordenar las ganancias totales dentro de cada peluquería y mes
  SELECT *,
    ROW_NUMBER() OVER (
      PARTITION BY Id_Peluqueria, Mes 
      ORDER BY Total_Ganado DESC 
    ) AS Posicion
  FROM GananciasPorEmpleado
)
-- Extraer unicamente al #1 de cada grupo
SELECT 
  Id_Peluqueria,
  Id_Empleado,
  Mes,
  Total_Ganado
FROM RankingGanancias
WHERE Posicion = 1
ORDER BY Id_Peluqueria, Mes;

-- consulta 4- Lista de clientes hombres que se cortan el pelo y la barba

SELECT 
    c.Nombres, 
    c.Apellido_Paterno, 
    c.Apellido_Materno 
FROM Cliente c
JOIN Citas ci ON c.Id_Cliente = ci.Id_Cliente
JOIN Detalle_Cita dc ON ci.Id_Cita = dc.Id_Cita
WHERE c.Genero = 'Hombre' AND dc.Id_Servicio IN (1, 2)
GROUP BY 
    c.Id_Cliente, 
    c.Nombres, 
    c.Apellido_Paterno, 
    c.Apellido_Materno
HAVING COUNT(DISTINCT dc.Id_Servicio) = 2;

-- consulta 5- Lista de clientes que se tiñen el pelo, indicando la comuna del cliente, la peluqueria donde se atendio y el valor que pago

SELECT Cliente.Nombres, Cliente.Apellido_Paterno, Comuna.Nombre_Comuna, Peluqueria.Nombre, Detalle_Cita.Subtotal 
FROM Cliente
JOIN Comuna ON Cliente.Id_Comuna = Comuna.Id_Comuna
JOIN Citas ON Cliente.Id_Cliente = Citas.Id_Cliente
JOIN Peluqueria ON Citas.Id_Peluqueria = Peluqueria.Id_Peluqueria
JOIN Detalle_Cita ON Citas.Id_Cita = Detalle_Cita.Id_Cita
JOIN Servicio ON Detalle_Cita.Id_Servicio = Servicio.Id_Servicio
WHERE Servicio.Nombre = 'Tinte';

-- Consulta 6- Identificar el horario mas concurrido por peluqueria durante el 2018 y 2029, desagregados por mes

WITH Concurrencia AS (
    SELECT 
        EXTRACT(YEAR FROM Citas.Fecha_Cita) AS Anio,
        EXTRACT(MONTH FROM Citas.Fecha_Cita) AS Mes,
        Peluqueria.Nombre,
        Horario.Hora_Inicio,
        Horario.Hora_Fin,
        COUNT(Citas.Id_Cita) AS Cantidad_Citas
    FROM Citas
    JOIN Peluqueria ON Citas.Id_Peluqueria = Peluqueria.Id_Peluqueria
    JOIN Horario ON Citas.Id_Horario = Horario.Id_Horario
    -- Sacar datos entre los años 2018 y 2029
    WHERE EXTRACT(YEAR FROM Citas.Fecha_Cita) BETWEEN 2018 AND 2029
    GROUP BY Anio, Mes, Peluqueria.Nombre, Horario.Hora_Inicio, Horario.Hora_Fin
),
Ranking AS (
    SELECT *, RANK() OVER(PARTITION BY Nombre, Anio, Mes ORDER BY Cantidad_Citas DESC) AS Rnk
    FROM Concurrencia
)
SELECT Anio, Mes, Nombre, Hora_Inicio, Hora_Fin, Cantidad_Citas
FROM Ranking
WHERE Rnk = 1
ORDER BY Anio ASC, Mes ASC, Nombre ASC;

-- consulta 7- Identificar al cliente por peluqueria que ha tenido las citas mas largas por mes

WITH d AS ( -- Calcula duración y extrae el mes/año de cada cita
  SELECT 
    c.Id_Peluqueria, 
    c.Id_Cliente, 
    TO_CHAR(c.Fecha_Cita, 'YYYY-MM') AS mes, -- PostgreSQL: Convierte fecha a 'AAAA-MM'
    EXTRACT(EPOCH FROM (h.Hora_Fin - h.Hora_Inicio)) / 60 AS dur -- PostgreSQL: Diferencia exacta en minutos
  FROM citas c 
  JOIN horario h ON c.Id_Horario = h.Id_Horario
),
r AS ( -- Asigna ranking sin colapsar filas
  SELECT *, 
    ROW_NUMBER() OVER (
      PARTITION BY Id_Peluqueria, mes -- Reinicia la numeración por cada peluquería y mes
      ORDER BY dur DESC -- Ordena de mayor a menor duración
    ) AS rn 
  FROM d
)
-- Filtra solo el registro con ranking 1
SELECT Id_Peluqueria, Id_Cliente, mes, dur 
FROM r 
WHERE rn = 1 
ORDER BY Id_Peluqueria, mes;

-- consulta 8- Identificar el servicio mas caro por peluqueria

SELECT DISTINCT Peluqueria.Nombre, Servicio.Nombre, Detalle_Cita.Precio_Unitario
FROM Peluqueria
JOIN Citas ON Peluqueria.Id_Peluqueria = Citas.Id_Peluqueria
JOIN Detalle_Cita ON Citas.Id_Cita = Detalle_Cita.Id_Cita
JOIN Servicio ON Detalle_Cita.Id_Servicio = Servicio.Id_Servicio
WHERE Detalle_Cita.Precio_Unitario = (
    SELECT MAX(Detalle_Cita_Dos.Precio_Unitario)
    FROM Citas AS Citas_Dos
    JOIN Detalle_Cita AS Detalle_Cita_Dos ON Citas_Dos.Id_Cita = Detalle_Cita_Dos.Id_Cita
    WHERE Citas_Dos.Id_Peluqueria = Peluqueria.Id_Peluqueria
);

-- consulta 9- Identificar al peluquero que ha trabajado mas por mes durante el 2021

WITH ConteoCitas AS -- Se genera la tabla ConteoCitas a partir de la siguiente declaracion:
(SELECT EXTRACT(MONTH FROM c.Fecha_Cita) AS Mes, -- Seleccionamos los meses extraidos de las fechas como MES
		e.Nombres,e.Apellidos, -- El nombre del empleado
		COUNT(c.Id_Cita) AS Total_Citas, -- Se cuenta el total de las citas del mes
        RANK() OVER (PARTITION BY EXTRACT(MONTH FROM c.Fecha_Cita) ORDER BY COUNT(c.Id_Cita) DESC) as Ranking -- Hacemos un ranking ventana, utilizando como base los meses de las fechas, ordenandolos de manera descendente por la cantidad de citas por mes, y el que este mas arriba es el 1ro
		FROM Citas c -- Desde las citas
		JOIN Empleado e ON c.Id_Empleado = e.Id_Empleado -- Unimos los empleados que tengan citas junto a la tabla empleados
		WHERE EXTRACT(YEAR FROM c.Fecha_Cita) = 2021 -- Extraemos solamente las citas que tengan el anio 2021
		GROUP BY Mes, e.Id_Empleado, e.Nombres, e.Apellidos) -- Y agrupamos por mes, id nombre y apellido empleado
SELECT Mes, Nombres,Apellidos,Total_Citas -- Se seleciona mes nombres y total citas
FROM ConteoCitas -- de la nueva tabla conteocitas
WHERE Ranking = 1 -- donde ranking sea 1, o sea quienes cumplieron con tener mas citas
ORDER BY Mes ASC; -- y se ordenan de orden ascendente

-- consulta 10- Identificar lista de total de peluquerias por comuna, cantidad de peluquerias, cantidad de clientes, residentes en la comuna

SELECT c.Nombre_Comuna, -- Se selecciona los datos que queremos ver, en ese caso el nombre de la comuna
COUNT(DISTINCT p.Id_Peluqueria) AS Cantidad_Peluquerias, -- Aqui hay unos count, en el cual contamos solamente las peluquerias UNICAS
COUNT(DISTINCT cl.Id_Cliente) AS Cantidad_Clientes_Residentes -- Aqui hay unos count, en el cual contamos solamente los clientes UNICOS
from Comuna c -- Sacamos los datos principales de la comuna
left join Cliente cl on  c.Id_Comuna = cl.Id_Comuna -- Se hace left join, ya que sacamos solamente los que tengan ids compatibles
left join Peluqueria p on  c.Id_Comuna = p.Id_Comuna 
group by c.Id_Comuna,c.Nombre_Comuna -- Agrupamos por nombre e id comuna
order by Cantidad_Peluquerias DESC; -- y los ordenamos por el tag Cantidad_Peluquerias

