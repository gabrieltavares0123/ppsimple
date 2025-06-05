.PHONY: clean jar test lint build up down restart

clean:
	gradlew clean

jar:
	gradlew bootJar

test:
	gradlew check

lint:
	gradlew ktlintCheck

build: clean lint test jar

down:
	docker compose down

up: down build
	docker compose up -d --build

restart: down up



