.PHONY: all verify backend-verify frontend-build build up down bench audit clean

MAVEN_IMAGE ?= maven:3.9.11-eclipse-temurin-21-alpine
NODE_IMAGE ?= node:20-alpine

all: verify build up

verify: backend-verify frontend-build

backend-verify:
	docker run --rm -v "$(CURDIR)/backend:/app" -w /app $(MAVEN_IMAGE) mvn -B clean verify

frontend-build:
	docker run --rm -v "$(CURDIR)/frontend:/app" -w /app $(NODE_IMAGE) sh -c "npm ci && npm run build -- --configuration production"

build:
	docker compose build

up:
	docker compose up -d --wait

down:
	docker compose down

bench:
	@echo "Ejecutando pruebas de carga k6..."
	k6 run scripts/k6/load-test.js

audit:
	@echo "Ejecutando auditoria Lighthouse y OWASP..."
	npx lhci autorun

clean:
	docker compose down --volumes --remove-orphans
