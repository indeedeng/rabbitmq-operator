kubectl get secret $1-runtime-secret -o json | jq -r '.data["default-password"]' | base64 --decode
echo
