#!/usr/bin/env bash
# Resumen legible de qué prácticas tienes en verde.
#   ./verificar.sh          todas
#   ./verificar.sh UT3      solo las de una unidad (UT4, UT5, UT6, UT7…)
set -uo pipefail
FILTRO="${1:-}"
PATRON="${FILTRO:+${FILTRO}*Test}"; PATRON="${PATRON:-*Test}"

mvn -B -q test -Dtest="$PATRON" -Dmaven.test.failure.ignore=true > /dev/null 2>&1

echo
printf "%-14s %-8s %s\n" "PRÁCTICA" "ESTADO" "DETALLE"
printf "%-14s %-8s %s\n" "────────" "──────" "───────"
ok=0; ko=0
for x in target/surefire-reports/TEST-*.xml; do
  [ -e "$x" ] || continue
  nombre=$(basename "$x" .xml | sed 's/TEST-es.iesx.daw.practicas.//;s/Test$//')
  t=$(grep -o 'tests="[0-9]*"' "$x" | head -1 | tr -dc 0-9)
  f=$(grep -o 'failures="[0-9]*"' "$x" | head -1 | tr -dc 0-9)
  e=$(grep -o 'errors="[0-9]*"' "$x" | head -1 | tr -dc 0-9)
  mal=$(( ${f:-0} + ${e:-0} ))
  if [ "$mal" -eq 0 ]; then
    printf "%-14s \033[32m%-8s\033[0m %s\n" "$nombre" "VERDE" "$t/$t"
    ok=$((ok+1))
  else
    printf "%-14s \033[31m%-8s\033[0m %s\n" "$nombre" "ROJO" "$((t-mal))/$t pasan"
    ko=$((ko+1))
    grep -o 'message="[^"]*"' "$x" | head -2 | sed 's/message="/    → /;s/"$//'
  fi
done
echo
[ "$ko" -eq 0 ] && echo "Todo en verde ($ok prácticas)." \
                || echo "$ok en verde · $ko por terminar."
