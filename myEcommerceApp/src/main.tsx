// import { StrictMode } from 'react'
// import { createRoot } from 'react-dom/client'
// import './index.css'
// import App from './App.tsx'

// createRoot(document.getElementById('root')!).render(
//   <StrictMode>
//     <App />
//   </StrictMode>,
// )
import React, { useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import keycloak from "./keycloak";

const Root = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    keycloak
      .init({ onLoad: "login-required" })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
      })
      .catch((err) => {
        console.error("Keycloak init error:", err);
      });
  }, []);

  if (!isAuthenticated) {
    return <div>Loading authentication...</div>;
  }

  return <App />;
};

ReactDOM.createRoot(document.getElementById("root")!).render(<Root />);