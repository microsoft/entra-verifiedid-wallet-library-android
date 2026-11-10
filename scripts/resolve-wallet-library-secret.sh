#!/usr/bin/env bash
set -euo pipefail

if [ -n "${WALLET_LIBRARY_API_KEY:-}" ]; then
  secret_value="${WALLET_LIBRARY_API_KEY}"
else
  if [ -n "${AZURE_KEY_VAULT_NAME:-}" ] && [ -n "${AZURE_KEY_VAULT_SECRET_NAME:-}" ]; then
    if command -v az >/dev/null 2>&1; then
      secret_value="$(az keyvault secret show --name "$AZURE_KEY_VAULT_SECRET_NAME" --vault-name "$AZURE_KEY_VAULT_NAME" --query value -o tsv)"
    else
      echo "Azure CLI is required when Azure Key Vault variables are provided" >&2
      exit 1
    fi
  else
    secret_value=""
  fi
fi

if [ -n "${GITHUB_ENV:-}" ]; then
  echo "WALLET_LIBRARY_API_KEY=${secret_value}" >> "$GITHUB_ENV"
fi

echo "$secret_value"
