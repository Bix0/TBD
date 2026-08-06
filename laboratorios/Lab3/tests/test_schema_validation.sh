#!/bin/bash
# ============================================================================
# Test automatizado: Regla de Schema Validation ($jsonSchema)
#
# Tarea: "Diseñe una regla de Schema Validation que impida asignar un ítem de
#        Loot a un personaje que no participó en la Raid, o que ya se encuentre
#        'caído' (fuera de combate) al momento de la distribución."
#
# La regla se valida en DOS capas:
#   A) BASE DE DATOS (implementación de la compañera, config/MongoSchemaConfig):
#      validador $jsonSchema sobre historial_loot que exige participoRaid=true y
#      estadoPersonaje en ["Activo","Vivo"] (rechaza "Caido").
#   B) APLICACIÓN: Personaje.estado ("Activo"/"Caido") -> RaidService.entregarItem
#      rechaza el loot si el personaje está caído y registra el estado en el historial.
#
# Requisitos: Docker corriendo y el stack levantado (backend en :8080).
# Uso: bash tests/test_schema_validation.sh
# ============================================================================

set -u

MONGO_CONTAINER="mongo-primary"
DB="db_laboratorio"
COLL="historial_loot_schema_test"
API="http://localhost:8080"

PASS=0
FAIL=0
ok()  { echo "  ✅ PASS: $1"; PASS=$((PASS+1)); }
bad() { echo "  ❌ FAIL: $1"; FAIL=$((FAIL+1)); }

mongo_eval() {
  docker exec "$MONGO_CONTAINER" mongosh --quiet --eval "$1"
}

# ============================================================================
# SECCIÓN A: VALIDADOR $jsonSchema A NIVEL DE BASE DE DATOS
# ============================================================================
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
echo "== A1) El validador quedó aplicado =="
v=$(mongo_eval "db.getSiblingDB('$DB').getCollectionInfos({name: '$COLL'})[0].options.validator")
if echo "$v" | grep -q "jsonSchema"; then
  ok "La colección tiene validador \$jsonSchema"
else
  bad "No se encontró el validador \$jsonSchema (respuesta: $v)"
fi

echo ""
echo "== A2) CASOS POSITIVOS (deben insertar) =="

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p1',itemId:'i1',participoRaid:true,estadoPersonaje:'Activo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "OK" ] && ok "Participó y está 'Activo' -> insertado" || bad "Participó y 'Activo' (esperado OK, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p2',itemId:'i2',participoRaid:true,estadoPersonaje:'Vivo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "OK" ] && ok "Participó y está 'Vivo' -> insertado" || bad "Participó y 'Vivo' (esperado OK, obtuve: $r)"

