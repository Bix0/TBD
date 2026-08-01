# Script de inicialización de datos de prueba para la API MMORPG
# Ejecutar desde: backend/mmorpg/data/
# O usar: powershell -ExecutionPolicy Bypass -File init-data.ps1

$BASE_URL = "http://localhost:8080"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Inicializando datos de prueba MMORPG" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si el servidor está corriendo
Write-Host "Verificando conexión con el servidor..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/api/jugadores" -Method Get -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Servidor conectado" -ForegroundColor Green
} catch {
    Write-Host "✗ Error: No se pudo conectar al servidor en $BASE_URL" -ForegroundColor Red
    Write-Host "  Asegúrate de que Spring Boot esté corriendo" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 1. Crear jugador 1 (admin)
Write-Host "1. Creando jugador admin..." -ForegroundColor Yellow
$body = @{
    username = "admin"
    password = "admin123"
    rol = "admin"
} | ConvertTo-Json
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/jugadores" -Method Post -Body $body -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

# 2. Crear jugador 2 (raider)
Write-Host "2. Creando jugador raider..." -ForegroundColor Yellow
$body = @{
    username = "raider1"
    password = "raider123"
    rol = "usuario"
} | ConvertTo-Json
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/jugadores" -Method Post -Body $body -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

# 3. Crear clan
Write-Host "3. Creando clan..." -ForegroundColor Yellow
$body = @{
    nombre = "Dragones Rojos"
    id_lider = 1
} | ConvertTo-Json
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/clanes" -Method Post -Body $body -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

# 4. Crear personaje 1 (líder)
Write-Host "4. Creando personaje líder..." -ForegroundColor Yellow
$body = @{
    id_jugador = 1
    id_clan = 1
    nombre = "Thorin"
    clase = "Guerrero"
    nivel = 60
    faccion = "Alianza"
    item_level = 450
    puntos_merito = 2000
    rol_clan = "Lider"
} | ConvertTo-Json
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/personajes" -Method Post -Body $body -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

# 5. Crear personaje 2 (raider)
Write-Host "5. Creando personaje raider..." -ForegroundColor Yellow
$body = @{
    id_jugador = 2
    id_clan = 1
    nombre = "Legolas"
    clase = "Ranger"
    nivel = 58
    faccion = "Alianza"
    item_level = 420
    puntos_merito = 1500
    rol_clan = "Raider"
} | ConvertTo-Json
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/personajes" -Method Post -Body $body -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

# 6. Crear items
Write-Host "6. Creando items..." -ForegroundColor Yellow

Write-Host "   - Creando Espada del Rey..." -ForegroundColor Yellow
$body = @{
    nombre = "Espada del Rey"
    item_lvl = 450
    ganancia_dkp = 100
} | ConvertTo-Json
Invoke-RestMethod -Uri "$BASE_URL/api/items" -Method Post -Body $body -ContentType "application/json"
Write-Host "     ✓ Creado" -ForegroundColor Green

Write-Host "   - Creando Poción de Vida..." -ForegroundColor Yellow
$body = @{
    nombre = "Poción de Vida"
    item_lvl = 50
    ganancia_dkp = 10
} | ConvertTo-Json
Invoke-RestMethod -Uri "$BASE_URL/api/items" -Method Post -Body $body -ContentType "application/json"
Write-Host "     ✓ Creado" -ForegroundColor Green

Write-Host "   - Creando Escudo del Guardian..." -ForegroundColor Yellow
$body = @{
    nombre = "Escudo del Guardian"
    item_lvl = 400
    ganancia_dkp = 80
} | ConvertTo-Json
Invoke-RestMethod -Uri "$BASE_URL/api/items" -Method Post -Body $body -ContentType "application/json"
Write-Host "     ✓ Creado" -ForegroundColor Green
Write-Host ""

# 7. Agregar clases permitidas
Write-Host "7. Agregando clases permitidas para items..." -ForegroundColor Yellow

Write-Host "   - Espada del Rey: Guerrero, Paladin..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/api/items/1/clases-permitidas?clase=Guerrero" -Method Post -ContentType "application/json"
Invoke-RestMethod -Uri "$BASE_URL/api/items/1/clases-permitidas?clase=Paladin" -Method Post -ContentType "application/json"
Write-Host "     ✓ Agregadas" -ForegroundColor Green

Write-Host "   - Escudo del Guardian: Guerrero, Paladin, Tanque..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/api/items/3/clases-permitidas?clase=Guerrero" -Method Post -ContentType "application/json"
Invoke-RestMethod -Uri "$BASE_URL/api/items/3/clases-permitidas?clase=Paladin" -Method Post -ContentType "application/json"
Invoke-RestMethod -Uri "$BASE_URL/api/items/3/clases-permitidas?clase=Tanque" -Method Post -ContentType "application/json"
Write-Host "     ✓ Agregadas" -ForegroundColor Green
Write-Host ""

# 8. Crear raid
Write-Host "8. Creando raid..." -ForegroundColor Yellow
$body = @{
    nombre = "Raid de Prueba"
    fecha = "2026-05-01T20:00:00"
    estado = "Programada"
    item_level_requerido = 400
    cupos_tanque = 2
    cupos_healer = 2
    cupos_dps = 6
} | ConvertTo-Json
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/raids" -Method Post -Body $body -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

# 9. Inscribir personaje a raid
Write-Host "9. Inscribiendo personaje a raid..." -ForegroundColor Yellow
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/raids/1/inscribir?idPersonaje=1" -Method Post -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

# 10. Agregar item al inventario
Write-Host "10. Agregando item al inventario..." -ForegroundColor Yellow
$body = @{
    id_item = 1
    cantidad = 1
    equipado = $true
} | ConvertTo-Json
$RESPONSE = Invoke-RestMethod -Uri "$BASE_URL/api/personajes/1/inventario" -Method Post -Body $body -ContentType "application/json"
Write-Host "   $RESPONSE" -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  ✓ Inicialización completada" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Datos creados:" -ForegroundColor Yellow
Write-Host "  - 2 jugadores (admin, raider1)"
Write-Host "  - 1 clan (Dragones Rojos)"
Write-Host "  - 2 personajes (Thorin, Legolas)"
Write-Host "  - 3 items (Espada del Rey, Poción de Vida, Escudo del Guardian)"
Write-Host "  - 1 raid (Raid de Prueba)"
Write-Host "  - 1 inscripción a raid"
Write-Host "  - 1 item en inventario (equipado)"
Write-Host ""
Write-Host "Para ver los datos, usa:" -ForegroundColor Yellow
Write-Host "  curl http://localhost:8080/api/jugadores"
Write-Host "  curl http://localhost:8080/api/personajes"
Write-Host "  curl http://localhost:8080/api/clanes"
Write-Host "  curl http://localhost:8080/api/items"
Write-Host "  curl http://localhost:8080/api/raids"
Write-Host "  curl http://localhost:8080/api/ranking"