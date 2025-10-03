import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../css/navbar.css";
import keycloak from "../keycloak";

const Navbar: React.FC = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
    const navigate = useNavigate();
//     const [userInfo, setUserInfo] = useState<UserInfoType | null>(null);

// const getUserInfo = async (): Promise<UserInfoType | null> => {
//   let data = null;
//   if (keycloak.authenticated) {
//     const response = await fetch(
//       `${keycloak.authServerUrl}/realms/${keycloak.realm}/protocol/openid-connect/userinfo`,
//       {
//         headers: {
//           Authorization: `Bearer ${keycloak.token}`,
//         },
//       }
//     );

//     data = await response.json();
//     console.log("User Info:", data);
//   }
//   return data;
// };

// useEffect(() => {
//   const fetchUserInfo = async () => {
//     if (keycloak.authenticated) {
//       try {
//         const data = await getUserInfo();
//         setUserInfo(data);
//       } catch (error) {
//         console.error("Error fetching user info:", error);
//         setUserInfo(null);
//       }
//     } else {
//       setUserInfo(null);
//     }
//   };

//   fetchUserInfo();
// }, [keycloak.authenticated]);

  const handleAuth = () => {
    setIsLoggedIn(!isLoggedIn);
  };

  return (
    <nav className="navbar">
      {/* Left side: Brand */}
      <div style={{ fontSize: "20px", fontWeight: "bold" }}>
        <Link to="/" style={{ color: "white", textDecoration: "none" }}>
          My E-Commerce
        </Link>
      </div>

      {/* Center: Links */}
      <div className="nav-links">
        {/* <Link to="/" style={{ color: "white", textDecoration: "none" }}>
          Products
        </Link>
        <Link to="/create" style={{ color: "white", textDecoration: "none" }}>
          Create Product */}
        {/* </Link> */}
         <button onClick={() => navigate("/list")}>list</button>
        <button onClick={() => navigate("/create")}>create order</button>
      </div>

      {/* Right side: Auth button */}
      {/* <button
        onClick={handleAuth}
        style={{
          
        }} */}
      {/* > */}
        {/* {isLoggedIn ? "Logout" : "Login"} */}
            {!keycloak.authenticated ? (
        <button className="auth-button" onClick={() => keycloak.login()} style={{ marginLeft: "20px" }}>
          Login
        </button>
      ) : (
        <button  className="auth-button" onClick={() => keycloak.logout()} style={{ marginLeft: "20px" }}>
          Logout
      </button>
      )}
      {/* </button> */}

    </nav>
  );
};

export default Navbar;