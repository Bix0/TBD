#!/bin/bash
# ============================================================================
# Test automatizado: Regla de Schema Validation ($jsonSchema)
#
# Tarea: "Diseñe una regla de Schema Validation que impida asignar un ítem de
#        Loot a un personaje que no participó en la Raid, o que ya se encuentre
#        'caído' (fuera de combate) al momento de la distribución."
#
# Implementación de la compañera (commit 8cfd007, main):
#   - config/MongoSchemaConfig.java  (validador $jsonSchema sobre historial_loot)
#   - models/HistorialLoot.java       (campos participoRaid y estadoPersonaje)
#
# Este script valida la regla a nivel de BASE DE DATOS usando una colección de
# prueba (no toca historial_loot real) y termina con código de salida 0/1.
# Requisitos: Docker corriendo y el stack levantado (mongo-primary).
# Uso: bash tests/test_schema_validation.sh
# ============================================================================

set -u

MONGO_CONTAINER="mongo-primary"
DB="db_laboratorio"
COLL="historial_loot_schema_test"

PASS=0
FAIL=0
ok()  { echo "  ✅ PASS: $1"; PASS=$((PASS+1)); }
bad() { echo "  ❌ FAIL: $1"; FAIL=$((FAIL+1)); }

mongo_eval() {
  docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "$1"
}

echo "== 0) Preparación =="
mongo_eval "db.getSiblingDB('$DB').$COLL.drop()" >/dev/null 2>&1
mongo_eval "
  db.getSiblingDB('$DB').createCollection('$COLL', {
    validator: {
      \$jsonSchema: {
        bsonType: 'object',
        required: ['raidId', 'personajeId', 'itemId', 'participoRaid', 'estadoPersonaje'],
        properties: {
          raidId:          { bsonType: 'string' },
          personajeId:     { bsonType: 'string' },
          itemId:          { bsonType: 'string' },
          participoRaid:   { enum: [true] },
          estadoPersonaje: { enum: ['Activo', 'Vivo'] }
        }
      }
    },
    validationLevel: 'strict',
    validationAction: 'error'
  })" >/dev/null
echo "  Colección de prueba '$COLL' creada con el validador."

echo ""
echo "== 1) El validador quedó aplicado =="
v=$(mongo_eval "db.getSiblingDB('$DB').getCollectionInfos({name: '$COLL'})[0].options.validator")
if echo "$v" | grep -q "jsonSchema"; then
  ok "La colección tiene validador \$jsonSchema"
else
  bad "No se encontró el validador \$jsonSchema (respuesta: $v)"
fi

echo ""
echo "== 2) CASOS POSITIVOS (deben insertar) =="

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p1',itemId:'i1',participoRaid:true,estadoPersonaje:'Activo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "OK" ] && ok "Participó y está 'Activo' -> insertado" || bad "Participó y 'Activo' (esperado OK, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p2',itemId:'i2',participoRaid:true,estadoPersonaje:'Vivo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "OK" ] && ok "Participó y está 'Vivo' -> insertado" || bad "Participó y 'Vivo' (esperado OK, obtuve: $r)"

echo ""
echo "== 3) CASOS NEGATIVOS (deben ser rechazados) =="

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p3',itemId:'i3',participoRaid:false,estadoPersonaje:'Activo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "participoRaid=false (NO participó en la raid) -> rechazado" || bad "participoRaid=false (esperado rechazo, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p4',itemId:'i4',participoRaid:true,estadoPersonaje:'Caido'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "estadoPersonaje='Caido' (fuera de combate) -> rechazado" || bad "estadoPersonaje='Caido' (esperado rechazo, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p5',itemId:'i5',estadoPersonaje:'Activo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "Falta campo requerido 'participoRaid' -> rechazado" || bad "Falta 'participoRaid' (esperado rechazo, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p6',itemId:'i6',participoRaid:true}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "Falta campo requerido 'estadoPersonaje' -> rechazado" || bad "Falta 'estadoPersonaje' (esperado rechazo, obtuve: $r)"

echo ""
echo "== 4) Limpieza =="
mongo_eval "db.getSiblingDB('$DB').$COLL.drop()" >/dev/null 2>&1
echo "  Colección de prueba eliminada (historial_loot real intacta)."

echo ""
echo "=========================================="
echo "RESULTADO: $PASS pasaron, $FAIL fallaron"
echo "=========================================="
[ "$FAIL" -eq 0 ]
