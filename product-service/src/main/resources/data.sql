-- Sample product data for development/testing
INSERT INTO products (sku, name, description, price, stock_quantity, category, brand, image_url, weight_kg, active, length_cm, width_cm, height_cm)
VALUES
    ('LAPTOP-001', 'ProBook 15 Laptop', 'High-performance laptop with Intel Core i7, 16GB RAM, 512GB SSD', 1299.99, 50, 'Electronics', 'TechPro', 'https://example.com/images/laptop-001.jpg', 1.8, true, 35.0, 24.0, 2.0),
    ('PHONE-001', 'SmartPhone X12', 'Latest smartphone with AMOLED display, 5G connectivity', 799.99, 200, 'Electronics', 'MobileTech', 'https://example.com/images/phone-001.jpg', 0.18, true, 15.0, 7.5, 0.8),
    ('TABLET-001', 'TabPro 10 Inch', '10-inch tablet with 2K display and stylus support', 499.99, 75, 'Electronics', 'TechPro', 'https://example.com/images/tablet-001.jpg', 0.5, true, 25.0, 17.0, 0.7),
    ('SHIRT-001', 'Classic Cotton T-Shirt', '100% premium cotton t-shirt, available in multiple colors', 29.99, 500, 'Clothing', 'FashionBrand', 'https://example.com/images/shirt-001.jpg', 0.2, true, null, null, null),
    ('SHOES-001', 'Running Pro Shoes', 'Lightweight running shoes with advanced cushioning technology', 89.99, 150, 'Footwear', 'SportsBrand', 'https://example.com/images/shoes-001.jpg', 0.4, true, null, null, null),
    ('BOOK-001', 'Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 34.99, 1000, 'Books', 'TechPublisher', 'https://example.com/images/book-001.jpg', 0.5, true, 23.0, 15.0, 2.5),
    ('WATCH-001', 'Smart Watch Pro', 'Fitness tracking smartwatch with heart rate monitor and GPS', 249.99, 80, 'Electronics', 'WearTech', 'https://example.com/images/watch-001.jpg', 0.05, true, 4.4, 3.8, 1.0),
    ('COFFEE-001', 'Premium Arabica Coffee', 'Single-origin Ethiopian Arabica coffee beans, 1kg bag', 19.99, 2000, 'Food & Beverages', 'CoffeeRoasters', 'https://example.com/images/coffee-001.jpg', 1.0, true, null, null, null),
    ('HEADPHONE-001', 'Noise-Cancelling Headphones', 'Premium wireless headphones with 30-hour battery life', 349.99, 60, 'Electronics', 'AudioTech', 'https://example.com/images/headphone-001.jpg', 0.25, true, null, null, null),
    ('CAMERA-001', 'DSLR Camera 24MP', 'Professional DSLR camera with 24-megapixel sensor and 4K video', 899.99, 30, 'Electronics', 'CameraPlus', 'https://example.com/images/camera-001.jpg', 0.75, true, 13.9, 10.8, 7.8);
