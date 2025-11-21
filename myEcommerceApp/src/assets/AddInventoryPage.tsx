import React, { useState } from "react";
import { toast } from "react-toastify";
import keycloak from "../keycloak";
import { useNavigate } from "react-router-dom";

const AddInventoryPage: React.FC = () => {
  const [skuCode, setSkuCode] = useState<string>("");
  const [quantity, setQuantity] = useState<number>(0);
  const navigate = useNavigate();


  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!skuCode || quantity <= 0) {
      toast.error("Please enter valid SKU and quantity");
      return;
    }

    // Here you can call your API to save inventory
    console.log("Inventory Data:", { skuCode, quantity });
    inventoryHandler();
    toast.success("Inventory added successfully!");

    // Reset form
    setSkuCode("");
    setQuantity(0);
  };

const inventoryHandler = async () => {
  const token = keycloak.token;
  try {
    const response = await fetch("http://localhost:9000/api/inventory", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify({ skuCode, quantity }),
    });

    if (!response.ok) {
      // Handle HTTP errors explicitly
      const errorText = await response.text();
      throw new Error(`Request failed: ${response.status} ${errorText}`);
    }

    console.log("Inventory updated successfully!");
    toast.success("the product " +  skuCode + "is successfully added.")
    navigate("/"); // ✅ Only navigate on success
  } catch (error) {
    console.error("Error occurred while updating inventory:", error);
    alert("Failed to update inventory. Please try again later.");
    toast.success("the product " +  skuCode + "failed to added in inventory.")

  }
};
  return (
    <div className="inventory-container">
      <h2>Inventory Page</h2>
        <div>
          <label htmlFor="sku">SKU Code:</label>
          <input
            type="text"
            id="sku"
            value={skuCode}
            onChange={(e) => setSkuCode(e.target.value)}
            required
          />
        </div>
        <div>
          <label htmlFor="quantity">Quantity:</label>
          <input
            type="number"
            id="quantity"
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
            required
          />
        </div>
        <button type="submit" onClick={inventoryHandler}>Add Inventory</button>
    </div>
  );
};

export default AddInventoryPage;