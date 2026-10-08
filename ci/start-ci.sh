#!/usr/bin/env bash
# Starts SonarQube and Jenkins for the SafeStep pipeline and wires them together (token, webhook, job).
# Secrets are generated on the first run and kept in ci/.env, which is git-ignored.
set -euo pipefail

cd "$(dirname "$0")"
ENV_FILE=".env"
SONAR_URL="http://localhost:9000"

random_secret() { head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20; }
set_env() { # name value
  grep -v "^$1=" "$ENV_FILE" > "$ENV_FILE.tmp" 2>/dev/null || true
  echo "$1=$2" >> "$ENV_FILE.tmp"
  mv "$ENV_FILE.tmp" "$ENV_FILE"
}
get_env() { grep "^$1=" "$ENV_FILE" 2>/dev/null | head -1 | cut -d= -f2- || true; }

touch "$ENV_FILE"
[ -n "$(get_env JENKINS_ADMIN_PASSWORD)" ] || set_env JENKINS_ADMIN_PASSWORD "Jk-$(random_secret)"
[ -n "$(get_env SONAR_ADMIN_PASSWORD)" ] || set_env SONAR_ADMIN_PASSWORD "Sq-$(random_secret)-9"
set_env BACKEND_REPO_PATH "$(cd .. && pwd -W 2>/dev/null || pwd)"

echo "==> Starting SonarQube"
docker compose --env-file "$ENV_FILE" up -d sonarqube-server
until [ "$(curl -s "$SONAR_URL/api/system/status" | sed -n 's/.*"status":"\([A-Z_]*\)".*/\1/p')" = "UP" ]; do
  echo "    waiting for SonarQube..."; sleep 10
done

SONAR_ADMIN_PASSWORD="$(get_env SONAR_ADMIN_PASSWORD)"
if curl -s -u "admin:admin" "$SONAR_URL/api/authentication/validate" | grep -q '"valid":true'; then
  echo "==> Changing the default SonarQube admin password"
  curl -s -u "admin:admin" -X POST "$SONAR_URL/api/users/change_password" \
    --data-urlencode "login=admin" --data-urlencode "previousPassword=admin" \
    --data-urlencode "password=$SONAR_ADMIN_PASSWORD" > /dev/null
fi

echo "==> Generating the analysis token for Jenkins"
curl -s -u "admin:$SONAR_ADMIN_PASSWORD" -X POST "$SONAR_URL/api/user_tokens/revoke" --data-urlencode "name=jenkins-token" > /dev/null || true
TOKEN="$(curl -s -u "admin:$SONAR_ADMIN_PASSWORD" -X POST "$SONAR_URL/api/user_tokens/generate" \
  --data-urlencode "name=jenkins-token" | sed -n 's/.*"token":"\([a-z0-9_]*\)".*/\1/p')"
[ -n "$TOKEN" ] || { echo "Could not generate the SonarQube token"; exit 1; }
set_env SONAR_TOKEN "$TOKEN"

echo "==> Registering the Jenkins webhook in SonarQube"
if ! curl -s -u "admin:$SONAR_ADMIN_PASSWORD" "$SONAR_URL/api/webhooks/list" | grep -q "Jenkins-CI-Webhook"; then
  curl -s -u "admin:$SONAR_ADMIN_PASSWORD" -X POST "$SONAR_URL/api/webhooks/create" \
    --data-urlencode "name=Jenkins-CI-Webhook" \
    --data-urlencode "url=http://jenkins-master:9089/sonarqube-webhook/" > /dev/null
fi

echo "==> Starting Jenkins (image build downloads the plugins and JDK 26 on the first run)"
docker compose --env-file "$ENV_FILE" up -d --build jenkins-master

echo "==> Done. Jenkins: http://localhost:9089 (user admin, password in ci/.env)  SonarQube: $SONAR_URL"
