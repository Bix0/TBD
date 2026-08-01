#!/bin/bash
# Script de inicialización de datos de prueba para la API MMORPG
# Ejecutar desde: backend/mmorpg/data/
# O usar: bash init-data.sh

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "  Inicializando datos de prueba MMORPG"
echo "=========================================="
echo ""

# Verificar si el servidor está corriendo
echo "Verificando conexión con el servidor..."
if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/jugadores" | grep -q "200\|404"; then
    echo "✓ Servidor conectado"
else
    echo "✗ Error: No se pudo conectar al servidor en $BASE_URL"
    echo "  Asegúrate de que Spring Boot esté corriendo"
    exit 1
fi
echo ""

# 1. Crear jugador 1 (admin)
echo "1. Creando jugador admin..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/jugadores" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123", "rol": "admin"}')
echo "   $RESPONSE"
echo ""

# 2. Crear jugador 2 (raider)
echo "2. Creando jugador raider..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/jugadores" \
  -H "Content-Type: application/json" \
  -d '{"username": "raider1", "password": "raider123", "rol": "usuario"}')
echo "   $RESPONSE"
echo ""

# 3. Crear clan
echo "3. Creando clan..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/clanes" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Dragones Rojos", "id_lider": 1}')
echo "   $RESPONSE"
echo ""

# 4. Crear personaje 1 (líder)
echo "4. Creando personaje líder..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/personajes" \
  -H "Content-Type: application/json" \
  -d '{"id_jugador": 1, "id_clan": 1, "nombre": "Thorin", "clase": "Guerrero", "nivel": 60, "faccion": "Alianza", "item_level": 450, "puntos_merito": 2000, "rol_clan": "Lider"}')
echo "   $RESPONSE"
echo ""

# 5. Crear personaje 2 (raider)
echo "5. Creando personaje raider..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/personajes" \
  -H "Content-Type: application/json" \
  -d '{"id_jugador": 2, "id_clan": 1, "nombre": "Legolas", "clase": "Ranger", "nivel": 58, "faccion": "Alianza", "item_level": 420, "puntos_merito": 1500, "rol_clan": "Raider"}')
echo "   $RESPONSE"
echo ""

# 6. Crear items
echo "6. Creando items..."
echo "   - Creando Espada del Rey..."
curl -s -X POST "$BASE_URL/api/items" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Espada del Rey", "item_lvl": 450, "ganancia_dkp": 100}'
echo ""

echo "   - Creando Poción de Vida..."
curl -s -X POST "$BASE_URL/api/items" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Poción de Vida", "item_lvl": 50, "ganancia_dkp": 10}'
echo ""

echo "   - Creando Escudo del Guardian..."
curl -s -X POST "$BASE_URL/api/items" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Escudo del Guardian", "item_lvl": 400, "ganancia_dkp": 80}'
echo ""

# 7. Agregar clases permitidas
echo "7. Agregando clases permitidas para items..."
echo "   - Espada del Rey: Guerrero, Paladin..."
curl -s -X POST "$BASE_URL/api/items/1/clases-permitidas?clase=Guerrero"
curl -s -X POST "$BASE_URL/api/items/1/clases-permitidas?clase=Paladin"
echo ""

echo "   - Escudo del Guardian: Guerrero, Paladin, Tanque..."
curl -s -X POST "$BASE_URL/api/items/3/clases-permitidas?clase=Guerrero"
curl -s -X POST "$BASE_URL/api/items/3/clases-permitidas?clase=Paladin"
curl -s -X POST "$BASE_URL/api/items/3/clases-permitidas?clase=Tanque"
echo ""

# 8. Crear raid
echo "8. Creando raid..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/raids" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Raid de Prueba", "fecha": "2026-05-01T20:00:00", "estado": "Programada", "item_level_requerido": 400, "cupos_tanque": 2, "cupos_healer": 2, "cupos_dps": 6}')
echo "   $RESPONSE"
echo ""

# 9. Inscribir personaje a raid
echo "9. Inscribiendo personaje a raid..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/raids/1/inscribir?idPersonaje=1")
echo "   $RESPONSE"
echo ""

# 10. Agregar item al inventario
echo "10. Agregando item al inventario..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/personajes/1/inventario" \
  -H "Content-Type: application/json" \
  -d '{"id_item": 1, "cantidad": 1, "equipado": true}')
echo "   $RESPONSE"
echo ""

echo "=========================================="
echo "  ✓ Inicialización completada"
echo "=========================================="
echo ""
echo "Datos creados:"
echo "  - 2 jugadores (admin, raider1)"
echo "  - 1 clan (Dragones Rojos)"
echo "  - 2 personajes (Thorin, Legolas)"
echo "  - 3 items (Espada del Rey, Poción de Vida, Escudo del Guardian)"
echo "  - 1 raid (Raid de Prueba)"
echo "  - 1 inscripción a raid"
echo "  - 1 item en inventario (equipado)"
echo ""
echo "Para ver los datos, usa:"
echo "  curl http://localhost:8080/api/jugadores"
echo "  curl http://localhost:8080/api/personajes"
echo "  curl http://localhost:8080/api/clanes"
echo "  curl http://localhost:8080/api/items"
echo "  curl http://localhost:8080/api/raids"
echo "  curl http://localhost:8080/api/ranking"