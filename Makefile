UPSTREAM_REPO = HelloAOLab/bible-api
UPSTREAM_COMMIT = 1744dd35f460ec88526c063d5c8ecd819072567a
SPEC_YAML = references/openapi.yaml
SPEC_JSON = references/openapi.json
CLIENT_DIR = scripts/generated_client

.PHONY: fetch-upstream spec client all clean

fetch-upstream:
	@mkdir -p vendor
	curl -sL "https://raw.githubusercontent.com/$(UPSTREAM_REPO)/$(UPSTREAM_COMMIT)/packages/helloao-tools/generation/api.ts" > vendor/api.ts
	curl -sL "https://raw.githubusercontent.com/$(UPSTREAM_REPO)/$(UPSTREAM_COMMIT)/packages/helloao-tools/generation/common-types.ts" > vendor/common-types.ts
	curl -sL "https://raw.githubusercontent.com/$(UPSTREAM_REPO)/$(UPSTREAM_COMMIT)/API-CHANGELOG.md" > vendor/API-CHANGELOG.md

spec: vendor/api.ts vendor/common-types.ts
	node scripts/generate_spec.js > $(SPEC_YAML)
	node scripts/generate_spec.js --json > $(SPEC_JSON)

client: $(SPEC_JSON)
	python3 scripts/generate_client.py $(SPEC_JSON) $(CLIENT_DIR)

all: spec client

clean:
	rm -rf $(CLIENT_DIR)/__pycache__
