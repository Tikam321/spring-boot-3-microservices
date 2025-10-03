import React, { useEffect, useState } from "react";
import "../App.css";
import { useNavigate } from "react-router-dom";
import keycloak from "../keycloak";



interface ProductForm {
  name: string;
  desc: string;
  skuCode: string;
  price: number;
  quantity: number;
}
interface ProductListProps {
  products: ProductForm[];
  onOrder: (id: number) => void;
}
type UserInfoType = {
email: string,
family_name: string,
given_name: string,
name: string,
preferred_username: string,
sub: string,
}

const ProductList: React.FC = () => {
   const [product, setProduct] = useState<ProductForm[]>([]);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState<number>(0);
  const navigate = useNavigate();

      const [userInfo, setUserInfo] = useState<UserInfoType | null>(null);
  
  const getUserInfo = async (): Promise<UserInfoType | null> => {
    let data = null;
    if (keycloak.authenticated) {
      const response = await fetch(
        `${keycloak.authServerUrl}/realms/${keycloak.realm}/protocol/openid-connect/userinfo`,
        {
          headers: {
            Authorization: `Bearer ${keycloak.token}`,
          },
        }
      );
  
      data = await response.json();
      console.log("User Info:", data);
    }
    return data;
  };
  
  useEffect(() => {
    const fetchUserInfo = async () => {
      if (keycloak.authenticated) {
        try {
          const data = await getUserInfo();
          setUserInfo(data);
        } catch (error) {
          console.error("Error fetching user info:", error);
          setUserInfo(null);
        }
      } else {
        setUserInfo(null);
      }
    };
  
    fetchUserInfo();
  }, [keycloak.authenticated]);

useEffect(() => {
    const loadProducts = async () => {
      try {
        const data = await fetchProducts();
        console.warn(data);
        
        setProduct(data);
      } catch (error) {
        console.error("Error loading products", error);
      } finally {
        setLoading(false);
      }
    };
    loadProducts();
  }, []);

  const fetchProducts = async (): Promise<ProductForm[]> => {
  try {
    const response = await fetch("http://localhost:9000/api/product", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch products");
    }

    return await response.json();
  } catch (error) {
    console.error(error);
    throw error;
  }
};

  function quantityHandler(skuCode: string, value: any): void {
      const parsedValue = parseInt(value, 10);
      console.warn(parsedValue);
      
setProduct(prevProducts => 
    prevProducts.map(p => 
      p.skuCode === skuCode ? { ...p, quantity: parsedValue } : p
    )
  );   
  }

  const createOrder = async (order: ProductForm) => {
    try {
    const response = await fetch("http://localhost:9000/api/order", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({orderName: order.name, price: order.price, quantity: order.quantity, skuCode: order.skuCode, email: userInfo?.email})
    });
    if (response.ok) {
      console.log("Order created successfully");
      navigate("/");
    }
  } catch (error) {
  }
};

  function orderItemHandler(skuCode: string): void {
   const item = product.find(p => p.skuCode === skuCode);
   if (item && item.quantity > 0) {


  }
}

  return (
    <div className="container">
      <h2>Products({product.length})</h2>
      <div className="grid">
        {product.map((product) => (
          <div key={product.name} className="product-card" 
           style={{
            border: "1px solid #ccc",
            borderRadius: "8px",
            padding: "16px",
            width: "200px",
            backgroundColor: "#ffeb3b", // highlight card
            color:  "black",
          }}>
            <p>productName: {product.name}</p>
            <p>Description: {product.desc}</p>
            <p>price: ₹{product.price}</p>
            <p>sku Code: {product.skuCode}</p>
          <input 
            type="number" 
            value={product.quantity || ''} 
            className="btn-primary" 
            onChange={(event: React.ChangeEvent<HTMLInputElement>) => 
              quantityHandler(product.skuCode, event.target.value)
            } 
            min="0"
          />            <button onClick={() => createOrder(product)} className="btn-success">
              Order
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProductList;