import React, { useState } from "react";
import "../App.css";
import { useNavigate } from "react-router-dom";
import keycloak from "../keycloak";

interface ProductForm {
  name: string;
  desc: string;
  skuCode: string;
  price: number;
}

const CreateProduct: React.FC = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState<ProductForm>({
    name: "",
    desc: "",
    skuCode: "",
    price: 0,
  });

  const createProduct = async () => {
      const token = keycloak.token;
    const response = await fetch("http://localhost:9000/api/product", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
         "Authorization": `Bearer ${token}` // attach access token
      },
      body: JSON.stringify(form), // ✅ request body
    });
    navigate("/") // navigate back to Product List
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: name === "price" ? Number(value) : value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Product Created:", form);
    // TODO: Call Product microservice API
  };

  return (
    <div className="container1">
      <div className="card1">
        <h2>Create Product</h2>
        <form onSubmit={handleSubmit} className="form">
          <input
            type="text"
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="Product Name"
            required
          />
          <input
            type="text"
            name="desc"
            value={form.desc}
            onChange={handleChange}
            placeholder="product Description"
            required
          />
          <input
            type="text"
            name="skuCode"
            value={form.skuCode}
            onChange={handleChange}
            placeholder="Sku Code"
            required
          />
          <input
            type="number"
            name="price"
            value={form.price}
            onChange={handleChange}
            placeholder="price"
            required
          />
          <button type="submit" className="btn-primary" onClick={createProduct}>Create</button>
        </form>
      </div>
    </div>
  );
};

export default CreateProduct;