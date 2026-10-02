#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
command -v java >/dev/null || { echo 'Install Java 17 or newer first.'; exit 1; }
[[ -f healthplus.jar ]] || { echo 'Extract the complete HealthPlus package first.'; exit 1; }
if [[ -z ${HEALTHPLUS_DB_USER:-} ]]; then
  read -r -p 'MySQL username (press Enter for root): ' db_user
  export HEALTHPLUS_DB_USER=${db_user:-root}
fi
if [[ ! ${HEALTHPLUS_DB_PASSWORD+x} ]]; then
  read -r -s -p 'Your local MySQL password: ' HEALTHPLUS_DB_PASSWORD
  echo
  export HEALTHPLUS_DB_PASSWORD
fi
exec java -cp 'healthplus.jar:lib/*' application.Main
