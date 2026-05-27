#!/usr/bin/env bash
set -euo pipefail

for port in 8081 8082 8083; do
  for attempt in $(seq 1 30); do
    if (echo >"/dev/tcp/localhost/${port}") >/dev/null 2>&1 || [ "${attempt}" -eq 30 ]; then
      break
    fi
    sleep 2
  done
done

email="smoke-$(date +%s)@example.com"
register_json=$(curl -fsS -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${email}\",\"password\":\"123456\",\"fullName\":\"Smoke Test User\"}")
user_id=$(printf "%s" "${register_json}" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

cat > /tmp/cv_payload.json <<EOF
{
  "userId": "${user_id}",
  "fullName": "Smoke Test User",
  "title": "Distributed Systems Engineer",
  "summary": "End-to-end smoke test profile for rich CV graph validation.",
  "skills": ["Java", "Spring Boot", "Kafka", "gRPC"],
  "experience": [
    {
      "companyName": "Acme Systems",
      "jobTitle": "Backend Engineer",
      "startDate": "2024-01",
      "endDate": "2026-05",
      "responsibilities": "Built event-driven APIs and service integrations."
    }
  ],
  "education": [
    {
      "institution": "Engineering University",
      "fieldOfStudy": "Software Engineering",
      "degreeLevel": "Bachelor",
      "graduationYear": "2026"
    }
  ],
  "projects": [
    {
      "projectName": "CV Builder",
      "technicalStack": "Spring Boot, Kafka, MySQL, gRPC",
      "projectUrl": "https://example.com/cv-builder",
      "architecturalSummary": "Microservice profile publishing pipeline."
    }
  ],
  "languages": [
    {"languageName": "Arabic", "fluencyTier": "Native"},
    {"languageName": "English", "fluencyTier": "Professional"}
  ]
}
EOF

cv_json=$(curl -fsS -X POST http://localhost:8082/api/cv/save \
  -H "Content-Type: application/json" \
  --data-binary @/tmp/cv_payload.json)
cv_id=$(printf "%s" "${cv_json}" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

curl -fsS -X PUT "http://localhost:8082/api/cv/${cv_id}/publish" >/tmp/publish.json
sleep 8

status=$(curl -sS -o /tmp/public_cv.json -w "%{http_code}" "http://localhost:8083/api/social/cv/${cv_id}")
python3 - <<PY
import json
status = "${status}"
if status != "200":
    raise SystemExit(f"Expected public detail 200 before suspension, got {status}")
with open("/tmp/public_cv.json", encoding="utf-8") as f:
    data = json.load(f)
for key, expected in {
    "skills": 4,
    "experience": 1,
    "education": 1,
    "projects": 1,
    "languages": 2,
}.items():
    actual = len(data.get(key) or [])
    if actual != expected:
        raise SystemExit(f"Expected {key} count {expected}, got {actual}")
print("PUBLIC_DETAIL_OK", data["id"], data["fullName"])
PY

curl -fsS "http://localhost:8083/ui/cv/${cv_id}" > /tmp/public_cv_page.html
if ! grep -q "Export PDF" /tmp/public_cv_page.html || ! grep -q "Work Experience" /tmp/public_cv_page.html; then
  echo "UI detail page missing PDF/profile sections" >&2
  exit 1
fi

docker exec cv-mysql mysql -uroot -ppassword -D auth_db \
  -e "UPDATE user_accounts SET status = 'SUSPENDED' WHERE id = '${user_id}';" >/dev/null
sleep 3

suspended_status=$(curl -sS -o /tmp/suspended_cv.json -w "%{http_code}" "http://localhost:8083/api/social/cv/${cv_id}")
curl -fsS http://localhost:8083/api/social/feed > /tmp/feed.json
python3 - <<PY
import json
cv_id = "${cv_id}"
status = "${suspended_status}"
if status != "403":
    raise SystemExit(f"Expected detail 403 after suspension, got {status}")
with open("/tmp/feed.json", encoding="utf-8") as f:
    feed = json.load(f)
if any(item.get("id") == cv_id for item in feed):
    raise SystemExit("Suspended CV still appears in feed")
print("GRPC_SUSPEND_OK", cv_id)
PY

printf "SMOKE_OK email=%s userId=%s cvId=%s\n" "${email}" "${user_id}" "${cv_id}"
