import { useState } from "react";
import CreateProduct from "./assets/CreateProduct";
import ProductList from "./assets/ProductList";
import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from "react-router-dom";
import Navbar from "./assets/Navbar";

// Wrap the component using useNavigate in a child of Router
const Home: React.FC = () => {
  const navigate = useNavigate(); // ✅ now inside Router
  const [count, setCount] = useState(0);

  const goToProducts = () => {
    navigate("/"); // navigate to Product List
  };

  return (
    <div>
      <h1>My E-Commerce App</h1>
      <nav style={{ padding: "10px", borderBottom: "1px solid #ccc", background: "black" }}>
        <button onClick={() => navigate("/list")}>list</button>
        <button onClick={() => navigate("/create")}>create order</button>
      </nav>
      <button onClick={goToProducts}>Go to Products</button>
    </div>
  );
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navbar />} />
         <Route path="/list" element={<ProductList />} />

        <Route path="/create" element={<CreateProduct />} />
      </Routes>
    </Router>
  );
}

export default App;