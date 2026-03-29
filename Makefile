UPSTREAM_REPO = HelloAOLab/bible-api
UPSTREAM_COMMIT = 1744dd35f460ec88526c063d5c8ecd819072567a
SPEC_YAML = references/openapi.yaml
SPEC_JSON = references/openapi.json
CLIENT_DIR = scripts/generated_client_clj

.PHONY: fetch-upstream spec client all clean

## Fetch upstream TypeScript type definitions at a pinned commit
fetch-upstream:
	@mkdir -p vendor
	curl -sL "https://raw.githubusercontent.com/$(UPSTREAM_REPO)/$(UPSTREAM_COMMIT)/packages/helloao-tools/generation/api.ts" > vendor/api.ts
	curl -sL "https://raw.githubusercontent.com/$(UPSTREAM_REPO)/$(UPSTREAM_COMMIT)/packages/helloao-tools/generation/common-types.ts" > vendor/common-types.ts
	curl -sL "https://raw.githubusercontent.com/$(UPSTREAM_REPO)/$(UPSTREAM_COMMIT)/API-CHANGELOG.md" > vendor/API-CHANGELOG.md

## Generate OpenAPI spec from vendor TS types
spec: vendor/api.ts vendor/common-types.ts
	node scripts/generate_spec.js > $(SPEC_YAML)
	node scripts/generate_spec.js --json > $(SPEC_JSON)

## Generate Clojure client from OpenAPI spec using openapi-generator-cli
client: $(SPEC_YAML)
	npx @openapitools/openapi-generator-cli generate \
		-i $(SPEC_YAML) \
		-g clojure \
		-o $(CLIENT_DIR) \
		--additional-properties=projectName=bible-api-client

## Run full pipeline
all: spec client

## Clean generated artifacts
clean:
	rm -rf $(CLIENT_DIR)
