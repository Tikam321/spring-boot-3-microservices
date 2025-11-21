import { useState } from "react";
import CreateProduct from "./assets/CreateProduct";
import ProductList from "./assets/ProductList";
import AddInventoryPage from "./assets/AddInventoryPage";

import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from "react-router-dom";
import Navbar from "./assets/Navbar";
import "./css/navbar.css";

import { ToastContainer } from "react-toastify";

// Wrap the component using useNavigate in a child of Router
const Home: React.FC = () => {
  const navigate = useNavigate(); // ✅ now inside Router
  const [count, setCount] = useState(0);

  const goToProducts = () => {
    navigate("/"); // navigate to Product List
  };

  return (
    <div>
      {/* <h1>My E-Commerce App</h1>
      <nav style={{ padding: "10px", borderBottom: "1px solid #ccc", background: "black" }}>
        <button onClick={() => navigate("/list")}>list</button>
        <button onClick={() => navigate("/create")}>create order</button>
        <button onClick={() => navigate("/addInventory")}> add Inventory </button>
      </nav>
      <button onClick={goToProducts}>Go to Products</button> */}

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
              <button onClick={() => navigate("/create")}>create product</button>
               <button onClick={() => navigate("/addInventory")}> add Inventory </button>
      
            </div>
            </nav>
    </div>
  );
};

function App() {
  return (

    <Router>

     <ToastContainer />

      <Routes>
        <Route path="/" element={<Home />} />
         <Route path="/list" element={<ProductList />} />
        <Route path="/create" element={<CreateProduct />} />
        <Route path="/addInventory" element={<AddInventoryPage/>} />
      </Routes>
    </Router>
  );
}

export default App;