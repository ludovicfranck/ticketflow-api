-- On crée la base pour Keycloak
CREATE DATABASE keycloak;

-- On crée une base pour  les autres services (exemple: user-service , ticket_service)
CREATE DATABASE user_service_db;
------
CREATE DATABASE ticket_service_db;
------
CREATE DATABASE document_service_db;
------
CREATE DATABASE notification_service_db;



-- On s'assure que l'utilisateur admin a tous les droits
GRANT ALL PRIVILEGES ON DATABASE keycloak TO admin;
GRANT ALL PRIVILEGES ON DATABASE user_service_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE ticket_service_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE document_service_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE notification_service_db TO admin;