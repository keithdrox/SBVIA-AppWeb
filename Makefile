.PHONY: up down test bench audit clean

up:
	docker-compose up --build -d

down:
	docker-compose down

test:
	@echo "Ejecutando pruebas unitarias y de integracion..."
	cd backend && ./mvnw clean test

bench:
	@echo "Ejecutando pruebas de carga k6..."
	k6 run k6/opts.js

audit:
	@echo "Ejecutando auditoria Lighthouse y OWASP..."
	npx lhci autorun

clean:
	docker-compose down -v --remove-orphans
