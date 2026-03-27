-- ============================================
-- User seed data
-- ============================================
INSERT INTO users (id, username, password, full_name, role, create_at, update_at) VALUES
(1, 'admin3', '$2a$10$ZxJgAjJn6WiGpXPBH3fPjOLjb8VBYAKj6vT6x0h4s0yT8e8t7gBay', 'Admin Three', 'ADMIN', NOW(), NOW()),
(2, 'user1', '$2a$10$ZxJgAjJn6WiGpXPBH3fPjOLjb8VBYAKj6vT6x0h4s0yT8e8t7gBay', 'User One', 'USER', NOW(), NOW()),
(3, 'user2', '$2a$10$ZxJgAjJn6WiGpXPBH3fPjOLjb8VBYAKj6vT6x0h4s0yT8e8t7gBay', 'User Two', 'USER', NOW(), NOW());

-- ============================================
-- Product seed data (20 items)
-- ============================================
INSERT INTO product (id, name, price, stock_quantity, description, image, is_deleted, version, create_at, update_at) VALUES
(1, 'Laptop Dell XPS 13', 999.99, 10, 'High-performance ultrabook', 'laptop.jpg', 0, 0, NOW(), NOW()),
(2, 'iPhone 15 Pro', 1199.99, 15, 'Latest Apple smartphone', 'iphone.jpg', 0, 0, NOW(), NOW()),
(3, 'Samsung Galaxy S24', 899.99, 20, 'Android flagship device', 'galaxy.jpg', 0, 0, NOW(), NOW()),
(4, 'MacBook Air M3', 1299.99, 8, 'Powerful laptop for professionals', 'macbook.jpg', 0, 0, NOW(), NOW()),
(5, 'iPad Pro 12.9', 1099.99, 12, 'Large tablet for creative work', 'ipad.jpg', 0, 0, NOW(), NOW()),
(6, 'Sony WH-1000XM5', 379.99, 25, 'Premium noise-cancelling headphones', 'sony_headphones.jpg', 0, 0, NOW(), NOW()),
(7, 'AirPods Pro 2', 249.99, 30, 'Wireless earbuds with ANC', 'airpods.jpg', 0, 0, NOW(), NOW()),
(8, 'Samsung 4K TV 55"', 699.99, 6, 'Ultra HD smart television', 'tv.jpg', 0, 0, NOW(), NOW()),
(9, 'LG OLED TV 65"', 1499.99, 4, 'Premium OLED display', 'oled_tv.jpg', 0, 0, NOW(), NOW()),
(10, 'Google Pixel 8 Pro', 999.99, 18, 'Google flagship phone', 'pixel.jpg', 0, 0, NOW(), NOW()),
(11, 'OnePlus 12', 799.99, 22, 'Fast charging Android phone', 'oneplus.jpg', 0, 0, NOW(), NOW()),
(12, 'Kindle Paperwhite', 139.99, 40, 'E-reader with backlight', 'kindle.jpg', 0, 0, NOW(), NOW()),
(13, 'Microsoft Surface Pro 10', 1299.99, 9, '2-in-1 tablet laptop', 'surface.jpg', 0, 0, NOW(), NOW()),
(14, 'Nintendo Switch OLED', 349.99, 16, 'Gaming console', 'switch.jpg', 0, 0, NOW(), NOW()),
(15, 'PlayStation 5', 499.99, 7, 'Next-gen gaming console', 'ps5.jpg', 0, 0, NOW(), NOW()),
(16, 'Xbox Series X', 499.99, 5, 'Premium gaming console', 'xbox.jpg', 0, 0, NOW(), NOW()),
(17, 'Apple Watch Series 9', 399.99, 20, 'Smartwatch for iPhone users', 'apple_watch.jpg', 0, 0, NOW(), NOW()),
(18, 'Samsung Galaxy Watch 6', 299.99, 24, 'Android smartwatch', 'galaxy_watch.jpg', 0, 0, NOW(), NOW()),
(19, 'GoPro Hero 12', 499.99, 11, 'Action camera for adventurers', 'gopro.jpg', 0, 0, NOW(), NOW()),
(20, 'DJI Air 3S', 999.99, 8, 'Professional drone', 'dji.jpg', 0, 0, NOW(), NOW());

-- ============================================
-- Inventory seed data (matches products)
-- ============================================
INSERT INTO inventory (id, product_id, stock, reserved, sold_count, create_at, update_at) VALUES
(1, 1, 10, 0, 0, NOW(), NOW()),
(2, 2, 15, 0, 0, NOW(), NOW()),
(3, 3, 20, 0, 0, NOW(), NOW()),
(4, 4, 8, 0, 0, NOW(), NOW()),
(5, 5, 12, 0, 0, NOW(), NOW()),
(6, 6, 25, 0, 0, NOW(), NOW()),
(7, 7, 30, 0, 0, NOW(), NOW()),
(8, 8, 6, 0, 0, NOW(), NOW()),
(9, 9, 4, 0, 0, NOW(), NOW()),
(10, 10, 18, 0, 0, NOW(), NOW()),
(11, 11, 22, 0, 0, NOW(), NOW()),
(12, 12, 40, 0, 0, NOW(), NOW()),
(13, 13, 9, 0, 0, NOW(), NOW()),
(14, 14, 16, 0, 0, NOW(), NOW()),
(15, 15, 7, 0, 0, NOW(), NOW()),
(16, 16, 5, 0, 0, NOW(), NOW()),
(17, 17, 20, 0, 0, NOW(), NOW()),
(18, 18, 24, 0, 0, NOW(), NOW()),
(19, 19, 11, 0, 0, NOW(), NOW()),
(20, 20, 8, 0, 0, NOW(), NOW());

-- ============================================
-- Order seed data (sample orders from users)
-- ============================================
INSERT INTO `order` (id, user_id, total_amount, status, create_at, update_at) VALUES
(1, 2, 2199.98, 'COMPLETED', NOW(), NOW()),
(2, 3, 649.97, 'PENDING', NOW(), NOW()),
(3, 2, 1299.99, 'SHIPPED', NOW(), NOW());

-- ============================================
-- Order Details (line items)
-- ============================================
INSERT INTO order_detail (id, order_id, product_id, quantity, price, create_at, update_at) VALUES
(1, 1, 2, 1, 1199.99, NOW(), NOW()),
(2, 1, 7, 1, 249.99, NOW(), NOW()),
(3, 2, 6, 1, 379.99, NOW(), NOW()),
(4, 2, 12, 1, 139.99, NOW(), NOW()),
(5, 3, 4, 1, 1299.99, NOW(), NOW());
