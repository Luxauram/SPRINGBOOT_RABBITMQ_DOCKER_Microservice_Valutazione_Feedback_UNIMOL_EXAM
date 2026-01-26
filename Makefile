.PHONY: build deploy start stop logs clean status help

# Colori per output (funziona su Linux/Mac, su Windows potrebbe non funzionare)
BLUE=\033[0;34m
GREEN=\033[0;32m
RED=\033[0;31m
YELLOW=\033[1;33m
NC=\033[0m # No Color

# Avvia tutto (build + deploy + monitoring)
start: swarm-init build deploy status
	@echo ""
	@echo "$(GREEN)✅ Sistema avviato con successo!$(NC)"
	@echo ""
	@echo "$(BLUE)🌐 Endpoints disponibili:$(NC)"
	@echo "  - API Gateway:         http://localhost:8080"
	@echo "  - User Role Service:   http://localhost:8081"
	@echo "  - Assessment Service:  http://localhost:8082"
	@echo "  - RabbitMQ Management: http://localhost:15672 (guest/guest)"
	@echo ""
	@echo "$(YELLOW)💡 Comandi utili:$(NC)"
	@echo "  make logs    # Vedi i log in tempo reale"
	@echo "  make status  # Stato dettagliato dei servizi"
	@echo "  make stop    # Ferma tutto"
	@echo ""
	@echo "$(YELLOW)⏳ Attendo 15 secondi per l'avvio completo...$(NC)"
	@sleep 15
	@echo ""
	@echo "$(BLUE)📊 Stato finale dei servizi:$(NC)"
	@docker service ls

# Inizializza Swarm
swarm-init:
	@echo "$(BLUE)🐝 Inizializzazione Docker Swarm...$(NC)"
	@docker swarm init 2>/dev/null || echo "$(YELLOW)⚠️  Swarm già inizializzato$(NC)"
	@echo ""

# Build delle immagini con output completo
build:
	@echo "$(BLUE)🔨 Build delle immagini Docker...$(NC)"
	@echo ""
	@echo "$(YELLOW)📦 Building API Gateway...$(NC)"
	docker build -f api-gateway/Dockerfile -t unimol/api-gateway:latest .
	@echo ""
	@echo "$(YELLOW)📦 Building User Role Service...$(NC)"
	docker build -f microservice-user-role/Dockerfile -t unimol/microservice-user-role:latest .
	@echo ""
	@echo "$(YELLOW)📦 Building Assessment Feedback Service...$(NC)"
	docker build -f microservice-assessment-feedback/Dockerfile -t unimol/microservice-assessment-feedback:latest .
	@echo ""
	@echo "$(GREEN)✅ Build completata!$(NC)"
	@echo ""

# Deploy dello stack
deploy:
	@echo "$(BLUE)🚢 Deploy dello stack su Docker Swarm...$(NC)"
	@echo ""
	docker stack deploy -c docker-compose.yml unimol
	@echo ""
	@echo "$(GREEN)✅ Deploy completato!$(NC)"
	@echo ""
	@echo "$(YELLOW)⏳ Attesa inizializzazione servizi (10 secondi)...$(NC)"
	@sleep 10

# Ferma tutto
stop:
	@echo "$(RED)🛑 Arresto dello stack...$(NC)"
	docker stack rm unimol
	@echo ""
	@echo "$(YELLOW)⏳ Attesa terminazione servizi...$(NC)"
	@sleep 5
	@echo "$(GREEN)✅ Stack rimosso!$(NC)"

# Vedi i log di tutti i servizi principali
logs:
	@echo "$(BLUE)📋 Log dei servizi (Ctrl+C per uscire)$(NC)"
	@echo ""
	docker service logs unimol_api-gateway -f --tail 50 & \
	docker service logs unimol_microservice-user-role -f --tail 50 & \
	docker service logs unimol_microservice-assessment-feedback -f --tail 50 & \
	wait

# Log di un singolo servizio
logs-gateway:
	docker service logs unimol_api-gateway -f --tail 100

logs-user:
	docker service logs unimol_microservice-user-role -f --tail 100

logs-assessment:
	docker service logs unimol_microservice-assessment-feedback -f --tail 100

logs-rabbitmq:
	docker service logs unimol_rabbitmq -f --tail 100

logs-postgres-users:
	docker service logs unimol_postgres-users -f --tail 100

logs-postgres-assessment:
	docker service logs unimol_postgres-assessment -f --tail 100

# Pulisci tutto (incluso Swarm e volumi)
clean: stop
	@echo "$(RED)🧹 Pulizia completa...$(NC)"
	@docker swarm leave --force 2>/dev/null || true
	@docker volume prune -f
	@docker system prune -f
	@echo "$(GREEN)✅ Pulizia completata!$(NC)"

# Mostra lo stato dettagliato
status:
	@echo "$(BLUE)📊 Stato dei servizi:$(NC)"
	@echo ""
	@docker service ls
	@echo ""
	@echo "$(BLUE)🔍 Dettaglio repliche API Gateway:$(NC)"
	@docker service ps unimol_api-gateway --no-trunc
	@echo ""
	@echo "$(BLUE)🔍 Dettaglio repliche User Role:$(NC)"
	@docker service ps unimol_microservice-user-role --no-trunc
	@echo ""
	@echo "$(BLUE)🔍 Dettaglio repliche Assessment Feedback:$(NC)"
	@docker service ps unimol_microservice-assessment-feedback --no-trunc

# Scala i servizi
scale-gateway:
	@read -p "Numero di repliche per API Gateway: " replicas; \
	docker service scale unimol_api-gateway=$$replicas
	@docker service ps unimol_api-gateway

scale-user:
	@read -p "Numero di repliche per User Role Service: " replicas; \
	docker service scale unimol_microservice-user-role=$$replicas
	@docker service ps unimol_microservice-user-role

scale-assessment:
	@read -p "Numero di repliche per Assessment Service: " replicas; \
	docker service scale unimol_microservice-assessment-feedback=$$replicas
	@docker service ps unimol_microservice-assessment-feedback

# Riavvia un servizio
restart-gateway:
	docker service update --force unimol_api-gateway

restart-user:
	docker service update --force unimol_microservice-user-role

restart-assessment:
	docker service update --force unimol_microservice-assessment-feedback

# Help
help:
	@echo "$(BLUE)📚 Comandi disponibili:$(NC)"
	@echo ""
	@echo "  $(GREEN)make start$(NC)              - Avvia l'intera architettura (build + deploy)"
	@echo "  $(GREEN)make stop$(NC)               - Ferma tutti i servizi"
	@echo "  $(GREEN)make logs$(NC)               - Mostra i log di tutti i microservizi"
	@echo "  $(GREEN)make logs-gateway$(NC)       - Log solo dell'API Gateway"
	@echo "  $(GREEN)make logs-user$(NC)          - Log solo del User Role Service"
	@echo "  $(GREEN)make logs-assessment$(NC)    - Log solo dell'Assessment Service"
	@echo "  $(GREEN)make status$(NC)             - Mostra lo stato dettagliato"
	@echo "  $(GREEN)make build$(NC)              - Build solo delle immagini"
	@echo "  $(GREEN)make deploy$(NC)             - Deploy senza rebuild"
	@echo "  $(GREEN)make clean$(NC)              - Rimuove tutto (incluso Swarm)"
	@echo "  $(GREEN)make scale-gateway$(NC)      - Scala il numero di repliche Gateway"
	@echo "  $(GREEN)make scale-user$(NC)         - Scala il numero di repliche User Service"
	@echo "  $(GREEN)make scale-assessment$(NC)   - Scala il numero di repliche Assessment"
	@echo "  $(GREEN)make restart-gateway$(NC)    - Riavvia l'API Gateway"
	@echo ""