echo ""
echo "== A3) CASOS NEGATIVOS (deben ser rechazados) =="

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p3',itemId:'i3',participoRaid:false,estadoPersonaje:'Activo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "participoRaid=false (NO participó en la raid) -> rechazado" || bad "participoRaid=false (esperado rechazo, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p4',itemId:'i4',participoRaid:true,estadoPersonaje:'Caido'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "estadoPersonaje='Caido' (fuera de combate) -> rechazado" || bad "estadoPersonaje='Caido' (esperado rechazo, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p5',itemId:'i5',estadoPersonaje:'Activo'}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "Falta campo requerido 'participoRaid' -> rechazado" || bad "Falta 'participoRaid' (esperado rechazo, obtuve: $r)"

r=$(mongo_eval "try { db.getSiblingDB('$DB').$COLL.insertOne({raidId:'r1',personajeId:'p6',itemId:'i6',participoRaid:true}); print('OK') } catch(e) { print('REJECTED') }")
[ "$r" = "REJECTED" ] && ok "Falta campo requerido 'estadoPersonaje' -> rechazado" || bad "Falta 'estadoPersonaje' (esperado rechazo, obtuve: $r)"

# ============================================================================
# SECCIÓN B: REGLA A NIVEL DE APLICACIÓN (estado del personaje)
# ============================================================================
echo ""
echo "== B1) Login admin =="
LOGIN=$(curl -s -X POST "$API/api/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}')
TOKEN=$(echo "$LOGIN" | sed 's/.*"token":"\([^"]*\)".*/\1/')
UIDJ=$(echo "$LOGIN" | sed 's/.*"id":"\([^"]*\)".*/\1/')
if [ -z "$TOKEN" ]; then
  bad "No se obtuvo token de admin (¿backend arriba?)"
else
  ok "Login admin OK"
fi

RAID_ID=$(mongo_eval "db.getSiblingDB('$DB').raids.findOne({estado:'Programada'})._id.toString()" | tr -d '\r')
ITEM_ID=$(mongo_eval "db.getSiblingDB('$DB').items.findOne({})._id.toString()" | tr -d '\r')
PJ_ID=""

if [ -n "$TOKEN" ] && [ -n "$RAID_ID" ] && [ -n "$ITEM_ID" ]; then
  echo ""
  echo "== B2) Crear personaje de prueba con estado 'Caido' =="
  PJ_ID=$(curl -s -X POST "$API/api/personajes" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"jugadorId\":\"$UIDJ\",\"nombre\":\"TestCaido\",\"clase\":\"Guerrero\",\"faccion\":\"Alianza\",\"rolClan\":\"Tanque\",\"nivel\":60,\"itemLevel\":200,\"puntosMerito\":500,\"estado\":\"Caido\"}" \
    | sed 's/.*"idPersonaje":"\([^"]*\)".*/\1/')
  if [ -z "$PJ_ID" ]; then
    bad "No se pudo crear el personaje de prueba"
  else
    ok "Personaje TestCaido creado (estado=Caido)"
  fi

  echo ""
  echo "== B3) Loot a personaje NO inscrito en la raid -> debe rechazar =="
  r=$(curl -s -w "|%{http_code}" -X POST "$API/api/raids/$RAID_ID/distribuir-loot-masivo" \
    -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
    -d "[{\"idPersonaje\":\"$PJ_ID\",\"idItem\":\"$ITEM_ID\",\"costoDkp\":0}]")
  if echo "$r" | grep -q "|400" && echo "$r" | grep -q "no participo"; then
    ok "No inscrito -> 400 'no participo en esta raid'"
  else
    bad "No inscrito (esperado 400 con 'no participo', obtuve: $r)"
  fi

  echo ""
  echo "== B4) Inscribir al personaje caído =="
  r=$(curl -s -w "|%{http_code}" -X POST "$API/api/raids/$RAID_ID/inscribir?idPersonaje=$PJ_ID" \
    -H "Authorization: Bearer $TOKEN")
  if echo "$r" | grep -q "|200"; then
    ok "Inscripción exitosa"
  else
    bad "Inscripción (esperado 200, obtuve: $r)"
  fi

  echo ""
  echo "== B5) Loot a personaje CAÍDO -> debe rechazar =="
  r=$(curl -s -w "|%{http_code}" -X POST "$API/api/raids/$RAID_ID/distribuir-loot-masivo" \
    -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
    -d "[{\"idPersonaje\":\"$PJ_ID\",\"idItem\":\"$ITEM_ID\",\"costoDkp\":0}]")
  if echo "$r" | grep -q "|400" && echo "$r" | grep -qi "caido"; then
    ok "Personaje caído -> 400 'esta caido y no puede recibir loot'"
  else
    bad "Personaje caído (esperado 400 con 'caido', obtuve: $r)"
  fi

  echo ""
  echo "== B6) Restaurar a 'Activo' y loot -> debe funcionar y pasar el validador =="
  mongo_eval "db.getSiblingDB('$DB').personajes.updateOne({_id:ObjectId('$PJ_ID')},{\$set:{estado:'Activo'}})" >/dev/null
  r=$(curl -s -w "|%{http_code}" -X POST "$API/api/raids/$RAID_ID/distribuir-loot-masivo" \
    -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
    -d "[{\"idPersonaje\":\"$PJ_ID\",\"idItem\":\"$ITEM_ID\",\"costoDkp\":0}]")
  if echo "$r" | grep -q "|200"; then
    ok "Personaje Activo -> 200 (lote distribuido)"
    doc=$(mongo_eval "db.getSiblingDB('$DB').historial_loot.findOne({personajeId:'$PJ_ID'})")
    if echo "$doc" | grep -q "participoRaid: true" && echo "$doc" | grep -q "estadoPersonaje: 'Activo'"; then
      ok "Historial con participoRaid=true y estadoPersonaje='Activo' (pasa validador real)"
    else
      bad "Historial no tiene los campos del validador (doc: $doc)"
    fi
  else
    bad "Personaje Activo (esperado 200, obtuve: $r)"
  fi

  echo ""
  echo "== B7) Limpieza =="
  curl -s -o /dev/null -X DELETE "$API/api/personajes/$PJ_ID" -H "Authorization: Bearer $TOKEN"
  mongo_eval "db.getSiblingDB('$DB').inscripciones_raid.deleteMany({personajeId:'$PJ_ID'})" >/dev/null
  mongo_eval "db.getSiblingDB('$DB').historial_loot.deleteMany({personajeId:'$PJ_ID'})" >/dev/null
  echo "  Personaje de prueba y sus registros eliminados."
fi

# ============================================================================
echo ""
echo "== Limpieza colección de prueba (A) =="
mongo_eval "db.getSiblingDB('$DB').$COLL.drop()" >/dev/null 2>&1
echo "  Colección '$COLL' eliminada (historial_loot real intacta)."

echo ""
echo "=========================================="
echo "RESULTADO: $PASS pasaron, $FAIL fallaron"
echo "=========================================="
[ "$FAIL" -eq 0 ]
