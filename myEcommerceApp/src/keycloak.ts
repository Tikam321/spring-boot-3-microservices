import Keycloak from "keycloak-js";

// ⚡ Replace with your Keycloak settings
const keycloak = new Keycloak({
  url: "http://keycloak.default.svc.cluster.local:8080",       // Keycloak base URL
//   url: "http://localhost:8181",
  realm: "spring-microservices-security-realm",                   // Your realm name
//   realm: "springBoot-microservices-realm",
  clientId: "angular-client",        // Your client id from Keycloak
});

export default keycloak;