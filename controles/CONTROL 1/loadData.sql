-- comunas con su id
INSERT INTO Comuna (Id_Comuna, Nombre_Comuna) VALUES
(1, 'Santiago Centro'),
(2, 'Providencia'),
(3, 'Maipu');

-- ejemplo de roles que existen en la peluqueria 
INSERT INTO Rol_Empleado (Id_Rol, Nombre_Rol) VALUES
(1, 'Peluquero Estilista'),
(2, 'Barbero'),
(3, 'Colorista');

-- ejemplo de categoria del producto
INSERT INTO Categoria_Producto (Id_Categoria_Prod, Nombre_Categoria) VALUES
(1, 'Fijacion y Moldeado'),
(2, 'Cuidado Capilar');

-- ejemplo de categoria del servicio
INSERT INTO Categoria_Servicio (Id_Categoria_Serv, Nombre_Categoria) VALUES
(1, 'Cortes'),
(2, 'Coloracion'),
(3, 'Cuidado Facial');

-- tabla de peluqueria
INSERT INTO Peluqueria (Id_Peluqueria, Id_Comuna, Nombre, Calle, Numero) VALUES
(1, 1, 'Estilo Urbano', 'Ahumada', '123'),
(2, 2, 'Elegance', 'Av. Providencia', '4560');

-- tabla de cliente
INSERT INTO Cliente (Id_Cliente, Rut_Cliente, Nombres, Apellido_Paterno, Apellido_Materno, Correo_Cliente, Fecha_Nac, Genero, Id_Comuna) VALUES
(1, '11111111-1', 'Juan', 'Perez', 'Soto', 'juan@mail.com', '1990-05-15', 'Hombre', 1),
(2, '22222222-2', 'Maria', 'Gonzalez', 'Rios', 'maria@mail.com', '1985-10-20', 'Mujer', 2),
(3, '33333333-3', 'Carlos', 'Lopez', 'Vera', 'carlos@mail.com', '1995-03-10', 'Hombre', 3),
(4, '44444444-4', 'Ana', 'Silva', 'Mora', 'ana@mail.com', '1992-12-05', 'Mujer', 1);

-- tabla de empreado
INSERT INTO Empleado (Id_Empleado, Rut_Empleado, Nombres, Apellidos, Id_Rol, Id_Peluqueria) VALUES
(1, '12345678-9', 'Pedro', 'Martinez', 2, 1),
(2, '98765432-1', 'Luis', 'Fernandez', 3, 2),
(3, '11223344-5', 'Rosa', 'Gomez', 1, 1);

-- tabla de producto
INSERT INTO Producto (Id_Producto, Nombre_Producto, Precio_Producto, Stock, Id_Categoria_Prod) VALUES
(1, 'Gel Fijador Extremo', 5000, 50, 1),
(2, 'Cera Modeladora Mate', 7000, 30, 1),
(3, 'Shampoo Reparador', 12000, 20, 2);

-- tabla de servicio
INSERT INTO Servicio (Id_Servicio, Nombre, Precio, Id_Categoria_Serv) VALUES
(1, 'Corte de pelo', 15000, 1),
(2, 'Corte de barba', 8000, 3),
(3, 'Tinte', 35000, 2),
(4, 'Lavado y Secado', 5000, 1);

-- asociar un cliente con una peluqieroa
INSERT INTO Clientes_Peluqueria (Id_Cliente, Id_Peluqueria) VALUES
(1, 1),
(2, 2),
(3, 1),
(4, 2);

-- Sueldos de los empreados con una antiguedad de 3 años
INSERT INTO Sueldo (Id_Sueldo, Id_Empleado, Fecha, Monto, Estado) VALUES
(1, 1, '2024-12-01', 800000, 'Pagado'),
(2, 1, '2025-01-01', 850000, 'Pagado'),
(3, 2, '2024-12-01', 900000, 'Pagado'),
(4, 2, '2025-01-01', 950000, 'Pagado'),
(5, 3, '2026-01-01', 750000, 'Pagado');

-- horario
INSERT INTO Horario (Id_Horario, Dia_Semana, Hora_Inicio, Hora_Fin) VALUES
(1, 1, '09:00:00', '10:00:00'), -- 1 hora
(2, 2, '10:00:00', '12:00:00'), -- 2 horas 
(3, 3, '15:00:00', '15:30:00'); -- 30 min

-- citas historial, donde aplicamos para el ejemplo el estado Realizado 
INSERT INTO Citas (Id_Cita, Id_Peluqueria, Id_Cliente, Id_Empleado, Id_Horario, Estado_Cita, Fecha_Cita) VALUES
(1, 1, 1, 1, 1, 'Realizada', '2026-02-15'), 
(2, 2, 2, 2, 2, 'Realizada', '2026-02-16'), 
(3, 1, 3, 1, 3, 'Realizada', '2026-03-01'), 
(4, 1, 1, 1, 1, 'Realizada', '2018-05-10'),
(5, 2, 4, 3, 2, 'Realizada', '2029-08-20'), 
(6, 2, 2, 2, 1, 'Realizada', '2021-11-05'); 

-- ejemplos de citas
-- Cita 1: Juan se corta el pelo y la barba
INSERT INTO Detalle_Cita (Id_Detalle, Id_Cita, Id_Producto, Id_Servicio, Precio_Unitario, Subtotal, Cantidad) VALUES
(1, 1, NULL, 1, 15000, 15000, 1),
(2, 1, NULL, 2, 8000, 8000, 1);

-- Cita 2: Maria se tiñe
INSERT INTO Detalle_Cita (Id_Detalle, Id_Cita, Id_Producto, Id_Servicio, Precio_Unitario, Subtotal, Cantidad) VALUES
(3, 2, NULL, 3, 35000, 35000, 1);

-- Cita 3: Carlos se corta la barba
INSERT INTO Detalle_Cita (Id_Detalle, Id_Cita, Id_Producto, Id_Servicio, Precio_Unitario, Subtotal, Cantidad) VALUES
(4, 3, NULL, 2, 8000, 8000, 1);

-- citas con años pasados y futuros
INSERT INTO Detalle_Cita (Id_Detalle, Id_Cita, Id_Producto, Id_Servicio, Precio_Unitario, Subtotal, Cantidad) VALUES
(5, 4, NULL, 1, 15000, 15000, 1),
(6, 5, NULL, 4, 5000, 5000, 1),
(7, 6, NULL, 3, 35000, 35000, 1);

-- la tabla de pagos 
INSERT INTO Pago (Id_Pago, Id_Cita, Fecha_Pago, Monto_Total, Medio_de_pago, Estado_pago, Tipo_Comprobante) VALUES
(1, 1, '2026-02-15', 23000, 'Tarjeta', 'Pagado', 'Boleta'),
(2, 2, '2026-02-16', 35000, 'Efectivo', 'Pagado', 'Factura'),
(3, 3, '2026-03-01', 8000, 'Tarjeta', 'Pagado', 'Boleta'),
(4, 4, '2018-05-10', 15000, 'Efectivo', 'Pagado', 'Boleta'),
(5, 5, '2029-08-20', 5000, 'Tarjeta', 'Pagado', 'Boleta'),
(6, 6, '2021-11-05', 35000, 'Transferencia', 'Pagado', 'Factura');